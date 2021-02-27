package com.lyft.kronos.internal.ntp;

import com.lyft.kronos.Clock;
import com.lyft.kronos.ntp.NtpPacket;
import com.lyft.kronos.ntp.NtpPackets;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.nio.ByteBuffer;
import java.util.concurrent.TimeUnit;

/**
 * Forked from https://android.googlesource.com/platform/frameworks/base/+/master/core/java/android/net/SntpClient.java
 *
 * Copyright (C) 2008 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 * Simple SNTP client class for retrieving network time.
 *
 * Sample usage:
 * <pre>SntpClient client = new SntpClient();
 * try {
 *     final SntpClient.Response response = client.requestTime("time.foo.com");
 *     long now = response.getNtpTime() + deviceClock.elapsedRealtime() - response.getNtpTimeReference();
 * } catch (IOException) { ... }
 * </pre>
 */
public class SntpClient {

    private static final int NTP_PACKET_SIZE = 48;

    private static final int NTP_PORT = 123;
    private static final int NTP_MODE_SERVER = 4;
    private static final int NTP_MODE_BROADCAST = 5;

    private static final int NTP_LEAP_NOSYNC = 3;
    private static final int NTP_STRATUM_DEATH = 0;
    private static final int NTP_STRATUM_MAX = 15;

    // Number of seconds between Jan 1, 1900 and Jan 1, 1970
    // 70 years plus 17 leap days
    public static final long OFFSET_1900_TO_1970 = ((365L * 70L) + 17L) * 24L * 60L * 60L;

    private static final long MAX_BOOT_MISMATCH_MS = 1_000L;

    private final Clock deviceClock;
    private final DnsResolver dnsResolver;
    private final DatagramFactory datagramFactory;

    private static class InvalidServerReplyException extends IOException {
        public InvalidServerReplyException(String message) {
            super(message);
        }
    }

    public SntpClient(Clock deviceClock, DnsResolver dnsResolver, DatagramFactory datagramFactory) {
        this.deviceClock = deviceClock;
        this.dnsResolver = dnsResolver;
        this.datagramFactory = datagramFactory;
    }

    private final NtpPackets ntpPackets = new NtpPackets.Impl();

    /**
     * Sends an SNTP request to the given host and processes the response.
     *
     * @param host host name of the server.
     * @param timeout network timeout in milliseconds.
     * @return Encapsulated response from the NTP server.
     * @throws IOException network error
     */
    public Response requestTime(String host, Long timeout) throws IOException {
        DatagramSocket socket = null;
        try {
            InetAddress address = dnsResolver.resolve(host);
            socket = datagramFactory.createSocket();
            socket.setSoTimeout(timeout.intValue());

            // get current time and write it to the request packet
            long requestTime = deviceClock.getCurrentTimeMs();
            long requestTicks = deviceClock.getElapsedTimeMs();

            NtpPacket requestNtpPacket = new NtpPacket(
                    /* leap */ (byte) 0,
                    /* version */ (byte) 3,
                    /* mode */ (byte) NtpPacket.ProtocolMode.Client.getValue(),
                    /* stratum */ (byte) 0,
                    /* poll interval */ (byte) 0,
                    /* precision */ (byte) 0,
                    /* root delay */ 0.0,
                    /* root dispersion */ 0.0,
                    /* reference time */ 0.0,
                    /* originate time */ 0.0,
                    /* receive time */ 0.0,
                    /* transmit time */ (double) (TimeUnit.MILLISECONDS.toSeconds(requestTime) + OFFSET_1900_TO_1970)
            );
            ByteBuffer requestByteBuffer = ntpPackets.encode(requestNtpPacket);
            DatagramPacket requestPacket = datagramFactory.createPacket(requestByteBuffer.array(), address, NTP_PORT);

            socket.send(requestPacket);

            // read the response
            ByteBuffer responseByteBuffer = ByteBuffer.allocate(NTP_PACKET_SIZE);
            DatagramPacket responsePacket = datagramFactory.createPacket(responseByteBuffer.array());
            socket.receive(responsePacket);

            long responseTicks = deviceClock.getElapsedTimeMs();
            long responseTime = requestTime + (responseTicks - requestTicks);

            NtpPacket responseNtpPacket = ntpPackets.decode(responseByteBuffer);

            // extract the results
            final byte leap = responseNtpPacket.getWarningLeapSecond();
            final byte mode = responseNtpPacket.getProtocolMode();
            final int stratum = responseNtpPacket.getStratum();
            final long originateTime = TimeUnit.SECONDS.toMillis((long) responseNtpPacket.getOriginateTimeSecondsSince1900() - OFFSET_1900_TO_1970);
            final long receiveTime =  TimeUnit.SECONDS.toMillis((long) responseNtpPacket.getReceiveTimeSecondsSince1900() - OFFSET_1900_TO_1970);
            final long transmitTime = TimeUnit.SECONDS.toMillis((long) responseNtpPacket.getTransmitTimeSecondsSince1900() - OFFSET_1900_TO_1970);

            checkValidServerReply(leap, mode, stratum, transmitTime);

            // long roundTripTime = responseTicks - requestTicks - (transmitTime - receiveTime);
            // receiveTime = originateTime + transit + skew
            // responseTime = transmitTime + transit - skew
            // clockOffset = ((receiveTime - originateTime) + (transmitTime - responseTime))/2
            //             = ((originateTime + transit + skew - originateTime) +
            //                (transmitTime - (transmitTime + transit - skew)))/2
            //             = ((transit + skew) + (transmitTime - transmitTime - transit + skew))/2
            //             = (transit + skew - transit + skew)/2
            //             = (2 * skew)/2 = skew
            long clockOffset = ((receiveTime - originateTime) + (transmitTime - responseTime)) / 2;

            // use the times on this side of the network latency
            // (response rather than request time)
            return new Response(responseTime, responseTicks, clockOffset, deviceClock);
        } finally {
            if (socket != null) {
                socket.close();
            }
        }
    }

