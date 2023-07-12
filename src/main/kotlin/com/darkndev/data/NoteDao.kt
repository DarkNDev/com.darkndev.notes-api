package com.darkndev.data

import com.darkndev.data.NoteDatabaseFactory.noteQuery
import com.darkndev.models.NoteRequest
import com.darkndev.models.Notes
import com.darkndev.models.Notes.content
import com.darkndev.models.Notes.id
import com.darkndev.models.Notes.title
import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.SqlExpressionBuilder.eq

class NoteDao {

    private fun resultRowToNote(row: ResultRow) = NoteRequest(
        id = row[id],
        title = row[title],
        content = row[content]
    )

    suspend fun allNotes(userId: Int): List<NoteRequest> = noteQuery {
        Notes.select { Notes.userId eq userId }.map(::resultRowToNote)
    }

    suspend fun addNote(userId: Int, note: NoteRequest): Boolean = noteQuery {
        val insertStatement = Notes.insert {
            it[id] = note.id
            it[Notes.userId] = userId
            it[title] = note.title
            it[content] = note.content
        }
        insertStatement.resultedValues?.singleOrNull()?.let(::resultRowToNote) == note
    }

    suspend fun editNote(userId: Int, note: NoteRequest): Boolean = noteQuery {
        Notes.update({ id eq note.id and (Notes.userId eq userId) }) {
            it[title] = note.title
            it[content] = note.content
        } > 0
    }

    suspend fun deleteNote(userId: Int, note: NoteRequest): Boolean = noteQuery {
        Notes.deleteWhere { id eq note.id and (Notes.userId eq userId) } > 0
    }

    suspend fun sync(userId: Int, notes: List<NoteRequest>) = noteQuery {
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