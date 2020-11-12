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
     * You should still call [sync]/[syncInBackground] once to ensure you have the correct time
     * as soon as possible.
     *
     * @return the current time in milliseconds.
     */
    override fun getCurrentTimeMs(): Long {
        return getCurrentTime().posixTimeMs
    }

    /**
     * Unlike the [getCurrentTimeMs] method, synchronization is not started here.
     * Unknown or expired time represented as null.
     *
     * @return the current time in milliseconds, or null.
     */
    fun getCachedNtpTimeMs(): Long?

    /**
     * @return [KronosTime] for the current time
     */
    fun getCurrentTime(): KronosTime

    /**
     * Synchronize time with an NTP server.
     *
     * @return true on the first successful response, false if no successful response.
     */
    fun sync(): Boolean

    /**
     * Calls [sync] in a background thread. This method returns immediately.
     */
    fun syncInBackground()

    /**
     * Shuts down the thread that performs syncing in the background. Any subsequent call to [getCurrentTime]
     * will throw [IllegalStateException]. You can call [shutdown] when you no longer need this service.
     */
    fun shutdown()
}
