package com.lyft.kronos.internal.ntp

import com.lyft.kronos.Clock
import com.lyft.kronos.SyncResponseCache
import com.lyft.kronos.internal.Constants.TIME_UNAVAILABLE
import com.nhaarman.mockito_kotlin.mock
import com.nhaarman.mockito_kotlin.times
import com.nhaarman.mockito_kotlin.verify
import com.nhaarman.mockito_kotlin.whenever
import org.assertj.core.api.Assertions.assertThat
import org.junit.Test
import java.util.concurrent.TimeUnit

class SntpResponseCacheTest {

    private val syncResponseCache = mock<SyncResponseCache>()

    private val deviceClock = mock<Clock>()

    private val cache: SntpResponseCacheImpl

    init {
        whenever(deviceClock.getCurrentTimeMs()).thenReturn(CURRENT_TIME_MS)
        whenever(deviceClock.getElapsedTimeMs()).thenReturn(ELAPSED_MS)
        whenever(deviceClock.getBootCount()).thenReturn(BOOT_COUNT)
        cache = SntpResponseCacheImpl(syncResponseCache, deviceClock)
    }

    @Test
    @Throws(Exception::class)
    fun testNoCache() {
        whenever(syncResponseCache.elapsedTime).thenReturn(TIME_UNAVAILABLE)
        assertThat(cache.get()).isNull()
    }

    @Test
    @Throws(Exception::class)
    fun testGetCache() {
        val currentTime = deviceClock.getCurrentTimeMs()
        val elapsedTime = deviceClock.getElapsedTimeMs()
        val currentOffset = TimeUnit.MINUTES.toMillis(5)
        val bootCount = deviceClock.getBootCount()

        whenever(syncResponseCache.currentTime).thenReturn(currentTime)
        whenever(syncResponseCache.elapsedTime).thenReturn(elapsedTime)
        whenever(syncResponseCache.bootCount).thenReturn(bootCount)
        whenever(syncResponseCache.currentOffset).thenReturn(currentOffset)

        val cachedResponse = cache.get()
        assertThat(cachedResponse).isNotNull()
        assertThat(cachedResponse!!.deviceCurrentTimestampMs).isEqualTo(currentTime)
        assertThat(cachedResponse.deviceElapsedTimestampMs).isEqualTo(elapsedTime)
        assertThat(cachedResponse.deviceBootCount).isEqualTo(bootCount)
        assertThat(cachedResponse.offsetMs).isEqualTo(currentOffset)
    }

    @Test
    @Throws(Exception::class)
    fun testGetCurrentTimeMs() {
        val currentTime = deviceClock.getCurrentTimeMs()
        val elapsedTime = deviceClock.getElapsedTimeMs()
        val currentOffset = TimeUnit.MINUTES.toMillis(-2)

        whenever(syncResponseCache.currentTime).thenReturn(currentTime)
        whenever(syncResponseCache.elapsedTime).thenReturn(elapsedTime)
        whenever(syncResponseCache.currentOffset).thenReturn(currentOffset)

        val cachedResponse = cache.get()

        // simulate time having passed 5 minutes
        whenever(deviceClock.getElapsedTimeMs()).thenReturn(ELAPSED_MS + TimeUnit.MINUTES.toMillis(5))

        // asssert that current time is 5 minutes ahead (time of cache with -2 min offset, + 5 minutes have passed
        assertThat(cachedResponse).isNotNull()
        assertThat(cachedResponse!!.currentTimeMs).isEqualTo(currentTime + TimeUnit.MINUTES.toMillis(3))
    }

    @Test
    @Throws(Exception::class)
    fun testUpdate() {
        val currentOffset = TimeUnit.HOURS.toMillis(5)
        val response = SntpClient.Response(CURRENT_TIME_MS, ELAPSED_MS, BOOT_COUNT, currentOffset, deviceClock)
        cache.update(response)

        verify(syncResponseCache, times(1)).currentTime = CURRENT_TIME_MS
        verify(syncResponseCache, times(1)).elapsedTime = ELAPSED_MS
        verify(syncResponseCache, times(1)).bootCount = BOOT_COUNT
        verify(syncResponseCache, times(1)).currentOffset = currentOffset
    }

    @Test
    @Throws(Exception::class)
    fun testClear() {
        cache.clear()

        verify(syncResponseCache, times(1)).clear()
    }

    companion object {

        private val CURRENT_TIME_MS = 1522964196L
        private val ELAPSED_MS = TimeUnit.HOURS.toMillis(8)
        private val BOOT_COUNT = 10
    }
}
