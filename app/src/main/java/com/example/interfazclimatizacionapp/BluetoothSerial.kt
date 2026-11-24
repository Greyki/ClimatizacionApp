package com.example.interfazclimatizacionapp


import android.annotation.SuppressLint
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothSocket
import java.io.InputStream
import java.io.OutputStream
import java.util.UUID
import android.os.Handler
import android.os.Looper

class BluetoothSerial {

    private val mainHandler = Handler(Looper.getMainLooper())
    private var socket: BluetoothSocket? = null
    private var output: OutputStream? = null

    private val uuid = UUID.fromString(
        "00001101-0000-1000-8000-00805F9B34FB"
    )

    @SuppressLint("MissingPermission")
    fun conectar(
        adapter: BluetoothAdapter,
        nombre: String = "HC-05",
        onResultado: (Boolean, String) -> Unit,
        onDatos: (String) -> Unit
    ) {
        Thread {
            try {
                val dispositivo = adapter.bondedDevices
                    .firstOrNull { it.name == nombre }
                    ?: throw Exception("HC-05 no está emparejado")

                socket = dispositivo.createRfcommSocketToServiceRecord(uuid)
                socket!!.connect()

                output = socket!!.outputStream

                mainHandler.post {
                    onResultado(true, "Conectado al HC-05")
                }


                val input: InputStream = socket!!.inputStream
                val buffer = ByteArray(1024)
                val linea = StringBuilder()

                while (socket!!.isConnected) {
                    val cantidad = input.read(buffer)

                    for (i in 0 until cantidad) {
                        val caracter = buffer[i].toInt().toChar()

                        if (caracter == '\n') {
                            onDatos(linea.toString().trim())
                            linea.clear()
                        } else if (caracter != '\r') {
                            linea.append(caracter)
                        }
                    }
                }

            } catch (e: Exception) {
                onResultado(false, "Error: ${e.message}")
            }
        }.start()
    }

    fun enviar(comando: String) {
        Thread {
            try {
                output?.write(comando.toByteArray())
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }.start()
    }

    fun desconectar() {
        try {
            socket?.close()
            socket = null
            output = null
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
}
