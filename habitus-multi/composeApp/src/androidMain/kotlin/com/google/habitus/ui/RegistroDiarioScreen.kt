package com.google.habitus.ui

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.google.habitus.model.Activity
import com.google.habitus.model.Food
import com.google.habitus.viewmodel.DiarioViewModel
import com.google.habitus.R

// --- COLORES ---
val FondoIcono = Color(0xFFF1F8FF)
val BordeCard = Color(0xFFE0E0E0)
val TextoSecundario = Color(0xFF9E9E9E)

// ==========================================
// PANTALLA PRINCIPAL (Orquestador)
// ==========================================
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RegistroDiarioScreen(viewModel: DiarioViewModel = remember { DiarioViewModel() }) {
    val alimentos by viewModel.alimentos.collectAsState()
    val actividades by viewModel.actividades.collectAsState()
    val cargando by viewModel.cargando.collectAsState()
    val mensaje by viewModel.mensaje.collectAsState()
    val recomendacionIA by viewModel.recomendacionIA.collectAsState()

    val cantidadesAlimentos = remember { mutableStateMapOf<Int, Double>() }
    val cantidadesActividades = remember { mutableStateMapOf<Int, Int>() }

    var pestanaSeleccionada by remember { mutableStateOf(0) }

    // ESTADO PARA LA BARRA DE BÚSQUEDA
    var textoBusqueda by remember { mutableStateOf("") }

    // Cuadro de Diálogo Inteligente
    if (mensaje.isNotEmpty()) {
        AlertDialog(
            onDismissRequest = { viewModel.limpiarMensaje() },
            confirmButton = {
                TextButton(onClick = { viewModel.limpiarMensaje() }) {
                    Text("Genial", color = AzulHabitus, fontWeight = FontWeight.Bold)
                }
            },
            title = { Text("¡Registro Exitoso!") },
            text = {
                Column(modifier = Modifier.verticalScroll(rememberScrollState())) {
                    Text(mensaje, color = Color.DarkGray)

                    if (recomendacionIA.isNotEmpty()) {
                        Spacer(modifier = Modifier.height(16.dp))
                        Card(
                            colors = CardDefaults.cardColors(containerColor = FondoIcono),
                            border = BorderStroke(1.dp, Color(0xFFBBDEFB)),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Column(modifier = Modifier.padding(12.dp)) {
                                Text("✨ Consejo de Habitus AI:", color = AzulHabitus, fontWeight = FontWeight.ExtraBold, fontSize = 14.sp)
                                Spacer(modifier = Modifier.height(8.dp))
                                Text(recomendacionIA, fontSize = 13.sp, color = Color.DarkGray, lineHeight = 18.sp)
                            }
                        }
                    }
                }
            }
        )
    }

    Column(modifier = Modifier.fillMaxSize().background(Color.White)) {
        CabeceraRegistro()

        // BARRA DE BÚSQUEDA
        OutlinedTextField(
            value = textoBusqueda,
            onValueChange = { textoBusqueda = it },
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 8.dp),
            placeholder = {
                Text(
                    text = if (pestanaSeleccionada == 0) "Buscar alimento..." else "Buscar actividad...",
                    color = Color.Gray
                )
            },
            leadingIcon = {
                Icon(imageVector = Icons.Default.Search, contentDescription = "Buscar", tint = AzulHabitus)
            },
            shape = RoundedCornerShape(12.dp),
            singleLine = true,
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = AzulHabitus,
                unfocusedBorderColor = BordeCard,
                focusedContainerColor = Color(0xFFF9F9F9),
                unfocusedContainerColor = Color.White
            )
        )

        TabRow(
            selectedTabIndex = pestanaSeleccionada,
            containerColor = Color.Transparent,
            contentColor = AzulHabitus
        ) {
            Tab(selected = pestanaSeleccionada == 0, onClick = { pestanaSeleccionada = 0; textoBusqueda = "" }, text = { Text("Alimentos") })
            Tab(selected = pestanaSeleccionada == 1, onClick = { pestanaSeleccionada = 1; textoBusqueda = "" }, text = { Text("Actividades") })
        }

        Box(modifier = Modifier.weight(1f)) {
            if (cargando) {
                CircularProgressIndicator(color = AzulHabitus, modifier = Modifier.align(Alignment.Center))
            } else {
                // FILTRAMOS LAS LISTAS ANTES DE MANDARLAS A LA CUADRÍCULA
                val alimentosFiltrados = if (textoBusqueda.isBlank()) alimentos
                else alimentos.filter { it.name.contains(textoBusqueda, ignoreCase = true) }

                val actividadesFiltradas = if (textoBusqueda.isBlank()) actividades
                else actividades.filter { it.name.contains(textoBusqueda, ignoreCase = true) }

                CuadriculaRegistros(
                    pestanaSeleccionada = pestanaSeleccionada,
                    alimentos = alimentosFiltrados,
                    actividades = actividadesFiltradas,
                    cantidadesAlimentos = cantidadesAlimentos,
                    cantidadesActividades = cantidadesActividades
                )
            }
        }

        BarraConfirmacion(
            alimentos = alimentos, // Mandamos la original para poder cruzar los IDs de lo guardado
            actividades = actividades,
            cantidadesAlimentos = cantidadesAlimentos,
            cantidadesActividades = cantidadesActividades,
            viewModel = viewModel
        )
    }
}

