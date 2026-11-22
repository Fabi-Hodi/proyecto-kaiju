package com.example.appkaiju_grupo10.repository

// Guarda el token JWT mientras la app está abierta.
// Hoy la app no llama endpoints protegidos, pero queda listo por si más adelante se necesitan.
object SesionKaiju {

    var token: String? = null
        private set                       // solo se cambia con las funciones de abajo

    fun iniciar(nuevoToken: String) {
        token = nuevoToken
    }

    fun cerrar() {
        token = null
    }
}
