package com.lyft.kronos.internal.ntp

import java.net.DatagramPacket
import java.net.DatagramSocket
import java.net.InetAddress
import java.net.SocketException

internal interface DatagramFactory {

    @Throws(SocketException::class)
    fun createSocket(): DatagramSocket

    fun createPacket(buffer: ByteArray): DatagramPacket

    fun createPacket(buffer: ByteArray, address: InetAddress, port: Int): DatagramPacket
}

internal class DatagramFactoryImpl : DatagramFactory {
    
    @Throws(SocketException::class)
    override fun createSocket(): DatagramSocket = DatagramSocket()

    override fun createPacket(buffer: ByteArray): DatagramPacket = DatagramPacket(buffer, buffer.size)

    override fun createPacket(buffer: ByteArray, address: InetAddress, port: Int): DatagramPacket = DatagramPacket(buffer, buffer.size, address, port)
}