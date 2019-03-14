package com.lyft.kronos

interface SyncResponseCache {
    var currentTime : Long
    var elapsedTime : Long
    var currentOffset: Long
    fun clear()
}