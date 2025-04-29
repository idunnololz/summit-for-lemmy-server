package com.idunnololz.summitForLemmy.server.taskManager

import com.idunnololz.summitForLemmy.server.network.objects.AllTasksData
import io.ktor.server.response.respond
import io.ktor.server.routing.RoutingCall
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.datetime.Clock

@Singleton
class TaskManagerController
@Inject
constructor(
  private val taskManager: TaskManager,
) {
  suspend fun getAllTasksInfo(call: RoutingCall) {
    call.respond(
      AllTasksData(
        taskInfo =
        taskManager.allTasks.map {
          it.toTaskInfo()
        },
        currentTime = Clock.System.now(),
      ),
    )
  }
}
