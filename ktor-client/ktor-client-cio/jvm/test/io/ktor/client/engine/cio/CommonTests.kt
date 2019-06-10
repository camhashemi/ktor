/*
 * Copyright 2014-2019 JetBrains s.r.o and contributors. Use of this source code is governed by the Apache 2.0 license.
 */

package io.ktor.client.engine.cio

import io.ktor.client.tests.*


class CIOCookiesTest : CookiesTest(CIO)

class CIOPostTest : PostTest(CIO)

class CIOFullFormTest : FullFormTest(CIO)

class CIOMultithreadedTest : MultithreadedTest(CIO)

class CIORedirectTest : HttpRedirectTest(CIO)

class CIOBuildersTest : BuildersTest(CIO)

class CIOFeaturesTest : FeaturesTest(CIO)

class CIOConnectionTest : ConnectionTest(CIO)

class CIOHttpClientTest : HttpClientTest(CIO)
