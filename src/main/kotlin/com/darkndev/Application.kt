package com.darkndev

import com.darkndev.data.NoteDatabaseFactory
import io.ktor.server.application.*
import com.darkndev.plugins.*
import io.ktor.server.netty.*

fun main(args: Array<String>): Unit = EngineMain.main(args)

fun Application.module() {
    NoteDatabaseFactory.init(environment.config)
    noteResponse()
}
