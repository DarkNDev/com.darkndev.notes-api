package com.darkndev.routes

import com.darkndev.data.UserDao
import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import io.ktor.server.util.*

fun Route.verify(
    userDao: UserDao
) {
    get("/verify") {
        val emailId = call.parameters.getOrFail("emailId")

        val user = userDao.getUserByUsername(emailId)
            ?: return@get call.respond(HttpStatusCode.Conflict, "Invalid user")

        if (user.verified)
            call.respond(HttpStatusCode.OK, "User already verified")
        else {
            val status = userDao.verifyUser(emailId)
            if (status)
                call.respond(HttpStatusCode.OK, "User verified")
            else
                call.respond(HttpStatusCode.OK, "Server error")
        }
    }
}