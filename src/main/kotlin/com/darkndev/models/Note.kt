package com.darkndev.models

import kotlinx.serialization.Serializable
import org.jetbrains.exposed.sql.Table

@Serializable
data class Note(
    val id: Int,
    val title: String,
    val content: String
) : java.io.Serializable

object Notes : Table() {
    val id = integer("id")
    val title = varchar("title", 128)
    val content = varchar("content", 4096)

    override val primaryKey: PrimaryKey = PrimaryKey(id)
}