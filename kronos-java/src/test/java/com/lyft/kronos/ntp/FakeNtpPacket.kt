package com.lyft.kronos.ntp

internal val fakeNtpPacket = NtpPacket(
    warningLeapSecond = 0,
    protocolVersion = 0,
    protocolMode = 0,
    stratum = 0,
    maximumPollIntervalPowerOfTwo = 0,
    precisionPowerOfTwo = 0,
    rootDelaySeconds = 0.0,
    rootDispersionSeconds = 0.0,
    referenceTimeSecondsSince1900 = 0.0,
    originateTimeSecondsSince1900 = 0.0,
    receiveTimeSecondsSince1900 = 0.0,
    transmitTimeSecondsSince1900 = 0.0,
)
