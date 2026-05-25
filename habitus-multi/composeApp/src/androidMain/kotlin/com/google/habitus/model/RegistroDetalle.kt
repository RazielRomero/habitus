package com.google.habitus.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class RegistroDetalle(
    @SerialName("id_registro")
    val idRegistro: Int,

    val nombre: String,
    val cantidad: Int,
    val calorias: Int,
    val fecha: String,
    val hora: String,
    val tipo: String
)

@Serializable
data class DetalleResponse(
    val registros: List<RegistroDetalle> = emptyList(),
    val error: String? = null
)

@Serializable
data class EliminarRegistroRequest(
    @SerialName("id_registro")
    val idRegistro: Int,

    val tipo: String
)

//El empaque para borrar el día completo
@Serializable
data class EliminarDiaRequest(
    @SerialName("user_id")
    val userId: Int,

    val fecha: String
)