/*
 * Copyright 2014-2019 JetBrains s.r.o and contributors. Use of this source code is governed by the Apache 2.0 license.
 */

package io.ktor.client.engine.okhttp

import io.ktor.client.request.*
import io.ktor.client.tests.utils.*
import kotlinx.coroutines.*
import kotlinx.coroutines.debug.*
import okhttp3.*
import kotlin.test.*

class RequestTests {

    class LoggingInterceptor : Interceptor {
        override fun intercept(chain: Interceptor.Chain): Response {
            val request = chain.request()
            val response = chain.proceed(request)
            return response
        }
    }

    @Test
    fun testFeatures() = clientTest(OkHttp) {
        config {
            engine {
                addInterceptor(LoggingInterceptor())
                addNetworkInterceptor(LoggingInterceptor())
            }
        }

        test { client ->
            client.get<String>("https://google.com")
        }
    }

    @Test
    fun testWithCancel() = clientTest(OkHttp) {
        test { client ->
            GlobalScope.launch {
                while (true) {
                    delay(1000)
                    DebugProbes.dumpCoroutines()
                }
            }
            repeat (1000) {
                println(it)
                withTimeoutOrNull(100) {
                    client.get<ByteArray>("$TEST_SERVER/content/stream")
                }
            }
        }
    }

}
