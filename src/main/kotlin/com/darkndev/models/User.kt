package com.darkndev.models

import kotlinx.serialization.Serializable

@Serializable
data class User(
    val id: Int,
    val emailId: String,
    val password: String,
    val salt: String,
    val verified: Boolean
)