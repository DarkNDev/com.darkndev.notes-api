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
            val userId = principal?.getClaim("userId", String::class)?.toInt()
                ?: return@get call.respond(HttpStatusCode.Unauthorized)
            val notes = noteDao.allNotes(userId)
            call.respond(HttpStatusCode.OK, notes)
        }

        post("/update") {
            val principal = call.principal<JWTPrincipal>()
            val userId = principal?.getClaim("userId", String::class)?.toInt()
                ?: return@post call.respond(HttpStatusCode.Unauthorized)
            val localNotesRequest = call.receive<List<NoteRequest>>()
            val status = updateNotes(userId, localNotesRequest, noteDao)
            call.respond(status.first, status.second)
        }
    }
}

suspend fun updateNotes(userId: Int, localNotes: List<NoteRequest>, noteDao: NoteDao): Pair<HttpStatusCode, String> {
    val serverNotes = noteDao.allNotes(userId)
    if (localNotes != serverNotes) {
        val status = noteDao.update(userId, localNotes)
        return if (status) HttpStatusCode.OK to "Notes Updated" else HttpStatusCode.Conflict to "Update Failed"
    }
    return Pair(HttpStatusCode.OK, "Update Not Needed")
}