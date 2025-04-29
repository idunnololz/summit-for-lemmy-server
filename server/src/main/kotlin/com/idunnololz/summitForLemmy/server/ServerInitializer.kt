package com.idunnololz.summitForLemmy.server

import com.idunnololz.summitForLemmy.server.auth.AuthHelper
import com.idunnololz.summitForLemmy.server.dataGatherer.DataGatherer
import com.idunnololz.summitForLemmy.server.db.DatabaseHelper
import com.idunnololz.summitForLemmy.server.lemmyStats.TrendingUpdater
import com.idunnololz.summitForLemmy.server.presets.db.PresetEntityTable
import com.idunnololz.summitForLemmy.server.routing.ApiRoutes
import com.idunnololz.summitForLemmy.server.taskManager.TaskManager
import com.idunnololz.summitForLemmy.server.utils.CoroutineScopeFactory
import com.idunnololz.summitForLemmy.server.utils.toDumbLogger
import io.ktor.server.application.Application
import io.ktor.server.application.install
import io.ktor.server.application.log
import io.ktor.server.auth.Authentication
import io.ktor.server.auth.jwt.JWTPrincipal
import io.ktor.server.auth.jwt.jwt
import io.ktor.server.plugins.origin
import io.ktor.server.plugins.ratelimit.RateLimit
import io.ktor.server.plugins.ratelimit.RateLimitName
import io.ktor.util.logging.KtorSimpleLogger
import javax.inject.Inject
import javax.inject.Singleton
import kotlin.time.Duration.Companion.seconds
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.flywaydb.core.Flyway
import org.jetbrains.exposed.sql.SchemaUtils
import org.jetbrains.exposed.sql.transactions.transaction

@Singleton
class ServerInitializer
@Inject
constructor(
  private val app: Application,
  private val coroutineScopeFactory: CoroutineScopeFactory,
  private val taskManager: TaskManager,
  private val dataGatherer: DataGatherer,
  private val trendingUpdater: TrendingUpdater,
  private val apiRoutes: ApiRoutes,
  private val authHelper: AuthHelper,
  private val databaseHelper: DatabaseHelper,
) {
  private val coroutineScope = coroutineScopeFactory.create()

  fun initialize(application: Application) = with(application) {

    databaseHelper.connectDatabase()

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
    if (Config.isDebug) {
      app.log.debug("Initializing...")
    }
    apiRoutes.initialize()
  }
}
