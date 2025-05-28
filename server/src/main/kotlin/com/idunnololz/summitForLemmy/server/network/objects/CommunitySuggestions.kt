package com.idunnololz.summitForLemmy.server.network.objects

import kotlinx.serialization.Serializable

@Serializable
class CommunitySuggestions(
  val popularLast7Days: List<TrendingCommunityData>,
  val trendingLast7Days: List<TrendingCommunityData>,
  val hot: List<TrendingCommunityData>,
  val randomCommunities: List<TrendingCommunityData>,
)
