package com.lyft.kronos.ntp

import okio.ByteString.Companion.decodeHex
import org.assertj.core.api.Assertions.assertThat
import org.junit.Test

class NtpPacketsTests {

    private val packets by lazy { NtpPackets.Impl() }

    // Decode

    // Use following commands to produce independent package picture.
    //
    // $ sudo tcpdump port 123 -vv -X
    // $ sntp REPLACE_ME_WITH_NTP_HOST
    //
    // tcpdump will output both raw hex package (last 24 hex blocks are 48 NTP bytes)
    // and resolved NTP properties. This information can be used to produce good test data.

    @Test
    fun decodeApple() {
        // time.apple.com

        val packetHex = "240108eb000000000000000c53484d00e3d28d742a667f0ee3d28d7982281345e3d28d798912f961e3d28d7989138fec"

        val packet = NtpPacket(
            warningLeapSecond = 0,
            protocolVersion = 4,
            protocolMode = 4,
            stratum = 1,
            maximumPollIntervalPowerOfTwo = 8,
            precisionPowerOfTwo = -21,
            rootDelaySeconds = 0.0,
            rootDispersionSeconds = 0.00018310546875,
            referenceTimeSecondsSince1900 = 3822226804.165626469,
            originateTimeSecondsSince1900 = 3822226809.508424000,
            receiveTimeSecondsSince1900 = 3822226809.535445772,
            transmitTimeSecondsSince1900 = 3822226809.535454745,
        )

        assertDecode(packetHex, packet)
    }

    @Test
    fun decodeAppleEuro() {
        // time.euro.apple.com

        val packetHex = "240108eb000000000000000753484d00e3d267942a6608b7e3d26794e740b34ee3d26794ebdf5990e3d26794ebe060d7"

        val packet = NtpPacket(
            warningLeapSecond = 0,
            protocolVersion = 4,
            protocolMode = 4,
            stratum = 1,
            maximumPollIntervalPowerOfTwo = 8,
            precisionPowerOfTwo = -21,
            rootDelaySeconds = 0.0,
            rootDispersionSeconds = 0.0001068115234375,
            referenceTimeSecondsSince1900 = 3822217108.165619415,
            originateTimeSecondsSince1900 = 3822217108.903330999,
            receiveTimeSecondsSince1900 = 3822217108.921376798,
            transmitTimeSecondsSince1900 = 3822217108.921392490,
        )

        assertDecode(packetHex, packet)
    }

    @Test
    fun decodeGoogle() {
        // time.google.com

        val packetHex = "240108ec0000000000000006474f4f47e3d28fbacdae52f4e3d28fbac48d1959e3d28fbacdae52f5e3d28fbacdae52f7"

        val packet = NtpPacket(
            warningLeapSecond = 0,
            protocolVersion = 4,
            protocolMode = 4,
            stratum = 1,
            maximumPollIntervalPowerOfTwo = 8,
            precisionPowerOfTwo = -20,
            rootDelaySeconds = 0.0,
            rootDispersionSeconds = 0.000091552734375,
            referenceTimeSecondsSince1900 = 3822227386.803441223,
            originateTimeSecondsSince1900 = 3822227386.767778000,
            receiveTimeSecondsSince1900 = 3822227386.803441223,
            transmitTimeSecondsSince1900 = 3822227386.803441224,
        )

        assertDecode(packetHex, packet)
    }

    @Test
    fun decodePool() {
        // pool.ntp.org

        val packetHex = "240208e900000cfa000008fac0356768e3d28bd2102d8729e3d290ea0e1c0443e3d290ea13774710e3d290ea137a40bd"

        val packet = NtpPacket(
            warningLeapSecond = 0,
            protocolVersion = 4,
            protocolMode = 4,
            stratum = 2,
            maximumPollIntervalPowerOfTwo = 8,
            precisionPowerOfTwo = -23,
            rootDelaySeconds = 0.050689697265625,
            rootDispersionSeconds = 0.035064697265625,
            referenceTimeSecondsSince1900 = 3822226386.063194701,
            originateTimeSecondsSince1900 = 3822227690.055115000,
            receiveTimeSecondsSince1900 = 3822227690.076038781,
            transmitTimeSecondsSince1900 = 3822227690.076084180,
        )

        assertDecode(packetHex, packet)
    }

    private fun assertDecode(packetHex: String, packet: NtpPacket) {
        assertThat(packets.decode(packetHex.decodeHex().asByteBuffer())).isEqualTo(packet)
    }
}
