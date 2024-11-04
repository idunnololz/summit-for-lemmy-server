package com.idunnololz.summitForLemmy.server.trending

import com.idunnololz.summitForLemmy.server.localStorage.LocalStorageManager
import com.idunnololz.summitForLemmy.server.trending.db.CommunityStatsEntity
import com.idunnololz.summitForLemmy.server.trending.db.CommunityStatsTable
import com.idunnololz.summitForLemmy.server.utils.sha256HashAsHexString
import io.ktor.client.*
import io.ktor.client.call.*
import io.ktor.client.request.*
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.encodeToJsonElement
import kotlinx.serialization.json.encodeToStream
import org.jetbrains.exposed.dao.id.EntityID
import org.jetbrains.exposed.sql.SchemaUtils
import org.jetbrains.exposed.sql.transactions.transaction
import org.jetbrains.exposed.sql.upsert
import java.io.File
import java.time.Instant
import java.time.OffsetDateTime
import java.time.ZoneOffset
import javax.inject.Inject
import kotlin.io.encoding.ExperimentalEncodingApi
import io.ktor.util.logging.*

class TrendingUpdater @Inject constructor(
    private val localStorageManager: LocalStorageManager,
    private val httpClient: HttpClient,
) {

    private val logger = KtorSimpleLogger("TrendingUpdater")

    private val trendsDataDir = File(localStorageManager.dataDir, "trends")

    @OptIn(ExperimentalEncodingApi::class, ExperimentalSerializationApi::class)
    suspend fun updateTrending() {
        logger.info("Updating trending stats...")

        val response: List<CommunityStats> = httpClient
            .get("https://data.lemmyverse.net/data/community.full.json")
            .body()

        transaction {
            SchemaUtils.create(CommunityStatsTable)

            for (community in response) {
                println(community.baseurl)

                CommunityStatsTable.upsert {
                    it[id] = EntityID<String>("${community.baseurl},${community.name}", CommunityStatsTable)

                    it[score] = community.score
                    it[baseurl] = community.baseurl
                    it[nsfw] = community.nsfw
                    it[counts] = community.counts
                    it[isSuspicious] = community.isSuspicious
                    it[name] = community.name
                    it[published] = community.published
                    it[time] = community.time
                    it[title] = community.title
                    it[url] = community.url
                    it[desc] = community.desc
                    it[icon] = community.icon
                    it[banner] = community.banner
                }
            }
        }

        trendsDataDir.mkdirs()

        transaction {
            CommunityStatsEntity.all().forEach { communityEntity ->
                // update the stat data...

                val file = File(trendsDataDir, communityEntity.id.value.sha256HashAsHexString())
                val dataTime = OffsetDateTime.ofInstant(Instant.ofEpochMilli(communityEntity.time), ZoneOffset.UTC)

                val communityTrendData: CommunityTrendData = getCommunityTrendData(file, communityEntity)

                val lastTime = communityTrendData.statsWithTime.lastOrNull()?.dt

                if (lastTime != null) {
                    val (hour, day, month, year) = lastTime.split("-").map { it.toInt() }

                    val lastDt = OffsetDateTime.of(
                        year,
                        month,
                        day,
                        hour,
                        0,
                        0,
                        0,
                        ZoneOffset.UTC
                    )
                    val currentDt = OffsetDateTime.of(
                        dataTime.year,
                        dataTime.monthValue,
                        dataTime.dayOfMonth,
                        dataTime.hour,
                        0,
                        0,
                        0,
                        ZoneOffset.UTC
                    )

                    if (lastDt >= currentDt) {
                        logger.debug("Ignoring community ${communityEntity.name} since it's already up to date...")
                        return@forEach // no need to update since data is up to date already
                    }
                }

                val updatedData = communityTrendData.copy(
                    statsWithTime = communityTrendData.statsWithTime + CommunityCountsWithTime(
                        communityEntity.counts,
                        communityEntity.score,
                        "${dataTime.hour}-${dataTime.dayOfMonth}-${dataTime.monthValue}-${dataTime.year}",
                    )
                )
                Json.encodeToJsonElement(updatedData)

                logger.debug("Updating community ${communityEntity.name}...")
                file.outputStream().use {
                    Json.encodeToStream(updatedData, it)
                }
            }
        }
        logger.info("Trending stats updated!")
    }

    private fun getCommunityTrendData(
        file: File,
        communityStatsEntity: CommunityStatsEntity,
    ): CommunityTrendData {

        if (file.exists()) {
            Json.decodeFromString<CommunityTrendData>(file.bufferedReader().use { it.readText() }).let {
                return it
            }
        }

        return CommunityTrendData(
            communityStatsEntity.baseurl,
            communityStatsEntity.nsfw,
            communityStatsEntity.isSuspicious,
            communityStatsEntity.name,
            communityStatsEntity.published,
            communityStatsEntity.title,
            communityStatsEntity.url,
            communityStatsEntity.desc,
            communityStatsEntity.icon,
            communityStatsEntity.banner,
            listOf(),
        )
    }

    @Serializable
    data class CommunityTrendData(
        val baseurl: String,
        val nsfw: Boolean,
        val isSuspicious: Boolean,
        val name: String,
        val published: Long,
        val title: String,
        val url: String,
        val desc: String,
        val icon: String? = null,
        val banner: String? = null,
        val statsWithTime: List<CommunityCountsWithTime>,
    )

    @Serializable
    data class CommunityCountsWithTime(
        val counts: CommunityCounts,
        val score: Double,
        val dt: String,
    )
}