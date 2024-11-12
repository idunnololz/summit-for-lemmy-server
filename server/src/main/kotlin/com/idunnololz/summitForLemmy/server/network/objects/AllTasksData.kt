package com.idunnololz.summitForLemmy.server.network.objects

import com.idunnololz.summitForLemmy.server.taskManager.TaskInfo
import kotlinx.datetime.Instant
import kotlinx.serialization.Serializable

@Serializable
class AllTasksData(
    val taskInfo: List<TaskInfo>,
    val currentTime: Instant,
)