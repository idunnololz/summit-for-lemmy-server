package com.idunnololz.summitForLemmy.server.lemmyStats

import com.idunnololz.summitForLemmy.server.lemmyStats.db.CommunityStatsEntity
import com.idunnololz.summitForLemmy.server.lemmyStats.db.CommunityStatsTable
import com.idunnololz.summitForLemmy.server.localStorage.LocalStorageManager
import com.idunnololz.summitForLemmy.server.network.objects.TrendingCommunityData
import com.idunnololz.summitForLemmy.server.network.objects.TrendingStats
import com.idunnololz.summitForLemmy.server.utils.CoroutineScopeFactory
import com.idunnololz.summitForLemmy.server.utils.sha256HashAsHexString
import com.idunnololz.summitForLemmy.server.utils.suspendTransaction
import com.idunnololz.summitForLemmy.server.utils.toDumbLogger
import io.ktor.util.logging.KtorSimpleLogger
import java.io.File
import java.time.Instant
import java.time.OffsetDateTime
import java.time.ZoneOffset
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.Deferred
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.runInterruptible
import kotlinx.coroutines.withContext
import kotlinx.datetime.Clock
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.encodeToJsonElement
import kotlinx.serialization.json.encodeToStream
import org.jetbrains.exposed.dao.id.EntityID
import org.jetbrains.exposed.sql.SchemaUtils
import org.jetbrains.exposed.sql.transactions.transaction
import org.jetbrains.exposed.sql.upsert

