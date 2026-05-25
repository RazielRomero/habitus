package com.google.habitus.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class LoginResponse(
    val mensaje: String,
    val id: Int,
    val token: String? = null,

    @SerialName("imc_actual")
    val imcActual: Double? = null,

    val usuario: String? = null
)