package io.ktor.network.util

import java.net.*

actual typealias NetworkAddress = SocketAddress

actual fun NetworkAddress(hostname: String, port: Int): NetworkAddress = InetSocketAddress(hostname, port)
