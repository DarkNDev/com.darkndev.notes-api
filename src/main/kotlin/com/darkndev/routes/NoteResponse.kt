package com.darkndev.routes

import com.darkndev.data.NoteDao
import com.darkndev.models.Note
import com.darkndev.models.NoteRequest
import io.ktor.http.*
import io.ktor.server.response.*
import io.ktor.server.application.*
import io.ktor.server.auth.*
import io.ktor.server.auth.jwt.*
import io.ktor.server.request.*
import io.ktor.server.routing.*

fun Route.noteResponse(noteDaoInstance: NoteDao) {
    authenticate {
        get("/notes") {
            val principal = call.principal<JWTPrincipal>()
            val userId = principal?.getClaim("userId", String::class)!!.toInt()
            val notes = noteDaoInstance.allNotes(userId).map {
                NoteRequest(
                    id = it.id,
                    title = it.title,
                    content = it.content
                )
            }
            call.respond(HttpStatusCode.OK, notes)
        }

        post("/sync") {
            val principal = call.principal<JWTPrincipal>()
            val userId = principal?.getClaim("userId", String::class)!!.toInt()
            val localNotesRequest = call.receive<List<NoteRequest>>()
            println("TAG: $userId")
            println("TAG: $localNotesRequest")
            val status = sync(userId, localNotesRequest, noteDaoInstance)
            if (status.first)
                call.respond(HttpStatusCode.OK, status.second)
            else
                call.respond(HttpStatusCode.NonAuthoritativeInformation, status.second)
        }
    }
}

suspend fun sync(userId: Int, localNotesRequest: List<NoteRequest>, noteDao: NoteDao): Pair<Boolean, String> {
    val serverNotes = noteDao.allNotes(userId)
    val localNotes = localNotesRequest.map {
        Note(
            id = it.id,
            title = it.title,
            userId = userId,
            content = it.content
        )
    }
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