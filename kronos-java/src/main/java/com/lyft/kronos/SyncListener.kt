package com.lyft.kronos


interface SyncListener {

    fun onStartSync(host: String)

    fun onSuccess(ticksDelta: Int, responseTimeMs: Long)

    fun onError(host: String, throwable: Throwable)
}
