package com.lyft.kronos.sntp

import java.net.SocketAddress
import java.nio.ByteBuffer

internal class FakeUdp : Udp {

    lateinit var requestResult: Udp.Result

    override fun request(address: SocketAddress, request: ByteBuffer, response: ByteBuffer) = requestResult
}
