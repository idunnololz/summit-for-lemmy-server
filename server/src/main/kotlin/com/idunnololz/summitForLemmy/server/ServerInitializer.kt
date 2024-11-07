package com.idunnololz.summitForLemmy.server

import com.idunnololz.summitForLemmy.server.routing.ApiRoutes
import com.idunnololz.summitForLemmy.server.taskManager.TaskManager
import com.idunnololz.summitForLemmy.server.trending.TrendingUpdater
import com.idunnololz.summitForLemmy.server.utils.CoroutineScopeFactory
import io.ktor.server.application.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import javax.inject.Inject
import javax.inject.Singleton
import kotlin.time.Duration.Companion.hours

@Singleton
class ServerInitializer @Inject constructor(
    private val app: Application,
    private val coroutineScopeFactory: CoroutineScopeFactory,
    private val taskManager: TaskManager,
    private val trendingUpdater: TrendingUpdater,
    private val apiRoutes: ApiRoutes,
) {

    private val coroutineScope = coroutineScopeFactory.create()

    fun initialize() {
        taskManager.schedule(
            taskName = "update-trending-task",
            // https://insanusmokrassar.github.io/KrontabPredictor/?krontab=0%200%20*%20*%20*
            kronTabString = "0 0 * * *",
        ) {
            withContext(Dispatchers.Default) {
                try {
                    trendingUpdater.updateTrending()
                } catch (e: Exception) {
                    app.log.error("Error updating trending data.", e)
                }
            }
        }

        apiRoutes.initialize()
    }
}