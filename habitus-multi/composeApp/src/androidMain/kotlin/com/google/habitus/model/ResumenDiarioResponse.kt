package com.google.habitus.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class DiaHistorial(
    val fecha: String,
    val consumidas: Int,
    val quemadas: Int
)

@Serializable
data class ResumenDiarioResponse(
    val consumidas: Int = 0,
    val quemadas: Int = 0,
    val netas: Int = 0,

    @SerialName("historial_semanal")
    val historialSemanal: List<DiaHistorial> = emptyList(),

    val error: String? = null
)