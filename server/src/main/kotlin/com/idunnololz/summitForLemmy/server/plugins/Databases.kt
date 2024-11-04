package com.idunnololz.summitForLemmy.server.plugins

import io.ktor.server.application.*
import org.jetbrains.exposed.sql.Database
import org.jetbrains.exposed.sql.transactions.transaction

fun Application.configureDatabases() {
    Database.connect(
        "jdbc:postgresql://localhost:5432/summit_db",
        user = "postgres",
        password = "hahaha"
    )

    transaction {
        exec("")
    }
}