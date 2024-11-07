package com.idunnololz.summitForLemmy.server.utils

import kotlinx.coroutines.delay
import kotlinx.io.IOException
import java.security.MessageDigest

@OptIn(ExperimentalStdlibApi::class)
fun String.sha256HashAsHexString(): String {
    val md = MessageDigest.getInstance("SHA-256")
    val digest = md.digest(this.toByteArray())
    return digest.toHexString()
}

suspend fun <T> retryIo(
    times: Int = Int.MAX_VALUE,
    initialDelay: Long = 100, // 0.1 second
    maxDelay: Long = 1000,    // 1 second
    factor: Double = 2.0,
    block: suspend () -> T): T
{
    var currentDelay = initialDelay
    repeat(times - 1) {
        try {
            return block()
        } catch (e: IOException) {
            // you can log an error here and/or make a more finer-grained
            // analysis of the cause to see if retry is needed
        }
        delay(currentDelay)
        currentDelay = (currentDelay * factor).toLong().coerceAtMost(maxDelay)
    }
    return block() // last attempt
}