package com.idunnololz.summitForLemmy.server.plugins

import io.ktor.server.application.*
import org.jetbrains.exposed.sql.Database
import org.jetbrains.exposed.sql.transactions.transaction

fun Application.configureDatabases() {
    val username = environment.config.propertyOrNull("ktor.deployment.postgres.username")?.getString()
    val password = environment.config.propertyOrNull("ktor.deployment.postgres.password")?.getString()

    requireNotNull(username) { "Postgres username is not specified."}
    requireNotNull(password) { "Postgres password is not specified."}

    Database.connect(
        "jdbc:postgresql://localhost:5432/summit_db",
        user = username,
        password = password,
    )

    transaction {
        exec("")
    }
}