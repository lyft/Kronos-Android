package com.lyft.kronos.internal.ntp

import com.lyft.kronos.Clock
import com.lyft.kronos.DefaultParam.CACHE_EXPIRATION_MS
import com.lyft.kronos.DefaultParam.TIMEOUT_MS
import com.lyft.kronos.SyncListener
import com.nhaarman.mockito_kotlin.*
import org.assertj.core.api.Assertions.*
import org.junit.Test
import java.io.IOException

class SntpServiceTest {
    private val sntpService : SntpService

    private val sntpClient = mock<SntpClient>()
    private val deviceClock = mock<Clock>()
    private val responseCache = mock<SntpResponseCache>()
    private val sntpSyncListener = mock<SyncListener>()
    private val mockResponse = mock<SntpClient.Response>()
    private val ntpHosts = listOf("2.us.pool.ntp.org", "1.us.pool.ntp.org", "0.us.pool.ntp.org")

    init {
        whenever(deviceClock.getElapsedTimeMs()).then({ System.currentTimeMillis() })
        sntpService = SntpServiceImpl(sntpClient, deviceClock, responseCache, sntpSyncListener, ntpHosts)
    }

    @Test
    fun testRecoverFromNetworkError() {

        val mockError = mock<IOException>()
        whenever(sntpClient.requestTime(any(), any())).thenThrow(mockError)
        assertThat(sntpService.sync()).isFalse()

        verify(sntpSyncListener, times(3)).onStartSync(any())
        verify(sntpSyncListener, times(1)).onError("2.us.pool.ntp.org", mockError)
        verify(sntpSyncListener, times(1)).onError("1.us.pool.ntp.org", mockError)
        verify(sntpSyncListener, times(1)).onError("0.us.pool.ntp.org", mockError)

        whenever(sntpClient.requestTime(any(), any())).thenReturn(mockResponse)
        assertThat(sntpService.sync()).isTrue()

        verify(sntpSyncListener, times(2)).onStartSync("2.us.pool.ntp.org")
        verify(sntpSyncListener, times(1)).onSuccess(any(), any())
    }

    @Test
    fun testOneOfThreeServerHasError() {
        val mockError = mock<IOException>()
        whenever(sntpClient.requestTime("2.us.pool.ntp.org", TIMEOUT_MS)).thenThrow(mockError)
        whenever(sntpClient.requestTime("1.us.pool.ntp.org", TIMEOUT_MS)).thenReturn(mockResponse)
        assertThat(sntpService.sync()).isTrue()

        verify(sntpSyncListener, times(1)).onStartSync("2.us.pool.ntp.org")
        verify(sntpSyncListener, times(1)).onError("2.us.pool.ntp.org", mockError)
        verify(sntpSyncListener, times(1)).onStartSync("1.us.pool.ntp.org")
        verify(sntpSyncListener, times(1)).onSuccess(any(), any())
    }

    @Test
    fun shouldReturnTimeUnavailableAfterShutdown() {
        sntpService.shutdown()
        verify(sntpClient, never()).requestTime(any(), any())
        assertThatExceptionOfType(IllegalStateException::class.java).isThrownBy { sntpService.sync() }
        assertThatExceptionOfType(IllegalStateException::class.java).isThrownBy { sntpService.syncInBackground() }
        assertThatExceptionOfType(IllegalStateException::class.java).isThrownBy { sntpService.currentTime() }
    }

    @Test
    fun `throw error if response has negative time value`() {
        whenever(sntpClient.requestTime("1.us.pool.ntp.org", TIMEOUT_MS)).thenReturn(mockResponse)
        whenever(sntpClient.requestTime("2.us.pool.ntp.org", TIMEOUT_MS)).thenReturn(mockResponse)
        whenever(mockResponse.currentTimeMs).thenReturn(-1)
        assertThat(sntpService.sync()).isFalse()

        verify(sntpSyncListener, times(1)).onError(any(), any<IllegalStateException>())
    }

    @Test
    fun outdatedCachedTimeShouldBeNull() {
        val outdatedResponse = SntpClient.Response(System.currentTimeMillis(),
                CACHE_EXPIRATION_MS * 2, 0, deviceClock)
        val spyResponse = spy(outdatedResponse)
        doReturn(true).whenever(spyResponse).isFromSameBoot
        whenever(responseCache.get()).thenReturn(spyResponse)

        assertThat(sntpService.cachedTime()).isNull()
        verify(sntpClient, never()).requestTime(any(), any())
    }

    @Test
    fun restoreCachedTimeWithoutUpdate() {
        val outdatedResponse = SntpClient.Response(System.currentTimeMillis(),
                System.currentTimeMillis() - CACHE_EXPIRATION_MS / 2, 0, deviceClock)
        val spyResponse = spy(outdatedResponse)
        doReturn(true).whenever(spyResponse).isFromSameBoot
        whenever(responseCache.get()).thenReturn(spyResponse)

        assertThat(sntpService.cachedTime()).isNotNull()
        verify(sntpClient, never()).requestTime(any(), any())
    }
}