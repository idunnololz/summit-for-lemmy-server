package com.idunnololz.summitForLemmy.server.taskManager

import com.idunnololz.summitForLemmy.server.utils.CoroutineScopeFactory
import dev.inmo.krontab.builder.buildSchedule
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class TaskManager
@Inject
constructor(
  private val coroutineScopeFactory: CoroutineScopeFactory,
) {
  private val tasks = mutableListOf<Task>()

  fun schedule(taskName: String, kronTabString: String, task: suspend () -> Unit): Task {
    val kronScheduler = dev.inmo.krontab.buildSchedule(kronTabString)

    return Task(
      taskName,
      coroutineScopeFactory.create(),
      kronScheduler,
      task,
    ).also {
      tasks.add(it)
      it.run()
    }
  }

  val allTasks
    get() = tasks
}
