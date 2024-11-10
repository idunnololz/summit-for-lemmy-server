package com.idunnololz.summitForLemmy.server.trending

import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class TrendingController @Inject constructor(
    private val trendingManager: TrendingManager,
    private val trendingUpdater: TrendingUpdater,
) {
    suspend fun getCommunityTrendData(communityName: String, instance: String): TrendingManager.CommunityTrendData? {
        return trendingManager.getCommunityTrendData(communityName, instance)
    }

    suspend fun updateTrending() {
        trendingUpdater.updateCommunitiesTrendData()
    }
}