package com.darkndev.data

import com.darkndev.data.NoteDatabaseFactory.noteQuery
import com.darkndev.models.Note
import com.darkndev.models.Notes
import com.darkndev.models.Notes.content
import com.darkndev.models.Notes.id
import com.darkndev.models.Notes.title
import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.SqlExpressionBuilder.eq
import org.jetbrains.exposed.sql.SqlExpressionBuilder.inList

class NoteDao {

    private fun resultRowToNote(row: ResultRow) = Note(
        id = row[id],
        title = row[title],
        content = row[content]
    )

    suspend fun allNotes(): List<Note> = noteQuery {
        Notes.selectAll().map(::resultRowToNote)
    }

    suspend fun addNote(note: Note): Boolean = noteQuery {
        val insertStatement = Notes.insert {
            it[id] = note.id
            it[title] = note.title
            it[content] = note.content
        }
        insertStatement.resultedValues?.singleOrNull()?.let(::resultRowToNote) == note
    }

    suspend fun editNote(note: Note): Boolean = noteQuery {
        Notes.update({ id eq note.id }) {
            it[title] = note.title
            it[content] = note.content
        } > 0
    }

    suspend fun deleteNote(note: Note): Boolean = noteQuery {
        Notes.deleteWhere { id eq note.id } > 0
    }

    suspend fun addSelectedNotes(notes: List<Note>) = noteQuery {
        val result = Notes.batchInsert(notes) {
            this[id] = it.id
            this[title] = it.title
            this[content] = it.content
        }
        result.size == notes.size
    }

    suspend fun updateSelectedNotes(notes: List<Note>) = noteQuery {
        val result = Notes.batchReplace(notes, true) {
            this[id] = it.id
            this[title] = it.title
            this[content] = it.content
        }
        result.size == notes.size
    }

    suspend fun deleteSelectedNotes(notes: List<Note>) = noteQuery {
        Notes.deleteWhere {
            id inList notes.map { it.id }
        } == notes.size
    }

    suspend fun deleteAllNotes() = noteQuery {
        val count = Notes.selectAll().count().toInt()
        Notes.deleteAll() == count
    }
}