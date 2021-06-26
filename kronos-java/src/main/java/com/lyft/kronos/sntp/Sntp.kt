package com.lyft.kronos.sntp

import com.lyft.kronos.ntp.NtpPacket
import com.lyft.kronos.ntp.NtpPackets
import java.net.InetSocketAddress
import java.nio.ByteBuffer

internal interface Sntp {

    fun request(requestPacket: NtpPacket): NtpPacket?

    class Impl(
        private val ntpHosts: List<String>,
        private val ntpPackets: NtpPackets,
        private val dns: Dns,
        private val udp: Udp,
    ) : Sntp {

        companion object {
            private const val NTP_PORT = 123
        }

        override fun request(requestPacket: NtpPacket) = ntpHosts
            .asSequence()
            .flatMap { dns.resolve(it).asSequence() }
            .mapNotNull {
                val requestBuffer = ntpPackets.encode(requestPacket)
                val responseBuffer = ByteBuffer.allocate(NtpPacket.SIZE_BYTES)

                when (udp.request(InetSocketAddress(it, NTP_PORT), requestBuffer, responseBuffer)) {
                    Udp.Result.Success -> ntpPackets.decode(responseBuffer)
                    Udp.Result.Failure -> null
                }
            }
            .firstOrNull()
    }
}
