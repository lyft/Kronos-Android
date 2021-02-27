package com.lyft.kronos.ntp

import java.nio.ByteBuffer

internal interface NtpPackets {

    fun decode(packetBuffer: ByteBuffer): NtpPacket
    fun encode(packet: NtpPacket): ByteBuffer

    class Impl : NtpPackets {

        companion object {
            private const val NTP_PACKET_SIZE_BYTES = 48
        }

        // Source of truth (SNTP 4): https://tools.ietf.org/html/rfc4330

        override fun decode(packetBuffer: ByteBuffer) = NtpPacket(
            warningLeapSecond = (packetBuffer.get(0).toInt().shr(6) and 3).toByte(),
            protocolVersion = (packetBuffer.get(0).toInt().shr(3) and 7).toByte(),
            protocolMode = (packetBuffer.get(0).toInt() and 7).toByte(),
            stratum = packetBuffer.get(1),
            maximumPollIntervalPowerOfTwo = packetBuffer.get(2),
            precisionPowerOfTwo = packetBuffer.get(3),
            rootDelaySeconds = packetBuffer.getIntervalSeconds(4),
            rootDispersionSeconds = packetBuffer.getIntervalSeconds(8),
            referenceTimeSecondsSince1900 = packetBuffer.getTimeSeconds(16),
            originateTimeSecondsSince1900 = packetBuffer.getTimeSeconds(24),
            receiveTimeSecondsSince1900 = packetBuffer.getTimeSeconds(32),
            transmitTimeSecondsSince1900 = packetBuffer.getTimeSeconds(40),
        )

        private fun ByteBuffer.getIntervalSeconds(position: Int): Double {
            val integer = getUInt16(position)
            val decimal = getUInt16Decimal(position + 2)

            return integer + decimal
        }

        private fun ByteBuffer.getTimeSeconds(position: Int): Double {
            val seconds = getUInt32(position)
            val secondsFraction = getUInt32Decimal(position + 4)

            return seconds + secondsFraction
        }

        override fun encode(packet: NtpPacket) = ByteBuffer.allocate(NTP_PACKET_SIZE_BYTES)
            .put(0, packet.warningLeapSecond.toInt().shl(6).or(packet.protocolVersion.toInt().shl(3)).or(packet.protocolMode.toInt()).toByte())
            .put(1, packet.stratum)
            .put(2, packet.maximumPollIntervalPowerOfTwo)
            .put(3, packet.precisionPowerOfTwo)
            .putIntervalSeconds(4, packet.rootDelaySeconds)
            .putIntervalSeconds(8, packet.rootDispersionSeconds)
            .putTimeSeconds(16, packet.referenceTimeSecondsSince1900)
            .putTimeSeconds(24, packet.originateTimeSecondsSince1900)
            .putTimeSeconds(32, packet.receiveTimeSecondsSince1900)
            .putTimeSeconds(40, packet.transmitTimeSecondsSince1900)

        private fun ByteBuffer.putIntervalSeconds(position: Int, seconds: Double) = this
            .putUInt16(position, seconds.toInt())
            .putUInt16Decimal(position + 2, seconds - seconds.toInt())

        private fun ByteBuffer.putTimeSeconds(position: Int, seconds: Double) = this
            .putUInt32(position, seconds.toLong())
            .putUInt32Decimal(position + 4, seconds - seconds.toLong())
    }
}
