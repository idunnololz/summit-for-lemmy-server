package com.idunnololz.summitForLemmy.server.lemmyStats

import com.idunnololz.summitForLemmy.server.network.objects.CommunitySuggestions
import io.ktor.http.HttpStatusCode
import io.ktor.server.response.respond
import io.ktor.server.routing.RoutingCall
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class LemmyStatsController
@Inject
constructor(
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

    val communityTrendData = lemmyStatsManager.getCommunityTrendData(
      communityName,
      instance,
    )

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
    val trendingCommunities = lemmyStatsManager.getTrendingCommunities()

    if (trendingCommunities == null) {
      call.respond(HttpStatusCode.NotFound, "no trend data")
      return
    }

    val data =
      trendingCommunities.sortedByDescending { it.trendStats.trendScore7Day }
        .take(50)

    call.respond(data)
  }

  suspend fun getHotCommunities(call: RoutingCall) {
    val trendingCommunities = lemmyStatsManager.getTrendingCommunities()

    if (trendingCommunities == null) {
      call.respond(HttpStatusCode.NotFound, "no trend data")
      return
    }

    val data =
      trendingCommunities.sortedByDescending { it.trendStats.hotScore }
        .take(50)

    call.respond(data)
  }

  suspend fun getCommunitySuggestions(call: RoutingCall) {
    val trendingCommunities = lemmyStatsManager.getTrendingCommunities()

    if (trendingCommunities == null) {
      call.respond(HttpStatusCode.NotFound, "no trend data")
      return
    }

    call.respond(
      CommunitySuggestions(
        popularLast7Days =
        trendingCommunities.sortedByDescending { it.trendStats.weeklyActiveUsers }
          .take(50),
        trendingLast7Days =
        trendingCommunities.sortedByDescending { it.trendStats.trendScore7Day }
          .take(50),
        hot =
        trendingCommunities.sortedByDescending { it.trendStats.hotScore }
          .take(50),
      ),
    )
  }
}
