package com.lyft.kronos

import com.lyft.kronos.DefaultParam.CACHE_EXPIRATION_MS
import com.lyft.kronos.DefaultParam.MIN_WAIT_TIME_BETWEEN_SYNC_MS
import com.lyft.kronos.DefaultParam.NTP_HOSTS
import com.lyft.kronos.DefaultParam.TIMEOUT_MS
import com.lyft.kronos.internal.KronosClockImpl
import com.lyft.kronos.internal.ntp.*

object ClockFactory {

    /**
     * Create a new instance of KronosClock which is synchronized with one of several
     * NTP server to ensure that the time is accurate. You should create only one instance
     * and keep it somewhere you can access throughout your application.
     *
     * @param syncResponseCache will be used to cache sync response so that time can be calculated after a successful clock sync
     * @param syncListener Allows you to log sync operation successes and errors.
     * @param ntpHosts Kronos synchronizes with this set of NTP servers. The default set of servers are ordered according to success rate from analytic
     * @param localClock local device clock that will be used as a fallback if NTP sync fails.
     * @param requestTimeoutMs Lengthen or shorten the timeout value. If the NTP server fails to respond within the given time, the next server will be contacted. If none of the server respond within the given time, the sync operation will be considered a failure.
     * @param minWaitTimeBetweenSyncMs Kronos attempts a synchronization at most once a minute. If you want to change the frequency, supply the desired time in milliseconds. Note that you should also supply a cacheExpirationMs value. For example, if you shorten the minWaitTimeBetweenSyncMs to 30 seconds, but leave the cacheExpirationMs to 1 minute, it will have no affect because the cache is still valid within the 1 minute window.
     * @param cacheExpirationMs Kronos will perform a background sync if the cache is stale. The cache is valid for 1 minute by default. It is simpliest to keep the cacheExpirationMs value the same as minWaitTimeBetweenSyncMs value.
     *
     */
    @JvmStatic
    @JvmOverloads
    fun createKronosClock(localClock: Clock,
                          syncResponseCache: SyncResponseCache,
                          syncListener: SyncListener? = null,
                          ntpHosts: List<String> = NTP_HOSTS,
                          requestTimeoutMs: Long = TIMEOUT_MS,
                          minWaitTimeBetweenSyncMs: Long = MIN_WAIT_TIME_BETWEEN_SYNC_MS,
                          cacheExpirationMs: Long = CACHE_EXPIRATION_MS): KronosClock {

        if (localClock is KronosClock) {
            throw IllegalArgumentException("Local clock should implement Clock instead of KronosClock")
        }

        val sntpClient = SntpClient(localClock, DnsResolverImpl(), DatagramFactoryImpl())
        val cache = SntpResponseCacheImpl(syncResponseCache, localClock)
        val ntpService = SntpServiceImpl(sntpClient, localClock, cache, syncListener, ntpHosts, requestTimeoutMs, minWaitTimeBetweenSyncMs, cacheExpirationMs)
        return KronosClockImpl(ntpService, localClock)
    }
}
