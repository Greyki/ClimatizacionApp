package com.example.interfazclimatizacionapp

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.google.firebase.FirebaseNetworkException
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException
import com.google.firebase.auth.FirebaseAuthInvalidUserException
import com.google.firebase.auth.FirebaseAuthUserCollisionException
import com.google.firebase.auth.FirebaseAuthWeakPasswordException

@Composable
fun PantallaLogin(onIngreso: () -> Unit) {
    var correo by remember { mutableStateOf("") }
    var clave by remember { mutableStateOf("") }
    var error by remember { mutableStateOf<String?>(null) }
    var cargando by remember { mutableStateOf(false) }

    // Inicia sesión o crea la cuenta. Firebase responde igual en los dos casos.
    fun enviar(crearCuenta: Boolean) {
        // Firebase se cae si recibe textos vacíos, por eso se revisa antes
        if (correo.isBlank() || clave.isBlank()) {
            error = "Completa el correo y la contraseña"
            return
        }
        val auth = FirebaseAuth.getInstance()
        val tarea = if (crearCuenta) {
            auth.createUserWithEmailAndPassword(correo.trim(), clave)
        } else {
            auth.signInWithEmailAndPassword(correo.trim(), clave)
        }
        cargando = true
        error = null
        tarea.addOnCompleteListener { resultado ->
            cargando = false
            if (resultado.isSuccessful) onIngreso() else error = mensajeError(resultado.exception)
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.White)
            .padding(24.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp, Alignment.CenterVertically),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Icon(
            painterResource(R.drawable.ic_aire),
            contentDescription = null,
            tint = Verde,
            modifier = Modifier.size(64.dp)
        )
        Text("Climatización automática", fontSize = 20.sp, fontWeight = FontWeight.Bold)
        Text("Inicia sesión para continuar", fontSize = 13.sp, color = GrisTexto)

        OutlinedTextField(
            value = correo,
            onValueChange = { correo = it },
            label = { Text("Correo") },
            singleLine = true,
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
            modifier = Modifier.fillMaxWidth()
        )
        OutlinedTextField(
            value = clave,
            onValueChange = { clave = it },
            label = { Text("Contraseña") },
            singleLine = true,
            visualTransformation = PasswordVisualTransformation(),
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
            modifier = Modifier.fillMaxWidth()
        )

        // Solo se muestra si hay un error
        error?.let { Text(it, fontSize = 13.sp, color = Rojo) }

        Button(
            onClick = { enviar(crearCuenta = false) },
            enabled = !cargando,
            colors = ButtonDefaults.buttonColors(containerColor = Verde),
            modifier = Modifier.fillMaxWidth()
        ) {
            Text(if (cargando) "Ingresando..." else "Iniciar sesión")
        }
        TextButton(onClick = { enviar(crearCuenta = true) }, enabled = !cargando) {
            Text("Crear cuenta nueva", color = Verde)
        }
    }
}

// Traduce los errores de Firebase (vienen en inglés) a mensajes para el usuario.
// WeakPassword va primero porque es un tipo de InvalidCredentials.
fun mensajeError(e: Exception?): String = when (e) {
    is FirebaseAuthWeakPasswordException -> "La contraseña debe tener al menos 6 caracteres"
    is FirebaseAuthInvalidCredentialsException,
    is FirebaseAuthInvalidUserException -> "Correo o contraseña incorrectos"
    is FirebaseAuthUserCollisionException -> "Ya existe una cuenta con ese correo"
    is FirebaseNetworkException -> "Sin conexión a internet"
    else -> "No se pudo completar, intenta de nuevo"
}

@Preview(showBackground = true)
@Composable
fun VistaPreviaLogin() {
    PantallaLogin(onIngreso = {})
}
