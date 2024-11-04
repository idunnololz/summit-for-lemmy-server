package com.idunnololz.summitForLemmy.server

import com.idunnololz.summitForLemmy.server.trending.TrendingUpdater
import com.idunnololz.summitForLemmy.server.utils.CoroutineScopeFactory
import io.ktor.server.application.*
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import javax.inject.Inject
import javax.inject.Singleton
import kotlin.time.Duration.Companion.hours

@Singleton
class ServerInitializer @Inject constructor(
    private val app: Application,
    private val coroutineScopeFactory: CoroutineScopeFactory,
    private val trendingUpdater: TrendingUpdater,
) {

    private val coroutineScope = coroutineScopeFactory.create()

    fun initialize() {
        coroutineScope.launch {
            while (true) {
                try {
                    trendingUpdater.updateTrending()
                } catch (e: Exception) {
                    app.log.error("Error updating trending data.", e)
                }
                delay(1.hours)
            }
        }
    }
}