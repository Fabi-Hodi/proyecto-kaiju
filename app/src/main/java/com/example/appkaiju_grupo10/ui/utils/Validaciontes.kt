package com.example.appkaiju_grupo10.ui.utils

import android.util.Patterns

// Cada funcion devuelve
fun validarEmail(email: String): String? = when {
    email.isBlank() -> "El email es obligatorio"
    !Patterns.EMAIL_ADDRESS.matcher(email).matches() -> "Formato de email inválido"
    else -> null
}

fun validarPassword(password: String): String? = when {
    password.isBlank() -> "La contraseña es obligatoria"
    password.length < 6 -> "Debe tener al menos 6 caracteres"
    else -> null
}