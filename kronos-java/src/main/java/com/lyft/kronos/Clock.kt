package com.lyft.kronos

interface Clock {
    /**
     * @return the current time in milliseconds (the number of milliseconds that have elapsed since 00:00:00 Coordinated Universal Time (UTC), Thursday, 1 January 1970).
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

interface KronosClock : Clock {

    /**
     * Return the number of milliseconds that have elapsed since 00:00:00 Coordinated Universal Time (UTC), Thursday, 1 January 1970.
     *
     * Note that calling this method will trigger [syncInBackground] being called when necessary.
     * You still call [sync]/[syncInBackground] once to ensure you have the correct
     * as soon as possible.
     *
     * @return the current time in milliseconds.
     */
    override fun getCurrentTimeMs(): Long {
        return getCurrentTime().posixTimeMs
    }

    /**
     * @return the current time in milliseconds, or null if no ntp sync has occurred.
     */
    fun getCurrentNtpTimeMs(): Long?

    /**
     * Same as [getCurrentTimeMs], but returned [KronosTime] object includes time since last NTP sync, or null if the time is coming from the fallback clock.
     *
     * @return [KronosTime] for the current time
     */
    fun getCurrentTime(): KronosTime

    /**
     * Synchronize time an NTP server.
     *
     * @return true on the first successful response, false if no successful response.
     */
    fun sync(): Boolean

    /**
     * Calls [sync] in a background thread. This method returns immediately.
     */
    fun syncInBackground()

    /**
     * You can call [shutdown] when you no longer need this service. This will shutdown
     * the thread that perform sync in the background. Any subsequent call to [currentTime]
     * will throw [IllegalStateException].
     */
    fun shutdown()
}
