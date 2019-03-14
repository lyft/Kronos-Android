package com.lyft.kronos.internal.ntp

import java.net.InetAddress
import java.net.UnknownHostException

internal interface DnsResolver {

    @Throws(UnknownHostException::class)
    fun resolve(host: String): InetAddress
}

internal class DnsResolverImpl : DnsResolver {
    
    @Throws(UnknownHostException::class)
    override fun resolve(host: String): InetAddress = InetAddress.getByName(host)
}