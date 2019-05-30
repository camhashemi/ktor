/*
 * Copyright 2014-2019 JetBrains s.r.o and contributors. Use of this source code is governed by the Apache 2.0 license.
 */

package io.ktor.network.selector

import io.ktor.util.*
import kotlinx.io.core.*
import java.nio.channels.spi.*

/**
 * Selector manager is a service that manages NIO selectors and selection threads
 */
interface JvmSelectorManager : SelectorManager {
    /**
     * NIO selector provider
     */
    @KtorExperimentalAPI
    val provider: SelectorProvider
}

/**
 * Creates a NIO entity via [create] and calls [setup] on it. If any exception happens then the entity will be closed
 * and an exception will be propagated.
 */
inline fun <C : Closeable, R> JvmSelectorManager.buildOrClose(
    create: SelectorProvider.() -> C,
    setup: C.() -> R
): R {
    while (true) {
        val result = create(provider)

        try {
            return setup(result)
        } catch (cause: Throwable) {
            result.close()
            throw cause
        }
    }
}
