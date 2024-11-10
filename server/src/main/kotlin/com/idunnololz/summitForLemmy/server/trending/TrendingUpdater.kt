package com.idunnololz.summitForLemmy.server.trending

import javax.inject.Inject
import io.ktor.util.logging.*
import korlibs.time.days
import korlibs.time.fromDays
import kotlinx.datetime.*
import kotlinx.serialization.Serializable
import javax.inject.Singleton
import kotlin.time.Duration

@Singleton
class TrendingUpdater @Inject constructor(
    private val trendingManager: TrendingManager,
) {
    private val logger = KtorSimpleLogger("TrendingUpdater")

    suspend fun updateCommunitiesTrendData() {
        logger.info("Updating community trend data...")

        val allCommunities = trendingManager.getAllCommunityData()

        logger.info("There are ${allCommunities.size} communities with trend data...")

        val data = AllCommunityTrendData(
            Clock.System.now().toString(),
            allCommunities
                .mapNotNull { communityData ->
                    getTrendingData(communityData.name, communityData.baseurl)
                }
                .filter { it.trendScore7Day != 0.0 && it.trendScore30Day != 0.0 }
                .sortedByDescending { it.trendScore7Day },
        )

        trendingManager.updateAllCommunityTrendData(data)
        logger.info("Community trend data updated.")
    }

    suspend fun getTrendingData(communityName: String, instance: String): CommunityTrendData? {
        val trendData = trendingManager.getCommunityTrendData(
            communityName = communityName,
            instance = instance
        )

        trendData ?: return null

        return CommunityTrendData(
            communityName = communityName,
            instance = instance,
            trendScore7Day = calculateTrendLastNDays(trendData, 7),
            trendScore30Day = calculateTrendLastNDays(trendData, 30),
        )
    }

    private fun calculateTrendLastNDays(
        trendData: TrendingManager.CommunityTrendData,
        days: Int
    ): Double {
        val lastNDays = getLastNDaysTrendData(trendData, days)
        val now = Clock.System.now()

        val lastNDaysData = lastNDays.map {
            val (_, day, month, year) = it.dt.split("-").map { it.toInt() }
            val daysAgo = (now - LocalDateTime(year, month, day, 0, 0, 0, 0)
                .toInstant(TimeZone.currentSystemDefault())).days.toInt()

            (days - daysAgo) to it.counts.usersActiveWeek
        }

        return calculateTrend(lastNDaysData)
    }

    private fun getLastNDaysTrendData(
        trendData: TrendingManager.CommunityTrendData,
        days: Int,
    ): List<TrendingManager.CommunityCountsWithTime> {

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
        val trendScore7Day: Double,
        val trendScore30Day: Double,
    )
}