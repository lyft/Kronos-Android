package com.lyft.kronos.sntp

import org.assertj.core.api.Assertions.assertThat
import org.junit.Test
import java.net.InetAddress

class DnsTests {

    private val dns by lazy { Dns.Impl() }

    @Test
    fun resolveLocalhost() {
        val actual = dns.resolve("localhost")
        val expected = arrayOf(InetAddress.getByName("127.0.0.1"), InetAddress.getByName("0:0:0:0:0:0:0:1"))

        assertThat(actual).isEqualTo(expected)
    }

    @Test
    fun resolveUnknown() {
        val actual = dns.resolve("unknown")
        val expected = emptyArray<InetAddress>()

        assertThat(actual).isEqualTo(expected)
    }
}
