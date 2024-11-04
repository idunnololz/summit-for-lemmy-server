package com.idunnololz.summitForLemmy.server.localStorage

import java.io.File
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class LocalStorageManager @Inject constructor() {
    val dataDir = File("data")
}