package com.darkndev.models

import kotlinx.serialization.Serializable

@Serializable
data class NoteRequest(
    val id: Int,
    val title: String,
    val content: String
) : java.io.Serializable
