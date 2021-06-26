package com.lyft.kronos.sntp

import java.net.InetAddress

internal class FakeDns : Dns {

    lateinit var resolveResult: Array<out InetAddress>

    override fun resolve(host: String) = resolveResult
}
