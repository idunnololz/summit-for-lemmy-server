package com.idunnololz.summitForLemmy.server.lemmyStats

import javax.inject.Inject
import io.ktor.util.logging.*
import korlibs.time.days
import korlibs.time.fromDays
import kotlinx.datetime.*
import kotlinx.serialization.Serializable
import javax.inject.Singleton
import kotlin.math.ln
import kotlin.math.max
import kotlin.math.pow
import kotlin.time.Duration

@Singleton
class TrendingUpdater @Inject constructor(
    private val lemmyStatsManager: LemmyStatsManager,
    private val trendingDataCache: TrendingDataCache,
) {
    private val logger = KtorSimpleLogger("TrendingUpdater")

    suspend fun updateCommunitiesTrendData() {
        logger.info("Updating community trend data...")

        val allCommunities = lemmyStatsManager.getAllCommunityData()

        logger.info("There are ${allCommunities.size} communities with trend data...")

        val data = AllCommunityTrendData(
            Clock.System.now().toString(),
            allCommunities
                .mapNotNull { communityData ->
                    getTrendingData(communityData.name, communityData.baseurl)
                }
                .filter { it.trendScore7Day != 0.0 || it.trendScore30Day != 0.0 || it.hotScore != 0.0 || it.weeklyActiveUsers != 0.0 }
                .sortedByDescending { it.trendScore7Day },
        )

        lemmyStatsManager.updateAllCommunityTrendData(data)
        logger.info("Community trend data updated.")

        trendingDataCache.trendingCommunityData = null
    }

    suspend fun getTrendingData(communityName: String, instance: String): CommunityTrendData? {
        val trendData = lemmyStatsManager.getCommunityTrendData(
            communityName = communityName,
            instance = instance
        )

        trendData ?: return null

        val hoursDiff = ((Clock.System.now().epochSeconds * 1000) - trendData.published) / 3600000

        val hotScore = if (hoursDiff < 720) {
            ln(max(2.0, calculateTrendLastNDays(trendData, 7, { this.usersActiveDay }) + 2.0)) / (hoursDiff + 2.0).pow(1.1)
        } else {
            0.0
        }

        return CommunityTrendData(
            communityName = communityName,
            instance = instance,
            weeklyActiveUsers = trendData.statsWithTime.lastOrNull()?.counts?.usersActiveWeek?.toDouble() ?: 0.0,
            trendScore7Day = calculateTrendLastNDays(trendData, 7),
            trendScore30Day = calculateTrendLastNDays(trendData, 30),
            hotScore = hotScore,
        )
    }

    private fun calculateTrendLastNDays(
        trendData: LemmyStatsManager.CommunityTrendData,
        days: Int,
        countToUseFn: CommunityCounts.() -> Int = { usersActiveWeek },
    ): Double {
        val lastNDays = getLastNDaysTrendData(trendData, days)
        val now = Clock.System.now()

        val lastNDaysData = lastNDays.map {
            val (_, day, month, year) = it.dt.split("-").map { it.toInt() }
            val daysAgo = (now - LocalDateTime(year, month, day, 0, 0, 0, 0)
                .toInstant(TimeZone.currentSystemDefault())).days.toInt()

            (days - daysAgo + 1) to it.counts.countToUseFn()
        }

        return calculateTrend(lastNDaysData)
    }

    private fun getLastNDaysTrendData(
        trendData: LemmyStatsManager.CommunityTrendData,
        days: Int,
    ): List<LemmyStatsManager.CommunityCountsWithTime> {

        val intermediateData = trendData.statsWithTime.associateBy {
            it.dt.split("-").drop(1).joinToString(separator = "-")
        }
        val sevenDaysAgo = Clock.System.now().minus(Duration.fromDays(days))

        val lastNDaysData = intermediateData.values.filter {
            val (hour, day, month, year) = it.dt.split("-").map { it.toInt() }

            LocalDateTime(year, month, day, hour, 0, 0, 0)
                .toInstant(TimeZone.currentSystemDefault()) > sevenDaysAgo
        }

        return lastNDaysData.sortedByDescending {
            val (hour, day, month, year) = it.dt.split("-").map { it.toInt() }
            LocalDateTime(year, month, day, hour, 0, 0, 0)
        }
    }

    /**
     * @param data list of [day, count]
     */
    private fun calculateTrend(data: List<Pair<Int, Int>>): Double {
        val totalCount = data.sumOf { it.second }
        var multipliedData = 0
        var summedDays = 0
        var squaredIndex = 0

        for ((day, count) in data) {
            multipliedData += day * count
            summedDays += day
            squaredIndex += day * day
        }

        val numerator = (data.size * multipliedData) - (totalCount * summedDays)
        val denominator = (data.size * squaredIndex) - (summedDays * summedDays)

        if (denominator == 0) {
            return 0.0
        }

        return numerator / denominator.toDouble()
    }

    @Serializable
    data class AllCommunityTrendData(
        val lastUpdateTime: String,
        val allTrendingData: List<CommunityTrendData>
    )

    @Serializable
    data class CommunityTrendData(
        val communityName: String,
        val instance: String,
        val weeklyActiveUsers: Double,
        val trendScore7Day: Double,
        val trendScore30Day: Double,
        val hotScore: Double,
    )
}