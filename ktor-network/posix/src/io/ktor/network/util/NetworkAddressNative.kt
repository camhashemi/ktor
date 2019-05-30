/*
 * Copyright 2014-2019 JetBrains s.r.o and contributors. Use of this source code is governed by the Apache 2.0 license.
 */

package io.ktor.network.util

import kotlinx.cinterop.*
import platform.posix.*

actual fun NetworkAddress(hostname: String, port: Int): NetworkAddress = ResolvedAddress(hostname, port)

actual abstract class NetworkAddress {
    abstract val info: addrinfo?
}

/**
 * TODO: copy
 */
class ConnectedAddress(override val info: addrinfo?) : NetworkAddress()

internal inline fun NetworkAddress.loop(block: (addrinfo) -> Unit) {
    var current = info ?: return
    while (true) {
        block(current)
        current = current.next() ?: break
    }
}

internal inline fun addrinfo.next(): addrinfo? = ai_next?.pointed

