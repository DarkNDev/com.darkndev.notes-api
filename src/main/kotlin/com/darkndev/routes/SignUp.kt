package com.darkndev.routes

import com.darkndev.data.UserDao
import com.darkndev.models.AuthRequest
import com.darkndev.security.hashing.SHA256HashingService
import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.config.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import org.apache.commons.mail.DefaultAuthenticator
import org.apache.commons.mail.EmailException
import org.apache.commons.mail.SimpleEmail
import org.apache.commons.validator.routines.EmailValidator

fun Route.signUp(
    hashingService: SHA256HashingService,
    userDao: UserDao,
    config: ApplicationConfig
) {
    post("/signup") {
        val request = call.receive<AuthRequest>()

        val areFieldsBlank = request.emailId.isBlank() || request.password.isBlank()

        val isPasswordTooShort = request.password.length < 8

        if (areFieldsBlank || isPasswordTooShort) {
            call.respond(HttpStatusCode.Conflict, "Invalid email or password")
            return@post
        }

        if (!EmailValidator.getInstance().isValid(request.emailId)) {
            call.respond(HttpStatusCode.Conflict, "Invalid email or password")
            return@post
        }

        val existingUser = userDao.getUserByUsername(request.emailId)
        if (existingUser != null) {
            call.respond(HttpStatusCode.Conflict, "User already exists")
            return@post
        }

        val saltedHash = hashingService.generateSaltedHash(request.password)
        val user = userDao.insertUser(request.emailId, saltedHash.hash, saltedHash.salt)
        if (user == null) {
            call.respond(HttpStatusCode.Conflict, "Server error")
            return@post
        } else {
            val emailSent = sendVerificationEmail(request.emailId, config)
            if (emailSent) {
                call.respond(HttpStatusCode.OK, "User created successfully. Check Inbox and verify email")
            } else {
                call.respond(HttpStatusCode.Conflict, "Server error")
            }
        }
    }
}

fun sendVerificationEmail(toEmail: String, config: ApplicationConfig): Boolean {
    val baseUrl = config.property("verification.url").getString()
    val verificationLink = "$baseUrl/verify?emailId=$toEmail"
    val hostName = config.property("verification.host").getString()
    val fromEmail = config.property("verification.from").getString()
    val fromPassword = config.property("verification.password").getString()

    try {
        val email = SimpleEmail()
        email.hostName = hostName
        email.setSmtpPort(465)
        email.setAuthenticator(DefaultAuthenticator(fromEmail, fromPassword))
        email.isSSLOnConnect = true
        email.setFrom(fromEmail, "Notes-Api")
        email.subject = "Verify Email"
        email.setMsg("Click on link to verify Email: $verificationLink")
        email.addTo(toEmail)
        email.send()
        return true
    } catch (e: EmailException) {
        e.printStackTrace()
        return false
    }
}