package com.google.habitus.ui

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.google.habitus.R
import com.google.habitus.viewmodel.LoginViewModel
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.TimeZone

// --- COLORES GLOBALES ---
val AzulBoton = Color(0xFF1565C0)
val GrisFondo = Color(0xFFF2F4F6)

// Datos para validar el formulario
data class DatosFormulario(
    val nombre: String,
    val correo: String,
    val contra: String,
    val repetirContra: String,
    val genero: String,
    val altura: String,
    val peso: String,
    val fechaNacimiento: String
)

//  Configuraciones visuales para los campos de texto
data class CampoConfig(
    val labelSecundario: String = "",
    val placeholder: String = "",
    val trailingIcon: @Composable (() -> Unit)? = null,
    val visualTransformation: VisualTransformation = VisualTransformation.None,
    val keyboardOptions: KeyboardOptions = KeyboardOptions.Default,
    val readOnly: Boolean = false
)

// ==========================================
// PANTALLA PRINCIPAL (Orquestador)
// ==========================================
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RegistroScreen(
    onVolverAlLogin: () -> Unit,
    viewModel: LoginViewModel = viewModel<LoginViewModel>()
) {
    // --- ESTADOS ---
    var nombre by remember { mutableStateOf("") }
    var correo by remember { mutableStateOf("") }
    var contra by remember { mutableStateOf("") }
    var repetirContra by remember { mutableStateOf("") }
    var genero by remember { mutableStateOf("") }
    var fechaNacimiento by remember { mutableStateOf("") }
    var altura by remember { mutableStateOf("") }
    var peso by remember { mutableStateOf("") }

    val mostrarDatePicker = remember { mutableStateOf(false) }
    val datePickerState = rememberDatePickerState()

    var errorLocal by remember { mutableStateOf("") }
    val estadoAuth by viewModel.estadoAuth.collectAsState()

    // --- DIÁLOGOS ---
    if (mostrarDatePicker.value) {
        DialogoCalendario(
            state = datePickerState,
            onDismiss = { mostrarDatePicker.value = false },
            onConfirm = { millis ->
                mostrarDatePicker.value = false
                val sdf = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()).apply {
                    timeZone = TimeZone.getTimeZone("UTC")
                }
                fechaNacimiento = sdf.format(Date(millis))
            }
        )
    }

    // --- INTERFAZ ---
    Box(modifier = Modifier.fillMaxSize().background(GrisFondo), contentAlignment = Alignment.Center) {
        Card(
            modifier = Modifier.fillMaxWidth(0.9f).padding(vertical = 32.dp),
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White),
            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
        ) {
            Column(
                modifier = Modifier.padding(horizontal = 24.dp, vertical = 32.dp).verticalScroll(rememberScrollState()),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                HeaderRegistro()

                CampoRegistro(
                    labelPrincipal = "NOMBRE COMPLETO",
                    value = nombre,
                    onValueChange = { nombre = it }
                )

                CampoRegistro(
                    labelPrincipal = "CORREO ELECTRÓNICO",
                    value = correo,
                    onValueChange = { correo = it },
                    config = CampoConfig(keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email))
                )

                CampoRegistro(
                    labelPrincipal = "CONTRASEÑA",
                    value = contra,
                    onValueChange = { contra = it },
                    config = CampoConfig(
                        labelSecundario = "(MÍN. 8 — MÁX. 16 CARACTERES)",
                        visualTransformation = PasswordVisualTransformation()
                    )
                )

                CampoRegistro(
                    labelPrincipal = "REPETIR CONTRASEÑA",
                    value = repetirContra,
                    onValueChange = { repetirContra = it },
                    config = CampoConfig(visualTransformation = PasswordVisualTransformation())
                )

                SelectorGenero(genero = genero, onGeneroChange = { genero = it })

                Box(modifier = Modifier.fillMaxWidth()) {
                    CampoRegistro(
                        labelPrincipal = "FECHA DE NACIMIENTO",
                        value = fechaNacimiento,
                        onValueChange = { },
                        config = CampoConfig(
                            labelSecundario = "(5–120 AÑOS)",
                            placeholder = "Toca para abrir calendario",
                            trailingIcon = { Icon(Icons.Default.DateRange, contentDescription = "Calendario", tint = Color.Black) },
                            readOnly = true
                        )
                    )
                    Box(modifier = Modifier.matchParentSize().background(Color.Transparent).clickable { mostrarDatePicker.value = true })
                }

                CampoRegistro(
                    labelPrincipal = "ESTATURA",
                    value = altura,
                    onValueChange = { altura = it },
                    config = CampoConfig(
                        labelSecundario = "(100–250 CM)",
                        placeholder = "cm",
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
                    )
                )

                CampoRegistro(
                    labelPrincipal = "PESO",
                    value = peso,
                    onValueChange = { peso = it },
                    config = CampoConfig(
                        labelSecundario = "(20–300 KG)",
                        placeholder = "kg",
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
                    )
                )

                Spacer(modifier = Modifier.height(8.dp))

                val datosActuales = DatosFormulario(nombre, correo, contra, repetirContra, genero, altura, peso, fechaNacimiento)

                SeccionBotonGuardar(
                    datos = datosActuales,
                    errorLocal = errorLocal,
                    estadoAuth = estadoAuth,
                    onErrorChange = { errorLocal = it },
                    onRegistroValido = { fechaSQL ->
                        viewModel.realizarRegistro(nombre, correo, contra, genero, altura, peso, fechaSQL)
                    }
                )

                Spacer(modifier = Modifier.height(24.dp))

                Row {
                    Text("¿Ya tienes cuenta? ", color = Color.Gray)
                    Text("Inicia sesión", color = AzulBoton, fontWeight = FontWeight.Bold, modifier = Modifier.clickable { onVolverAlLogin() })
                }
            }
        }
    }
}

