package com.lyft.kronos.ntp

internal data class NtpPacket(

    /**
     * A warning of an impending leap second to be inserted / deleted
     * in the last minute of the current day.
     */
    val warningLeapSecond: Byte,

    val protocolVersion: Byte,

    val protocolMode: Byte,

    /**
     * The stratum level of the local clock.
     */
    val stratum: Byte,

    /**
     * Used as a power of two, where the resulting value is
     * the maximum interval between successive messages in seconds.
     * Values range from 4 (16 seconds) to 17 (131072 seconds: about 36 hours).
     */
    val maximumPollIntervalPowerOfTwo: Byte,

    /**
     * Used as an exponent of two, where the resulting value is
     * the precision of the system clock in seconds.
     * Values range from -6 for mains-frequency clocks to -20 for microsecond clocks found in some workstations.
     */
    val precisionPowerOfTwo: Byte,

    /**
     * The number indicating the total roundtrip delay to the primary reference source.
     * Note that this variable can take on both positive and negative values, depending on
     * the relative time and frequency offsets.
     * Values range from negative values of a few milliseconds to positive values of several hundred milliseconds.
     */
    val rootDelaySeconds: Double,

    /**
     * The number indicating the maximum error due to the clock frequency tolerance.
     * Values range from zero to several hundred milliseconds.
     */
    val rootDispersionSeconds: Double,

    /**
     * The time the system clock was last set or corrected.
     */
    val referenceTimeSecondsSince1900: Double,

    /**
     * The time at which the request departed the client for the server.
     */
    val originateTimeSecondsSince1900: Double,

    /**
     * The time at which the request arrived at the server or the reply arrived at the client.
     */
    val receiveTimeSecondsSince1900: Double,

    /**
     * The time at which the request departed the client or the reply departed the server.
     */
    val transmitTimeSecondsSince1900: Double

) {

    companion object {
        const val SIZE_BYTES = 48
    }

    enum class WarningLeapSecond(val value: Byte) {
        NoWarning(0),
        WarningLastMinuteHas61Seconds(1),
        WarningLastMinuteHas59Seconds(2),
        AlertClockUnsynchronized(3),
    }

    enum class ProtocolMode(val value: Byte) {
        Reserved(0),
        SymmetricActive(1),
        SymmetricPassive(2),
        Client(3),
        Server(4),
        Broadcast(5),
        ReservedNtp(6),
        ReservedPrivate(7),
    }

    enum class Stratum(val valueFrom: Byte, val valueTill: Byte) {
        KissOfDeath(0, 0),
        PrimaryReference(1, 1),
        SecondaryReference(2, 25),
        Reserved(16, 127),
    }

}
