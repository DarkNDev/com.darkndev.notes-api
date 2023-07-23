package com.darkndev.routes

import com.darkndev.data.UserDao
import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.config.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import io.ktor.server.util.*
import org.apache.commons.validator.routines.EmailValidator

fun Route.resend(
    userDao: UserDao,
    config: ApplicationConfig
) {
    get("/resend") {
        val emailId = call.parameters.getOrFail("emailId")

        if (!EmailValidator.getInstance().isValid(emailId)) {
            call.respond(HttpStatusCode.Conflict, "Enter valid email")
            return@get
        }

        val user = userDao.getUserByUsername(emailId)
            ?: return@get call.respond("Invalid user")

        if (user.verified) {
            call.respond(HttpStatusCode.OK, "User already verified")
        } else {
            val emailSent = sendVerificationEmail(emailId, config)
            if (emailSent) {
                call.respond(HttpStatusCode.OK, "Verification email sent")
            } else {
                call.respond(HttpStatusCode.Conflict, "Server Error")
            }
        }
    }
}