// ========================================================
// SUB-COMPONENTES VISUALES
// ========================================================

@Composable
fun HeaderRegistro() {
    Image(painter = painterResource(id = R.drawable.logo), contentDescription = "Logo", modifier = Modifier.height(60.dp), contentScale = ContentScale.Fit)
    Spacer(modifier = Modifier.height(24.dp))
    Text("Crear cuenta", fontSize = 24.sp, fontWeight = FontWeight.Bold, color = Color.Black)
    Spacer(modifier = Modifier.height(8.dp))
    Text("Registra tus datos para comenzar", fontSize = 14.sp, color = Color.Gray)
    Spacer(modifier = Modifier.height(32.dp))
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DialogoCalendario(state: DatePickerState, onDismiss: () -> Unit, onConfirm: (Long) -> Unit) {
    DatePickerDialog(
        onDismissRequest = onDismiss,
        confirmButton = {
            TextButton(onClick = { state.selectedDateMillis?.let { onConfirm(it) } }) {
                Text("Aceptar", color = AzulBoton, fontWeight = FontWeight.Bold)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancelar", color = Color.Gray) }
        }
    ) {
        DatePicker(state = state)
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SelectorGenero(genero: String, onGeneroChange: (String) -> Unit) {
    var expandirGenero by remember { mutableStateOf(false) }
    val opcionesGenero = listOf("Masculino", "Femenino", "Otro")

    Column(modifier = Modifier.fillMaxWidth().padding(bottom = 16.dp)) {
        Text("GÉNERO", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Color.Black)
        Spacer(modifier = Modifier.height(4.dp))
        ExposedDropdownMenuBox(expanded = expandirGenero, onExpandedChange = { expandirGenero = !expandirGenero }) {
            OutlinedTextField(
                value = genero.ifEmpty { "Selecciona" }, onValueChange = {}, readOnly = true,
                modifier = Modifier.fillMaxWidth().menuAnchor(), shape = RoundedCornerShape(12.dp),
                trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expandirGenero) },
                colors = OutlinedTextFieldDefaults.colors(focusedContainerColor = Color(0xFFF9F9F9), unfocusedContainerColor = Color(0xFFF9F9F9), focusedBorderColor = AzulBoton, unfocusedBorderColor = Color(0xFFE0E0E0))
            )
            ExposedDropdownMenu(expanded = expandirGenero, onDismissRequest = { expandirGenero = false }, modifier = Modifier.background(Color.White)) {
                opcionesGenero.forEach { seleccion ->
                    DropdownMenuItem(text = { Text(seleccion) }, onClick = { onGeneroChange(seleccion); expandirGenero = false })
                }
            }
        }
    }
}

@Composable
fun SeccionBotonGuardar(
    datos: DatosFormulario,
    errorLocal: String,
    estadoAuth: String,
    onErrorChange: (String) -> Unit,
    onRegistroValido: (String) -> Unit
) {
    val mensajeMostrar = errorLocal.ifEmpty { if (estadoAuth != "Ingresa tus datos") estadoAuth else "" }

    if (mensajeMostrar.isNotEmpty()) {
        Text(
            text = mensajeMostrar,
            color = if (mensajeMostrar.contains("Error") || mensajeMostrar.contains("coinciden")) Color.Red else AzulBoton,
            fontSize = 13.sp, modifier = Modifier.padding(bottom = 16.dp)
        )
    }

    Button(
        onClick = {
            onErrorChange("")
            val errorValidacion = validarFormulario(datos)

            if (errorValidacion != null) {
                onErrorChange(errorValidacion)
            } else {
                val partesFecha = datos.fechaNacimiento.split("/")
                val fechaParaMySQL = "${partesFecha[2]}-${partesFecha[1]}-${partesFecha[0]}"
                onRegistroValido(fechaParaMySQL)
            }
        },
        modifier = Modifier.fillMaxWidth().height(50.dp),
        shape = RoundedCornerShape(12.dp),
        colors = ButtonDefaults.buttonColors(containerColor = AzulBoton)
    ) {
        Text("Crear cuenta", fontWeight = FontWeight.Bold, color = Color.White)
    }
}
// LÓGICA DE VALIDACIÓN AISLADA
fun validarFormulario(datos: DatosFormulario): String? {
    val (nombre, correo, contra, repetirContra, genero, altura, peso, fechaNacimiento) = datos

    if (nombre.isEmpty() || correo.isEmpty() || contra.isEmpty() || repetirContra.isEmpty() || genero.isEmpty() || altura.isEmpty() || peso.isEmpty() || fechaNacimiento.isEmpty()) return "Por favor, llena todos los campos"
    if (listOf("@gmail.com", "@hotmail.com", "@outlook.com", "@yahoo.com", "@live.com", "@icloud.com").none { correo.lowercase().endsWith(it) }) return "Usa un correo válido (ej. gmail, hotmail, outlook)"
    if (contra.length !in 8..16) return "La contraseña debe tener entre 8 y 16 caracteres"
    if (contra != repetirContra) return "Las contraseñas no coinciden"

    val alturaNum = altura.toIntOrNull()
    if (alturaNum == null || alturaNum !in 100..250) return "La estatura debe estar entre 100 y 250 cm"

    val pesoNum = peso.toDoubleOrNull()
    if (pesoNum == null || pesoNum !in 20.0..300.0) return "El peso debe estar entre 20 y 300 kg"

    if (!"^([0-2][0-9]|3[0-1])/(0[1-9]|1[0-2])/([0-9]{4})$".toRegex().matches(fechaNacimiento)) return "Usa una fecha válida seleccionándola del calendario"

    val anio = fechaNacimiento.split("/")[2].toInt()
    val anioActual = java.util.Calendar.getInstance()[java.util.Calendar.YEAR]

    if (anioActual - anio !in 5..120) return "Debes tener entre 5 y 120 años para registrarte"

    return null
}

// COMPONENTES GENÉRICOS
@Composable
fun CampoRegistro(
    labelPrincipal: String,
    value: String,
    onValueChange: (String) -> Unit,
    config: CampoConfig = CampoConfig()
) {
    Column(modifier = Modifier.fillMaxWidth().padding(bottom = 16.dp)) {
        Row(verticalAlignment = Alignment.Bottom) {
            Text(labelPrincipal, fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Color.Black)
            if (config.labelSecundario.isNotEmpty()) {
                Text(" ${config.labelSecundario}", fontSize = 11.sp, color = Color.Gray)
            }
        }
        Spacer(modifier = Modifier.height(4.dp))
        OutlinedTextField(
            value = value,
            onValueChange = onValueChange,
            readOnly = config.readOnly,
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(12.dp),
            placeholder = { Text(config.placeholder, color = Color.Gray) },
            trailingIcon = config.trailingIcon,
            visualTransformation = config.visualTransformation,
            keyboardOptions = config.keyboardOptions,
            colors = OutlinedTextFieldDefaults.colors(
                focusedContainerColor = Color(0xFFF9F9F9),
                unfocusedContainerColor = Color(0xFFF9F9F9),
                focusedBorderColor = Color(0xFF1565C0),
                unfocusedBorderColor = Color(0xFFE0E0E0)
            ),
            singleLine = true
        )
    }
}