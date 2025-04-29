package com.idunnololz.summitForLemmy.server.presets.db

import com.idunnololz.summitForLemmy.server.lemmyStats.db.StringEntity
import com.idunnololz.summitForLemmy.server.lemmyStats.db.StringEntityClass
import com.idunnololz.summitForLemmy.server.lemmyStats.db.StringIdTable
import com.idunnololz.summitForLemmy.server.presets.db.PresetEntityTable.bool
import org.jetbrains.exposed.dao.id.EntityID
import org.jetbrains.exposed.sql.Column
import org.jetbrains.exposed.sql.statements.api.ExposedBlob

object PresetEntityTable : StringIdTable(idColumnSize = 512) {
  val presetName: Column<String> = varchar("presetName", length = 255)
  val presetDescription: Column<String> = text("presetDescription")
  val presetData: Column<ExposedBlob> = blob("presetData")
  val createTs: Column<Long> = long("createTs")
  val updateTs: Column<Long> = long("updateTs")
  val isApproved: Column<Boolean> = bool("isApproved")
  val hasPreview: Column<Boolean> = bool("hasPreview")
}

class PresetEntity(id: EntityID<String>) : StringEntity(id) {
  companion object : StringEntityClass<PresetEntity>(PresetEntityTable)

  var presetName by PresetEntityTable.presetName
  var presetDescription by PresetEntityTable.presetDescription
  var presetData by PresetEntityTable.presetData
  var createTs by PresetEntityTable.createTs
  var updateTs by PresetEntityTable.updateTs
  var isApproved by PresetEntityTable.isApproved
  val hasPreview by PresetEntityTable.hasPreview
}
