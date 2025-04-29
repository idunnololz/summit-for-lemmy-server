package com.idunnololz.summitForLemmy.server

import com.idunnololz.summitForLemmy.server.plugins.configureSerialization
import com.idunnololz.summitForLemmy.server.presets.db.PresetEntityTable
import io.ktor.server.application.Application
import org.jetbrains.exposed.sql.SchemaUtils

fun main(args: Array<String>) {
  println(args.joinToString())
  Config.isDebug = args.none { it == "--release" }

  println(System.getProperty("jwt.access.secret"))

  io.ktor.server.netty.EngineMain.main(args)
}

@Suppress("unused") // Referenced in application.yaml
fun Application.module() {
  configureSerialization()

  val server =
    DaggerServer.builder()
      .applicationModule(ApplicationModule(this))
      .build()

  server.serverInitializer().initialize(this)
}
