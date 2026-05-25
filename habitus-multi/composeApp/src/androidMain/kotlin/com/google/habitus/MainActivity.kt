package com.google.habitus

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.*
import com.google.habitus.ui.HomeScreen
import com.google.habitus.ui.LoginScreen
import com.google.habitus.ui.RegistroScreen

// Creamos un "mapa" simple de nuestras pantallas
enum class Pantalla {
    LOGIN,
    REGISTRO,
    HOME
}

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        enableEdgeToEdge()
        super.onCreate(savedInstanceState)

        setContent {
            // Empezamos siempre en la pantalla de Login
            var pantallaActual by remember { mutableStateOf(Pantalla.LOGIN) }

            // Aquí el sistema decide qué dibujar
            when (pantallaActual) {
                Pantalla.LOGIN -> {
                    LoginScreen(
                        onLoginExitoso = { pantallaActual = Pantalla.HOME },
                        onNavegarRegistro = { pantallaActual = Pantalla.REGISTRO } // 👈 Conectamos el botón para cambiar de pantalla
                    )
                }
                Pantalla.REGISTRO -> {
                    // Aquí llamas a tu pantalla de registro (Asegúrate de tenerla creada)
                    RegistroScreen(
                        onVolverAlLogin = { pantallaActual = Pantalla.LOGIN }
                    )
                }
                Pantalla.HOME -> {
                    HomeScreen(
                        onLogout = { pantallaActual = Pantalla.LOGIN }
                    )
                }
            }
        }
    }
}