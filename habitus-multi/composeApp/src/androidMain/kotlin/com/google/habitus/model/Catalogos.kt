package com.google.habitus.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

// Molde para un alimento individual
@Serializable
data class Food(
    val id: Int,
    val name: String,

    // El nombre real en el JSON de PHP
    @SerialName("calories_per_unit")
    // El nombre estilo camello para que SonarQube y Kotlin sean felices
    val caloriesPerUnit: Int,

    val unit: String
)

// Molde para una actividad individual
@Serializable
data class Activity(
    val id: Int,
    val name: String,

    @SerialName("calories_per_minute")
    val caloriesPerMinute: Int
)

@Serializable
data class CatalogosResponse(
    val foods: List<Food> = emptyList(),
    val activities: List<Activity> = emptyList(),
    val error: String? = null
)