@Singleton
class LemmyStatsManager
@Inject
constructor(
  private val localStorageManager: LocalStorageManager,
  private val trendingDataCache: TrendingDataCache,
  private val coroutineScopeFactory: CoroutineScopeFactory,
) {
  private val logger = KtorSimpleLogger("TrendingManager").toDumbLogger()

  private val trendsDataDir = File(localStorageManager.dataDir, "trends")
  private val trendingDataFile =
    File(localStorageManager.dataDir, "trending_communities.json")

  private val coroutineScope = coroutineScopeFactory.create()
  private val dbContext = Dispatchers.Default.limitedParallelism(1)

  private var getTrendingCommunityJob: Deferred<List<TrendingCommunityData>>? = null

  suspend fun updateTrendingData(trendingData: List<CommunityStats>) {
    suspendTransaction {
      for (community in trendingData) {
        println(community.baseurl)

        CommunityStatsTable.upsert {
          it[id] = EntityID(dbKey(community.name, community.baseurl), CommunityStatsTable)

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

    suspendTransaction {
      CommunityStatsEntity.all().forEach { communityEntity ->
        // update the stat data...

        val file = File(trendsDataDir, communityEntity.id.value.sha256HashAsHexString())
        val dataTime = OffsetDateTime.ofInstant(
          Instant.ofEpochMilli(communityEntity.time),
          ZoneOffset.UTC,
        )

        val communityTrendData: CommunityTrendData =
          getCommunityTrendDataOrInsert(file, communityEntity)

        val lastTime = communityTrendData.statsWithTime.lastOrNull()?.dt

        if (lastTime != null) {
          val (hour, day, month, year) = lastTime.split("-").map { it.toInt() }

          val lastDt =
            OffsetDateTime.of(
              year,
              month,
              day,
              hour,
              0,
              0,
              0,
              ZoneOffset.UTC,
            )
          val currentDt =
            OffsetDateTime.of(
              dataTime.year,
              dataTime.monthValue,
              dataTime.dayOfMonth,
              dataTime.hour,
              0,
              0,
              0,
              ZoneOffset.UTC,
            )

          if (lastDt >= currentDt) {
            logger.debug(
              "Ignoring community ${communityEntity.name} since it's already up to date...",
            )
            return@forEach // no need to update since data is up to date already
          }
        }

        val updatedData =
          communityTrendData.copy(
            lastUpdateTime = Clock.System.now().toString(),
            statsWithTime =
            communityTrendData.statsWithTime +
              CommunityCountsWithTime(
                communityEntity.counts,
                communityEntity.score,
                "${dataTime.hour}-${dataTime.dayOfMonth}-${dataTime.monthValue}-${dataTime.year}",
              ),
          )
        Json.encodeToJsonElement(updatedData)

        logger.debug("Updating community ${communityEntity.name}...")
        file.outputStream().use {
          Json.encodeToStream(updatedData, it)
        }
      }
    }
  }

  suspend fun updateAllCommunityTrendData(data: TrendingUpdater.AllCommunityTrendData) {
    trendingDataFile.parentFile.mkdirs()

    trendingDataFile.outputStream().use {
      Json.encodeToStream(data, it)
    }
  }

  suspend fun getAllCommunityTrendData(): TrendingUpdater.AllCommunityTrendData? = try {
    Json.decodeFromString<TrendingUpdater.AllCommunityTrendData>(
      runInterruptible {
        trendingDataFile.bufferedReader().use { it.readText() }
      },
    ).let {
      return it
    }
  } catch (e: Exception) {
    null
  }

  suspend fun getAllCommunityData(): List<CommunityStatsEntity> {
    val data = mutableListOf<CommunityStatsEntity>()

    transaction {
      CommunityStatsEntity.all().mapTo(data) { it }
    }

    return data
  }

  suspend fun getCommunityTrendData(communityName: String, instance: String): CommunityTrendData? {
    val file = File(trendsDataDir, dbKey(communityName, instance).sha256HashAsHexString())

    if (!file.exists()) {
      return null
    }

    return runInterruptible(Dispatchers.IO) {
      Json.decodeFromString<CommunityTrendData>(
        file.bufferedReader().use { it.readText() },
      ).let {
        it
      }
    }
  }

  fun getCommunityStatsEntity(communityName: String, instance: String) =
    CommunityStatsEntity.findById(dbKey(communityName, instance))

  suspend fun getTrendingCommunities(force: Boolean = false): List<TrendingCommunityData>? {
    if (!force) {
      trendingDataCache.trendingCommunityData?.let {
        return it
      }
    }

    val allTrendingData = getAllCommunityTrendData() ?: return null

    if (getTrendingCommunityJob?.isCompleted == true) {
      getTrendingCommunityJob = null
    }

    return getTrendingCommunityJob?.await() ?: run {
      val job =
        coroutineScope.async {
          withContext(dbContext) {
            if (!force) {
              trendingDataCache.trendingCommunityData?.let {
                return@withContext it
              }
            }

            suspendTransaction {
              allTrendingData.allTrendingData
                .mapNotNull {
                  val communityTrendData =
                    getCommunityStatsEntity(
                      communityName = it.communityName,
                      instance = it.instance,
                    ) ?: return@mapNotNull null

                  with(communityTrendData) {
                    TrendingCommunityData(
                      baseurl = baseurl,
                      nsfw = nsfw,
                      isSuspicious = isSuspicious,
                      name = name,
                      published = published,
                      title = title,
                      url = url,
                      desc = desc,
                      trendStats =
                      TrendingStats(
                        weeklyActiveUsers = communityTrendData.counts.usersActiveWeek.toDouble(),
                        trendScore7Day = it.trendScore7Day,
                        trendScore30Day = it.trendScore30Day,
                        hotScore = it.hotScore,
                      ),
                      counts = communityTrendData.counts,
                      lastUpdateTime = allTrendingData.lastUpdateTime,
                      icon = icon,
                      banner = banner,
                    )
                  }
                }
                .also {
                  trendingDataCache.trendingCommunityData = it
                }
            }
          }
        }
      getTrendingCommunityJob = job
      job.await()
    }
  }

  @Suppress("NOTHING_TO_INLINE")
  private inline fun dbKey(communityName: String, instance: String) = "$instance,$communityName"

  private fun getCommunityTrendDataOrInsert(
    file: File,
    communityStatsEntity: CommunityStatsEntity,
  ): CommunityTrendData {
    if (file.exists()) {
      Json.decodeFromString<CommunityTrendData>(
        file.bufferedReader().use { it.readText() },
      ).let {
        return it
      }
    }

    return CommunityTrendData(
      baseurl = communityStatsEntity.baseurl,
      nsfw = communityStatsEntity.nsfw,
      isSuspicious = communityStatsEntity.isSuspicious,
      name = communityStatsEntity.name,
      published = communityStatsEntity.published,
      title = communityStatsEntity.title,
      url = communityStatsEntity.url,
      desc = communityStatsEntity.desc,
      lastUpdateTime = null,
      icon = communityStatsEntity.icon,
      banner = communityStatsEntity.banner,
      statsWithTime = listOf(),
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
    val lastUpdateTime: String? = null,
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