// ==========================================
// SUB-COMPONENTES ESTRUCTURALES
// ==========================================
@Composable
fun CabeceraRegistro() {
    Column(modifier = Modifier.padding(start = 16.dp, end = 16.dp, top = 16.dp, bottom = 4.dp)) {
        Text("Registro Diario", style = MaterialTheme.typography.headlineMedium, color = AzulHabitus, fontWeight = FontWeight.Bold)
        Text("Selecciona y acumula lo de hoy", color = Color.Gray)
    }
}

@Composable
fun CuadriculaRegistros(
    pestanaSeleccionada: Int,
    alimentos: List<Food>,
    actividades: List<Activity>,
    cantidadesAlimentos: MutableMap<Int, Double>,
    cantidadesActividades: MutableMap<Int, Int>
) {
    if (pestanaSeleccionada == 0 && alimentos.isEmpty()) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Text("No se encontraron alimentos", color = Color.Gray)
        }
    } else if (pestanaSeleccionada == 1 && actividades.isEmpty()) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Text("No se encontraron actividades", color = Color.Gray)
        }
    } else {
        LazyVerticalGrid(
            columns = GridCells.Adaptive(minSize = 140.dp),
            contentPadding = PaddingValues(16.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
            modifier = Modifier.fillMaxSize()
        ) {
            if (pestanaSeleccionada == 0) {
                item(span = { GridItemSpan(maxLineSpan) }) {
                    SeccionHeader(titulo = "Alimentos consumidos", subtitulo = "Máximo 10 porciones por alimento.")
                }
                items(alimentos.size) { index ->
                    val alimento = alimentos[index]
                    val cantidadActual = cantidadesAlimentos[alimento.id] ?: 0.0
                    CardItemAlimento(
                        alimento = alimento,
                        cantidad = cantidadActual,
                        onCantidadChange = { nuevaCantidad ->
                            if (nuevaCantidad in 0.0..10.0) cantidadesAlimentos[alimento.id] = nuevaCantidad
                        }
                    )
                }
            } else {
                item(span = { GridItemSpan(maxLineSpan) }) {
                    SeccionHeader(titulo = "Actividad física", subtitulo = "Máximo 180 min por actividad.")
                }
                items(actividades.size) { index ->
                    val actividad = actividades[index]
                    val minutosActuales = cantidadesActividades[actividad.id] ?: 0
                    CardItemActividad(
                        actividad = actividad,
                        minutos = minutosActuales,
                        onMinutosChange = { nuevosMinutos ->
                            if (nuevosMinutos in 0..180) cantidadesActividades[actividad.id] = nuevosMinutos
                        }
                    )
                }
            }
        }
    }
}

