package com.google.habitus.api

import com.google.habitus.model.*
import io.ktor.client.*
import io.ktor.client.call.*
import io.ktor.client.plugins.contentnegotiation.*
import io.ktor.client.request.*
import io.ktor.http.*
import io.ktor.serialization.kotlinx.json.*
import kotlinx.serialization.json.Json

class ApiClient {
    private val client = HttpClient {
        install(ContentNegotiation) {
            json(Json { ignoreUnknownKeys = true })
        }
    }

    // Se lee desde las variables de entorno/local.properties inyectadas en BuildConfig
    private val ipServidor = com.google.habitus.BuildConfig.SERVER_IP

    // Rutas que heredan la IP automáticamente
    private val baseUrl = "http://$ipServidor/backend_habitus/api/index.php"
    private val baseBackend = "http://$ipServidor/backend_habitus"

    suspend fun loginUsuario(request: LoginRequest): LoginResponse {
        return client.post("$baseUrl/login") {
            contentType(ContentType.Application.Json)
            setBody(request)
        }.body()
    }

    suspend fun registrarUsuario(request: RegisterRequest): RegisterResponse {
        return client.post("$baseUrl/register") {
            contentType(ContentType.Application.Json)
            setBody(request)
        }.body()
    }

    suspend fun obtenerCatalogos(): CatalogosResponse {
        return try {
            val url = "$baseBackend/get_catalog.php"
            client.get(url).body()
        } catch (e: Exception) {
            CatalogosResponse(error = e.message)
        }
    }

    suspend fun guardarRegistroDiario(request: RegistroDiarioRequest): RespuestaSimple {
        return try {
            val url = "$baseBackend/save_registro.php"
            client.post(url) {
                contentType(ContentType.Application.Json)
                setBody(request)
            }.body()
        } catch (e: Exception) {
            RespuestaSimple(error = "Error al conectar: ${e.message}")
        }
    }

    suspend fun obtenerResumenHoy(userId: Int, dias: Int = 7): ResumenDiarioResponse {
        return try {
            val tiempo = System.currentTimeMillis()
            val url = "$baseBackend/get_historial.php?user_id=$userId&dias=$dias&t=$tiempo"
            client.get(url).body()
        } catch (e: Exception) {
            ResumenDiarioResponse(error = "Error al conectar: ${e.message}")
        }
    }

    suspend fun obtenerPerfil(userId: Int): PerfilUsuario {
        return try {
            val url = "$baseBackend/get_perfil.php?user_id=$userId"
            client.get(url).body()
        } catch (e: Exception) {
            PerfilUsuario(error = "Error de conexión: ${e.message}")
        }
    }

    suspend fun actualizarPerfil(perfil: PerfilUsuario): RespuestaSimple {
        return try {
            val url = "$baseBackend/update_perfil.php"
            client.post(url) {
                contentType(ContentType.Application.Json)
                setBody(perfil)
            }.body()
        } catch (e: Exception) {
            RespuestaSimple(error = "Error al guardar: ${e.message}")
        }
    }

    suspend fun obtenerDetallesHistorial(userId: Int, dias: Int): DetalleResponse {
        return try {
            val tiempo = System.currentTimeMillis()
            val url = "$baseBackend/get_registros_detalle.php?user_id=$userId&dias=$dias&t=$tiempo"
            client.get(url).body()
        } catch (e: Exception) {
            DetalleResponse(error = "Error al conectar: ${e.message}")
        }
    }

    suspend fun eliminarRegistro(request: EliminarRegistroRequest): RespuestaSimple {
        return try {
            val url = "$baseBackend/delete_registro.php"
            client.post(url) {
                contentType(ContentType.Application.Json)
                setBody(request)
            }.body()
        } catch (e: Exception) {
            RespuestaSimple(error = "Error al borrar: ${e.message}")
        }
    }

    //Borra el dia completo
    suspend fun eliminarRegistroDia(request: EliminarDiaRequest): RespuestaSimple {
        return try {
            val url = "$baseBackend/delete_dia.php"
            client.post(url) {
                contentType(ContentType.Application.Json)
                setBody(request)
            }.body()
        } catch (e: Exception) {
            RespuestaSimple(error = "Error de conexión: ${e.message}")
        }
    }

    suspend fun descargarReportePdf(userId: Int, dias: Int): ByteArray? {
        return try {
            val url = "$baseBackend/get_pdf_report.php?user_id=$userId&dias=$dias"

            val respuesta = client.get(url)

            if (respuesta.status == io.ktor.http.HttpStatusCode.OK) {
                respuesta.body<ByteArray>()
            } else {
                null
            }
        } catch (e: Exception) {
            null
        }
    }
}