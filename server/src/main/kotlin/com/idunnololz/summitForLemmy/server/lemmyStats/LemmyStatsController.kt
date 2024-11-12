package com.idunnololz.summitForLemmy.server.lemmyStats

import com.idunnololz.summitForLemmy.server.network.objects.TrendingCommunityData
import com.idunnololz.summitForLemmy.server.network.objects.TrendingStats
import io.ktor.http.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import org.jetbrains.exposed.sql.transactions.transaction
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class LemmyStatsController @Inject constructor(
    private val lemmyStatsManager: LemmyStatsManager,
    private val trendingUpdater: TrendingUpdater,
    private val trendingDataCache: TrendingDataCache,
) {
    suspend fun getCommunityTrendData(call: RoutingCall) {
        val communityName = call.queryParameters["communityName"]
        val instance = call.queryParameters["instance"]

        if (communityName == null) {
            call.respond(HttpStatusCode.UnprocessableEntity, "communityName missing.")
            return
        }
        if (instance == null) {
            call.respond(HttpStatusCode.UnprocessableEntity, "instance missing.")
            return
        }

        val communityTrendData = lemmyStatsManager.getCommunityTrendData(communityName, instance)

        if (communityTrendData == null) {
            call.respond(HttpStatusCode.NotFound, "No trend data for that community/instance.")
            return
        }

        call.respond(communityTrendData)
    }

    suspend fun updateTrending(call: RoutingCall) {
        call.respond("Ok")

        trendingUpdater.updateCommunitiesTrendData()
    }

    suspend fun getAllCommunityTrendData(call: RoutingCall) {
        val data = lemmyStatsManager.getAllCommunityTrendData()

        if (data != null) {
            call.respond(data)
        } else {
            call.respond(HttpStatusCode.NotFound, "no trend data")
        }
    }

    suspend fun getTopTrendingCommunities(call: RoutingCall) {
        val trendingCommunities = getTrendingCommunities()

        if (trendingCommunities == null) {
            call.respond(HttpStatusCode.NotFound, "no trend data")
            return
        }

        val data = trendingCommunities.sortedByDescending { it.trendStats.trendScore7Day }
            .take(50)

        call.respond(data)
    }

    suspend fun getHotCommunities(call: RoutingCall) {
        val trendingCommunities = getTrendingCommunities()

        if (trendingCommunities == null) {
            call.respond(HttpStatusCode.NotFound, "no trend data")
            return
        }

        val data = trendingCommunities.sortedByDescending { it.trendStats.hotScore }
            .take(50)

        call.respond(data)
    }

    suspend fun getTrendingCommunities(): List<TrendingCommunityData>? {
        trendingDataCache.trendingCommunityData?.let {
            return it
        }

        val allTrendingData = lemmyStatsManager.getAllCommunityTrendData() ?: return null

        return transaction {
            allTrendingData.allTrendingData
                .mapNotNull {
                    val communityTrendData = lemmyStatsManager.getCommunityStatsEntity(
                        communityName = it.communityName,
                        instance = it.instance
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
                            trendStats = TrendingStats(
                                trendScore7Day = it.trendScore7Day,
                                trendScore30Day = it.trendScore30Day,
                                hotScore = it.hotScore,
                            ),
                            lastUpdateTime = allTrendingData.lastUpdateTime,
                            icon = icon,
                            banner = banner
                        )
                    }
                }
                .also {
                    trendingDataCache.trendingCommunityData = it
                }
        }
    }
}