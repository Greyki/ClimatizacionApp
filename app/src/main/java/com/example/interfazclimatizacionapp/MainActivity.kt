package com.example.interfazclimatizacionapp

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Slider
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

// Colores que utilizados en la pantalla
val Verde = Color(0xFF2E7D32)
val Rojo = Color(0xFFC62828)
val GrisClaro = Color(0xFFF2F2F2)
val GrisOscuro = Color(0xFF9E9E9E)
val GrisTexto = Color(0xFF666666)

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            Pantalla()
        }
    }
}

@Composable
fun Pantalla() {
    // Variables con estado: cuando cambian, la pantalla se redibuja sola
    var presencia by remember { mutableStateOf(true) }
    var temperatura by remember { mutableStateOf(27f) }
    var umbral by remember { mutableStateOf(26f) }

    // Pasamos los valores a enteros para mostrarlos sin decimales
    val temp = temperatura.toInt()
    val limite = umbral.toInt()

    // Este if decide qué muestra la tarjeta principal (3 estados)
    val estado: String
    val motivo: String
    val colorTarjeta: Color

    if (presencia && temp >= limite) {
        estado = "Encendido"
        motivo = "Hay presencia y $temp °C supera el umbral de $limite °C"
        colorTarjeta = Verde
    } else if (!presencia) {
        estado = "Apagado"
        motivo = "No hay nadie en la habitación"
        colorTarjeta = GrisOscuro
    } else {
        estado = "Apagado"
        motivo = "Hay presencia, pero $temp °C está bajo el umbral de $limite °C"
        colorTarjeta = GrisOscuro
    }

    // Valores que dependen del estado, para las tarjetas de sensores
    val textoPresencia = if (presencia) "Detectada" else "Vacía"
    val colorPresencia = if (presencia) Verde else GrisTexto
    val colorTemperatura = if (temp >= limite) Rojo else Color.Black
    val textoAlerta = if (estado == "Encendido") "Sonando" else "Silencio"
    val colorAlerta = if (estado == "Encendido") Rojo else GrisTexto

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .background(Color.White)
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {

        // Título
        Text("Climatización automática", fontSize = 20.sp, fontWeight = FontWeight.Bold)
        Text("Habitación 1", fontSize = 13.sp, color = GrisTexto)

        // Tarjeta principal: cambia de color y texto según el if de arriba
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .background(colorTarjeta, RoundedCornerShape(16.dp))
                .padding(16.dp)
        ) {
            Text("Aire acondicionado", fontSize = 13.sp, color = Color.White)
            Text(estado, fontSize = 28.sp, fontWeight = FontWeight.Bold, color = Color.White)
            Text(motivo, fontSize = 13.sp, color = Color.White)
        }

        // Primera fila de sensores
        Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            TarjetaSensor("Presencia", textoPresencia, colorPresencia, Modifier.weight(1f))
            TarjetaSensor("Temperatura", "$temp °C", colorTemperatura, Modifier.weight(1f))
        }

        // Segunda fila de sensores
        Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            TarjetaSensor("Humedad", "48 %", Color.Black, Modifier.weight(1f))
            TarjetaSensor("Alerta sonora", textoAlerta, colorAlerta, Modifier.weight(1f))
        }

        // Controles para probar la pantalla (simulan los sensores)
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .background(GrisClaro, RoundedCornerShape(12.dp))
                .padding(12.dp)
        ) {
            Text("Simulación de sensores", fontSize = 13.sp, color = GrisTexto)

            // Interruptor de presencia
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("Presencia", fontSize = 16.sp)
                Switch(
                    checked = presencia,
                    onCheckedChange = { presencia = it }
                )
            }

            // Slider de temperatura
            Text("Temperatura: $temp °C", fontSize = 16.sp)
            Slider(
                value = temperatura,
                onValueChange = { temperatura = it },
                valueRange = 15f..40f
            )
        }

        // Umbral de temperatura
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .background(GrisClaro, RoundedCornerShape(12.dp))
                .padding(12.dp)
        ) {
            Text("Umbral de temperatura", fontSize = 13.sp, color = GrisTexto)
            Text("$limite °C", fontSize = 22.sp, fontWeight = FontWeight.Bold)
            Slider(
                value = umbral,
                onValueChange = { umbral = it },
                valueRange = 18f..35f
            )
        }

        // Historial de temperatura (tabla)
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .background(GrisClaro, RoundedCornerShape(12.dp))
                .padding(12.dp)
        ) {
            Text("Historial de temperatura", fontSize = 13.sp, color = GrisTexto)

            FilaTabla("Hora", "Temp.", "Presencia", FontWeight.Bold)
            FilaTabla("14:00", "24 °C", "Sí")
            FilaTabla("14:30", "25 °C", "Sí")
            FilaTabla("15:00", "27 °C", "Sí")
            FilaTabla("15:30", "28 °C", "No")
        }
    }
}

// Una tarjeta de sensor. La escribimos una vez y la usamos 4 veces.
@Composable
fun TarjetaSensor(titulo: String, valor: String, colorValor: Color, modifier: Modifier) {
    Column(
        modifier = modifier
            .background(GrisClaro, RoundedCornerShape(12.dp))
            .padding(12.dp)
    ) {
        Text(titulo, fontSize = 12.sp, color = GrisTexto)
        Text(valor, fontSize = 20.sp, fontWeight = FontWeight.Bold, color = colorValor)
    }
}

// Una fila de la tabla: 3 celdas del mismo ancho
@Composable
fun FilaTabla(
    hora: String,
    temperatura: String,
    presencia: String,
    peso: FontWeight = FontWeight.Normal
) {
    Row(modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp)) {
        Text(hora, modifier = Modifier.weight(1f), fontWeight = peso)
        Text(temperatura, modifier = Modifier.weight(1f), fontWeight = peso)
        Text(presencia, modifier = Modifier.weight(1f), fontWeight = peso)
    }
}

@Preview(showBackground = true)
@Composable
fun VistaPrevia() {
    Pantalla()
}