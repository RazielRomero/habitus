package com.google.habitus.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.habitus.api.ApiClient
import com.google.habitus.model.*
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class HistorialViewModel : ViewModel() {
    private val apiClient = ApiClient()

    private val _resumen = MutableStateFlow(ResumenDiarioResponse())
    val resumen: StateFlow<ResumenDiarioResponse> = _resumen

    private val _cargando = MutableStateFlow(true)
    val cargando: StateFlow<Boolean> = _cargando

    private val _filtroDias = MutableStateFlow(7)
    val filtroDias: StateFlow<Int> = _filtroDias

    private val _registrosDia = MutableStateFlow<List<RegistroDetalle>>(emptyList())
    val registrosDia: StateFlow<List<RegistroDetalle>> = _registrosDia

    init {
        cargarResumen(7)
        cargarDetalles(7)
    }

    fun cambiarFiltro(dias: Int) {
        _filtroDias.value = dias
        cargarResumen(dias)
        cargarDetalles(dias)
    }

    private fun cargarResumen(dias: Int) {
        viewModelScope.launch {
            _cargando.value = true
            val idUsuario = UsuarioSession.idUsuarioActual
            if (idUsuario != -1) {
                _resumen.value = apiClient.obtenerResumenHoy(idUsuario, dias)
            }
            _cargando.value = false
        }
    }

    private fun cargarDetalles(dias: Int) {
        viewModelScope.launch {
            val idUsuario = UsuarioSession.idUsuarioActual
            if (idUsuario != -1) {
                val response = apiClient.obtenerDetallesHistorial(idUsuario, dias)
                _registrosDia.value = response.registros
            }
        }
    }

    fun eliminarRegistro(idRegistro: Int, tipo: String) {
        viewModelScope.launch {
            val request = EliminarRegistroRequest(idRegistro, tipo)
            val response = apiClient.eliminarRegistro(request)
            if (response.error == null) {
                // Actualizamos ambas partes de la interfaz
                actualizarDatos()
            }
        }
    }

    // 3. Borra el día y refresca TODA la pantalla
    fun eliminarDiaCompleto(fecha: String) {
        viewModelScope.launch {
            val idUsuario = UsuarioSession.idUsuarioActual

            if (idUsuario != -1) {
                val request = EliminarDiaRequest(idUsuario, fecha)
                val response = apiClient.eliminarRegistroDia(request)

                if (response.error == null) {
                    // Tarea completada: Refrescamos resumen y lista de detalles
                    actualizarDatos()
                }
            }
        }
    }

    // Función auxiliar para no repetir código de recarga
    private fun actualizarDatos() {
        cargarResumen(_filtroDias.value)
        cargarDetalles(_filtroDias.value)
    }
}