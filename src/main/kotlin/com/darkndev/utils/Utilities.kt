package com.darkndev.utils

import java.time.Instant
import java.time.ZoneId
import java.time.ZonedDateTime

infix fun Long.isBefore(other: Long) = ZonedDateTime.ofInstant(
    Instant.ofEpochMilli(this),
    ZoneId.systemDefault()
).isBefore(
    ZonedDateTime.ofInstant(
        Instant.ofEpochMilli(other),
        ZoneId.systemDefault()
    )
)