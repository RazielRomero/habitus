package com.google.habitus.ui

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.google.habitus.viewmodel.MiCuentaViewModel
import androidx.lifecycle.viewmodel.compose.viewModel

@Composable
fun MiCuentaScreen(
    viewModel: MiCuentaViewModel = viewModel(),
    onCerrarSesion: () -> Unit
) {
    val perfil by viewModel.perfil.collectAsState()
    val modoEdicion by viewModel.modoEdicion.collectAsState()
    val cargando by viewModel.cargando.collectAsState()
    val mensaje by viewModel.mensaje.collectAsState()
    val cerrarSesion by viewModel.cerrarSesion.collectAsState()

    var mostrarErrores by remember { mutableStateOf(false) }

    // Controla si se muestra el cuadro de confirmación para cerrar sesión
    var mostrarConfirmacionLogout by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        viewModel.cargarPerfil()
    }

    LaunchedEffect(cerrarSesion) {
        if (cerrarSesion) {
            viewModel.resetearCerrarSesion()
            onCerrarSesion()
        }
    }

    // Cuadro de diálogo para mensajes generales (éxito o error)
    if (mensaje.isNotEmpty()) {
        AlertDialog(
            onDismissRequest = { viewModel.limpiarMensaje() },
            confirmButton = {
                TextButton(onClick = { viewModel.limpiarMensaje() }) {
                    Text("Aceptar", color = AzulHabitus)
                }
            },
            title = { Text("Aviso") },
            text = { Text(mensaje) }
        )
    }

    // Confirmación de cierre de sesión
    if (mostrarConfirmacionLogout) {
        AlertDialog(
            onDismissRequest = { mostrarConfirmacionLogout = false },
            title = { Text("Cerrar Sesión") },
            text = { Text("¿Estás seguro de que deseas cerrar sesión?") },
            dismissButton = {
                TextButton(onClick = { mostrarConfirmacionLogout = false }) {
                    Text("Cancelar", color = Color.Gray)
                }
            },
            confirmButton = {
                TextButton(onClick = {
                    mostrarConfirmacionLogout = false
                    viewModel.hacerLogout() // Aquí sí cerramos la sesión de verdad
                }) {
                    Text("Sí, salir", color = Color(0xFFD32F2F))
                }
            }
        )
    }

    if (cargando) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            CircularProgressIndicator(color = AzulHabitus)
        }
        return
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
            .verticalScroll(rememberScrollState()),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Spacer(modifier = Modifier.height(16.dp))
        Text("Mi Cuenta", style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Bold, color = AzulHabitus)
        Text("Gestiona tu información personal", color = Color.Gray)

        Spacer(modifier = Modifier.height(24.dp))

        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = Color.White),
            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {

                @Composable
                fun CampoPerfil(etiqueta: String, valor: String, campoClave: String, tipoTeclado: KeyboardType = KeyboardType.Text) {

                    // Lógica de errores específicos
                    var esError = false
                    var mensajeError = ""

                    if (modoEdicion && mostrarErrores) {
                        if (valor.isBlank()) {
                            esError = true
                            mensajeError = "Este campo es obligatorio"
                        } else {
                            when (campoClave) {
                                "birth_date" -> {
                                    if (!valor.matches(Regex("^\\d{4}-\\d{2}-\\d{2}$"))) {
                                        esError = true
                                        mensajeError = "Usa formato AAAA-MM-DD (Ej. 1998-12-31)"
                                    }
                                }
                                "height_cm", "weight_kg" -> {
                                    if (valor.toDoubleOrNull() == null) {
                                        esError = true
                                        mensajeError = "Debe ser un número válido"
                                    }
                                }
                            }
                        }
                    }

                    if (modoEdicion) {
                        OutlinedTextField(
                            value = valor,
                            onValueChange = { textoEntrante ->
                                var textoFinal = textoEntrante

                                // Filtros automáticos al escribir
                                when (campoClave) {
                                    "name", "gender" -> {
                                        if (textoFinal.isNotEmpty()) {
                                            textoFinal = textoFinal.replaceFirstChar { it.uppercase() }
                                        }
                                    }
                                    "height_cm", "weight_kg" -> {
                                        textoFinal = textoFinal.filter { it.isDigit() || it == '.' }
                                    }
                                    "birth_date" -> {
                                        textoFinal = textoFinal.filter { it.isDigit() || it == '-' }
                                    }
                                }

                                viewModel.actualizarCampoString(campoClave, textoFinal)
                            },
                            label = { Text(etiqueta) },
                            isError = esError,
                            supportingText = {
                                if (esError) {
                                    Text(mensajeError, color = MaterialTheme.colorScheme.error)
                                }
                            },
                            modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp),
                            keyboardOptions = KeyboardOptions(keyboardType = tipoTeclado)
                        )
                    } else {
                        val textoMostrar = when (campoClave) {
                            "height_cm" -> valor.toDoubleOrNull()?.toInt()?.toString() ?: valor // Quita decimales forzosamente
                            "weight_kg" -> valor.toDoubleOrNull()?.let { numero ->
                                if (numero % 1.0 == 0.0) numero.toInt().toString() else numero.toString()
                            } ?: valor // Quita .00 pero respeta si pesa .5
                            else -> valor
                        }

                        Column(modifier = Modifier.padding(bottom = 12.dp)) {
                            Text(etiqueta, color = Color.Gray, fontSize = 12.sp)
                            Text(textoMostrar, fontSize = 16.sp, fontWeight = FontWeight.Medium)
                            HorizontalDivider(modifier = Modifier.padding(top = 4.dp), color = Color(0xFFEEEEEE))
                        }
                    }
                }

                CampoPerfil("Nombre completo", perfil.name, "name")
                CampoPerfil("Correo electrónico", perfil.email, "email", KeyboardType.Email)
                CampoPerfil("Género", perfil.gender, "gender")
                CampoPerfil("Fecha de Nacimiento", perfil.birthDate, "birth_date")

                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Box(modifier = Modifier.weight(1f)) {
                        CampoPerfil("Altura (cm)", perfil.heightCm, "height_cm", KeyboardType.Number)
                    }
                    Box(modifier = Modifier.weight(1f)) {
                        CampoPerfil("Peso (kg)", perfil.weightKg, "weight_kg", KeyboardType.Number)
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        if (modoEdicion) {
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                OutlinedButton(
                    onClick = {
                        mostrarErrores = false
                        viewModel.cargarPerfil()
                        viewModel.toggleEdicion()
                    },
                    modifier = Modifier.weight(1f).padding(end = 8.dp)
                ) {
                    Text("Cancelar", color = Color.Gray)
                }
                Button(
                    onClick = {
                        val fechaValida = perfil.birthDate.matches(Regex("^\\d{4}-\\d{2}-\\d{2}$"))
                        val alturaValida = perfil.heightCm.toDoubleOrNull() != null
                        val pesoValido = perfil.weightKg.toDoubleOrNull() != null

                        val datosValidos = perfil.name.isNotBlank() &&
                                perfil.email.isNotBlank() &&
                                perfil.gender.isNotBlank() &&
                                perfil.birthDate.isNotBlank() && fechaValida &&
                                perfil.heightCm.isNotBlank() && alturaValida &&
                                perfil.weightKg.isNotBlank() && pesoValido

                        if (datosValidos) {
                            mostrarErrores = false
                            viewModel.guardarCambios()
                        } else {
                            mostrarErrores = true
                        }
                    },
                    modifier = Modifier.weight(1f).padding(start = 8.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = AzulHabitus)
                ) {
                    Text("Guardar")
                }
            }
        } else {
            Button(
                onClick = { viewModel.toggleEdicion() },
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.buttonColors(containerColor = AzulHabitus)
            ) {
                Text("Editar Perfil")
            }

            Spacer(modifier = Modifier.height(16.dp))

            OutlinedButton(
                onClick = { mostrarConfirmacionLogout = true },
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.outlinedButtonColors(contentColor = Color(0xFFD32F2F))
            ) {
                Text("Cerrar Sesión")
            }
        }
    }
}