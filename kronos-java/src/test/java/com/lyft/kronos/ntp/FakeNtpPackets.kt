package com.lyft.kronos.ntp

import java.nio.ByteBuffer

internal class FakeNtpPackets : NtpPackets {

    lateinit var decodeResult: NtpPacket
    lateinit var encodeResult: ByteBuffer

    override fun decode(packetBuffer: ByteBuffer) = decodeResult
    override fun encode(packet: NtpPacket) = encodeResult
}
