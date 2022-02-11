package com.lyft.kronos.internal

import android.content.Context
import android.os.Build
import android.os.SystemClock
import android.provider.Settings
import com.lyft.kronos.Clock

internal class AndroidSystemClock(private val context: Context) : Clock {
    override fun getCurrentTimeMs(): Long = System.currentTimeMillis()
    override fun getElapsedTimeMs(): Long = SystemClock.elapsedRealtime()
    override fun getBootCount(): Int? =
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            Settings.Global.getInt(context.contentResolver, Settings.Global.BOOT_COUNT)
        } else {
            null
        }
}
