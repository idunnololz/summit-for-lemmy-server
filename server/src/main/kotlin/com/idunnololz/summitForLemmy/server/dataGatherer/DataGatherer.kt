package com.idunnololz.summitForLemmy.server.dataGatherer

import com.idunnololz.summitForLemmy.server.trending.TrendingManager
import com.idunnololz.summitForLemmy.server.utils.retryIo
import io.ktor.client.*
import io.ktor.client.call.*
import io.ktor.client.request.*
import io.ktor.http.*
import io.ktor.util.logging.*
import kotlinx.io.IOException
import javax.inject.Inject

class DataGatherer @Inject constructor(
    private val httpClient: HttpClient,
    private val trendingManager: TrendingManager,
) {

    private val logger = KtorSimpleLogger("TrendingUpdater")

    suspend fun updateCommunitiesData() {
        logger.info("Updating trending stats...")

        retryIo(
            initialDelay = 1000,
            maxDelay = 16_000,
        ) {
            val response = httpClient
                .get("https://data.lemmyverse.net/data/community.full.json")

            if (response.status == HttpStatusCode.OK) {
                trendingManager.updateTrendingData(response.body())
            } else {
                throw IOException()
            }
        }

        logger.info("Trending stats updated!")
    }

}