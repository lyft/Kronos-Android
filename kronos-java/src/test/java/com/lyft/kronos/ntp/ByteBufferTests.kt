package com.lyft.kronos.ntp

import okio.ByteString.Companion.decodeHex
import okio.ByteString.Companion.toByteString
import org.assertj.core.api.Assertions.assertThat
import org.junit.Test
import java.nio.ByteBuffer

class ByteBufferTests {

    private data class UInt16(val byteBufferHex: String, val value: Double)

    private val valuesUInt16 = listOf(
        UInt16("00000006", 0.000091552734375),
        UInt16("00000007", 0.0001068115234375),
        UInt16("0000000c", 0.00018310546875),
        UInt16("000008fa", 0.035064697265625),
        UInt16("00000cfa", 0.050689697265625),
    )

    @Test
    fun getUInt16() {
        valuesUInt16.forEach {
            val buffer = it.byteBufferHex.decodeHex().asByteBuffer()
            val value = buffer.getUInt16(0) + buffer.getUInt16Decimal(2)

            assertThat(value).isEqualTo(it.value)
        }
    }

    @Test
    fun putUInt16() {
        valuesUInt16.forEach {
            val buffer = ByteBuffer.allocate(4)
                .putUInt16(0, it.value.toInt())
                .putUInt16Decimal(2, it.value - it.value.toInt())

            val bufferHex = buffer.toByteString().hex()

            assertThat(bufferHex).isEqualTo(it.byteBufferHex)
        }
    }

    private data class UInt32(val byteBufferHex: String, val value: Double)

    private val valuesUInt32 = listOf(
        UInt32("e3d26794ebe06000", 3822217108.921392490),
        UInt32("e3d28d7989139000", 3822226809.535454745),
        UInt32("e3d28fbacdae5000", 3822227386.803441224),
        UInt32("e3d290ea137a4000", 3822227690.076084180),
    )

    @Test
    fun getUInt32() {
        valuesUInt32.forEach {
            val buffer = it.byteBufferHex.decodeHex().asByteBuffer()
            val value = buffer.getUInt32(0) + buffer.getUInt32Decimal(4)

            assertThat(value).isEqualTo(it.value)
        }
    }

    @Test
    fun putUInt32() {
        valuesUInt32.forEach {
            val buffer = ByteBuffer.allocate(8)
                .putUInt32(0, it.value.toLong())
                .putUInt32Decimal(4, it.value - it.value.toLong())

            val bufferHex = buffer.toByteString().hex()

            assertThat(bufferHex).isEqualTo(it.byteBufferHex)
        }
    }
}
