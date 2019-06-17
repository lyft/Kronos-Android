# Kronos-Android
Synchronized Time Android Library

Kronos is an open source Network Time Protocol (NTP) synchronization library for providing a trusted clock on the JVM.

Unlike the device clock, the time reported by Kronos is unaffected when the local time is changed while your app is running. Instead, Kronos stores _accurate time_ along with a delta between the NTP time and the system uptime. Since uptime increases monotonically, Kronos isn't affected by device time changes.
Accessing `KronosClock.getCurrentTimeMs()` will return the local time based on the last known _accurate time + delta since last sync_.

Introduction
------------

Include the following in your build.gradle file:
```groovy
implementation "com.lyft.kronos:kronos-android:$latest_version"
```

Obtain a Kronos clock that is synchronized with NTP servers.

```kotlin
class YourApplication : Application() {
    
    lateinit var kronosClock: KronosClock
    
    override fun onCreate() {
        super.onCreate()
        
        kronosClock = AndroidClockFactory.createKronosClock(applicationContext)
        kronosClock.syncInBackground()
    }
}
```

Replace usages of 


```java
System.currentTimeMillis()
```

with


```java
kronosClock.getCurrentTimeMs()
```

If the NTP server cannot be reached or Kronos has not yet been synced, `getCurrentTimeMs()` will return time from the fallback clock and trigger `syncInBackground()`. If you'd rather control the fallback, you can use `getCurrentNtpTimeMs()`, which returns `null` instead of falling back. 
To get metadata with an individual timestamp, use `KronosClock.getCurrentTime()`, which returns an instance of `KronosTime`. `KronosTime` contains the `currentTime` and the `timeSinceLastNtpSyncMs`, which will be `null` if `currentTime` is coming from the device clock.

Since it relies on system uptime, Kronos detects and requires a new sync after each reboot. 

Customization
-------------

Kronos comes with a set of reasonable default configurations. You can customize the configuration by using AndroidClockFactory.createKronosClock with the following optional parameters:

* syncListener 
    * Allows you to log sync operation successes and errors, which maybe useful for custom analytics. Pass an implementation of `SyncListener`.
* ntpHosts
    * Specify a list of NTP servers with which to sync.
* requestTimeoutMs
    * Lengthen or shorten the timeout value. If the NTP server fails to respond within the given time, the next server will be contacted. If none of the server respond within the given time, the sync operation will be considered a failure.
* minWaitTimeBetweenSyncMs
    * Kronos attempts a synchronization at most once a minute. If you want to change the frequency, supply the desired interval in milliseconds. Note that you should also supply a cacheExpirationMs value. For example, if you shorten the minWaitTimeBetweenSyncMs to 30 seconds, but leave the cacheExpirationMs to 1 minute, it will have no affect because the cache is still valid within the 1 minute window.
* cacheExpirationMs
    * Kronos will perform a background sync if the cache is stale. The cache is valid for 1 minute by default. It is simpliest to keep the cacheExpirationMs value the same as minWaitTimeBetweenSyncMs value.
                     

With or without Android
--------
For usage with non-Android modules, Kronos provides access to the Kotlin-only base library called Kronos-Java, which depends on an externally provided local clock and a cache. The Android library simply abstracts away the creation of the clock and cache by extracting the Android system clock from a provided Context and creating its own cache using SharedPreferences.

To use Kronos-Java include the following in your build.gradle file:

```groovy
implementation "com.lyft.kronos:kronos-java:$latest_version"
```


Version infromation are listed under [releases](https://github.com/lyft/Kronos-Android/releases)

Looking for Kronos for your iOS application? Check out [Kronos for iOS](https://github.com/lyft/Kronos)


License
-------

    Copyright (C) 2018 Lyft Inc.
    
    Licensed under the Apache License, Version 2.0 (the "License");
    you may not use this file except in compliance with the License.
    You may obtain a copy of the License at
    
        http://www.apache.org/licenses/LICENSE-2.0

    Unless required by applicable law or agreed to in writing, software
    distributed under the License is distributed on an "AS IS" BASIS,
    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
    See the License for the specific language governing permissions and
    limitations under the License.
    
