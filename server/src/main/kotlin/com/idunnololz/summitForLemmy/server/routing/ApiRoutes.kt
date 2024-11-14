package com.idunnololz.summitForLemmy.server.routing

import com.idunnololz.summitForLemmy.server.dataGatherer.DataGatherer
import com.idunnololz.summitForLemmy.server.taskManager.TaskManagerController
import com.idunnololz.summitForLemmy.server.lemmyStats.LemmyStatsController
import io.ktor.server.application.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ApiRoutes @Inject constructor(
    private val app: Application,
    private val lemmyStatsController: LemmyStatsController,
    private val taskManagerController: TaskManagerController,
    private val dataGatherer: DataGatherer,
) {
    fun initialize() {
        app.routing {
            get("/") {
                call.respondRedirect("https://summit.idunnololz.com/")
            }
            route("/v1") {
                get("/community-stats") {
                    lemmyStatsController.getCommunityTrendData(call)
                }
                get("/tasks") {
                    taskManagerController.getAllTasksInfo(call)
                }
                get("/all-community-trend-data") {
                    lemmyStatsController.getAllCommunityTrendData(call)
                }
                get("/top-trending-communities") {
                    lemmyStatsController.getTopTrendingCommunities(call)
                }
                get("/hot-communities") {
                    lemmyStatsController.getHotCommunities(call)
                }
                get("/community-suggestions") {
                    lemmyStatsController.getCommunitySuggestions(call)
                }
                post("/run-data-gatherer") {
                    call.respond("Ok")

                    dataGatherer.updateCommunitiesData()
                }
                post("/run-update-trending") {
                    lemmyStatsController.updateTrending(call)
                }
            }
        }
    }
}