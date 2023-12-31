package com.darkndev.models

import org.jetbrains.exposed.sql.Table

object Notes : Table() {
    val id = integer("id")
    val userId = integer("userId")
    val title = varchar("title", 128)
    val content = varchar("content", 4096)

    override val primaryKey: PrimaryKey = PrimaryKey(id)
}