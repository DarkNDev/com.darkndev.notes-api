package com.darkndev.models

import kotlinx.serialization.Serializable

@Serializable
data class AuthRequest(
    val emailId:String,
    val password:String
)
