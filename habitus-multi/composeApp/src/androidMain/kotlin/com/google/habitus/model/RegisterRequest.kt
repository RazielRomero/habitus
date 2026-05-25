package com.google.habitus.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class RegisterRequest(
    val name: String,
    val email: String,
    val password: String,
    val gender: String,

    @SerialName("height_cm")
    val heightCm: Double,

    @SerialName("weight_kg")
    val weightKg: Double,

    @SerialName("date_of_birth")
    val dateOfBirth: String
)