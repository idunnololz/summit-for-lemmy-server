package com.idunnololz.summitForLemmy.server.presets

import com.idunnololz.summitForLemmy.server.lemmyStats.db.CommunityStatsTable
import com.idunnololz.summitForLemmy.server.localStorage.LocalStorageManager
import com.idunnololz.summitForLemmy.server.presets.db.PresetEntity
import com.idunnololz.summitForLemmy.server.presets.db.PresetEntityTable
import com.idunnololz.summitForLemmy.server.presets.db.PresetEntityTable.isApproved
import com.idunnololz.summitForLemmy.server.presets.db.PresetEntityTable.presetData
import com.idunnololz.summitForLemmy.server.presets.db.PresetEntityTable.presetDescription
import com.idunnololz.summitForLemmy.server.presets.db.PresetEntityTable.presetName
import com.idunnololz.summitForLemmy.server.presets.db.PresetEntityTable.updateTs
import com.idunnololz.summitForLemmy.server.presets.objects.PresetDto
import com.idunnololz.summitForLemmy.server.utils.suspendTransaction
import io.ktor.http.HttpStatusCode
import io.ktor.http.content.PartData
import io.ktor.http.content.forEachPart
import io.ktor.server.request.receive
import io.ktor.server.request.receiveMultipart
import io.ktor.server.request.receiveParameters
import io.ktor.server.response.respond
import io.ktor.server.routing.RoutingCall
import io.ktor.util.cio.writeChannel
import io.ktor.utils.io.copyAndClose
import kotlinx.serialization.json.Json
import java.util.UUID
import javax.inject.Inject
import java.io.File

class PresetsController
@Inject
constructor(
  private val presetsManager: PresetsManager,
) {

  suspend fun insert(call: RoutingCall) {
    val multipartData = call.receiveMultipart()
    var presetDto: PresetDto? = (multipartData.readPart() as? PartData.FormItem)?.let { part ->
      if (part.name == "preset") {
        Json.decodeFromString(part.value)
      } else {
        null
      }
    }
    var hasPreview: Boolean = false
    val ts = System.currentTimeMillis()

    presetDto = presetDto?.copy(
      id = UUID.randomUUID().toString(),
      createTs = ts,
      updateTs = ts,
      isApproved = false,
      hasPreviews = hasPreview,
    )

    if (presetDto == null) {
      call.respond(HttpStatusCode.BadRequest, "Invalid preset data.")
      return
    }

    if (presetDto.presetName.length > 300) {
      call.respond(HttpStatusCode.BadRequest, "Preset name too long.")
      return
    }
    if (presetDto.presetDescription.length > 25_000) {
      call.respond(HttpStatusCode.BadRequest, "Preset description too long.")
      return
    }

    val presetPreviews = presetsManager.getPresetPreviewsForPendingPreset(presetDto)

    multipartData.forEachPart { part ->
      when (part) {
        is PartData.FileItem -> {
          if (part.originalFileName == "phoneScreenshot") {
            hasPreview = true
            part.provider().copyAndClose(presetPreviews.phoneScreenshotFile.writeChannel())
          } else if (part.originalFileName == "tabletScreenshot") {
            hasPreview = true
            part.provider().copyAndClose(presetPreviews.tabletScreenshotFile.writeChannel())
          }
        }
        else -> {}
      }
      part.dispose()
    }

    presetDto = presetDto.copy(
      hasPreviews = hasPreview
    )

    presetsManager.insert(presetDto)

    call.respond(presetDto)
  }

  suspend fun get(call: RoutingCall) {
    call.respond(presetsManager.get(approvedOnly = true))
  }

  suspend fun getAll(call: RoutingCall) {
    call.respond(presetsManager.get(approvedOnly = false))
  }

  suspend fun approve(call: RoutingCall) {
    val presetDto = call.receive<PresetDto>()

    val pendingPresetPreviews = presetsManager.getPresetPreviewsForPendingPreset(presetDto)
    val approvedPresetPreviews = presetsManager.getPresetPreviewsForApprovedPreset(presetDto)

    pendingPresetPreviews.copyTo(approvedPresetPreviews)

    presetsManager.update(presetDto) {
      it[isApproved] = true

//      it[presetName] = presetDto.presetName
//      it[presetDescription] = presetDto.presetDescription
//      it[presetData] = ExposedBlob(presetDto.presetData.toByteArray())
//      it[updateTs] = System.currentTimeMillis()
    }
      .onSuccess {

        pendingPresetPreviews.phoneScreenshotFile

        call.respond("Ok")
      }
      .onFailure {
        call.respond(HttpStatusCode.BadRequest)
      }
  }
}
