package com.idunnololz.summitForLemmy.server.network.objects

import kotlinx.serialization.Serializable

@Serializable
class CommunitySuggestions(
    val trendingLast7Days: List<TrendingCommunityData>,
    val hot: List<TrendingCommunityData>,
)