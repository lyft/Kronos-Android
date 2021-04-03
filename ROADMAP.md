# Refactoring

Goals:

* Provide explicit external API.
* Rework internal implementation to follow the SNTP RFC as much as possible.
* Cover all internal components with extensive suite of unit tests.
* Eliminate forked Google Java code.
* Leverage Kotlin type system ([errors](https://arturdryomov.dev/posts/designing-errors-with-kotlin/) in particular).

## Internal API

> The common theme of components below is to take over `SntpClient`
> which has a lot of responsibilities. Basically, it does everything
> which limits testing and introduces a bus factor of sorts.
> At the same time, it’s not really our implementation — it’s a fork
> of an internal Google Java component. Yes, it’s not even Kotlin.

### `.ntp.NtpPacket`

Representation of an NTP packet. ATM `SntpClient` uses bytes on its own.

```kotlin
internal data class NtpPacket
```

### `.ntp.NtpPackets`

Converter of bytes to `NtpPacket` instances.
ATM `SntpClient` decodes and encodes bytes on its own.

```kotlin
internal interface NtpPackets {

    fun decode(packetBuffer: ByteBuffer): NtpPacket
    fun encode(packet: NtpPacket): ByteBuffer
}
```

### `.sntp.Dns`

DNS resolution point. ATM there is `DnsResolver`
but it resolves hosts to IPs 1:1 which is not true IRL.

```kotlin
internal interface Dns {

    fun resolve(host: String): Array<InetAddress>
}
```

### `.sntp.Udp`

UDP transferrer. ATM there is `DatagramFactory`
but it does not hide socket-level communication, so `SntpClient` does the transfer.

```kotlin
internal interface Udp {

    enum class Result { Success, Failure }

    fun request(address: SocketAddress, request: ByteBuffer, response: ByteBuffer): Result
}
```

### `.sntp.Sntp`

SNTP transferrer, uses `NtpPackets`, `Dns` and `Udp` to make it happen.
ATM `SntpClient` does all that with little to none backing abstractions.

```kotlin
internal interface Sntp {

    fun request(clientPacket: NtpPacket): NtpPacket?
}
```

### `.sntp.SntpTime`

SNTP time representation based on `NtpPacket` exchange.
ATM there is `SntpClient.Response` with similar fields but it holds `Clock` instance in addition to values.

```kotlin
internal data class SntpTime(
    val timeUnixEpochMillis: Long,
    val systemUptimeMillis: Long,
    val offsetMillis: Long,
)
```

### `.sntp.SntpClock`

SNTP time calculator, uses `Sntp` and `Clock` to figure out the current `SntpTime` value.
ATM — surprise! — `SntpClient` does that.

```kotlin
internal interface SntpClock {

    fun request(): SntpTime?
}
```

## External API

### `.Clock`

**The** API consumers use. This is a departure from current `Clock` and `KronosClock`.

* There is no information about a previous SNTP sync age.
  I believe it should be hidden from consumers as an implementation detail.
  Consumers are interested in accurate time and Kronos can provide the definition
  of accuracy on its own.
* There is no background sync triggers. At the same time, there is a recommended
  sync timeout on successful sync. NTP servers provide polling interval which should
  be used to optimize the network traffic. I believe the sync should be done
  by consumers. For example, Android has short-living processes and it makes sense
  to use something like WorkManager to run periodic sync. At the same time,
  server processes are long-lived and might get away with a thread-based sync.
  We might want to provide recipes for this.

```kotlin
interface Clock {

    sealed class SyncResult {
        data class Success(val nextSyncTimeoutMillis: Long) : SyncResult()
        object Failure : SyncResult()
    }

    fun nowUnixEpochMillis(): Long?

    fun sync(): SyncResult
}
```

### `.SystemClock`

Representation of the OS-backed clock. We might want to provide both JVM and Android implementations.
ATM there is `Clock` with the same signature but for some reason `KronosClock` implements it as well.

```kotlin
interface SystemClock {

    fun nowUnixEpochMillis(): Long
    fun nowUptimeMillis(): Long
}
```

### `.ClockStorage`

Storage for preserving SNTP information between process deaths.
Similar to `SyncResponseCache` but with semi-atomic operations instead of three different calls for read / write.

```kotlin
interface ClockStorage {

    data class Time(
        val unixEpochMillis: Long,
        val uptimeMillis: Long,
        val offsetMillis: Long,
    )

    fun write(time: Time)
    fun read(): Time?
}
```

# Travis → GitHub Actions

Travis was bought out and its future is in flux.
[Some stories](https://www.jeffgeerling.com/blog/2020/travis-cis-new-pricing-plan-threw-wrench-my-open-source-works) are not encouraging.

GitHub Actions integrates with GitHub better and gives us better control over potential billing options.

# Kotlin Multiplatform

Since internal components are abstracted by design (`interface` all over the place)
to place the groundwork for good testing approaches, it might be interesting
to leverage this for the multiplatform.

A lot of code can be reused across platforms.
In fact, almost all of it, except `Dns`, `Udp` and `ClockStorage`.
The current implementation uses `java.nio.ByteBuffer` but it can be replaced
with multiplatform `okio.Buffer`.
