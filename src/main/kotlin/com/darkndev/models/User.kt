package com.darkndev.models

import kotlinx.serialization.Serializable
import org.jetbrains.exposed.sql.Table

@Serializable
data class User(
    val id: Int,
    val username: String,
    val password: String,
    val salt: String,
    val updated: Long
)

object Users : Table() {
    val id = integer("id").autoIncrement()
    val username = varchar("username", 64)
    val password = varchar("password", 64)
    val salt = varchar("salt", 64)
    val updated = long("updated")

    override val primaryKey = PrimaryKey(id)
}