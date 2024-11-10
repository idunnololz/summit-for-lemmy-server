package com.idunnololz.summitForLemmy.server.trending

import io.ktor.client.*
import javax.inject.Inject
import io.ktor.util.logging.*
import korlibs.time.days
import korlibs.time.fromDays
import kotlinx.datetime.*
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import javax.inject.Singleton
import kotlin.time.Duration

@Singleton
class TrendingUpdater @Inject constructor(
    private val httpClient: HttpClient,
    private val trendingManager: TrendingManager,
) {

    private val logger = KtorSimpleLogger("TrendingUpdater")

    suspend fun updateTrendingData() {
        val allCommunities = trendingManager.getAllCommunityData()

        for (communityData in allCommunities) {
            updateTrendingData(communityData.name, communityData.baseurl)
        }
    }

    suspend fun updateTrendingData(communityName: String, instance: String) {
        val trendData = trendingManager.getCommunityTrendData(
            communityName = communityName,
            instance = instance
        )

        trendData ?: return

        val last7Days = getLast7DaysTrendData(trendData)
        val now = Clock.System.now()

        val last7DaysData = last7Days.map {
            val (_, day, month, year) = it.dt.split("-").map { it.toInt() }
            val daysAgo = (now - LocalDateTime(year, month, day, 0, 0, 0, 0)
                .toInstant(TimeZone.currentSystemDefault())).days.toInt()

            daysAgo to it.counts.usersActiveDay
        }

        logger.info(Json.encodeToString(last7Days))
        logger.info(calculateTrend(last7DaysData).toString())
    }

    private fun getLast7DaysTrendData(
        trendData: TrendingManager.CommunityTrendData
    ): List<TrendingManager.CommunityCountsWithTime> {

        val intermediateData = trendData.statsWithTime.associateBy {
            it.dt.split("-").drop(1).joinToString(separator = "-")
        }
        val sevenDaysAgo = Clock.System.now().minus(Duration.fromDays(7))

        val last7DaysData = intermediateData.values.filter {
            val (hour, day, month, year) = it.dt.split("-").map { it.toInt() }

            LocalDateTime(year, month, day, hour, 0, 0, 0)
                .toInstant(TimeZone.currentSystemDefault()) > sevenDaysAgo
        }

        return last7DaysData.sortedByDescending {
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
}