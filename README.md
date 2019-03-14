# Kronos-Android
Synchronized Time Android Library

Kronos takes care of synchronizing and caching time with an [NTP](http://www.ntp.org) server(s). Once synchronized, your application can trust that it has the correct time even if the user manually updates their date and time settings.

Do you have bug reports from your customers saying your estimated time of arrival is wrong? Do you have customers who try to trick your timing logic by changing their date and time settings? If so, Kronos for Android is here to help! Kronos is crushing time sensitive bugs here at Lyft and it has been serving millions of drivers and passengers with accurate time information. 

Introduction
------------

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


That's it!

Customization
-------------

Kronos comes with a set of reasonable default configurations. You can customize the configuration by using AndroidClockFactory.createKronosClock with the following optional parameters:

* syncListener 
    * Allows you to log sync operation successes and errors, which maybe useful for custom analytics.
* ntpHosts
    * Specify a list of NTP servers to sync with. 
* requestTimeoutMs
    * Lengthen or shorten the timeout value. If the NTP server fails to respond within the given time, the next server will be contacted. If none of the server respond within the given time, the sync operation will be considered a failure.
* minWaitTimeBetweenSyncMs
    * Kronos attempts a synchronization at most once a minute. If you want to change the frequency, supply the desired time in milliseconds. Note that you should also supply a cacheExpirationMs value. For example, if you shorten the minWaitTimeBetweenSyncMs to 30 seconds, but leave the cacheExpirationMs to 1 minute, it will have no affect because the cache is still valid within the 1 minute window. 
* cacheExpirationMs
    * Kronos will perform a background sync if the cache is stale. The cache is valid for 1 minute by default. It is simpliest to keep the cacheExpirationMs value the same as minWaitTimeBetweenSyncMs value.
                     

Download
--------
Include the following in your build.gradle file:
```groovy
implementation "com.lyft.kronos:kronos-android:$latest_version"
```

Kronos is also available as a pure Java library
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
    
