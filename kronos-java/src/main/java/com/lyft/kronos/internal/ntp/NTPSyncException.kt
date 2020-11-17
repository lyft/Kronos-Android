package com.lyft.kronos.internal.ntp

import java.lang.RuntimeException

class NTPSyncException(message: String) : RuntimeException(message)