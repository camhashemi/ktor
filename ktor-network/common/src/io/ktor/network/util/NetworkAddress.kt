package io.ktor.network.util

expect fun NetworkAddress(hostname: String, port: Int): NetworkAddress

expect abstract class NetworkAddress

val NetworkAddress.hostname: String get() = TODO()
