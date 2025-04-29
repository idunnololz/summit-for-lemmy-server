package com.idunnololz.summitForLemmy.server.db

import com.idunnololz.summitForLemmy.server.lemmyStats.db.CommunityStatsTable
import com.idunnololz.summitForLemmy.server.presets.db.PresetEntityTable
import com.idunnololz.summitForLemmy.server.utils.toDumbLogger
import com.zaxxer.hikari.HikariConfig
import com.zaxxer.hikari.HikariDataSource
import io.ktor.server.application.Application
import io.ktor.util.logging.KtorSimpleLogger
import org.flywaydb.core.Flyway
import org.flywaydb.core.api.FlywayException
import org.jetbrains.exposed.sql.Database
import org.jetbrains.exposed.sql.SchemaUtils
import org.jetbrains.exposed.sql.SchemaUtils.createIndex
import org.jetbrains.exposed.sql.SchemaUtils.sortTablesByReferences
import org.jetbrains.exposed.sql.Table
import org.jetbrains.exposed.sql.exists
import org.jetbrains.exposed.sql.transactions.transaction
import javax.inject.Inject
import javax.sql.DataSource

class DatabaseHelper @Inject constructor(
  private val application: Application
) {

  private val logger = KtorSimpleLogger("DatabaseHelper").toDumbLogger()

  fun connectDatabase() {
    val datasource = hikariDataSource()
    migrate(datasource)
    Database.connect(datasource)

    transaction {
      // Prints the CREATE table statements necessary to create the table
      // When adding a table, add the table to this list and have it generate the CREATE statement. Then copy and paste
      // the CREATE statement to its own create table file in resources/db/migration/V1__Create_<table_name>.sql
      logger.info(
        "Table CREATE statements:\n" +
        createStatements(CommunityStatsTable, PresetEntityTable).joinToString("\n") { "$it;"}
      )
    }
  }

  private fun hikariDataSource(): HikariDataSource {
    val username = application.environment.config.propertyOrNull(
      "ktor.deployment.postgres.username",
    )?.getString()
    val password = application.environment.config.propertyOrNull(
      "ktor.deployment.postgres.password",
    )?.getString()

    requireNotNull(username) { "Postgres username is not specified." }
    requireNotNull(password) { "Postgres password is not specified." }

    return HikariDataSource(
      HikariConfig().apply {
        driverClassName = "org.postgresql.Driver"
        jdbcUrl = "jdbc:postgresql://localhost:5432/summit_db"
        maximumPoolSize = 3
        isAutoCommit = true
        transactionIsolation = "TRANSACTION_REPEATABLE_READ"
        this.username = username
        this.password = password
        validate()
      }
    )
  }

  /**
   * Based on [SchemaUtils.createStatements] however that method only returns the statements for tables not already
   * created. This function always returns the statements.
   */
  private fun createStatements(vararg tables: Table): List<String> {
    if (tables.isEmpty()) return emptyList()

    val toCreate = sortTablesByReferences(tables.toList())
    val alters = arrayListOf<String>()
    return toCreate.flatMap { table ->
      val (create, alter) = table.ddl.partition { it.startsWith("CREATE ") }
      val indicesDDL = table.indices.flatMap { createIndex(it) }
      alters += alter
      create + indicesDDL
    } + alters
  }

  private fun migrate(dataSource: DataSource) {
    try {
      val flyway = Flyway.configure()
        .dataSource(dataSource)
        .baselineOnMigrate(true)
        .load()
      flyway.migrate()
    } catch (e: FlywayException) {
      logger.info("Failed to migrate database")
      logger.info(e.stackTraceToString())
    }
  }
}