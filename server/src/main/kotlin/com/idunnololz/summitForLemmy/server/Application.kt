package com.idunnololz.summitForLemmy.server

import com.idunnololz.summitForLemmy.server.plugins.configureDatabases
import com.idunnololz.summitForLemmy.server.plugins.configureSerialization
import io.ktor.server.application.*

fun main(args: Array<String>) {
    io.ktor.server.netty.EngineMain.main(args)
}

@Suppress("unused") // Referenced in application.yaml
fun Application.module() {
    configureSerialization()
    configureDatabases()

    val server = DaggerServer.builder()
        .applicationModule(ApplicationModule(this))
        .build()

    server.serverInitializer().initialize()
}