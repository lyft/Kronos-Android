package com.lyft.kronos.internal

import com.lyft.kronos.Clock
import com.lyft.kronos.KronosClock
import com.lyft.kronos.KronosTime
import com.lyft.kronos.internal.ntp.SntpService
import com.nhaarman.mockito_kotlin.mock
import com.nhaarman.mockito_kotlin.whenever
import org.assertj.core.api.Assertions.assertThat
import org.junit.Test
import org.mockito.Mockito.never
import org.mockito.Mockito.verify

class KronosClockTest {

    private val deviceClock = mock<Clock>()

    private val ntpService = mock<SntpService>()

    private val kronosClock: KronosClock

    init {
        kronosClock = KronosClockImpl(ntpService, deviceClock)
    }

    @Test
    fun currentTimeUsesNtpTimeWhenAvailable() {
        whenever(ntpService.currentTime()).thenReturn(KronosTime(5678L, 0L))
        whenever(deviceClock.getCurrentTimeMs()).thenReturn(1234L)

        assertThat(kronosClock.getCurrentTimeMs()).isEqualTo(5678L)
        verify(ntpService).currentTime()
        verify(deviceClock, never()).getCurrentTimeMs()
    }

    @Test
    fun currentTimeDelegatesWhenNtpTimeNotAvailable() {
        whenever(ntpService.currentTime()).thenReturn(null)
        whenever(deviceClock.getCurrentTimeMs()).thenReturn(1234L)

        assertThat(kronosClock.getCurrentTime().posixTimeMs).isEqualTo(1234L)
        verify(deviceClock).getCurrentTimeMs()
        verify(ntpService).currentTime()
    }

    @Test
    fun elapsedShouldAlwaysDelegate() {
        whenever(deviceClock.getElapsedTimeMs()).thenReturn(1234L)

        assertThat(kronosClock.getElapsedTimeMs()).isEqualTo(1234L)
        verify(deviceClock).getElapsedTimeMs()
    }
}