@Composable
fun BarraConfirmacion(
    alimentos: List<Food>,
    actividades: List<Activity>,
    cantidadesAlimentos: MutableMap<Int, Double>,
    cantidadesActividades: MutableMap<Int, Int>,
    viewModel: DiarioViewModel
) {
    val totalSelecciones = cantidadesAlimentos.count { it.value > 0 } + cantidadesActividades.count { it.value > 0 }

    if (totalSelecciones > 0) {
        val calsConsumidas = cantidadesAlimentos.entries.sumOf { (id, cant) ->
            val food = alimentos.find { it.id == id }
            (food?.caloriesPerUnit ?: 0) * cant
        }
        val calsQuemadas = cantidadesActividades.entries.sumOf { (id, mins) ->
            val act = actividades.find { it.id == id }
            (act?.caloriesPerMinute ?: 0) * mins
        }
        val balancePrevio = calsConsumidas.toInt() - calsQuemadas.toInt()

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp))
                .background(Color(0xFFE3F2FD))
                .padding(16.dp)
        ) {
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Text("Resumen a guardar:", fontWeight = FontWeight.Bold, color = AzulHabitus)
                Text("Balance: $balancePrevio kcal", fontWeight = FontWeight.Bold)
            }
            Text("$totalSelecciones registros listos", color = Color.DarkGray, fontSize = 14.sp)

            Spacer(modifier = Modifier.height(12.dp))

            Button(
                onClick = {
                    cantidadesAlimentos.filter { it.value > 0 }.forEach { (id, cant) ->
                        alimentos.find { it.id == id }?.let { viewModel.agregarAlimento(it, cant.toInt()) }
                    }
                    cantidadesActividades.filter { it.value > 0 }.forEach { (id, mins) ->
                        actividades.find { it.id == id }?.let { viewModel.agregarActividad(it, mins) }
                    }

                    viewModel.confirmarRegistro()
                    cantidadesAlimentos.clear()
                    cantidadesActividades.clear()
                },
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.buttonColors(containerColor = AzulHabitus)
            ) {
                Text("Confirmar Registro")
            }
        }
    }
}

// ========================================================
// COMPONENTES DE DISEÑO (CARDS Y HEADER)
// ========================================================
@Composable
fun SeccionHeader(titulo: String, subtitulo: String) {
    Column(
        modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(titulo, fontSize = 20.sp, fontWeight = FontWeight.Bold, color = Color.Black)
        Spacer(modifier = Modifier.height(4.dp))
        Text(subtitulo, fontSize = 14.sp, color = TextoSecundario)
    }
}

@Composable
fun CardItemAlimento(alimento: Food, cantidad: Double, onCantidadChange: (Double) -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth().wrapContentHeight(),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        border = BorderStroke(1.dp, BordeCard),
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(
            modifier = Modifier.padding(12.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Box(
                modifier = Modifier.size(50.dp).clip(CircleShape).background(FondoIcono),
                contentAlignment = Alignment.Center
            ) {
                val imagenId = obtenerImagenAlimento(alimento.name)
                Image(
                    painter = painterResource(id = imagenId),
                    contentDescription = alimento.name,
                    modifier = Modifier.size(28.dp)
                )
            }
            Spacer(modifier = Modifier.height(12.dp))

            Text(alimento.name, fontSize = 14.sp, fontWeight = FontWeight.Bold, textAlign = TextAlign.Center, minLines = 2)
            Text("${alimento.caloriesPerUnit} kcal / ${alimento.unit}", fontSize = 11.sp, color = TextoSecundario, textAlign = TextAlign.Center)
            Spacer(modifier = Modifier.height(12.dp))

            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceEvenly,
                modifier = Modifier.fillMaxWidth().border(1.dp, BordeCard, RoundedCornerShape(8.dp)).padding(4.dp)
            ) {
                IconButton(onClick = { onCantidadChange(cantidad - 0.5) }, modifier = Modifier.size(28.dp)) {
                    Text("-", color = AzulHabitus, fontWeight = FontWeight.Bold, fontSize = 18.sp)
                }
                Box(
                    modifier = Modifier.border(1.dp, BordeCard, RoundedCornerShape(4.dp)).padding(horizontal = 12.dp, vertical = 4.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(String.format("%.1f", cantidad), fontSize = 14.sp, fontWeight = FontWeight.Bold)
                }
                IconButton(onClick = { onCantidadChange(cantidad + 0.5) }, modifier = Modifier.size(28.dp)) {
                    Text("+", color = AzulHabitus, fontWeight = FontWeight.Bold, fontSize = 18.sp)
                }
            }
            Spacer(modifier = Modifier.height(8.dp))
            Text("${String.format("%.1f", cantidad)} ${alimento.unit}", fontSize = 10.sp, color = TextoSecundario)
        }
    }
}

