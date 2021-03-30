package com.lyft.kronos.sntp

import org.assertj.core.api.Assertions.assertThat
import org.junit.Test
import java.net.InetAddress
import java.net.InetSocketAddress
import java.nio.ByteBuffer

class UdpTests {

    private val udp by lazy { Udp.Impl() }

    @Test
    fun requestUnknown() {
        val address = InetSocketAddress(InetAddress.getLoopbackAddress(), 42)
        val request = ByteBuffer.allocate(42)
        val response = ByteBuffer.allocate(42)

        assertThat(udp.request(address, request, response)).isEqualTo(Udp.Result.Failure)
    }
}
