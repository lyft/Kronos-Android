package com.lyft.kronos.ntp

import java.nio.ByteBuffer

internal interface NtpPackets {

    fun decode(packetBuffer: ByteBuffer): NtpPacket

    class Impl : NtpPackets {

        // Source of truth (SNTP 4): https://tools.ietf.org/html/rfc4330

        override fun decode(packetBuffer: ByteBuffer) = NtpPacket(
            warningLeapSecond = (packetBuffer.get(0).toInt().shr(6) and 3).toByte(),
            protocolVersion = (packetBuffer.get(0).toInt().shr(3) and 7).toByte(),
            protocolMode = (packetBuffer.get(0).toInt() and 7).toByte(),
            stratum = packetBuffer.get(1),
            maximumPollIntervalPowerOfTwo = packetBuffer.get(2),
            precisionPowerOfTwo = packetBuffer.get(3),
            rootDelaySeconds = packetBuffer.intervalSeconds(4),
            rootDispersionSeconds = packetBuffer.intervalSeconds(8),
            referenceTimeSecondsSince1900 = packetBuffer.timeSeconds(16),
            originateTimeSecondsSince1900 = packetBuffer.timeSeconds(24),
            receiveTimeSecondsSince1900 = packetBuffer.timeSeconds(32),
            transmitTimeSecondsSince1900 = packetBuffer.timeSeconds(40),
        )

        private fun ByteBuffer.intervalSeconds(position: Int): Double {
            val integer = getUInt16(position)
            val decimal = getUInt16Decimal(position + 2)

            return integer + decimal
        }

        private fun ByteBuffer.timeSeconds(position: Int): Double {
            val seconds = getUInt32(position)
            val secondsFraction = getUInt32Decimal(position + 4)

            return seconds + secondsFraction
        }

        private fun ByteBuffer.getUInt16(position: Int): Int = this
            .getShort(position)
            .toInt()
            .and(0xffff)

        private fun ByteBuffer.getUInt16Decimal(position: Int): Double = getUInt16(position)
            .toDouble() / (0xffff + 1)

        private fun ByteBuffer.getUInt32(position: Int): Long = this
            .getInt(position)
            .toLong()
            .and(0xffffffff)

        private fun ByteBuffer.getUInt32Decimal(position: Int): Double = getUInt32(position)
            .toDouble() / (0xffffffff + 1)
    }
}
