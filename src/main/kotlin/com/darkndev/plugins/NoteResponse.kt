package com.darkndev.plugins

import com.darkndev.data.NoteDao
import com.darkndev.models.Note
import io.ktor.http.*
import io.ktor.serialization.kotlinx.*
import io.ktor.serialization.kotlinx.json.*
import io.ktor.server.response.*
import io.ktor.server.application.*
import io.ktor.server.plugins.contentnegotiation.ContentNegotiation
import io.ktor.server.request.*
import io.ktor.server.routing.*
import io.ktor.server.websocket.*
import kotlinx.serialization.json.Json
import java.time.Duration

fun Application.noteResponse() {
    install(ContentNegotiation) {
        json()
    }
    install(WebSockets) {
        contentConverter = KotlinxWebsocketSerializationConverter(Json)
        pingPeriod = Duration.ofSeconds(15)
        timeout = Duration.ofSeconds(15)
        maxFrameSize = Long.MAX_VALUE
        masking = false
    }
    val noteDaoInstance = NoteDao()

    routing {

        get("/") {
            call.respond("Welcome to Notes Api!!")
        }

        get("/notes") {
            val notes = noteDaoInstance.allNotes()
            call.respond(HttpStatusCode.OK, notes)
        }

        post("/sync") {
            val localNotes = call.receive<List<Note>>()
            val status = sync(localNotes, noteDaoInstance)
            if (status.first)
                call.respond(HttpStatusCode.OK, status.second)
            else
                call.respond(HttpStatusCode.NonAuthoritativeInformation, status.second)
        }

        /*post("/notes/add") {
            val note = call.receive<Note>()
            if (!noteDaoInstance.addNote(note))
                call.respond(HttpStatusCode.NonAuthoritativeInformation, "Error: Add Failed")
            else
                call.respond(HttpStatusCode.OK, "Success")
        }

        post("/notes/edit") {
            val note = call.receive<Note>()
            val success = noteDaoInstance.editNote(note)
            if (success) call.respond(HttpStatusCode.OK, "Success")
            else call.respond(HttpStatusCode.NonAuthoritativeInformation, "Error: Edit Failed")
        }

        post("/notes/delete") {
            val note = call.receive<Note>()
            val success = noteDaoInstance.deleteNote(note)
            if (success) call.respond(HttpStatusCode.OK, "Success")
            else call.respond(HttpStatusCode.NonAuthoritativeInformation, "Error: Delete Failed")
        }

        post("/notes/delete-all") {
            val status = noteDaoInstance.deleteAllNotes()
            if (status) call.respond(HttpStatusCode.OK, "Success")
            else call.respond(HttpStatusCode.NonAuthoritativeInformation, "Error: Delete Failed")
        }

        post("/notes/add-all") {
            val notes = call.receive<List<Note>>()
            val status = noteDaoInstance.addAllNotes(notes)
            if (status) call.respond(HttpStatusCode.OK, "Success")
            else call.respond(HttpStatusCode.NonAuthoritativeInformation, "Error: Add Failed")
        }*/
    }
}

suspend fun sync(localNotes: List<Note>, noteDao: NoteDao): Pair<Boolean, String> {
    val serverNotes = noteDao.allNotes()
    if (localNotes != serverNotes) {
        val newInsertServerNotes = localNotes.filter { localNote ->
            val serverNote =
                serverNotes.find { serverNote -> localNote.id == serverNote.id }
            serverNote == null
        }

        val updateServerNotes = localNotes.filter { localNote ->
            val serverNote =
                serverNotes.find { serverNote -> localNote.id == serverNote.id }
            serverNote != null && serverNote != localNote
        }

        val deleteServerNotes = serverNotes.filter { serverNote ->
            val localNote = localNotes.find { localNote -> serverNote.id == localNote.id }
            localNote == null
        }

        val addStatus = noteDao.addSelectedNotes(newInsertServerNotes)
        val updateStatus = noteDao.updateSelectedNotes(updateServerNotes)
        val deleteStatus = noteDao.deleteSelectedNotes(deleteServerNotes)
        val status = addStatus && updateStatus && deleteStatus
        return if (status) true to "Sync Successful: Added: ${newInsertServerNotes.size} Updated: ${updateServerNotes.size} Deleted: ${deleteServerNotes.size}" else false to "Sync Failed"
    }
    return Pair(true, "Sync Not Needed")
}