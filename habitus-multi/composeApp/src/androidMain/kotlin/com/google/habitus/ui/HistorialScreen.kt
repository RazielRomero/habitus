package com.google.habitus.ui

import android.content.Context
import android.content.Intent
import androidx.core.content.FileProvider
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.google.habitus.viewmodel.HistorialViewModel
import java.io.File
import java.io.FileOutputStream
import kotlinx.coroutines.launch

// --- COLORES PRINCIPALES Y VIBRANTES ---
val AzulBorde = Color(0xFF1976D2)
val AzulRelleno = Color(0xFFBBDEFB)
val NaranjaBorde = Color(0xFFF57C00)
val NaranjaRelleno = Color(0xFFFFE0B2)
val FondoDashboard = Color(0xFFF8F9FA)
val GrisBordeTarjeta = Color(0xFFE0E0E0)

// Colores vibrantes para las píldoras de la tabla (Estado)
val AzulVibranteTexto = Color(0xFF0056B3)
val AzulVibranteFondo = Color(0xFFE3F2FD)

val RojoVibranteTexto = Color(0xFFC62828)
val RojoVibranteFondo = Color(0xFFFFEBEE)

val NaranjaVibranteTexto = Color(0xFFE65100)
val NaranjaVibranteFondo = Color(0xFFFFF3E0)

