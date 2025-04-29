package com.idunnololz.summitForLemmy.server.presets.objects

import kotlinx.serialization.Serializable

@Serializable
data class PresetDto(
  val id: String? = null,
  val presetName: String,
  val presetDescription: String,
  val presetData: String,
  val createTs: Long,
  val updateTs: Long,
  val isApproved: Boolean? = null,
  val hasPreviews: Boolean? = null,
)