@Composable
fun CardItemActividad(actividad: Activity, minutos: Int, onMinutosChange: (Int) -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth().wrapContentHeight(),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        border = BorderStroke(1.dp, BordeCard),
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(
            modifier = Modifier.padding(12.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Box(
                modifier = Modifier.size(50.dp).clip(CircleShape).background(FondoIcono),
                contentAlignment = Alignment.Center
            ) {
                val imagenId = obtenerImagenActividad(actividad.name)
                Image(
                    painter = painterResource(id = imagenId),
                    contentDescription = actividad.name,
                    modifier = Modifier.size(28.dp)
                )
            }
            Spacer(modifier = Modifier.height(12.dp))

            Text(actividad.name, fontSize = 14.sp, fontWeight = FontWeight.Bold, textAlign = TextAlign.Center, minLines = 2)
            Text("${actividad.caloriesPerMinute} kcal / min", fontSize = 11.sp, color = TextoSecundario, textAlign = TextAlign.Center)
            Spacer(modifier = Modifier.height(12.dp))

            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceEvenly,
                modifier = Modifier.fillMaxWidth().border(1.dp, BordeCard, RoundedCornerShape(8.dp)).padding(4.dp)
            ) {
                IconButton(onClick = { onMinutosChange(minutos - 10) }, modifier = Modifier.size(28.dp)) {
                    Text("-", color = AzulHabitus, fontWeight = FontWeight.Bold, fontSize = 18.sp)
                }
                Box(
                    modifier = Modifier.border(1.dp, BordeCard, RoundedCornerShape(4.dp)).padding(horizontal = 12.dp, vertical = 4.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text("$minutos", fontSize = 14.sp, fontWeight = FontWeight.Bold)
                }
                IconButton(onClick = { onMinutosChange(minutos + 10) }, modifier = Modifier.size(28.dp)) {
                    Text("+", color = AzulHabitus, fontWeight = FontWeight.Bold, fontSize = 18.sp)
                }
            }
            Spacer(modifier = Modifier.height(8.dp))
            Text("$minutos min", fontSize = 10.sp, color = TextoSecundario)
        }
    }
}

// --- TRADUCTOR DE IMÁGENES ---
@Composable
fun obtenerImagenAlimento(nombre: String): Int {
    return when (nombre.trim().lowercase()) {
        "agua" -> R.drawable.agua
        "tortilla de maíz" -> R.drawable.tortilla
        "arroz" -> R.drawable.arroz
        "avena" -> R.drawable.avena
        "carne de res asada" -> R.drawable.carne
        "ensalada mixta" -> R.drawable.ensalada
        "frijoles" -> R.drawable.frijoles
        "galletas saladas" -> R.drawable.galletas
        "huevos" -> R.drawable.huevos
        "jugo de naranja" -> R.drawable.jugo
        "leche" -> R.drawable.leche
        "manzana" -> R.drawable.manzana
        "nueces mixtas" -> R.drawable.nueces
        "pan integral" -> R.drawable.pan
        "pescado" -> R.drawable.pescado
        "plátano" -> R.drawable.platano
        "pollo" -> R.drawable.pollo
        "queso" -> R.drawable.queso
        "refresco" -> R.drawable.refresco
        "yogurt natural" -> R.drawable.yogurt
        else -> android.R.drawable.ic_menu_camera
    }
}

@Composable
fun obtenerImagenActividad(nombre: String): Int {
    return when (nombre.trim().lowercase()) {
        "bicicleta" -> R.drawable.bicicleta
        "caminar rápido" -> R.drawable.caminar_rapido
        "caminar suave" -> R.drawable.caminar_suave
        "correr intenso" -> R.drawable.correr_intenso
        "correr ligero" -> R.drawable.correr_ligero
        "entrenamiento de fuerza" -> R.drawable.fuerza
        "natación" -> R.drawable.natacion
        "saltar la cuerda" -> R.drawable.cuerda
        "subir escaleras" -> R.drawable.escaleras
        "yoga" -> R.drawable.yoga
        else -> android.R.drawable.ic_menu_directions
    }
}