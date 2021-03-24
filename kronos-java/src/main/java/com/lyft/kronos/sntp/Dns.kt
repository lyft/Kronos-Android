package com.lyft.kronos.sntp

import java.net.InetAddress
import java.net.UnknownHostException

internal interface Dns {

    fun resolve(host: String): Array<out InetAddress>

    class Impl : Dns {

        override fun resolve(host: String) = try {
            InetAddress.getAllByName(host).orEmpty()
        } catch (e: SecurityException) {
            emptyArray()
        } catch (e: UnknownHostException) {
            emptyArray()
        }
    }
}
