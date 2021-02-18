package com.lyft.kronos.ntp

import java.nio.ByteBuffer

internal fun ByteBuffer.getUInt16(position: Int): Int = this
    .getShort(position)
    .toInt()
    .and(0xffff)

internal fun ByteBuffer.getUInt16Decimal(position: Int): Double = this
    .getUInt16(position)
    .toDouble() / (0xffff + 1)

internal fun ByteBuffer.putUInt16(position: Int, value: Int): ByteBuffer = this
    .putShort(position, value.toShort())

internal fun ByteBuffer.putUInt16Decimal(position: Int, value: Double): ByteBuffer = this
    .putUInt16(position, (value * (0xffff + 1)).toInt())

internal fun ByteBuffer.getUInt32(position: Int): Long = this
    .getInt(position)
    .toLong()
    .and(0xffff_ffff)

internal fun ByteBuffer.getUInt32Decimal(position: Int): Double = this
    .getUInt32(position)
    .toDouble() / (0xffff_ffff + 1)

internal fun ByteBuffer.putUInt32(position: Int, value: Long): ByteBuffer = this
    .putInt(position, value.toInt())

internal fun ByteBuffer.putUInt32Decimal(position: Int, value: Double): ByteBuffer = this
    .putUInt32(position, (value * (0xffff_ffff + 1)).toLong())
