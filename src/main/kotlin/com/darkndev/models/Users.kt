package com.darkndev.models

import org.jetbrains.exposed.sql.Table

object Users : Table() {
    val id = integer("id").autoIncrement()
    val emailId = varchar("email", 64)
    val password = varchar("password", 64)
    val salt = varchar("salt", 64)
    val verified = bool("verified")

    override val primaryKey = PrimaryKey(id)
}