// ==========================================
// PANTALLA PRINCIPAL (Orquestador)
// ==========================================
@Composable
fun HistorialScreen(viewModel: HistorialViewModel = remember { HistorialViewModel() }) {
    val cargando by viewModel.cargando.collectAsState()
    val filtroSeleccionado by viewModel.filtroDias.collectAsState()
    var tabActivo by remember { mutableStateOf(0) }

    // Estados para validación y notificación de borrado
    var fechaParaEliminar by remember { mutableStateOf<String?>(null) }
    var mostrarMensajeExito by remember { mutableStateOf(false) }

    // ESTADOS Y ÁMBITOS PARA LA GESTIÓN DE PDF
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    var exportandoPdf by remember { mutableStateOf(false) }

    if (cargando) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            CircularProgressIndicator(color = AzulBorde)
        }
        return
    }

    // Cuadro de diálogo de confirmación de borrado
    if (fechaParaEliminar != null) {
        AlertDialog(
            onDismissRequest = { fechaParaEliminar = null },
            title = { Text("¿Eliminar registro?") },
            text = { Text("¿Estás seguro de que deseas borrar por completo el registro del día ${fechaParaEliminar}? Esta acción no se puede deshacer.") },
            dismissButton = {
                TextButton(onClick = { fechaParaEliminar = null }) {
                    Text("Cancelar", color = Color.Gray)
                }
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        val fecha = fechaParaEliminar
                        if (fecha != null) {
                            viewModel.eliminarDiaCompleto(fecha)
                            fechaParaEliminar = null
                            mostrarMensajeExito = true
                        }
                    }
                ) {
                    Text("Eliminar", color = Color(0xFFD32F2F))
                }
            }
        )
    }

    // Aviso de registro eliminado con éxito
    if (mostrarMensajeExito) {
        AlertDialog(
            onDismissRequest = { mostrarMensajeExito = false },
            title = { Text("Aviso") },
            text = { Text("El registro ha sido eliminado con éxito.") },
            confirmButton = {
                TextButton(onClick = { mostrarMensajeExito = false }) {
                    Text("Aceptar", color = AzulBorde)
                }
            }
        )
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(FondoDashboard)
            .padding(16.dp)
    ) {
        // 🔥 LÍNEA 1: Cabecera y Botón PDF con espacio entre ellos
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text("Dashboard", fontSize = 24.sp, fontWeight = FontWeight.Bold, color = Color.DarkGray)

            // COMPONENTE DEL BOTÓN EXPORTAR / PROGRESO
            if (exportandoPdf) {
                CircularProgressIndicator(modifier = Modifier.size(24.dp), strokeWidth = 2.dp, color = AzulBorde)
            } else {
                IconButton(
                    onClick = {
                        coroutineScope.launch {
                            exportandoPdf = true
                            val api = com.google.habitus.api.ApiClient()
                            val idUsuario = com.google.habitus.model.UsuarioSession.idUsuarioActual ?: 0

                            val bytes = api.descargarReportePdf(idUsuario, filtroSeleccionado)
                            if (bytes != null) {
                                guardarYAbrirPdf(context, bytes, "Reporte_Habitus_${filtroSeleccionado}_Dias")
                            }
                            exportandoPdf = false
                        }
                    }
                ) {
                    Icon(
                        imageVector = Icons.Default.Share,
                        contentDescription = "Exportar PDF",
                        tint = AzulBorde
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        //  Pestañas centradas con su propio espacio
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.Center
        ) {
            Row(
                modifier = Modifier
                    .border(1.dp, AzulBorde, RoundedCornerShape(8.dp))
                    .clip(RoundedCornerShape(8.dp))
            ) {
                TabPestana("Gráficas", activo = tabActivo == 0) { tabActivo = 0 }
                TabPestana("Tabla", activo = tabActivo == 1) { tabActivo = 1 }
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        // Filtros de Tiempo
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            FiltroBoton("7 Días", activo = filtroSeleccionado == 7) { viewModel.cambiarFiltro(7) }
            FiltroBoton("15 Días", activo = filtroSeleccionado == 15) { viewModel.cambiarFiltro(15) }
            FiltroBoton("30 Días", activo = filtroSeleccionado == 30) { viewModel.cambiarFiltro(30) }
        }
        Spacer(modifier = Modifier.height(16.dp))

        // Contenedor Dinámico
        if (tabActivo == 0) {
            VistaGraficas(viewModel)
        } else {
            VistaTabla(viewModel, onIntentarEliminar = { fechaParaEliminar = it })
        }
    }
}

// ==========================================
// SUB-PANTALLA 1: GRÁFICAS
// ==========================================
@Composable
fun VistaGraficas(viewModel: HistorialViewModel) {
    val resumen by viewModel.resumen.collectAsState()

    Column(modifier = Modifier.verticalScroll(rememberScrollState())) {
        val lista = resumen.historialSemanal
        val promConsumido = if (lista.isNotEmpty()) lista.map { it.consumidas }.average().toInt() else 0
        val promQuemado = if (lista.isNotEmpty()) lista.map { it.quemadas }.average().toInt() else 0
        val balancePromedio = promConsumido - promQuemado

        LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            item { PildoraResumen("Prom. consumido:", "$promConsumido kcal") }
            item { PildoraResumen("Prom. quemado:", "$promQuemado kcal") }
            item { PildoraResumen("Balance Neto:", "$balancePromedio kcal") }
        }

        Spacer(modifier = Modifier.height(24.dp))

        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = Color.White),
            border = BorderStroke(1.dp, GrisBordeTarjeta)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text("CALORÍAS CONSUMIDAS VS QUEMADAS", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = Color.Gray)
                Spacer(modifier = Modifier.height(16.dp))

                val maxCalorias = resumen.historialSemanal.maxOfOrNull { maxOf(it.consumidas, it.quemadas) }?.coerceAtLeast(1) ?: 1000

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(200.dp)
                        .horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(16.dp),
                    verticalAlignment = Alignment.Bottom
                ) {
                    resumen.historialSemanal.forEach { dia ->
                        DobleBarra(dia.fecha, dia.consumidas, dia.quemadas, maxCalorias)
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = Color.White),
            border = BorderStroke(1.dp, GrisBordeTarjeta)
        ) {
            Column(modifier = Modifier.padding(16.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                Text("ESTADO ENERGÉTICO", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = Color.Gray, modifier = Modifier.fillMaxWidth())
                Spacer(modifier = Modifier.height(24.dp))

                val meta = 2000f
                val consumidas = resumen.consumidas.toFloat()
                val progresoVisible = if (consumidas > 0) (consumidas / meta).coerceAtMost(1f) else 0f
                val porcentaje = if (meta > 0) ((consumidas / meta) * 100).toInt() else 0

                Box(contentAlignment = Alignment.Center) {
                    CircularProgressIndicator(
                        progress = 1f,
                        modifier = Modifier.size(160.dp),
                        strokeWidth = 16.dp,
                        color = AzulRelleno
                    )
                    CircularProgressIndicator(
                        progress = progresoVisible,
                        modifier = Modifier.size(160.dp),
                        strokeWidth = 16.dp,
                        color = AzulBorde
                    )

                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text("$porcentaje%", fontSize = 32.sp, fontWeight = FontWeight.ExtraBold, color = AzulBorde)
                        Text("${resumen.consumidas} kcal", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = Color.DarkGray)
                        Text("de ${meta.toInt()} kcal", fontSize = 12.sp, color = Color.Gray)
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))

                val mensajeInteligente = when {
                    porcentaje == 0 -> "Aún no has registrado calorías hoy."
                    porcentaje < 50 -> "Aún tienes energía disponible. ¡No olvides tus comidas!"
                    porcentaje <= 100 -> "¡Excelente! Estás dentro de tu meta diaria."
                    else -> "¡Cuidado! Has superado tu meta calórica."
                }

                Text(
                    text = mensajeInteligente,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Medium,
                    color = Color.DarkGray,
                    textAlign = TextAlign.Center
                )

                Spacer(modifier = Modifier.height(16.dp))

                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.Center, verticalAlignment = Alignment.CenterVertically) {
                    Box(modifier = Modifier.size(12.dp).clip(RoundedCornerShape(6.dp)).background(AzulBorde))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Consumido", fontSize = 12.sp, color = Color.Gray)

                    Spacer(modifier = Modifier.width(24.dp))

                    Box(modifier = Modifier.size(12.dp).clip(RoundedCornerShape(6.dp)).background(AzulRelleno))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Restante", fontSize = 12.sp, color = Color.Gray)
                }
            }
        }
        Spacer(modifier = Modifier.height(32.dp))
    }
}

