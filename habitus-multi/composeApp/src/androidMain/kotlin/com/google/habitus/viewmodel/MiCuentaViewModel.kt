package com.google.habitus.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.habitus.api.ApiClient
import com.google.habitus.model.PerfilUsuario
import com.google.habitus.model.UsuarioSession
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class MiCuentaViewModel : ViewModel() {
    private val apiClient = ApiClient()

    private val _perfil = MutableStateFlow(PerfilUsuario())
    val perfil: StateFlow<PerfilUsuario> = _perfil

    private val _modoEdicion = MutableStateFlow(false)
    val modoEdicion: StateFlow<Boolean> = _modoEdicion

    private val _cargando = MutableStateFlow(true)
    val cargando: StateFlow<Boolean> = _cargando

    private val _mensaje = MutableStateFlow("")
    val mensaje: StateFlow<String> = _mensaje

    private val _cerrarSesion = MutableStateFlow(false)
    val cerrarSesion: StateFlow<Boolean> = _cerrarSesion

    init {
        cargarPerfil()
    }

    fun cargarPerfil() {
        viewModelScope.launch {
            _cargando.value = true
            val idUsuario = UsuarioSession.idUsuarioActual

            if (idUsuario != -1) {
                _perfil.value = apiClient.obtenerPerfil(idUsuario)
            }
            _cargando.value = false
        }
    }

    fun actualizarCampoString(campo: String, valor: String) {
        val actual = _perfil.value
        _perfil.value = when (campo) {
            "name" -> actual.copy(name = valor)
            "email" -> actual.copy(email = valor)
            "gender" -> actual.copy(gender = valor)
            "birth_date" -> actual.copy(birthDate = valor)
            "height_cm" -> actual.copy(heightCm = valor)
            "weight_kg" -> actual.copy(weightKg = valor)
            else -> actual
        }
    }

    fun toggleEdicion() {
        _modoEdicion.value = !_modoEdicion.value
    }

    fun guardarCambios() {
        val perfilActual = _perfil.value

        val errorValidacion = validarPerfilFormulario(perfilActual)

        if (errorValidacion != null) {
            _mensaje.value = errorValidacion
            return
        }

        viewModelScope.launch {
            _cargando.value = true
            val respuesta = apiClient.actualizarPerfil(perfilActual)

            if (respuesta.mensaje != null) {
                _mensaje.value = respuesta.mensaje
                _modoEdicion.value = false
            } else {
                _mensaje.value = respuesta.error ?: "Error desconocido"
            }
            _cargando.value = false
        }
    }

    fun limpiarMensaje() {
        _mensaje.value = ""
    }

    fun hacerLogout() {
        UsuarioSession.limpiarSesion()
        _cerrarSesion.value = true
    }

    fun resetearCerrarSesion() {
        _cerrarSesion.value = false
    }

    // ============================
    // LÓGICA DE VALIDACIÓN AISLADA
    // ============================
    private fun validarPerfilFormulario(perfil: PerfilUsuario): String? {
        val name = perfil.name.trim()
        val email = perfil.email.trim()
        val gender = perfil.gender.trim()
        val birthDate = perfil.birthDate.trim()
        val heightCm = perfil.heightCm.trim()
        val weightKg = perfil.weightKg.trim()

        if (name.isEmpty() || email.isEmpty() || gender.isEmpty() || birthDate.isEmpty() || heightCm.isEmpty() || weightKg.isEmpty()) {
            return "Por favor, llena todos los campos"
        }

        // Solo permitimos opciones específicas de género (ignorando mayúsculas/minúsculas)
        val generosPermitidos = listOf("masculino", "femenino", "otro")
        if (gender.lowercase() !in generosPermitidos) {
            return "El género debe ser: Masculino, Femenino u Otro"
        }

        val dominiosPermitidos = listOf("@gmail.com", "@hotmail.com", "@outlook.com", "@yahoo.com", "@live.com", "@icloud.com")
        if (dominiosPermitidos.none { email.lowercase().endsWith(it) }) {
            return "Usa un correo válido (ej. gmail, hotmail, outlook)"
        }

        val alturaNum = heightCm.toDoubleOrNull()?.toInt()
        if (alturaNum == null || alturaNum !in 100..250) {
            return "La estatura debe estar entre 100 y 250 cm"
        }

        val pesoNum = weightKg.toDoubleOrNull()
        if (pesoNum == null || pesoNum !in 20.0..300.0) {
            return "El peso debe estar entre 20 y 300 kg"
        }

        if (!"^([0-9]{4})-(0[1-9]|1[0-2])-(0[1-9]|[1-2][0-9]|3[0-1])$".toRegex().matches(birthDate)) {
            return "La fecha debe tener el formato AAAA-MM-DD (ej. 2004-01-10)"
        }

        val anio = birthDate.split("-")[0].toInt()
        val anioActual = java.util.Calendar.getInstance()[java.util.Calendar.YEAR]
        if (anioActual - anio !in 5..120) {
            return "Debes tener entre 5 y 120 años"
        }

        return null
    }
}