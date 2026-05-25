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
import kotlin.math.pow
import kotlin.math.roundToInt

@Composable
fun ImcScreen() {
    var pesoInput by remember { mutableStateOf("") }
    var alturaInput by remember { mutableStateOf("") }
    var imcResultado by remember { mutableStateOf(0.0) }
    var categoria by remember { mutableStateOf("") }
    var colorCategoria by remember { mutableStateOf(Color.Gray) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
            .verticalScroll(rememberScrollState()),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Spacer(modifier = Modifier.height(16.dp))
        Text("Calculadora de IMC", style = MaterialTheme.typography.headlineMedium, color = AzulHabitus, fontWeight = FontWeight.Bold)
        Text("Descubre tu Índice de Masa Corporal", color = Color.Gray)

        Spacer(modifier = Modifier.height(24.dp))

        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = Color.White),
            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
        ) {
            Column(modifier = Modifier.padding(16.dp), horizontalAlignment = Alignment.CenterHorizontally) {

                OutlinedTextField(
                    value = pesoInput,
                    onValueChange = { pesoInput = it },
                    label = { Text("Peso (kg)") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(16.dp))

                OutlinedTextField(
                    value = alturaInput,
                    onValueChange = { alturaInput = it },
                    label = { Text("Altura (cm)") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(24.dp))

                Button(
                    onClick = {
                        val peso = pesoInput.toDoubleOrNull() ?: 0.0
                        val alturaCm = alturaInput.toDoubleOrNull() ?: 0.0

                        if (peso > 0 && alturaCm > 0) {
                            val alturaM = alturaCm / 100
                            imcResultado = peso / alturaM.pow(2)

                            // Determinar en qué categoría cae
                            when {
                                imcResultado < 18.5 -> {
                                    categoria = "Bajo peso"
                                    colorCategoria = Color(0xFF2196F3) // Azul
                                }
                                imcResultado in 18.5..24.9 -> {
                                    categoria = "Peso normal"
                                    colorCategoria = Color(0xFF4CAF50) // Verde
                                }
                                imcResultado in 25.0..29.9 -> {
                                    categoria = "Sobrepeso"
                                    colorCategoria = Color(0xFFFF9800) // Naranja
                                }
                                else -> {
                                    categoria = "Obesidad"
                                    colorCategoria = Color(0xFFF44336) // Rojo
                                }
                            }
                        }
                    },
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.buttonColors(containerColor = AzulHabitus)
                ) {
                    Text("Calcular")
                }
            }
        }

        // Esta tarjeta solo aparece si ya se hizo un cálculo
        if (imcResultado > 0.0) {
            Spacer(modifier = Modifier.height(24.dp))
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = colorCategoria.copy(alpha = 0.1f)),
                elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
            ) {
                Column(
                    modifier = Modifier.padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text("Tu IMC es", fontSize = 16.sp, color = Color.Gray)

                    // Redondeamos para que no salgan 10 decimales
                    val imcRedondeado = (imcResultado * 10.0).roundToInt() / 10.0
                    Text("$imcRedondeado", fontSize = 48.sp, fontWeight = FontWeight.Bold, color = colorCategoria)

                    Text(categoria, fontSize = 20.sp, fontWeight = FontWeight.SemiBold, color = colorCategoria)
                }
            }
        }
    }
}