package com.darkndev.routes

import com.darkndev.data.NoteDao
import com.darkndev.models.NoteRequest
import io.ktor.http.*
import io.ktor.server.response.*
import io.ktor.server.application.*
import io.ktor.server.auth.*
import io.ktor.server.auth.jwt.*
import io.ktor.server.request.*
import io.ktor.server.routing.*

fun Route.noteResponse(noteDao: NoteDao) {
    authenticate {
        get("/notes") {
            val principal = call.principal<JWTPrincipal>()
            val userId = principal?.getClaim("userId", String::class)!!.toInt()
            val notes = noteDao.allNotes(userId)
            call.respond(HttpStatusCode.OK, notes)
        }

        post("/sync") {
            val principal = call.principal<JWTPrincipal>()
            val userId = principal?.getClaim("userId", String::class)!!.toInt()
            val localNotesRequest = call.receive<List<NoteRequest>>()
            val status = sync(userId, localNotesRequest, noteDao)
            if (status.first)
                call.respond(HttpStatusCode.OK, status.second)
            else
                call.respond(HttpStatusCode.NonAuthoritativeInformation, status.second)
        }

        post("/add") {
            val principal = call.principal<JWTPrincipal>()
            val userId = principal?.getClaim("userId", String::class)!!.toInt()
            val addNoteRequest = call.receive<NoteRequest>()
            val status = noteDao.addNote(userId, addNoteRequest)
            if (status)
                call.respond(HttpStatusCode.OK, "Note Added")
            else
                call.respond(HttpStatusCode.NonAuthoritativeInformation, "Failed")
        }

        post("/edit") {
            val principal = call.principal<JWTPrincipal>()
            val userId = principal?.getClaim("userId", String::class)!!.toInt()
            val editNoteRequest = call.receive<NoteRequest>()
            val status = noteDao.editNote(userId, editNoteRequest)
            if (status)
                call.respond(HttpStatusCode.OK, "Note Updated")
            else
                call.respond(HttpStatusCode.NonAuthoritativeInformation, "Failed")
        }

        post("/delete") {
            val principal = call.principal<JWTPrincipal>()
            val userId = principal?.getClaim("userId", String::class)!!.toInt()
            val deleteNoteRequest = call.receive<NoteRequest>()
            val status = noteDao.deleteNote(userId, deleteNoteRequest)
            if (status)
                call.respond(HttpStatusCode.OK, "Note Deleted")
            else
                call.respond(HttpStatusCode.NonAuthoritativeInformation, "Failed")
        }
    }
}

suspend fun sync(userId: Int, localNotes: List<NoteRequest>, noteDao: NoteDao): Pair<Boolean, String> {
    val serverNotes = noteDao.allNotes(userId)
    if (localNotes != serverNotes) {
        val status = noteDao.sync(userId, localNotes)
        return if (status) true to "Sync Successful" else false to "Sync Failed"
    }
    return Pair(true, "Sync Not Needed")
}