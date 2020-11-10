package com.lyft.kronos.demo

import android.content.Context
import android.os.SystemClock
import android.util.AttributeSet
import android.widget.TextView
import com.lyft.kronos.Clock
import com.lyft.kronos.AndroidClockFactory
import java.text.SimpleDateFormat
import java.util.*
import java.util.concurrent.TimeUnit

class TextClock : TextView {

    var clock : Clock = AndroidClockFactory.createDeviceClock()

    private val ticker = object : Runnable {
        override fun run() {
            text = SimpleDateFormat.getTimeInstance().format(Date(clock.getCurrentTimeMs()))
            handler.postDelayed(this, ONE_SECOND)
        }
    }

    constructor(context: Context?) : super(context)
    constructor(context: Context?, attrs: AttributeSet?) : super(context, attrs)
    constructor(context: Context?, attrs: AttributeSet?, defStyleAttr: Int) : super(context, attrs, defStyleAttr)

    override fun onAttachedToWindow() {
        super.onAttachedToWindow()

        val delay = ONE_SECOND - SystemClock.uptimeMillis() % ONE_SECOND //update the text at the next change of seconds i.e xx:xx:xx:000
        handler.postDelayed(ticker, delay)
    }

    override fun onDetachedFromWindow() {
        super.onDetachedFromWindow()
        handler.removeCallbacks(ticker)
    }

    companion object {
        val ONE_SECOND: Long = TimeUnit.SECONDS.toMillis(1)
    }
}
