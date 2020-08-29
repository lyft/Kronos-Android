package com.lyft.kronos.internal

import com.lyft.kronos.Clock
import com.lyft.kronos.KronosClock
import com.lyft.kronos.KronosTime
import com.lyft.kronos.internal.ntp.SntpService

internal class KronosClockImpl(private val ntpService: SntpService, private val fallbackClock: Clock) : KronosClock {

    override fun sync() = ntpService.sync()

    override fun syncInBackground() = ntpService.syncInBackground()

    override fun shutdown() = ntpService.shutdown()

    override fun getElapsedTimeMs(): Long = fallbackClock.getElapsedTimeMs()

    override fun getCurrentTime(): KronosTime {
        val currentTime = ntpService.currentTime()
        return currentTime ?: KronosTime(posixTimeMs = fallbackClock.getCurrentTimeMs(), timeSinceLastNtpSyncMs = null)
    }

    override fun getCachedNtpTimeMs() : Long? = ntpService.cachedTime()?.posixTimeMs
}