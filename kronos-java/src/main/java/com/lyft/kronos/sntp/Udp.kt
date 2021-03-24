package com.lyft.kronos.sntp

import java.lang.Exception
import java.net.DatagramPacket
import java.net.DatagramSocket
import java.net.SocketAddress
import java.nio.ByteBuffer
import java.util.concurrent.TimeUnit

internal interface Udp {

    enum class Result { Success, Failure }

    fun request(address: SocketAddress, request: ByteBuffer, response: ByteBuffer): Result

    class Impl : Udp {

        companion object {
            private val TIMEOUT_MILLIS = TimeUnit.SECONDS.toMillis(3).toInt()
        }

        override fun request(address: SocketAddress, request: ByteBuffer, response: ByteBuffer): Result {
            val requestPacket = DatagramPacket(request.array(), request.capacity(), address)
            val responsePacket = DatagramPacket(response.array(), response.capacity())

            return try {
                DatagramSocket().use { socket ->
                    socket.soTimeout = TIMEOUT_MILLIS

                    socket.send(requestPacket)
                    socket.receive(responsePacket)
                }

                Result.Success
            } catch (e: Exception) {
                Result.Failure
            }
        }
    }
}
