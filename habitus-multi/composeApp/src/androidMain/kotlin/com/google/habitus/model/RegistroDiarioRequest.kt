package com.google.habitus.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Serializable
data class RegistroDiarioRequest(
    @SerialName("user_id")
    val userId: Int,

    @SerialName("record_date")
    val recordDate: String = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date()),

    @SerialName("alimentos_consumidos")
    val foods: List<FoodLogRequest>,

    @SerialName("actividades_realizadas")
    val activities: List<ActivityLogRequest>
)

@Serializable
data class FoodLogRequest(
    @SerialName("food_id")
    val id: Int,

    @SerialName("quantity")
    val cantidad: Int
)

@Serializable
data class ActivityLogRequest(
    @SerialName("activity_id")
    val id: Int,

    @SerialName("minutes")
    val minutos: Int
)

@Serializable
data class ResumenDiario(
    val bmi: Double? = null,
    @SerialName("calories_in") val caloriesIn: Double? = null,
    @SerialName("calories_out") val caloriesOut: Double? = null,
    @SerialName("net_balance") val netBalance: Double? = null,
    val status: String? = null
)

// Tu molde de respuesta de la IA
@Serializable
data class RespuestaSimple(
    val mensaje: String? = null,

    @SerialName("recomendacion_ia")
    val recomendacionIa: String? = null,

    val resumen: ResumenDiario? = null,

    val error: String? = null
)