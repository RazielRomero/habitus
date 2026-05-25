package com.google.habitus.ui

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.google.habitus.R

enum class PestanaHome {
    INDEX,
    REGISTRO_DIARIO,
    HISTORIAL,
    MI_CUENTA,
    CALCULAR_IMC
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(onLogout: () -> Unit) {
    var pestanaActual by remember { mutableStateOf(PestanaHome.INDEX) }
    var menuExpandido by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    // CONTENEDOR PARA LOGO Y TEXTO
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(12.dp),
                        modifier = Modifier.padding(start = 0.dp)
                    ) {
                        Image(
                            painter = painterResource(id = R.drawable.logo2),
                            contentDescription = "Logo Habitus",
                            modifier = Modifier
                                .height(100.dp) // Aumentado para mejor visibilidad
                                .widthIn(max = 150.dp),
                            contentScale = ContentScale.Fit
                        )
                        Text(
                            text = "",
                            fontWeight = FontWeight.ExtraBold,
                            color = Color.White,
                            fontSize = 20.sp,
                            letterSpacing = 1.2.sp
                        )
                    }
                },
                navigationIcon = {
                    if (pestanaActual != PestanaHome.INDEX) {
                        IconButton(onClick = { pestanaActual = PestanaHome.INDEX }) {
                            // Ícono oficial de flecha hacia atrás en blanco puro
                            Icon(
                                imageVector = Icons.Default.ArrowBack,
                                contentDescription = "Volver al inicio",
                                tint = Color.White
                            )
                        }
                    }
                },
                actions = {
                    //Ícono oficial del menú hamburguesa en blanco puro
                    IconButton(onClick = { menuExpandido = true }) {
                        Icon(
                            imageVector = Icons.Default.Menu,
                            contentDescription = "Menú de opciones",
                            tint = Color.White
                        )
                    }

                    DropdownMenu(
                        expanded = menuExpandido,
                        onDismissRequest = { menuExpandido = false }
                    ) {
                        DropdownMenuItem(
                            text = { Text("Inicio") },
                            onClick = { pestanaActual = PestanaHome.INDEX; menuExpandido = false }
                        )
                        DropdownMenuItem(
                            text = { Text("Registro diario") },
                            onClick = { pestanaActual = PestanaHome.REGISTRO_DIARIO; menuExpandido = false }
                        )
                        DropdownMenuItem(
                            text = { Text("Historial") },
                            onClick = { pestanaActual = PestanaHome.HISTORIAL; menuExpandido = false }
                        )
                        DropdownMenuItem(
                            text = { Text("Mi cuenta") },
                            onClick = { pestanaActual = PestanaHome.MI_CUENTA; menuExpandido = false }
                        )
                        DropdownMenuItem(
                            text = { Text("Calcular IMC") },
                            onClick = { pestanaActual = PestanaHome.CALCULAR_IMC; menuExpandido = false }
                        )
                    }
                },
                // Fondo de la barra superior en Azul
                colors = TopAppBarDefaults.topAppBarColors(containerColor = AzulHabitus)
            )
        },
        containerColor = FondoHabitus
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .padding(paddingValues)
                .fillMaxSize()
        ) {
            when (pestanaActual) {
                PestanaHome.INDEX -> IndexScreen(onNavegar = { pestanaActual = it })
                PestanaHome.REGISTRO_DIARIO -> RegistroDiarioScreen()
                PestanaHome.HISTORIAL -> HistorialScreen()

                // Conecta tus pantallas reales aquí
                PestanaHome.MI_CUENTA -> MiCuentaScreen(onCerrarSesion = { onLogout() })
                PestanaHome.CALCULAR_IMC -> ImcScreen()
            }
        }
    }
}

@Composable
fun VistaTemporal(titulo: String) {
    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(titulo, style = MaterialTheme.typography.headlineMedium)
            Text("Pantalla en construcción", color = MaterialTheme.colorScheme.secondary)
        }
    }
}