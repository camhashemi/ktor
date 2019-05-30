/*
 * Copyright 2014-2019 JetBrains s.r.o and contributors. Use of this source code is governed by the Apache 2.0 license.
 */

package io.ktor.network.sockets

import io.ktor.network.selector.*
import io.ktor.network.util.*

internal actual suspend fun TCPSocketBuilder.Companion.connect(
    selector: SelectorManager,
    networkAddress: NetworkAddress,
    socketOptions: SocketOptions.TCPClientSocketOptions
): Socket {
    require(selector is JvmSelectorManager)

    return selector.buildOrClose({ openSocketChannel() }) {
        assignOptions(socketOptions)
        nonBlocking()

        SocketImpl(this, socket()!!, selector).apply {
            connect(remoteAddress)
        }
    }
}

internal actual fun TCPSocketBuilder.Companion.bind(
    selector: SelectorManager,
    localAddress: NetworkAddress?,
    socketOptions: SocketOptions.AcceptorOptions
): ServerSocket {
    require(selector is JvmSelectorManager)

    return selector.buildOrClose({ openServerSocketChannel() }) {
        assignOptions(socketOptions)
        nonBlocking()

        ServerSocketImpl(this, selector).apply {
            channel.socket().bind(localAddress)
        }
    }
}
