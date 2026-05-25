package com.google.habitus.model

// Un 'object' en Kotlin es un Singleton. Significa que solo existe UNA copia de esto
// en toda la app, perfecto para guardar los datos del usuario que inició sesión.
object UsuarioSession {
    var idUsuarioActual: Int = -1
    var tokenActual: String = ""

    fun limpiarSesion() {
        idUsuarioActual = -1
        tokenActual = ""
    }
}