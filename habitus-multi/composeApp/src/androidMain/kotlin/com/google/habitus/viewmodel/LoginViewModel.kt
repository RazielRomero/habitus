package com.google.habitus.viewmodel

import androidx.lifecycle.ViewModel
import com.google.habitus.api.ApiClient
import com.google.habitus.model.LoginRequest
import com.google.habitus.model.RegisterRequest
import com.google.habitus.model.UsuarioSession
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class LoginViewModel : ViewModel() {

    private val apiClient = ApiClient()
    private val scope = CoroutineScope(Dispatchers.Main)

    // Estado para mostrar mensajes de texto en la pantalla
    private val _estadoAuth = MutableStateFlow("Ingresa tus datos")
    val estadoAuth: StateFlow<String> = _estadoAuth

    // Bandera para avisar que debemos navegar a la pantalla Home
    private val _navegarAlHome = MutableStateFlow(false)
    val navegarAlHome: StateFlow<Boolean> = _navegarAlHome

    fun realizarLogin(correo: String, contra: String) {
        // Validación local previa
        if (correo.isBlank() || contra.isBlank()) {
            _estadoAuth.value = "Error: Los campos no pueden estar vacíos"
            return
        }

        _estadoAuth.value = "Conectando con el servidor..."
        scope.launch {
            try {
                val request = LoginRequest(email = correo, password = contra)
                val respuesta = apiClient.loginUsuario(request)

                _estadoAuth.value = "¡Bienvenido ${respuesta.usuario}! IMC: ${respuesta.imcActual}"

                // Guardamos el ID en la memoria global
                UsuarioSession.idUsuarioActual = respuesta.id

                // Si llegamos hasta aquí sin errores, levantamos la bandera para cambiar de pantalla
                _navegarAlHome.value = true

            } catch (e: Exception) {
                // Convertimos toda la excepción a texto en minúsculas para rastrear el tipo de error real
                val errorCompleto = e.toString().lowercase()

                _estadoAuth.value = when {
                    // SI EL SERVIDOR ESTÁ APAGADO: Captura rechazos de conexión, tiempos de espera o errores de red
                    errorCompleto.contains("connect") ||
                            errorCompleto.contains("refused") ||
                            errorCompleto.contains("unresolved") ||
                            errorCompleto.contains("timeout") -> {
                        "Error: No se pudo conectar al servidor."
                    }
                    // SI LAS CREDENCIALES SON INCORRECTAS: Captura errores de respuesta HTTP o fallas al procesar el JSON del login fallido
                    errorCompleto.contains("401") ||
                            errorCompleto.contains("serialization") ||
                            errorCompleto.contains("json") -> {
                        "Error: Correo o contraseña incorrectos."
                    }
                    // Cualquier otra anomalía de red imprevista
                    else -> {
                        "Error: Falla de comunicación con el servidor."
                    }
                }
            }
        }
    }

    fun realizarRegistro(nombre: String, correo: String, contra: String, genero: String, altura: String, peso: String, fechaNacimiento: String) {
        _estadoAuth.value = "Registrando usuario..."
        scope.launch {
            try {
                val request = RegisterRequest(
                    name = nombre,
                    email = correo,
                    password = contra,
                    gender = genero,
                    heightCm = altura.toDoubleOrNull() ?: 0.0,
                    weightKg = peso.toDoubleOrNull() ?: 0.0,
                    dateOfBirth = fechaNacimiento
                )
                val respuesta = apiClient.registrarUsuario(request)

                if (respuesta.mensaje != null) {
                    _estadoAuth.value = "¡Registro exitoso! Ya puedes iniciar sesión."
                } else {
                    _estadoAuth.value = "Error: ${respuesta.error}"
                }
            } catch (e: Exception) {
                _estadoAuth.value = "Error de conexión: Verifica tu servidor."
            }
        }
    }

    // Función para reiniciar el estado
    fun resetearNavegacion() {
        _navegarAlHome.value = false
        _estadoAuth.value = "Ingresa tus datos"
    }
}