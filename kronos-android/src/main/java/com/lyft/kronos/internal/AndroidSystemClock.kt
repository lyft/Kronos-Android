package com.lyft.kronos.internal

import android.os.SystemClock
import com.lyft.kronos.Clock

internal class AndroidSystemClock : Clock {
    override fun getCurrentTimeMs(): Long = System.currentTimeMillis()
    override fun getElapsedTimeMs(): Long = SystemClock.elapsedRealtime()
}