package com.google.habitus.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.habitus.api.ApiClient
import com.google.habitus.model.*
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

data class SeleccionAlimento(val alimento: Food, val cantidad: Int)
data class SeleccionActividad(val actividad: Activity, val minutos: Int)

class DiarioViewModel : ViewModel() {
    private val apiClient = ApiClient()

    private val _alimentos = MutableStateFlow<List<Food>>(emptyList())
    val alimentos: StateFlow<List<Food>> = _alimentos

    private val _actividades = MutableStateFlow<List<Activity>>(emptyList())
    val actividades: StateFlow<List<Activity>> = _actividades

    private val _cargando = MutableStateFlow(true)
    val cargando: StateFlow<Boolean> = _cargando

    private val _alimentosSeleccionados = MutableStateFlow<List<SeleccionAlimento>>(emptyList())
    val alimentosSeleccionados: StateFlow<List<SeleccionAlimento>> = _alimentosSeleccionados

    private val _actividadesSeleccionadas = MutableStateFlow<List<SeleccionActividad>>(emptyList())
    val actividadesSeleccionadas: StateFlow<List<SeleccionActividad>> = _actividadesSeleccionadas

    private val _mensaje = MutableStateFlow("")
    val mensaje: StateFlow<String> = _mensaje

    private val _recomendacionIA = MutableStateFlow("")
    val recomendacionIA: StateFlow<String> = _recomendacionIA

    init {
        cargarDatos()
    }

    private fun cargarDatos() {
        viewModelScope.launch {
            _cargando.value = true
            val respuesta = apiClient.obtenerCatalogos()
            // Se asume que CatalogosResponse devuelve listas o null
            _alimentos.value = respuesta.foods ?: emptyList()
            _actividades.value = respuesta.activities ?: emptyList()
            _cargando.value = false
        }
    }

    fun agregarAlimento(alimento: Food, cantidad: Int) {
        val nuevaLista = _alimentosSeleccionados.value.toMutableList()
        nuevaLista.add(SeleccionAlimento(alimento, cantidad))
        _alimentosSeleccionados.value = nuevaLista
    }

    fun agregarActividad(actividad: Activity, minutos: Int) {
        val nuevaLista = _actividadesSeleccionadas.value.toMutableList()
        nuevaLista.add(SeleccionActividad(actividad, minutos))
        _actividadesSeleccionadas.value = nuevaLista
    }

    fun confirmarRegistro() {
        viewModelScope.launch {
            _cargando.value = true

            try {
                val alimentosRequest = _alimentosSeleccionados.value.map {
                    FoodLogRequest(id = it.alimento.id, cantidad = it.cantidad)
                }

                val actividadesRequest = _actividadesSeleccionadas.value.map {
                    ActivityLogRequest(id = it.actividad.id, minutos = it.minutos)
                }

                val request = RegistroDiarioRequest(
                    userId = UsuarioSession.idUsuarioActual,
                    foods = alimentosRequest,
                    activities = actividadesRequest
                )

                val respuesta = apiClient.guardarRegistroDiario(request)

                if (respuesta.mensaje != null) {
                    _mensaje.value = respuesta.mensaje
                    _recomendacionIA.value = respuesta.recomendacionIa ?: ""

                    _alimentosSeleccionados.value = emptyList()
                    _actividadesSeleccionadas.value = emptyList()
                } else {
                    _mensaje.value = respuesta.error ?: "Error desconocido"
                }

            } catch (e: Exception) {
                _mensaje.value = "Crash Evitado. El error es: ${e.message}"
            } finally {
                _cargando.value = false
            }
        }
    }

    fun limpiarMensaje() {
        _mensaje.value = ""
        _recomendacionIA.value = ""
    }
}