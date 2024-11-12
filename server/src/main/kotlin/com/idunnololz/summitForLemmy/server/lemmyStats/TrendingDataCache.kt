package com.idunnololz.summitForLemmy.server.lemmyStats

import com.idunnololz.summitForLemmy.server.network.objects.TrendingCommunityData
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class TrendingDataCache @Inject constructor() {
    var trendingCommunityData: List<TrendingCommunityData>? = null
}