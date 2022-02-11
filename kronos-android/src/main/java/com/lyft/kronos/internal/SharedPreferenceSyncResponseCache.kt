package com.lyft.kronos.internal

import android.content.SharedPreferences
import com.lyft.kronos.SyncResponseCache
import com.lyft.kronos.internal.Constants.TIME_UNAVAILABLE

internal class SharedPreferenceSyncResponseCache(private val sharedPreferences: SharedPreferences) : SyncResponseCache {
    override var currentTime: Long
        get() = sharedPreferences.getLong(KEY_CURRENT_TIME, TIME_UNAVAILABLE)
        set(value) = sharedPreferences.edit().putLong(KEY_CURRENT_TIME, value).apply()
    override var elapsedTime: Long
        get() = sharedPreferences.getLong(KEY_ELAPSED_TIME, TIME_UNAVAILABLE)
        set(value) = sharedPreferences.edit().putLong(KEY_ELAPSED_TIME, value).apply()
    override var bootCount: Int?
        get() =
            if (sharedPreferences.contains(KEY_BOOT_COUNT))
                sharedPreferences.getInt(KEY_BOOT_COUNT, 0)
            else null
        set(value) =
            if (value == null)
                sharedPreferences.edit().remove(KEY_BOOT_COUNT).apply()
            else
                sharedPreferences.edit().putInt(KEY_BOOT_COUNT, value).apply()
    override var currentOffset: Long
        get() = sharedPreferences.getLong(KEY_OFFSET, TIME_UNAVAILABLE)
        set(value) = sharedPreferences.edit().putLong(KEY_OFFSET, value).apply()

    override fun clear() {
        sharedPreferences.edit().clear().apply()
    }

    companion object {
        internal const val SHARED_PREFERENCES_NAME = "com.lyft.kronos.shared_preferences"
        internal const val KEY_CURRENT_TIME = "com.lyft.kronos.cached_current_time"
        internal const val KEY_ELAPSED_TIME = "com.lyft.kronos.cached_elapsed_time"
        internal const val KEY_BOOT_COUNT = "com.lyft.kronos.cached_boot_count"
        internal const val KEY_OFFSET = "com.lyft.kronos.cached_offset"
    }
}


