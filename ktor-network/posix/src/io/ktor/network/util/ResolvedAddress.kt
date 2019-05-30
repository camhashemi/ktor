/*
 * Copyright 2014-2019 JetBrains s.r.o and contributors. Use of this source code is governed by the Apache 2.0 license.
 */

package io.ktor.network.util

import kotlinx.cinterop.*
import kotlinx.io.core.*
import platform.posix.*

internal class ResolvedAddress(
    private val hostname: String?, port: Int
) : NetworkAddress(), Closeable {
    private lateinit var _info: CPointer<addrinfo>

    override val info: addrinfo
        get() = _info.pointed

    init {
        val hints: CValue<addrinfo> = cValue {
            ai_family = AF_UNSPEC
            ai_socktype = SOCK_STREAM
            ai_flags = AI_PASSIVE
            ai_protocol = 0
        }

        val portInfo = port.toString()

        memScoped {
            val result = alloc<CPointerVar<addrinfo>>()

            val code = getaddrinfo(hostname, portInfo, hints, result.ptr)
            when (code) {
                0 -> {}
                EAI_NONAME -> error("Bad hostname: $hostname")
//                EAI_ADDRFAMILY -> error("Bad address family")
                EAI_AGAIN -> error("Try again")
                EAI_BADFLAGS -> error("Bad flags")
//                EAI_BADHINTS -> error("Bad hint")
                EAI_FAIL -> error("FAIL")
                EAI_FAMILY -> error("Bad family")
//                EAI_MAX -> error("max reached")
                EAI_MEMORY -> error("OOM")
//                EAI_NODATA -> error("NO DATA")
                EAI_OVERFLOW -> error("OVERFLOW")
//                EAI_PROTOCOL -> error("PROTOCOL ERROR")
                EAI_SERVICE -> error("SERVICE ERROR")
                EAI_SOCKTYPE -> error("SOCKET TYPE ERROR")
                EAI_SYSTEM -> error("SYSTEM ERROR")
                else -> error("Unknown error: $code")
            }

            _info = result.value!!
        }
    }

    override fun close() {
        freeaddrinfo(_info)
    }
}
