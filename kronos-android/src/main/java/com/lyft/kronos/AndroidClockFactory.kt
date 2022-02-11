package com.lyft.kronos

import android.content.Context
import com.lyft.kronos.DefaultParam.CACHE_EXPIRATION_MS
import com.lyft.kronos.DefaultParam.MAX_NTP_RESPONSE_TIME_MS
import com.lyft.kronos.DefaultParam.MIN_WAIT_TIME_BETWEEN_SYNC_MS
import com.lyft.kronos.DefaultParam.NTP_HOSTS
import com.lyft.kronos.DefaultParam.TIMEOUT_MS
import com.lyft.kronos.internal.AndroidSystemClock
import com.lyft.kronos.internal.SharedPreferenceSyncResponseCache

object AndroidClockFactory {
    /**
     * Create a device clock that uses the OS/device specific API to retrieve time
     */
    @JvmStatic
    fun createDeviceClock(context: Context): Clock = AndroidSystemClock(context)

    @JvmStatic
    @JvmOverloads
    fun createKronosClock(context: Context,
                          syncListener: SyncListener? = null,
                          ntpHosts: List<String> = NTP_HOSTS,
                          requestTimeoutMs: Long = TIMEOUT_MS,
                          minWaitTimeBetweenSyncMs: Long = MIN_WAIT_TIME_BETWEEN_SYNC_MS,
                          cacheExpirationMs: Long = CACHE_EXPIRATION_MS,
                          maxNtpResponseTimeMs: Long = MAX_NTP_RESPONSE_TIME_MS): KronosClock {

        val deviceClock = createDeviceClock(context)
        val cache = SharedPreferenceSyncResponseCache(context.getSharedPreferences(SharedPreferenceSyncResponseCache.SHARED_PREFERENCES_NAME, Context.MODE_PRIVATE))

        return ClockFactory.createKronosClock(deviceClock, cache, syncListener, ntpHosts, requestTimeoutMs, minWaitTimeBetweenSyncMs, cacheExpirationMs, maxNtpResponseTimeMs)
    }
}
