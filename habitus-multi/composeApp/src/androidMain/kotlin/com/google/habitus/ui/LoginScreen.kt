package com.google.habitus.ui

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.google.habitus.R
import com.google.habitus.viewmodel.LoginViewModel

@Composable
fun LoginScreen(
    onLoginExitoso: () -> Unit,
    onNavegarRegistro: () -> Unit,
    viewModel: LoginViewModel = viewModel<LoginViewModel>()
) {
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }

    //Bandera para encender los errores visuales si intentan entrar sin llenar datos
    var mostrarErroresVacios by remember { mutableStateOf(false) }

    // Escuchamos las respuestas del ViewModel
    val estadoAuth by viewModel.estadoAuth.collectAsState()
    val navegarAlHome by viewModel.navegarAlHome.collectAsState()

    LaunchedEffect(navegarAlHome) {
        if (navegarAlHome) {
            viewModel.resetearNavegacion()
            onLoginExitoso()
        }
    }

    val azulBoton = Color(0xFF1565C0)
    val grisFondo = Color(0xFFF2F4F6)

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(grisFondo),
        contentAlignment = Alignment.Center
    ) {
        Card(
            modifier = Modifier
                .fillMaxWidth(0.85f)
                .wrapContentHeight(),
            shape = RoundedCornerShape(28.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White),
            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
        ) {
            Column(
                modifier = Modifier.padding(horizontal = 24.dp, vertical = 32.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Image(
                    painter = painterResource(id = R.drawable.logo),
                    contentDescription = "Logo",
                    modifier = Modifier.height(150.dp),
                    contentScale = ContentScale.Fit
                )

                Spacer(modifier = Modifier.height(24.dp))

                Text("Bienvenido", fontSize = 28.sp, fontWeight = FontWeight.Bold)
                Text("Inicia sesión para continuar", fontSize = 14.sp, color = Color.Gray)

                Spacer(modifier = Modifier.height(32.dp))

                // Email
                Column(modifier = Modifier.fillMaxWidth()) {
                    Text("CORREO ELECTRÓNICO", fontSize = 11.sp, fontWeight = FontWeight.Bold, modifier = Modifier.padding(bottom = 4.dp))

                    val errorEmail = mostrarErroresVacios && email.isBlank()

                    OutlinedTextField(
                        value = email,
                        onValueChange = { email = it },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp),
                        isError = errorEmail,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedContainerColor = Color(0xFFF9F9F9),
                            unfocusedContainerColor = Color(0xFFF9F9F9),
                            focusedBorderColor = azulBoton,
                            unfocusedBorderColor = Color(0xFFE0E0E0)
                        ),
                        singleLine = true,
                        supportingText = {
                            if (errorEmail) Text("Ingresa tu correo", color = MaterialTheme.colorScheme.error)
                        }
                    )
                }

                Spacer(modifier = Modifier.height(8.dp))

                // Password
                Column(modifier = Modifier.fillMaxWidth()) {
                    Text("CONTRASEÑA", fontSize = 11.sp, fontWeight = FontWeight.Bold, modifier = Modifier.padding(bottom = 4.dp))

                    val errorPassword = mostrarErroresVacios && password.isBlank()

                    OutlinedTextField(
                        value = password,
                        onValueChange = { password = it },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp),
                        visualTransformation = PasswordVisualTransformation(),
                        isError = errorPassword,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedContainerColor = Color(0xFFF9F9F9),
                            unfocusedContainerColor = Color(0xFFF9F9F9),
                            focusedBorderColor = azulBoton,
                            unfocusedBorderColor = Color(0xFFE0E0E0)
                        ),
                        singleLine = true,
                        supportingText = {
                            if (errorPassword) Text("Ingresa tu contraseña", color = MaterialTheme.colorScheme.error)
                        }
                    )
                }

                // Mostramos los errores o el estado de carga que viene desde el servidor
                if (estadoAuth.isNotEmpty() && estadoAuth != "Ingresa tus datos") {
                    Spacer(modifier = Modifier.height(8.dp))

                    // Contenedor bonito para errores
                    val esError = estadoAuth.contains("Error")
                    Surface(
                        color = if (esError) Color(0xFFFFEBEE) else Color(0xFFE3F2FD),
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(
                            text = estadoAuth,
                            color = if (esError) Color(0xFFC62828) else azulBoton,
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Medium,
                            modifier = Modifier.padding(12.dp),
                            textAlign = androidx.compose.ui.text.style.TextAlign.Center
                        )
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))

                Button(
                    onClick = {
                        //  Validamos visualmente antes de ir al servidor
                        if (email.isBlank() || password.isBlank()) {
                            mostrarErroresVacios = true
                        } else {
                            mostrarErroresVacios = false
                            viewModel.realizarLogin(email.trim(), password)
                        }
                    },
                    modifier = Modifier.fillMaxWidth().height(50.dp),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = azulBoton)
                ) {
                    Text("Entrar", fontWeight = FontWeight.Bold, color = Color.White)
                }

                Spacer(modifier = Modifier.height(24.dp))

                Row {
                    Text("¿No tienes cuenta? ", color = Color.Gray)
                    Text(
                        "Regístrate",
                        color = azulBoton,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.clickable { onNavegarRegistro() }
                    )
                }
            }
        }
    }
}