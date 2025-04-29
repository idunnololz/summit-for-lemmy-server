package com.idunnololz.summitForLemmy.server.taskManager

import dev.inmo.krontab.KronScheduler
import dev.inmo.krontab.doInfinity
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import kotlinx.datetime.Clock
import kotlinx.datetime.Instant
import kotlinx.serialization.Serializable

class Task(
  val taskName: String,
  private val coroutineScope: CoroutineScope,
  private val kronScheduler: KronScheduler,
  private val task: suspend () -> Unit,
) {
  var lastRunTime: Instant? = null
  var isRunning: Boolean = false
  var lastError: Exception? = null
  var runs = 0

  fun run() {
    coroutineScope.launch {
      kronScheduler.doInfinity {
        lastRunTime = Clock.System.now()
        isRunning = true

        try {
          task()
        } catch (e: CancellationException) {
          throw e
        } catch (e: Exception) {
          lastError = e
        }

        isRunning = false
        runs++
      }
    }
  }

  suspend fun nextTime() = kronScheduler.next()
}

@Serializable
class TaskInfo(
  val taskName: String,
  val lastRunTime: Instant?,
  val nextRunTime: Instant?,
  val isRunning: Boolean,
  val lastError: String?,
  val runs: Int,
)

suspend fun Task.toTaskInfo() = TaskInfo(
  taskName,
  lastRunTime,
  nextTime()?.let {
    Instant.fromEpochMilliseconds(it.unixMillisLong)
  },
  isRunning,
  lastError?.stackTraceToString(),
  runs,
)
