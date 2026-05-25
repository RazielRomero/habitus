package com.google.habitus.model

import kotlinx.serialization.Serializable

@Serializable
data class RegisterResponse(
    val mensaje: String? = null,
    val error: String? = null
)