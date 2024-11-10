package com.idunnololz.summitForLemmy.server.routing

import com.idunnololz.summitForLemmy.server.dataGatherer.DataGatherer
import com.idunnololz.summitForLemmy.server.taskManager.TaskManager
import com.idunnololz.summitForLemmy.server.taskManager.TaskManagerController
import com.idunnololz.summitForLemmy.server.taskManager.toTaskInfo
import com.idunnololz.summitForLemmy.server.trending.TrendingController
import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.http.content.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ApiRoutes @Inject constructor(
    private val app: Application,
    private val trendingController: TrendingController,
    private val taskManagerController: TaskManagerController,
    private val dataGatherer: DataGatherer,
) {
    fun initialize() {
        app.routing {
            get("/") {
                call.respondRedirect("https://summit.idunnololz.com/")
            }
            get("/get-community-trend-data") {
                val communityName = call.queryParameters["communityName"]
                val instance = call.queryParameters["instance"]

                if (communityName == null) {
                    call.respond(HttpStatusCode.UnprocessableEntity, "communityName missing.")
                    return@get
                }
                if (instance == null) {
                    call.respond(HttpStatusCode.UnprocessableEntity, "instance missing.")
                    return@get
                }

                val communityTrendData = trendingController.getCommunityTrendData(communityName, instance)

                if (communityTrendData == null) {
                    call.respond(HttpStatusCode.NotFound, "No trend data for that community/instance.")
                    return@get
                }

                call.respond(communityTrendData)
            }
            get("/tasks") {
                call.respond(taskManagerController.getAllTasksInfo())
            }
            get("/get-all-community-trend-data") {
                val data = trendingController.getAllCommunityTrendData()

                if (data != null) {
                    call.respond(data)
                } else {
                    call.respond(HttpStatusCode.NotFound, "no trend data")
                }
            }
            get("/run-data-gatherer") {
                call.respond("Ok")

                dataGatherer.updateCommunitiesData()
            }
            get("/run-update-trending") {
                call.respond("Ok")

                trendingController.updateTrending()
            }
            // Static plugin. Try to access `/static/index.html`
            staticResources("/static", "static")
        }
    }
}