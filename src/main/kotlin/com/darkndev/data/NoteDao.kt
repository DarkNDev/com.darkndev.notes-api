package com.darkndev.data

import com.darkndev.data.NoteDatabaseFactory.noteQuery
import com.darkndev.models.Note
import com.darkndev.models.Notes
import com.darkndev.models.Notes.content
import com.darkndev.models.Notes.id
import com.darkndev.models.Notes.title
import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.SqlExpressionBuilder.eq

class NoteDao {

    private fun resultRowToNote(row: ResultRow) = Note(
        id = row[id],
        title = row[title],
        content = row[content]
    )

    suspend fun allNotes(userId: Int): List<Note> = noteQuery {
        Notes.select { Notes.userId eq userId }.map(::resultRowToNote)
    }

    suspend fun update(userId: Int, notes: List<Note>) = noteQuery {
        Notes.deleteWhere {
            Notes.userId eq userId
        }
        val result = Notes.batchInsert(notes) {
            this[id] = it.id
            this[Notes.userId] = userId
            this[title] = it.title
            this[content] = it.content
        }
        result.size == notes.size
    }
}