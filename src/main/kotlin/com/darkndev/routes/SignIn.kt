package com.darkndev.routes

import com.darkndev.data.UserDao
import com.darkndev.models.AuthRequest
import com.darkndev.models.AuthResponse
import com.darkndev.security.hashing.SHA256HashingService
import com.darkndev.security.hashing.SaltedHash
import com.darkndev.security.token.JwtTokenService
import com.darkndev.security.token.TokenClaim
import com.darkndev.security.token.TokenConfig
import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*

fun Route.signIn(
    hashingService: SHA256HashingService,
    tokenService: JwtTokenService,
    tokenConfig: TokenConfig,
    userDao:UserDao
) {
    post("/signin") {
        val request = call.receive<AuthRequest>()

        val user = userDao.getUserByUsername(request.username)
        if (user == null) {
            call.respond(HttpStatusCode.Conflict, "Incorrect username or password")
            return@post
        }

        val isValidPassword = hashingService.verify(
            value = request.password,
            saltedHash = SaltedHash(
                hash = user.password,
                salt = user.salt
            )
        )

        if (!isValidPassword) {
            call.respond(HttpStatusCode.Conflict, "Incorrect username or password")
            return@post
        }

        val token = tokenService.generate(
            config = tokenConfig,
            TokenClaim(
                name = "userId",
                value = user.id.toString()
            )
        )

        call.respond(HttpStatusCode.OK, AuthResponse(token))
    }
}