package com.darkndev.plugins

import com.darkndev.data.NoteDao
import com.darkndev.data.UserDao
import com.darkndev.routes.noteResponse
import com.darkndev.routes.signIn
import com.darkndev.routes.signUp
import com.darkndev.security.hashing.SHA256HashingService
import com.darkndev.security.token.JwtTokenService
import com.darkndev.security.token.TokenConfig
import io.ktor.http.*
import io.ktor.serialization.kotlinx.json.*
import io.ktor.server.application.*
import io.ktor.server.plugins.contentnegotiation.*
import io.ktor.server.response.*
import io.ktor.server.routing.*

fun Application.configureRouting(
    hashingService: SHA256HashingService,
    tokenService: JwtTokenService,
    tokenConfig: TokenConfig,
    noteDao: NoteDao,
    userDao: UserDao
) {
    install(ContentNegotiation) {
        json()
    }
    routing {
        get("/") {
            call.respond("Welcome to Notes Api!!")
        }
        get("/users") {
            val users = userDao.allUsers()
            call.respond(HttpStatusCode.OK, users)
        }
        signIn(hashingService, tokenService, tokenConfig, userDao)
        signUp(hashingService, userDao)
        noteResponse(noteDao)
    }
}
