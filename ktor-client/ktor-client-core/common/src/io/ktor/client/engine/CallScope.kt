/*
 * Copyright 2014-2019 JetBrains s.r.o and contributors. Use of this source code is governed by the Apache 2.0 license.
 */

package io.ktor.client.engine

import kotlinx.coroutines.*
import kotlinx.io.core.*
import kotlin.coroutines.*

/**
 * Base jvm implementation for [HttpClientEngine]
 */
@Suppress("KDocMissingDocumentation")
abstract class CallScope(name: String) : CoroutineScope, Closeable {
    private val clientContext = SupervisorJob()

    abstract val dispatcher: CoroutineDispatcher

    override val coroutineContext: CoroutineContext by lazy {
        dispatcher + clientContext + CoroutineName("$name-context")
    }

    /**
     * Create [CoroutineContext] to execute call.
     */
    protected fun newCall(): CoroutineContext = coroutineContext + Job(clientContext)

    override fun close() {
        clientContext.complete()

        clientContext.invokeOnCompletion {
            val current = dispatcher
            if (current is Closeable) {
                current.close()
            }
        }
    }
}
