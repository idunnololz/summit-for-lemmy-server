package com.idunnololz.summitForLemmy.server.presets

import com.idunnololz.summitForLemmy.server.lemmyStats.db.CommunityStatsTable
import com.idunnololz.summitForLemmy.server.localStorage.LocalStorageManager
import com.idunnololz.summitForLemmy.server.presets.db.PresetEntity
import com.idunnololz.summitForLemmy.server.presets.db.PresetEntityTable
import com.idunnololz.summitForLemmy.server.presets.objects.PresetDto
import com.idunnololz.summitForLemmy.server.utils.suspendTransaction
import io.ktor.http.HttpStatusCode
import io.ktor.server.request.receive
import io.ktor.server.response.respond
import io.ktor.server.routing.RoutingCall
import org.jetbrains.exposed.dao.id.EntityID
import org.jetbrains.exposed.sql.SqlExpressionBuilder.eq
import org.jetbrains.exposed.sql.selectAll
import org.jetbrains.exposed.sql.statements.UpdateStatement
import org.jetbrains.exposed.sql.statements.api.ExposedBlob
import org.jetbrains.exposed.sql.update
import org.jetbrains.exposed.sql.upsert
import java.io.File
import javax.inject.Inject

class PresetsManager
@Inject
constructor(
  private val localStorageManager: LocalStorageManager,
) {


  suspend fun get(approvedOnly: Boolean) =
    suspendTransaction {
      if (approvedOnly) {
        PresetEntityTable.selectAll()
          .where { PresetEntityTable.isApproved eq true }
          .map { PresetEntity.wrapRow(it) }
      } else {
        PresetEntity.all().toList()
      }
        .map {
          PresetDto(
            id = it.id.value,
            presetName = it.presetName,
            presetDescription = it.presetDescription,
            presetData = it.presetData.bytes.toString(Charsets.UTF_8),
            createTs = it.createTs,
            updateTs = it.updateTs,
            isApproved = it.isApproved,
          )
        }
    }

  suspend fun insert(presetDto: PresetDto): Result<Unit> {
    val presetId = presetDto.id
      ?: return Result.failure(RuntimeException("'id' is null."))

    suspendTransaction {
      PresetEntityTable.upsert {
        it[id] = EntityID(presetId, CommunityStatsTable)
        it[presetName] = presetDto.presetName
        it[presetDescription] = presetDto.presetDescription
        it[presetData] = ExposedBlob(presetDto.presetData.toByteArray())
        it[createTs] = presetDto.createTs
        it[updateTs] = presetDto.updateTs
        it[isApproved] = presetDto.isApproved ?: false
        it[this.hasPreview] = presetDto.hasPreviews ?: false
      }
    }

    return Result.success(Unit)
  }

  suspend fun update(presetDto: PresetDto, updateFn: (UpdateStatement) -> Unit): Result<Unit> {
    val id = presetDto.id

    if (id.isNullOrBlank()) {
      return Result.failure(RuntimeException("'id' is null."))
    }

    val entry = suspendTransaction {
      PresetEntity.findById(id)
    }

    if (entry == null) {
      return Result.failure(RuntimeException("entry with 'id' ${id} does not exist."))
    }

    suspendTransaction {
      PresetEntityTable.update({ PresetEntityTable.id eq id }) {
        updateFn(it)
      }
    }

    return Result.success(Unit)
  }

  fun getPresetPreviewsForPendingPreset(presetDto: PresetDto): PresetPreviews =
    PresetPreviews(
      phoneScreenshotFile = File(localStorageManager.presetScreenshotUploadsDir, "${presetDto.id}.1.jpeg"),
      tabletScreenshotFile = File(localStorageManager.presetScreenshotUploadsDir, "${presetDto.id}.2.jpeg")
    )

  fun getPresetPreviewsForApprovedPreset(presetDto: PresetDto): PresetPreviews =
    PresetPreviews(
      phoneScreenshotFile = File(localStorageManager.presetScreenshotPublicDir, "${presetDto.id}.1.jpeg"),
      tabletScreenshotFile = File(localStorageManager.presetScreenshotPublicDir, "${presetDto.id}.2.jpeg")
    )
}