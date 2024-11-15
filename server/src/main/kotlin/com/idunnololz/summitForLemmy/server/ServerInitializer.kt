package com.idunnololz.summitForLemmy.server

import com.idunnololz.summitForLemmy.server.dataGatherer.DataGatherer
import com.idunnololz.summitForLemmy.server.routing.ApiRoutes
import com.idunnololz.summitForLemmy.server.taskManager.TaskManager
import com.idunnololz.summitForLemmy.server.lemmyStats.TrendingUpdater
import com.idunnololz.summitForLemmy.server.utils.CoroutineScopeFactory
import io.ktor.server.application.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ServerInitializer @Inject constructor(
    private val app: Application,
    private val coroutineScopeFactory: CoroutineScopeFactory,
    private val taskManager: TaskManager,
    private val dataGatherer: DataGatherer,
    private val trendingUpdater: TrendingUpdater,
    private val apiRoutes: ApiRoutes,
) {

    private val coroutineScope = coroutineScopeFactory.create()

    fun initialize() {
        taskManager.schedule(
            taskName = "update-communities-data-task",
            // https://insanusmokrassar.github.io/KrontabPredictor/?krontab=0%200%20*%20*%20*
            kronTabString = "0 0 * * *",
        ) {
            withContext(Dispatchers.Default) {
                try {
                    dataGatherer.updateCommunitiesData()
                } catch (e: Exception) {
                    app.log.error("Error updating trending data.", e)
                }
            }
        }
        taskManager.schedule(
            taskName = "update-trending-task",
            // https://insanusmokrassar.github.io/KrontabPredictor/?krontab=0%200%200%2F12%20*%20*
            kronTabString = "0 0 0/12 * *",
        ) {
            withContext(Dispatchers.Default) {
                try {
                    trendingUpdater.updateCommunitiesTrendData()
                } catch (e: Exception) {
                    app.log.error("Error updating trending data.", e)
                }
            }
        }

//        coroutineScope.launch {
//            val d = trendingUpdater.getTrendingData("technology", "lemmy.world")
//
//            app.log.info("data: ${Json.encodeToString(d)}")
//        }

        app.developmentMode
        if (Config.IS_DEBUG) {
            app.log.debug("Initializing...")
        }
        apiRoutes.initialize()
    }
}