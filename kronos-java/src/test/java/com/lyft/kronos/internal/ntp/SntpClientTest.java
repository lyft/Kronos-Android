package com.lyft.kronos.internal.ntp;

import com.lyft.kronos.Clock;
import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.Inet4Address;
import java.net.InetAddress;
import java.net.SocketException;
import java.net.UnknownHostException;
import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.mockito.stubbing.Answer;

import static junit.framework.Assert.fail;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.eq;
import static org.mockito.Matchers.same;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class SntpClientTest {

    private static final String HOST = "host";
    private static final long TIMEOUT = 1200;

    @Mock
    Clock deviceClock;
    @Mock
    DnsResolver dnsResolver;
    @Mock
    DatagramFactory datagramFactory;
    @Mock
    DatagramSocket datagramSocket;

    private SntpClient sntpClient;

    @Before
    public void setUp() throws SocketException, UnknownHostException {
        MockitoAnnotations.initMocks(this);
        when(datagramFactory.createSocket()).thenReturn(datagramSocket);
        sntpClient = new SntpClient(deviceClock, dnsResolver, datagramFactory);

        when(dnsResolver.resolve(eq(HOST))).thenReturn(mock(Inet4Address.class));
    }

    private void setupDeviceClock(long current) {
        when(deviceClock.getCurrentTimeMs()).thenReturn(current);
    }

    private void setupDeviceClock(long current, long elapsed) {
        when(deviceClock.getCurrentTimeMs()).thenReturn(current);
        when(deviceClock.getElapsedTimeMs()).thenReturn(elapsed);
    }

    @Test
    public void requestTimeMustSetModeAndVersion() throws IOException {
        sntpClient.requestTime(HOST, TIMEOUT);

        ArgumentCaptor<byte[]> argumentCaptor = ArgumentCaptor.forClass(byte[].class);

        Mockito.verify(datagramFactory).createPacket(argumentCaptor.capture());

        byte firstOctet = argumentCaptor.getValue()[0];

        int mode = firstOctet & 0b111;
        assertThat(mode).isEqualTo(3);

        int version = firstOctet >> 3;
        assertThat(version).isEqualTo(3);
    }

    @Test
    public void requestTimeMustSendTransmitTimestamp() throws IOException {
        setupDeviceClock(1474571532122L);
        DatagramPacket packet = mock(DatagramPacket.class);
        when(datagramFactory.createPacket(any(byte[].class), any(InetAddress.class), eq(123))).thenReturn(packet);

        sntpClient.requestTime(HOST, TIMEOUT);

        ArgumentCaptor<byte[]> argumentCaptor = ArgumentCaptor.forClass(byte[].class);

        Mockito.verify(datagramFactory).createPacket(argumentCaptor.capture(), any(Inet4Address.class), eq(123));
        Mockito.verify(datagramSocket).send(same(packet));

        byte[] buffer = argumentCaptor.getValue();

        long readTimeStamp = SntpClient.readTimeStamp(buffer, 40);
        assertThat(readTimeStamp).isBetween(1474571532121L, 1474571532123L);
    }

    @Test
    public void requestTimeShouldReceiveTime() throws IOException {
        final long currentTime = 1473227267196L;
        final long requestElapsedTime = 271963948L;
        final long responseElapsedTime = 271964006L;
        final long firstElapsedTime = 271966013L;
        final long secondElapsedTime = 271966019L;

        when(deviceClock.getCurrentTimeMs())
                .thenReturn(currentTime);
        when(deviceClock.getElapsedTimeMs())
                .thenReturn(requestElapsedTime)
                .thenReturn(responseElapsedTime)
                .thenReturn(firstElapsedTime)
                .thenReturn(secondElapsedTime)
                .thenThrow(new RuntimeException("No more interactions expected."));

        final long ntpTime = 1473227268363L;
        final byte[] response = {
                0x1c, 0x2, 0x3, 0xffffffe9, 0x0, 0x0, 0x1, 0xffffffbb, 0x0, 0x0, 0x7, 0xffffffae, 0xffffff8e, 0x42, 0x65, 0xd,
                0xffffffdb, 0x7a, 0x26, 0x1c, 0xffffff9b, 0x27, 0x6, 0xffffffa3, 0xffffffdb, 0x7a, 0x28, 0xffffff83, 0x32, 0x2d,
                0xe, 0x7, 0xffffffdb, 0x7a, 0x28, 0xffffff84, 0x55, 0xffffffbc, 0x7e, 0xffffffef, 0xffffffdb, 0x7a, 0x28,
                0xffffff84, 0x55, 0xffffffc1, 0x4, 0x3
        };
        when(datagramFactory.createPacket(any(byte[].class))).then((Answer<DatagramPacket>) invocation -> {
            byte[] buffer = invocation.getArgument(0);
            System.arraycopy(response, 0, buffer, 0, buffer.length);
            return mock(DatagramPacket.class);
        });

        final SntpClient.Response clientResponse = sntpClient.requestTime(HOST, TIMEOUT);

        Mockito.verify(datagramFactory).createPacket(any(byte[].class));
        Mockito.verify(datagramSocket).receive(any(DatagramPacket.class));

        assertThat(clientResponse.getCurrentTimeMs())
                .isEqualTo(ntpTime + (firstElapsedTime - responseElapsedTime));
        assertThat(clientResponse.getResponseAge())
                .isEqualTo(secondElapsedTime - responseElapsedTime);
        assertThat(clientResponse.getOffsetMs())
                .isEqualTo(ntpTime - (currentTime + (responseElapsedTime - requestElapsedTime)));
    }

    @Test
    public void requestTimeMustCloseTheSocket() throws IOException {
        sntpClient.requestTime(HOST, TIMEOUT);

        Mockito.verify(datagramSocket).close();
    }

    @Test
    public void requestTimeMustCloseTheSocketWithException() throws IOException {
        when(datagramFactory.createPacket(any(byte[].class), any(InetAddress.class), eq(123))).thenReturn(mock(DatagramPacket.class));

        IOException ioException = new IOException("I'm Expected");
        Mockito.doThrow(ioException).when(datagramSocket).send(any(DatagramPacket.class));

        try {
            sntpClient.requestTime(HOST, TIMEOUT);
            fail("Exception expected.");
        } catch (IOException e) {
            assertThat(e).isSameAs(ioException);
        }

        Mockito.verify(datagramSocket).close();
    }

    @Test
    public void requestTimeShouldSetTimeout() throws IOException {
        sntpClient.requestTime(HOST, 1234L);

        Mockito.verify(datagramSocket).setSoTimeout(1234);
    }

    @Test
    public void responseShouldNotAdjustWhenBootTimeSeemsEqual() {
        final SntpClient.Response original = new SntpClient.Response(10_000_000L, 6_000L, 1L, deviceClock);
        // 10 seconds passed
        setupDeviceClock(10_010_000L, 16_000L);

        assertThat(original.isFromSameBoot());
    }

    @Test
    public void responseShouldAdjustWhenBootTimeChanged() {
        final SntpClient.Response original = new SntpClient.Response(10_000_000L, 6_000L, 1L, deviceClock);
        // 10 seconds passed, but we rebooted 5 seconds ago
        setupDeviceClock(10_010_000L, 5_000L);

        assertThat(original.isFromSameBoot()).isFalse();
    }
}
