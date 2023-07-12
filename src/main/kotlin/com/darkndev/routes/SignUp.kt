package com.darkndev.routes

import com.darkndev.data.UserDao
import com.darkndev.models.AuthRequest
import com.darkndev.security.hashing.SHA256HashingService
import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*

fun Route.signUp(
    hashingService: SHA256HashingService
) {
    val userDao = UserDao()
    post("/signup") {
        val request = call.receive<AuthRequest>()

        val areFieldsBlank = request.username.isBlank() || request.password.isBlank()

        val isPasswordTooShort = request.password.length < 8

        if (areFieldsBlank || isPasswordTooShort) {
            call.respond(HttpStatusCode.Conflict, "Check the Fields")
            return@post
        }

        val existingUser = userDao.getUserByUsername(request.username)
        if (existingUser != null) {
            call.respond(HttpStatusCode.Conflict, "User Exists")
            return@post
        }

        val saltedHash = hashingService.generateSaltedHash(request.password)
        val user = userDao.insertUser(request.username, saltedHash.hash, saltedHash.salt)
        if (user == null) {
            call.respond(HttpStatusCode.Conflict, "Server error")
            return@post
        }
        call.respond(HttpStatusCode.OK, "User Created")
    }
}