package com.idunnololz.summitForLemmy.server.taskManager

import com.idunnololz.summitForLemmy.server.network.objects.AllTasksData
import io.ktor.server.response.*
import io.ktor.server.routing.*
import kotlinx.datetime.Clock
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class TaskManagerController @Inject constructor(
    private val taskManager: TaskManager,
) {
    suspend fun getAllTasksInfo(call: RoutingCall) {
        call.respond(
            AllTasksData(
                taskInfo = taskManager.allTasks.map {
                    it.toTaskInfo()
                },
                currentTime = Clock.System.now(),
            )
        )
    }
}