package com.idunnololz.summitForLemmy.server.network.objects

import kotlinx.serialization.Serializable

@Serializable
class TrendingCommunityData(
    val baseurl: String,
    val nsfw: Boolean,
    val isSuspicious: Boolean,
    val name: String,
    val published: Long,
    val title: String,
    val url: String,
    val desc: String,
    val trendStats: TrendingStats,
    val lastUpdateTime: String? = null,
    val icon: String? = null,
    val banner: String? = null,
)

@Serializable
class TrendingStats(
    val weeklyActiveUsers: Double,
    val trendScore7Day: Double,
    val trendScore30Day: Double,
    val hotScore: Double,
)