package com.darkndev.data

import com.darkndev.models.Notes
import com.darkndev.models.Users
import io.ktor.server.config.*
import kotlinx.coroutines.Dispatchers
import org.jetbrains.exposed.sql.Database
import org.jetbrains.exposed.sql.SchemaUtils
import org.jetbrains.exposed.sql.transactions.experimental.newSuspendedTransaction
import org.jetbrains.exposed.sql.transactions.transaction

object NoteDatabaseFactory {
    fun init(config: ApplicationConfig) {
        val url = config.property("postgres.url").getString()
        val name = config.property("postgres.name").getString()
        val port = config.property("postgres.port").getString()
        val driver = config.property("postgres.driver").getString()
        val username = config.property("postgres.user").getString()
        val password = config.property("postgres.password").getString()
        val connectionUrl = "jdbc:postgresql://$url:$port/$name"
        Database.connect(
            url = connectionUrl,
            driver = driver,
            user = username,
            password = password
        )
        transaction {
            SchemaUtils.create(Notes)
            SchemaUtils.create(Users)
        }
    }

    suspend fun <T> noteQuery(block: suspend () -> T): T =
        newSuspendedTransaction(Dispatchers.IO) { block() }

    suspend fun <T> userQuery(block: suspend () -> T): T =
        newSuspendedTransaction(Dispatchers.IO) { block() }
}