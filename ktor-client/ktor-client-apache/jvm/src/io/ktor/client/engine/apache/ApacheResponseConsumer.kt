/*
 * Copyright 2014-2019 JetBrains s.r.o and contributors. Use of this source code is governed by the Apache 2.0 license.
 */

package io.ktor.client.engine.apache

import kotlinx.coroutines.*
import kotlinx.coroutines.io.*
import org.apache.http.*
import org.apache.http.nio.*
import org.apache.http.nio.protocol.*
import org.apache.http.protocol.*
import java.lang.Exception
import kotlin.coroutines.*

internal class ApacheResponseConsumer(
    override val coroutineContext: CoroutineContext,
    private val block: (HttpResponse, ByteReadChannel) -> Unit
) : CoroutineScope, HttpAsyncResponseConsumer<Unit> {
    private val contentReader = ContentReader()

    override fun isDone(): Boolean = !isActive

    override fun responseReceived(response: HttpResponse) {
        block(response, contentReader.content)
    }

    override fun consumeContent(decoder: ContentDecoder, ioctrl: IOControl) {
        contentReader.consume(decoder, ioctrl)
    }

    override fun failed(cause: Exception) {
        cancel(CancellationException("Fail to execute request", cause))
        contentReader.close(cause)
    }

    override fun cancel(): Boolean {
        coroutineContext.cancel()
        contentReader.close(CancellationException("Request canceled", null))
        return true
    }

    override fun getResult() = Unit

    override fun responseCompleted(context: HttpContext?) {
        contentReader.close()
    }

    override fun getException(): Exception? = null

    override fun close(): Unit = Unit
}
