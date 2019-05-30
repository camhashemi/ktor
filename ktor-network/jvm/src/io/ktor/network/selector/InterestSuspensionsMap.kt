/*
 * Copyright 2014-2019 JetBrains s.r.o and contributors. Use of this source code is governed by the Apache 2.0 license.
 */

package io.ktor.network.selector

import io.ktor.util.*
import kotlinx.atomicfu.*
import kotlinx.coroutines.*

@Suppress("KDocMissingDocumentation")
@InternalAPI
class InterestSuspensionsMap {
    private val interestHandlers: AtomicArray<CancellableContinuation<Unit>?> = atomicArrayOfNulls(SelectInterest.size)


    fun addSuspension(interest: SelectInterest, continuation: CancellableContinuation<Unit>) {
        val index = interest.ordinal
        val result = interestHandlers[index].compareAndSet(null, continuation)
        if (!result) {
            throw IllegalStateException("Handler for ${interest.name} is already registered")
        }
    }

    @Suppress("LoopToCallChain")
    inline fun invokeForEachPresent(readyOps: Int, block: CancellableContinuation<Unit>.() -> Unit) {
        val flags = SelectInterest.flags

        for (ordinal in 0 until flags.size) {
            if (flags[ordinal] and readyOps != 0) {
                removeSuspension(ordinal)?.block()
            }
        }
    }

    inline fun invokeForEachPresent(block: CancellableContinuation<Unit>.(SelectInterest) -> Unit) {
        for (interest in SelectInterest.AllInterests) {
            removeSuspension(interest)?.run { block(interest) }
        }
    }

    fun removeSuspension(interest: SelectInterest): CancellableContinuation<Unit>? =
        interestHandlers[interest.ordinal].getAndSet(null)

    fun removeSuspension(interestOrdinal: Int): CancellableContinuation<Unit>? {
        return interestHandlers[interestOrdinal].getAndSet(null)
    }

    override fun toString(): String =
        "R ${reference(SelectInterest.READ)} W ${reference(SelectInterest.WRITE)} C ${reference(SelectInterest.CONNECT)} A ${reference(
            SelectInterest.ACCEPT
        )}"

    private inline fun reference(interest: SelectInterest): CancellableContinuation<Unit>? =
        interestHandlers[interest.ordinal].value

    companion object
}
