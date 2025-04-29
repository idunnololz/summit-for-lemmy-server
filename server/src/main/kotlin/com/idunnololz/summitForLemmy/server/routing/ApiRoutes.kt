package com.idunnololz.summitForLemmy.server.routing

import com.idunnololz.summitForLemmy.server.auth.AuthHelper
import com.idunnololz.summitForLemmy.server.dataGatherer.DataGatherer
import com.idunnololz.summitForLemmy.server.lemmyStats.LemmyStatsController
import com.idunnololz.summitForLemmy.server.localStorage.LocalStorageManager
import com.idunnololz.summitForLemmy.server.presets.PresetsController
import com.idunnololz.summitForLemmy.server.taskManager.TaskManagerController
import io.ktor.server.application.Application
import io.ktor.server.application.install
import io.ktor.server.auth.Authentication
import io.ktor.server.auth.authenticate
import io.ktor.server.auth.jwt.JWTPrincipal
import io.ktor.server.auth.jwt.jwt
import io.ktor.server.http.content.staticFiles
import io.ktor.server.plugins.origin
import io.ktor.server.plugins.ratelimit.RateLimit
import io.ktor.server.plugins.ratelimit.RateLimitName
import io.ktor.server.plugins.ratelimit.rateLimit
import io.ktor.server.response.respond
import io.ktor.server.response.respondRedirect
import io.ktor.server.routing.get
import io.ktor.server.routing.patch
import io.ktor.server.routing.post
import io.ktor.server.routing.route
import io.ktor.server.routing.routing
import java.io.File
import javax.inject.Inject
import javax.inject.Singleton
import kotlin.time.Duration.Companion.seconds

@Singleton
class ApiRoutes
@Inject
constructor(
  private val app: Application,
  private val lemmyStatsController: LemmyStatsController,
  private val taskManagerController: TaskManagerController,
  private val dataGatherer: DataGatherer,
  private val presetsController: PresetsController,
  private val authHelper: AuthHelper,
  private val localStorageManager: LocalStorageManager,
) {

  companion object {
    private const val AUTH_ADMIN = "auth-admin"
  }

  fun initialize() {
    app.install(RateLimit) {
      register(RateLimitName("post_preset")) {
        rateLimiter(limit = 1, refillPeriod = 60.seconds)
        requestKey { call -> call.request.origin.remoteAddress }
      }
      register(RateLimitName("patch_preset")) {
        rateLimiter(limit = 10, refillPeriod = 60.seconds)
        requestKey { call -> call.request.origin.remoteAddress }
      }
    }
    app.install(Authentication) {
      jwt(AUTH_ADMIN) {
        verifier {
          authHelper.makeJWTVerifier()
        }

//        println("generateTokenPair: ${authHelper.generateTokenPair(0).token}")

        validate { token ->
          if (token.payload.expiresAt.time > System.currentTimeMillis()) {
            JWTPrincipal(token.payload)
          } else {
            null
          }
        }
      }
    }

    localStorageManager.mkdirs()

    app.routing {
      staticFiles("/public", localStorageManager.publicDir)
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
        get("/preset") {
          presetsController.get(call)
        }
        rateLimit(RateLimitName("post_preset")) {
          post("/preset") {
            presetsController.insert(call)
          }
        }
//        rateLimit(RateLimitName("patch_preset")) {
//          patch("/preset") {
//            presetsController.update(call)
//          }
//        }

        authenticate(AUTH_ADMIN) {
          staticFiles("/auth/public", localStorageManager.authPublicDir)
        }

        route("/preset") {
          authenticate(AUTH_ADMIN) {
            staticFiles("/auth/public", localStorageManager.authPublicDir)
            get("/all") {
              presetsController.getAll(call)
            }
            post("/approve") {
              presetsController.approve(call)
            }
          }
        }
      }
    }
  }
}
