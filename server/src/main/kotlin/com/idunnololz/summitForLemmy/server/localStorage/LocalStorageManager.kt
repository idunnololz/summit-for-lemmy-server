package com.idunnololz.summitForLemmy.server.localStorage

import java.io.File
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class LocalStorageManager
@Inject
constructor() {
  val dataDir = File("data")
  val publicDir = File(dataDir, "public")
  val authPublicDir = File(dataDir, "auth_public")
  val presetScreenshotUploadsDir = File(authPublicDir, "presets")
  val presetScreenshotPublicDir = File(publicDir, "presets")

  fun mkdirs() {
    dataDir.mkdirs()
    presetScreenshotUploadsDir.mkdirs()
    presetScreenshotPublicDir.mkdirs()
    publicDir.mkdirs()
    authPublicDir.mkdirs()
  }
}
