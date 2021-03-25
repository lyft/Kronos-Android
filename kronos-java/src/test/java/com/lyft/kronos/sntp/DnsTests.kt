package com.lyft.kronos.sntp

import org.assertj.core.api.Assertions.assertThat
import org.junit.Test
import java.net.InetAddress

class DnsTests {

    private val dns by lazy { Dns.Impl() }

    @Test
    fun resolveLocalhost() {
        assertThat(dns.resolve("localhost")).allMatch { it.isLoopbackAddress }
    }

    @Test
    fun resolveUnknown() {
        assertThat(dns.resolve("unknown")).isEqualTo(emptyArray<InetAddress>())
    }
}
