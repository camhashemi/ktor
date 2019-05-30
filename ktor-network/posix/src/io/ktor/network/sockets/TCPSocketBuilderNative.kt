/*
 * Copyright 2014-2019 JetBrains s.r.o and contributors. Use of this source code is governed by the Apache 2.0 license.
 */

package io.ktor.network.sockets

import io.ktor.network.selector.*
import io.ktor.network.util.*
import kotlinx.cinterop.*
import platform.posix.*
import kotlin.coroutines.*

private val DEFAULT_BACKLOG_SIZE = 50

internal actual suspend fun TCPSocketBuilder.Companion.connect(
    selector: SelectorManager,
    networkAddress: NetworkAddress,
    socketOptions: SocketOptions.TCPClientSocketOptions
): Socket = memScoped {
    networkAddress.loop { info ->
        val descriptor = socket(info.ai_family, info.ai_socktype, info.ai_protocol)
            .check()

        connect(descriptor, info.ai_addr, info.ai_addrlen)
            .check()

        fcntl(descriptor, F_SETFL, O_NONBLOCK)
            .check()

        return TCPSocketNative(
            descriptor, selector,
            ConnectedAddress(info), ConnectedAddress(null),
            coroutineContext
        )
    }

    error("Fail to connect.")
}

internal actual fun TCPSocketBuilder.Companion.bind(
    selector: SelectorManager,
    localAddress: NetworkAddress?,
    socketOptions: SocketOptions.AcceptorOptions
): ServerSocket = memScoped {
    check(localAddress is ResolvedAddress)

    localAddress.loop { info ->
        val descriptor = socket(info.ai_family, info.ai_socktype, info.ai_protocol)
            .check()

        fcntl(descriptor, F_SETFL, O_NONBLOCK)
            .check { it == 0 }

        bind(descriptor, info.ai_addr, info.ai_addrlen)
            .check()

        listen(descriptor, DEFAULT_BACKLOG_SIZE)
            .check() /* TODO: introduce backlog option */

        return TCPServerSocketNative(descriptor, selector, ConnectedAddress(info), selector.coroutineContext)
    }

    error("Fail to bind.")
}

internal inline fun Int.check(
    message: String = "Native method failed with $this.",
    block: (Int) -> Boolean = { it >= 0 }
): Int {
    if (!block(this)) error(message)
    return this
}
