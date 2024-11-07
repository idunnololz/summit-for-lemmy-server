package com.idunnololz.summitForLemmy.server.taskManager

import kotlinx.datetime.Clock
import kotlinx.datetime.Instant
import kotlinx.serialization.Serializable
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class TaskManagerController @Inject constructor(
    private val taskManager: TaskManager,
) {
    suspend fun getAllTasksInfo(): AllTasksData {
        return AllTasksData(
            taskInfo = taskManager.allTasks.map {
                it.toTaskInfo()
            },
            currentTime = Clock.System.now(),
        )
    }
}

@Serializable
class AllTasksData(
    val taskInfo: List<TaskInfo>,
    val currentTime: Instant,
)