// ==========================================
// SUB-PANTALLA 2: TABLA
// ==========================================
@Composable
fun VistaTabla(viewModel: HistorialViewModel, onIntentarEliminar: (String) -> Unit) {
    val resumen by viewModel.resumen.collectAsState()

    Card(
        modifier = Modifier.fillMaxSize(),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        shape = RoundedCornerShape(8.dp),
        border = BorderStroke(1.dp, GrisBordeTarjeta)
    ) {
        Column {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Color(0xFFF8F9FA))
                    .padding(horizontal = 16.dp, vertical = 12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("FECHA", modifier = Modifier.weight(2f), fontSize = 10.sp, fontWeight = FontWeight.ExtraBold, color = Color.Gray)
                Text("CONSUMIDO", modifier = Modifier.weight(1.5f), fontSize = 10.sp, fontWeight = FontWeight.ExtraBold, color = Color.Gray)
                Text("QUEMADO", modifier = Modifier.weight(1.5f), fontSize = 10.sp, fontWeight = FontWeight.ExtraBold, color = Color.Gray)
                Text("BALANCE", modifier = Modifier.weight(1.5f), fontSize = 10.sp, fontWeight = FontWeight.ExtraBold, color = Color.Gray)
                Text("ESTADO", modifier = Modifier.weight(1.5f), fontSize = 10.sp, fontWeight = FontWeight.ExtraBold, color = Color.Gray)
                Spacer(modifier = Modifier.weight(0.5f))
            }
            Divider(color = GrisBordeTarjeta, thickness = 1.dp)

            val diasConRegistro = resumen.historialSemanal.filter { it.consumidas > 0 || it.quemadas > 0 }

            if (diasConRegistro.isEmpty()) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text("No hay registros en este periodo.", color = Color.Gray)
                }
            } else {
                LazyColumn(modifier = Modifier.fillMaxSize()) {
                    items(diasConRegistro) { dia ->
                        val balance = dia.consumidas - dia.quemadas
                        val (textoEstado, colorTexto, colorFondo) = when {
                            balance < -500 -> Triple("Deficiente", NaranjaVibranteTexto, NaranjaVibranteFondo)
                            balance > 500 -> Triple("Excesivo", RojoVibranteTexto, RojoVibranteFondo)
                            else -> Triple("Equilibrado", AzulVibranteTexto, AzulVibranteFondo)
                        }

                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 16.dp, vertical = 16.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(dia.fecha, modifier = Modifier.weight(2f), fontSize = 12.sp, color = Color.DarkGray)
                            Text("${dia.consumidas} kcal", modifier = Modifier.weight(1.5f), fontSize = 12.sp, color = Color.DarkGray)
                            Text("${dia.quemadas} kcal", modifier = Modifier.weight(1.5f), fontSize = 12.sp, color = Color.DarkGray)
                            Text("$balance", modifier = Modifier.weight(1.5f), fontSize = 12.sp, fontWeight = FontWeight.Bold, color = Color.DarkGray)

                            Box(modifier = Modifier.weight(1.5f)) {
                                Box(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(12.dp))
                                        .background(colorFondo)
                                        .padding(horizontal = 10.dp, vertical = 4.dp),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(textoEstado, fontSize = 10.sp, fontWeight = FontWeight.Bold, color = colorTexto)
                                }
                            }

                            IconButton(
                                onClick = { onIntentarEliminar(dia.fecha) },
                                modifier = Modifier.weight(0.5f).size(24.dp)
                            ) {
                                Icon(Icons.Filled.Delete, contentDescription = "Borrar", tint = Color.Red.copy(alpha = 0.8f))
                            }
                        }
                        Divider(color = GrisBordeTarjeta, thickness = 0.5.dp)
                    }
                }
            }
        }
    }
}