    private static void checkValidServerReply(
            byte leap, byte mode, int stratum, long transmitTime)
            throws InvalidServerReplyException {
        if (leap == NTP_LEAP_NOSYNC) {
            throw new InvalidServerReplyException("unsynchronized server");
        }
        if ((mode != NTP_MODE_SERVER) && (mode != NTP_MODE_BROADCAST)) {
            throw new InvalidServerReplyException("untrusted mode: " + mode);
        }
        if ((stratum == NTP_STRATUM_DEATH) || (stratum > NTP_STRATUM_MAX)) {
            throw new InvalidServerReplyException("untrusted stratum: " + stratum);
        }
        if (transmitTime == 0) {
            throw new InvalidServerReplyException("zero transmitTime");
        }
    }

    /**
     * Reads an unsigned 32 bit big endian number from the given offsetMs in the buffer.
     */
    private static long read32(byte[] buffer, int offset) {
        byte b0 = buffer[offset];
        byte b1 = buffer[offset + 1];
        byte b2 = buffer[offset + 2];
        byte b3 = buffer[offset + 3];

        // convert signed bytes to unsigned values
        int i0 = ((b0 & 0x80) == 0x80 ? (b0 & 0x7F) + 0x80 : b0);
        int i1 = ((b1 & 0x80) == 0x80 ? (b1 & 0x7F) + 0x80 : b1);
        int i2 = ((b2 & 0x80) == 0x80 ? (b2 & 0x7F) + 0x80 : b2);
        int i3 = ((b3 & 0x80) == 0x80 ? (b3 & 0x7F) + 0x80 : b3);

        return ((long) i0 << 24) + ((long) i1 << 16) + ((long) i2 << 8) + (long) i3;
    }

    /**
     * Reads the NTP time stamp at the given offsetMs in the buffer and returns
     * it as a system time (milliseconds since January 1, 1970).
     */
    static long readTimeStamp(byte[] buffer, int offset) {
        long seconds = read32(buffer, offset);
        long fraction = read32(buffer, offset + 4);
        return ((seconds - OFFSET_1900_TO_1970) * 1000) + ((fraction * 1000L) / 0x100000000L);
    }

    public static final class Response {

        // Device wall clock time (milliseconds since unix epoch)
        private final long deviceCurrentTimestampMs;

        // Device system uptime (milliseconds since reboot)
        private final long deviceElapsedTimestampMs;

        // delta between NTP and device clock (milliseconds)
        private final long offsetMs;

        private final Clock deviceClock;

        Response(long deviceCurrentTimestampMs, long deviceElapsedTimestampMs, long offsetMs, Clock deviceClock) {
            this.deviceCurrentTimestampMs = deviceCurrentTimestampMs;
            this.deviceElapsedTimestampMs = deviceElapsedTimestampMs;
            this.offsetMs = offsetMs;
            this.deviceClock = deviceClock;
        }

        /**
         * @return device wall clock time when this response was created
         */
        long getDeviceCurrentTimestampMs() {
            return deviceCurrentTimestampMs;
        }

        /**
         * @return device elapsed time when this response was created
         */
        long getDeviceElapsedTimestampMs() {
            return deviceElapsedTimestampMs;
        }

        /**
         * Returns the current time as computed from the NTP transaction.
         *
         * @return current time value computed from NTP server response as milliseconds since unix epoch
         */
        public long getCurrentTimeMs() {
            return deviceCurrentTimestampMs + offsetMs + getResponseAge();
        }

        /**
         * @return offsetMs between device wall clock time and NTP time (ntpTimestampMs - deviceTime)
         */
        public long getOffsetMs() {
            return offsetMs;
        }

        /**
         * Returns the age of this response.
         *
         * @return age of this response in milliseconds
         */
        public long getResponseAge() {
            return deviceClock.getElapsedTimeMs() - deviceElapsedTimestampMs;
        }

        /**
         * @return True if the system has not been rebooted since this was created, false otherwise.
         */
        boolean isFromSameBoot() {
            final long bootTime = this.deviceCurrentTimestampMs - this.deviceElapsedTimestampMs;
            final long systemCurrentTimeMs = deviceClock.getCurrentTimeMs();
            final long systemElapsedTimeMs = deviceClock.getElapsedTimeMs();
            final long systemBootTime = systemCurrentTimeMs - systemElapsedTimeMs;
            return Math.abs(bootTime - systemBootTime) < MAX_BOOT_MISMATCH_MS;
        }
    }
}
