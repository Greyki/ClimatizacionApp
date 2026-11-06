package com.example.interfazclimatizacionapp

import android.Manifest
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.ContextCompat

const val CANAL_ALERTAS = "alertas"
const val ID_CRITICA = 1
const val ID_ENCENDIDO = 2

// Android exige un "canal" para mostrar notificaciones. Crearlo varias veces no hace nada.
fun crearCanal(context: Context) {
    val canal = NotificationChannel(
        CANAL_ALERTAS,
        "Alertas de climatización",
        NotificationManager.IMPORTANCE_HIGH
    )
    context.getSystemService(NotificationManager::class.java).createNotificationChannel(canal)
}

fun notificar(context: Context, id: Int, titulo: String, texto: String) {
    // Desde Android 13 el usuario tiene que dar permiso. Sin permiso, no se muestra nada.
    if (Build.VERSION.SDK_INT >= 33 &&
        ContextCompat.checkSelfPermission(context, Manifest.permission.POST_NOTIFICATIONS)
        != PackageManager.PERMISSION_GRANTED
    ) return

    // Al tocar la notificación se abre la app (sin duplicar la pantalla si ya está abierta)
    val abrirApp = PendingIntent.getActivity(
        context,
        0,
        Intent(context, MainActivity::class.java)
            .addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP or Intent.FLAG_ACTIVITY_CLEAR_TOP),
        PendingIntent.FLAG_IMMUTABLE
    )

    val notificacion = NotificationCompat.Builder(context, CANAL_ALERTAS)
        .setSmallIcon(R.drawable.ic_alerta)
        .setContentTitle(titulo)
        .setContentText(texto)
        .setContentIntent(abrirApp)
        .setAutoCancel(true)
        .build()

    // Mismo id = reemplaza la notificación anterior de ese tipo en vez de acumularlas
    NotificationManagerCompat.from(context).notify(id, notificacion)
}

// Ejecuta accion() solo cuando condicion pasa de false a true.
// Así se avisa una vez al cruzar el límite, no cada vez que la temperatura cambia,
// y tampoco al abrir la pantalla.
// ponytail: sin margen, si la temperatura oscila justo en el límite avisa cada vez; agregar margen (ej. 1 °C) con sensores reales
@Composable
fun AlActivarse(condicion: Boolean, accion: () -> Unit) {
    var anterior by remember { mutableStateOf(condicion) }
    LaunchedEffect(condicion) {
        if (condicion && !anterior) accion()
        anterior = condicion
    }
}
