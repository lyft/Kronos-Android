package com.lyft.kronos.demo

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.Bundle
import android.provider.Settings
import android.support.v7.app.AppCompatActivity
import android.support.v7.widget.AppCompatImageButton
import android.util.Log
import com.lyft.kronos.AndroidClockFactory

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        bindSettingsButton()
        bindDeviceClock()
        bindKronosClock()
    }

    private fun bindSettingsButton() {
        findViewById<AppCompatImageButton>(R.id.settings_button).setOnClickListener({
            startActivity(Intent(Settings.ACTION_DATE_SETTINGS))
        })
    }

    private fun bindDeviceClock() {
        findViewById<TextClock>(R.id.android_clock).clock = AndroidClockFactory.createDeviceClock()
    }

    private fun bindKronosClock() {
        val app = applicationContext as DemoApplication
        findViewById<TextClock>(R.id.kronos_clock).clock = app.kronosClock
    }

    override fun onAttachedToWindow() {
        super.onAttachedToWindow()
        registerReceiver()
    }

    override fun onDetachedFromWindow() {
        super.onDetachedFromWindow()
        unregisterReceiver()
    }

    private val timeSettingsChangedReciever = object : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            Log.d(TAG, "Date and time settings changed")
            val app = applicationContext as DemoApplication
            app.kronosClock.syncInBackground()
        }
    }

    private fun registerReceiver() {
        val filter = IntentFilter()
        filter.addAction(Intent.ACTION_DATE_CHANGED)
        filter.addAction(Intent.ACTION_TIME_CHANGED)
        filter.addAction(Intent.ACTION_TIMEZONE_CHANGED)

        applicationContext.registerReceiver(timeSettingsChangedReciever, filter)

        Log.d(TAG, "Registered to receive date time settings changes")
    }

    private fun unregisterReceiver() {
        applicationContext.unregisterReceiver(timeSettingsChangedReciever)
        Log.d(TAG, "No longer registered to receive date time settings changes")
    }

    companion object {
        const val TAG = "MainActivity"
    }
}
