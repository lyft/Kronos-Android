package com.lyft.kronos

import java.util.concurrent.TimeUnit

object DefaultParam {
    val NTP_HOSTS = listOf("0.pool.ntp.org", "1.pool.ntp.org", "2.pool.ntp.org", "3.pool.ntp.org")
    // Sync with NTP if the cache is older than this value
    val CACHE_EXPIRATION_MS = TimeUnit.MINUTES.toMillis(1)
    // Sync with NTP only after MIN_WAIT_TIME_BETWEEN_SYNC_MS regardless of success or failure
    val MIN_WAIT_TIME_BETWEEN_SYNC_MS = TimeUnit.MINUTES.toMillis(1)
    val TIMEOUT_MS = TimeUnit.SECONDS.toMillis(6)
    val MAX_NTP_RESPONSE_TIME_MS = TimeUnit.SECONDS.toMillis(5)
}
