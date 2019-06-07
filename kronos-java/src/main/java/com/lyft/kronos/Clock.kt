package com.lyft.kronos

interface Clock {
    /**
     * @return the current time in milliseconds.
     */
    fun getCurrentTimeMs(): Long

    /**
     * @return milliseconds since boot, including time spent in sleep.
     */
    fun getElapsedTimeMs(): Long
}

data class KronosTime(
        /**
         * Number of milliseconds that have elapsed since 00:00:00 Coordinated Universal Time (UTC), Thursday, 1 January 1970
         */
        val posixTimeMs: Long,

        /**
         * Number of milliseconds since the last successful NTP sync, or null if the time is coming from the fallback clock
         */
        val timeSinceLastNtpSyncMs: Long?
)

/**
 * When [KronosClock] has been synchronized, calling [getCurrentTimeMs]
 * will return the true time. However if [sync] fails or the class has
 * been shutdown via [shutdown] method, then it will return the time
 * indicated by the local device.
 */
interface KronosClock : Clock {
    fun getCurrentTime(): KronosTime
    override fun getCurrentTimeMs(): Long {
        return getCurrentTime().posixTimeMs
    }

    /**
     * @return the current time in milliseconds, or null if no ntp sync has occurred.
     */
    fun getCurrentNtpTimeMs(): Long?

    fun sync(): Boolean
    fun syncInBackground()
    fun shutdown()
}
