package com.idunnololz.summitForLemmy.server.trending.db

import com.idunnololz.summitForLemmy.server.trending.CommunityCounts
import kotlinx.serialization.json.Json
import org.jetbrains.exposed.dao.Entity
import org.jetbrains.exposed.dao.EntityClass
import org.jetbrains.exposed.dao.id.EntityID
import org.jetbrains.exposed.dao.id.IdTable
import org.jetbrains.exposed.sql.Column
import org.jetbrains.exposed.sql.json.json

open class StringIdTable(name: String = "", idColumnName: String = "id", idColumnSize: Int = 32) : IdTable<String>(name) {
    final override val id: Column<EntityID<String>> = varchar(idColumnName, idColumnSize).entityId()
    override val primaryKey = PrimaryKey(id)
}

abstract class StringEntity(id: EntityID<String>) : Entity<String>(id)

abstract class StringEntityClass<E: StringEntity>(table: IdTable<String>) : EntityClass<String, E> (table)


object CommunityStatsTable : StringIdTable(idColumnSize = 512) {
    val score: Column<Double> = double("score")
    val baseurl: Column<String> = varchar("baseurl", length = 255)
    val nsfw: Column<Boolean> = bool("nsfw")
    val counts: Column<CommunityCounts> = json<CommunityCounts>("counts", Json.Default)
    val isSuspicious: Column<Boolean> = bool("isSuspicious")
    val name: Column<String> = varchar("name", length = 255)
    val published: Column<Long> = long("published")
    val time: Column<Long> = long("time")
    val title: Column<String> = varchar("title", length = 255)
    val url: Column<String> = varchar("url", length = 512)
    val desc: Column<String> = text("desc")
    val icon: Column<String?> = text("icon").nullable()
    val banner: Column<String?> = text("banner").nullable()
}

class CommunityStatsEntity(id: EntityID<String>) : StringEntity(id) {
    companion object : StringEntityClass<CommunityStatsEntity>(CommunityStatsTable)

    var score by CommunityStatsTable.score
    var baseurl by CommunityStatsTable.baseurl
    var nsfw by CommunityStatsTable.nsfw
    var counts by CommunityStatsTable.counts
    var isSuspicious by CommunityStatsTable.isSuspicious
    var name by CommunityStatsTable.name
    var published by CommunityStatsTable.published
    var time by CommunityStatsTable.time
    var title by CommunityStatsTable.title
    var url by CommunityStatsTable.url
    var desc by CommunityStatsTable.desc
    var icon by CommunityStatsTable.icon
    var banner by CommunityStatsTable.banner
    var primaryKey by CommunityStatsTable.id
}