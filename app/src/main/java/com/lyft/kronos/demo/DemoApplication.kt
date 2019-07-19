package com.lyft.kronos.demo

import android.app.Application
import android.util.Log
import com.lyft.kronos.KronosClock
import com.lyft.kronos.SyncListener
import com.lyft.kronos.AndroidClockFactory

class DemoApplication : Application() {

    lateinit var kronosClock: KronosClock

    override fun onCreate() {
        super.onCreate()

        val syncListener: SyncListener = object : SyncListener {
            override fun onStartSync(host: String) {
                Log.d(TAG, "Clock sync started ($host)")
            }

            override fun onSuccess(ticksDelta: Int, responseTimeMs: Long) {
                Log.d(TAG, "Clock sync succeed. Time delta: $ticksDelta. Response time: $responseTimeMs")
            }

            override fun onError(host: String, throwable: Throwable) {
                Log.e(TAG, "Clock sync failed ($host)", throwable)
            }
        }

        kronosClock = AndroidClockFactory.createKronosClock(this, syncListener)
        kronosClock.syncInBackground()
    }

    companion object {
        const val TAG = "DemoApplication"
    }
}