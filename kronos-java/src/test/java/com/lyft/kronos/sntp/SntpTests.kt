package com.lyft.kronos.sntp

import com.lyft.kronos.ntp.FakeNtpPackets
import com.lyft.kronos.ntp.fakeNtpPacket
import org.assertj.core.api.Assertions.assertThat
import org.junit.Test
import java.net.InetAddress
import java.nio.ByteBuffer

class SntpTests {

    private val ntpHosts = listOf("fake.domain")
    private val ntpPackets = FakeNtpPackets()
    private val dns = FakeDns()
    private val udp = FakeUdp()

    private val sntp = Sntp.Impl(
        ntpHosts = ntpHosts,
        ntpPackets = ntpPackets,
        dns = dns,
        udp = udp,
    )

    @Test
    fun `request without DNS results`() {
        dns.resolveResult = emptyArray()

        assertThat(sntp.request(fakeNtpPacket)).isNull()
    }

    @Test
    fun `request with DNS results but UDP failure`() {
        ntpPackets.encodeResult = ByteBuffer.allocate(0)
        dns.resolveResult = arrayOf(InetAddress.getLoopbackAddress())
        udp.requestResult = Udp.Result.Failure

        assertThat(sntp.request(fakeNtpPacket)).isNull()
    }

    @Test
    fun `request with DNS results and UDP success`() {
        ntpPackets.encodeResult = ByteBuffer.allocate(0)
        ntpPackets.decodeResult = fakeNtpPacket
        dns.resolveResult = arrayOf(InetAddress.getLoopbackAddress())
        udp.requestResult = Udp.Result.Success

        assertThat(sntp.request(fakeNtpPacket)).isEqualTo(fakeNtpPacket)
    }
}
