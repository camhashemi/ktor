/*
 * Copyright 2014-2019 JetBrains s.r.o and contributors. Use of this source code is governed by the Apache 2.0 license.
 */

package io.ktor.client.engine

import kotlinx.coroutines.*
import java.util.concurrent.*
import kotlin.coroutines.*

/**
 * Base jvm implementation for [HttpClientEngine]
 */
@Suppress("KDocMissingDocumentation")
abstract class HttpClientJvmEngine(engineName: String) : HttpClientEngine {
    private val clientContext = SupervisorJob()
    private val _dispatcher by lazy {
        Executors.newFixedThreadPool(config.threadsCount) {
            Thread(it).apply {
                isDaemon = true
            }
        }.asCoroutineDispatcher()
    }

    @UseExperimental(InternalCoroutinesApi::class)
    override val dispatcher: CoroutineDispatcher
        get() = _dispatcher

    @UseExperimental(InternalCoroutinesApi::class)
    override val coroutineContext: CoroutineContext by lazy {
        _dispatcher + clientContext + CoroutineName("$engineName-context")
    }

    /**
     * Create [CoroutineContext] to execute call.
     */
    protected suspend fun createCallContext(): CoroutineContext {
        val result = coroutineContext + Job(clientContext)

        currentContext()[Job]!!.invokeOnCompletion {  cause ->
            if (cause != null) result.cancel()
        }

        return result
    }

    override fun close() {
        clientContext.complete()

        clientContext.invokeOnCompletion {
            _dispatcher.close()
        }
    }
}

private suspend inline fun currentContext() = coroutineContext
