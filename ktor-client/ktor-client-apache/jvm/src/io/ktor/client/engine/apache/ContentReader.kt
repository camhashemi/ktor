/*
 * Copyright 2014-2019 JetBrains s.r.o and contributors. Use of this source code is governed by the Apache 2.0 license.
 */

package io.ktor.client.engine.apache

import kotlinx.coroutines.*
import kotlinx.coroutines.io.*
import org.apache.http.nio.*
import kotlin.coroutines.*
import kotlin.coroutines.intrinsics.*

internal class ContentReader(
) : CoroutineDispatcher(), Continuation<Unit> {
    val content: ByteReadChannel get() = handler.channel

    private val state: CompletableJob = Job()

    private lateinit var currentDecoder: ContentDecoder
    private var nextEvent: Runnable? = null

    override val context: CoroutineContext = this + state

    private val handler = GlobalScope.writer(context) {
        try {
            yield()

            while (state.isActive) {
                if (currentDecoder.isCompleted) {
                    yield()
                }

                channel.write {
                    if (!state.isActive) return@write
                    currentDecoder.read(it)
                }
            }

        } catch (cause: Throwable) {
            channel.close(cause)
        } finally {
            channel.close()
        }
    }

//    init {
//        nextEvent = Runnable { handler.startCoroutineUninterceptedOrReturn(this) }
//    }

    fun consume(decoder: ContentDecoder, ioctrl: IOControl) {
        if (state.isCompleted) return
        currentDecoder = decoder

        if (!runNextEvent()) ioctrl.shutdown()
    }

    /**
     * [Continuation]
     */
    override fun resumeWith(result: Result<Unit>) {}

    /**
     * [CoroutineDispatcher]
     */
    override fun isDispatchNeeded(context: CoroutineContext): Boolean = nextEvent == null

    override fun dispatch(context: CoroutineContext, block: Runnable) {
        nextEvent = block
    }

    fun close(cause: Throwable? = null) {
        if (state.isCompleted) return
        if (cause == null) {
            state.complete()
        } else {
            state.completeExceptionally(cause)
        }

        runNextEvent()
        check(nextEvent == null) { "Internal ApacheEngine error. Dispatch event after close." }
    }

    private inline fun runNextEvent(): Boolean {
        try {
            val event = nextEvent ?: error("Internal ApacheEngine error. No task to resume.")
            nextEvent = null
            event.run()
        } catch (_: Throwable) {
            return false
        }

        return true
    }

}
