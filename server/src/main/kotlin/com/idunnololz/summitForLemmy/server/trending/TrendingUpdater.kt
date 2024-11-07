package com.idunnololz.summitForLemmy.server.trending

import com.idunnololz.summitForLemmy.server.localStorage.LocalStorageManager
import com.idunnololz.summitForLemmy.server.trending.db.CommunityStatsEntity
import com.idunnololz.summitForLemmy.server.trending.db.CommunityStatsTable
import com.idunnololz.summitForLemmy.server.utils.retryIo
import com.idunnololz.summitForLemmy.server.utils.sha256HashAsHexString
import io.ktor.client.*
import io.ktor.client.call.*
import io.ktor.client.request.*
import io.ktor.http.*
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
import javax.inject.Singleton

@Singleton
class TrendingUpdater @Inject constructor(
    private val httpClient: HttpClient,
    private val trendingManager: TrendingManager,
) {

    private val logger = KtorSimpleLogger("TrendingUpdater")

    suspend fun updateTrending() {
        logger.info("Updating trending stats...")

        retryIo(
            initialDelay = 1000,
            maxDelay = 16_000,
        ) {
            val response = httpClient
                .get("https://data.lemmyverse.net/data/community.full.json")

            if (response.status == HttpStatusCode.OK) {
                trendingManager.updateTrendingData(response.body())
            } else {

            }
        }

        logger.info("Trending stats updated!")
    }
}