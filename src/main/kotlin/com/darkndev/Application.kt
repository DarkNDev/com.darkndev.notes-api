package com.darkndev

import com.darkndev.data.NoteDao
import com.darkndev.data.NoteDatabaseFactory
import com.darkndev.data.UserDao
import com.darkndev.plugins.configureRouting
import com.darkndev.plugins.configureSecurity
import io.ktor.server.application.*
import com.darkndev.security.hashing.SHA256HashingService
import com.darkndev.security.token.JwtTokenService
import com.darkndev.security.token.TokenConfig
import io.ktor.server.netty.*
import java.time.Duration

fun main(args: Array<String>): Unit = EngineMain.main(args)

fun Application.module() {
    NoteDatabaseFactory.init(environment.config)

    val tokenService = JwtTokenService()

    //Must set token expiresIn, environment variable for jwt-secret and issuer and audience in application config
    val tokenConfig = TokenConfig(
        issuer = environment.config.property("jwt.issuer").getString(),
        audience = environment.config.property("jwt.audience").getString(),
        expiresIn = Duration.ofDays(365).toMillis(),
        secret = environment.config.property("jwt.secret").getString()
    )

    val hashingService = SHA256HashingService()
    val noteDao = NoteDao()
    val userDao = UserDao()

    configureSecurity(tokenConfig)
    configureRouting(hashingService, tokenService, tokenConfig, noteDao, userDao, environment.config)
}
