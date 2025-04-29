package com.idunnololz.summitForLemmy.server.presets

import java.io.File

class PresetPreviews(
  val phoneScreenshotFile: File,
  val tabletScreenshotFile: File,
)

fun PresetPreviews.copyTo(other: PresetPreviews) {
  this.phoneScreenshotFile.copyTo(other.phoneScreenshotFile, overwrite = true)
  this.tabletScreenshotFile.copyTo(other.tabletScreenshotFile, overwrite = true)
}