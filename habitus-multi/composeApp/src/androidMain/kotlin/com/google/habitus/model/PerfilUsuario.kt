package com.google.habitus.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class PerfilUsuario(
    val id: Int = -1,
    val name: String = "",
    val email: String = "",
    val gender: String = "",

    @SerialName("birth_date")
    val birthDate: String = "",

    @SerialName("height_cm")
    val heightCm: String = "",

    @SerialName("weight_kg")
    val weightKg: String = "",

    val error: String? = null
)