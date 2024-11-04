package com.idunnololz.summitForLemmy.server.utils

import java.security.MessageDigest

@OptIn(ExperimentalStdlibApi::class)
fun String.sha256HashAsHexString(): String {
    val md = MessageDigest.getInstance("SHA-256")
    val digest = md.digest(this.toByteArray())
    return digest.toHexString()
}