// COMPONENTES VISUALES REUTILIZABLES
@Composable
fun TabPestana(texto: String, activo: Boolean, onClick: () -> Unit) {
    Box(
        modifier = Modifier
            .background(if (activo) AzulBorde else Color.Transparent)
            .clickable { onClick() }
            .padding(horizontal = 24.dp, vertical = 8.dp)
    ) {
        Text(
            text = texto,
            fontSize = 14.sp,
            fontWeight = FontWeight.Bold,
            color = if (activo) Color.White else AzulBorde
        )
    }
}

@Composable
fun FiltroBoton(texto: String, activo: Boolean, onClick: () -> Unit) {
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(16.dp))
            .background(if (activo) AzulRelleno else FondoDashboard)
            .border(1.dp, if (activo) AzulBorde else GrisBordeTarjeta, RoundedCornerShape(16.dp))
            .clickable { onClick() }
            .padding(horizontal = 16.dp, vertical = 6.dp)
    ) {
        Text(texto, fontSize = 12.sp, fontWeight = FontWeight.Bold, color = if (activo) AzulBorde else Color.Gray)
    }
}

@Composable
fun PildoraResumen(titulo: String, valor: String) {
    Surface(shape = RoundedCornerShape(16.dp), border = BorderStroke(1.dp, GrisBordeTarjeta), color = Color.White) {
        Row(modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp)) {
            Text(titulo, fontSize = 12.sp, color = Color.Gray)
            Spacer(modifier = Modifier.width(4.dp))
            Text(valor, fontSize = 12.sp, fontWeight = FontWeight.Bold, color = AzulBorde)
        }
    }
}

@Composable
fun DobleBarra(fecha: String, consumidas: Int, quemadas: Int, max: Int) {
    val alturaConsumo = (consumidas.toFloat() / max).coerceIn(0.01f, 1f)
    val alturaQuema = (quemadas.toFloat() / max).coerceIn(0.01f, 1f)

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Bottom,
        modifier = Modifier.fillMaxHeight()
    ) {
        Row(
            modifier = Modifier.weight(1f),
            verticalAlignment = Alignment.Bottom,
            horizontalArrangement = Arrangement.spacedBy(2.dp)
        ) {
            Box(modifier = Modifier.width(16.dp).fillMaxHeight(alturaConsumo).background(AzulRelleno).border(1.dp, AzulBorde))
            Box(modifier = Modifier.width(16.dp).fillMaxHeight(alturaQuema).background(NaranjaRelleno).border(1.dp, NaranjaBorde))
        }
        Spacer(modifier = Modifier.height(8.dp))
        Text(fecha, fontSize = 10.sp, color = Color.Gray)
    }
}

// FUNCIÓN AUXILIAR PARA ALMACENAR Y DISPARAR EL VISOR DE PDF NATIVO
fun guardarYAbrirPdf(context: Context, pdfBytes: ByteArray, nombreArchivo: String) {
    try {
        val carpeta = File(context.cacheDir, "reportes")
        if (!carpeta.exists()) carpeta.mkdirs()

        val archivo = File(carpeta, "$nombreArchivo.pdf")
        val stream = FileOutputStream(archivo)
        stream.write(pdfBytes)
        stream.close()

        val uri = FileProvider.getUriForFile(
            context,
            "${context.packageName}.provider",
            archivo
        )

        val intent = Intent(Intent.ACTION_VIEW).apply {
            setDataAndType(uri, "application/pdf")
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        }
        context.startActivity(intent)
    } catch (e: Exception) {
        e.printStackTrace()
    }
}