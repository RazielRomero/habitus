package com.google.habitus.ui

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AddCircleOutline
import androidx.compose.material.icons.rounded.DateRange
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.google.habitus.R

val AzulHabitusIndex = Color(0xFF1976D2)

@Composable
fun IndexScreen(onNavegar: (PestanaHome) -> Unit) {
    Column(modifier = Modifier.fillMaxSize().verticalScroll(rememberScrollState())) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(AzulHabitusIndex, RoundedCornerShape(bottomStart = 32.dp, bottomEnd = 32.dp))
                .padding(top = 16.dp, bottom = 60.dp, start = 24.dp, end = 24.dp)
        ) {
            Column {
                Text("Bienvenido/a", color = Color.White.copy(alpha = 0.8f), fontSize = 16.sp)
                Text("¿Qué deseas hacer hoy?", color = Color.White, fontSize = 24.sp, fontWeight = FontWeight.Bold)
            }
        }

        Card(
            modifier = Modifier.fillMaxWidth().padding(horizontal = 24.dp).offset(y = (-30).dp).shadow(8.dp, RoundedCornerShape(24.dp)),
            colors = CardDefaults.cardColors(containerColor = Color.White),
            shape = RoundedCornerShape(24.dp)
        ) {
            Column(modifier = Modifier.padding(24.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                Image(painter = painterResource(id = R.drawable.index), contentDescription = null, modifier = Modifier.size(160.dp), contentScale = ContentScale.Fit)
                Spacer(modifier = Modifier.height(16.dp))
                Text("Registra lo que comes y tu actividad física, consulta tu historial y mantén un control sencillo de tu balance diario.", color = Color.DarkGray, fontSize = 14.sp, textAlign = androidx.compose.ui.text.style.TextAlign.Center, lineHeight = 20.sp)
                Spacer(modifier = Modifier.height(24.dp))
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                    BotonAccion("Registrar\nDatos", Icons.Filled.AddCircleOutline, Color(0xFFE3F2FD), AzulHabitusIndex, Modifier.weight(1f)) { onNavegar(PestanaHome.REGISTRO_DIARIO) }
                    BotonAccion("Ver\nHistorial", Icons.Rounded.DateRange, Color(0xFFFFF3E0), Color(0xFFF57C00), Modifier.weight(1f)) { onNavegar(PestanaHome.HISTORIAL) }
                }
                Spacer(modifier = Modifier.height(16.dp))
                Text("Salud, hábitos y equilibrio.", fontSize = 12.sp, color = AzulHabitusIndex, fontWeight = FontWeight.Bold)
            }
        }

        Card(
            modifier = Modifier.fillMaxWidth().padding(horizontal = 24.dp, vertical = 8.dp).padding(bottom = 32.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White),
            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
            shape = RoundedCornerShape(24.dp)
        ) {
            Column(modifier = Modifier.padding(24.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                Text("¿Quiénes somos?", color = AzulHabitusIndex, fontSize = 20.sp, fontWeight = FontWeight.Bold, modifier = Modifier.fillMaxWidth())
                Spacer(modifier = Modifier.height(16.dp))
                Image(painter = painterResource(id = R.drawable.nosotros), contentDescription = null, modifier = Modifier.height(180.dp).fillMaxWidth(), contentScale = ContentScale.Fit)
                Spacer(modifier = Modifier.height(16.dp))
                Text("HABITUS te ayuda a registrar tus hábitos de alimentación y actividad física mediante pictogramas claros y fáciles de usar.", color = Color.DarkGray, fontSize = 14.sp, textAlign = androidx.compose.ui.text.style.TextAlign.Justify, lineHeight = 20.sp)
            }
        }
    }
}

@Composable
fun BotonAccion(texto: String, icono: ImageVector, colorFondo: Color, colorIcono: Color, modifier: Modifier = Modifier, onClick: () -> Unit) {
    Column(modifier = modifier.clip(RoundedCornerShape(16.dp)).background(colorFondo).clickable { onClick() }.padding(16.dp), horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.Center) {
        Icon(imageVector = icono, contentDescription = null, tint = colorIcono, modifier = Modifier.size(32.dp))
        Spacer(modifier = Modifier.height(8.dp))
        Text(text = texto, color = Color.DarkGray, fontWeight = FontWeight.Bold, fontSize = 14.sp, textAlign = androidx.compose.ui.text.style.TextAlign.Center)
    }
}