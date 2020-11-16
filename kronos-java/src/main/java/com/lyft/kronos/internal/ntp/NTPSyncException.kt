package com.lyft.kronos.internal.ntp

import java.lang.RuntimeException

open class NTPSyncException(message: String) : RuntimeException(message)