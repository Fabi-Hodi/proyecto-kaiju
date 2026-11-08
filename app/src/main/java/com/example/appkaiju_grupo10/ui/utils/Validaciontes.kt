package com.example.appkaiju_grupo10.ui.utils

import android.util.Patterns
import com.example.appkaiju_grupo10.model.TipoMovimiento
import java.text.Normalizer

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
fun validarNombre(nombre: String): String? = when {
    nombre.isBlank() -> "El nombre es obligatorio"
    !nombre.trim().contains(" ") -> "Ingresa nombre y apellido"
    else -> null
}

// ----- RUT chileno -----

// Quita puntos y guion: "12.345.678-5" -> "123456785"
private fun limpiarRut(rut: String): String =
    rut.replace(".", "").replace("-", "").trim().uppercase()

// Formato unico para guardar en la BD: "12345678-5"
fun normalizarRut(rut: String): String {
    val limpio = limpiarRut(rut)
    return "${limpio.dropLast(1)}-${limpio.last()}"
}

// Algoritmo para calcular el dígito verificador
private fun calcularDigitoVerificador(cuerpo: String): Char {
    var suma = 0
    var multiplicador = 2
    for (digito in cuerpo.reversed()) {
        suma += digito.digitToInt() * multiplicador
        multiplicador = if (multiplicador == 7) 2 else multiplicador + 1
    }
    return when (val resultado = 11 - (suma % 11)) {
        11 -> '0'
        10 -> 'K'
        else -> resultado.digitToChar()
    }
}

fun validarRut(rut: String): String? {
    val limpio = limpiarRut(rut)
    if (limpio.isBlank()) return "El RUT es obligatorio"

    val cuerpo = limpio.dropLast(1)
    val dv = limpio.last()

    if (cuerpo.length !in 7..8 || !cuerpo.all { it.isDigit() }) {
        return "Formato inválido (ej: 12.345.678-5)"
    }
    if (calcularDigitoVerificador(cuerpo) != dv) {
        return "RUT inválido: el dígito verificador no coincide"
    }
    return null
}
fun validarConfirmacion(password: String, confirmacion: String): String? = when {
    confirmacion.isBlank() -> "Repite la contraseña"
    confirmacion != password -> "Las contraseñas no coinciden"
    else -> null
}
fun validarCantidad(texto: String, tipo: TipoMovimiento, stockActual: Int): String? {
    val cantidad = texto.toIntOrNull()
        ?: return if (texto.isBlank()) "Ingresa una cantidad" else "Solo números enteros"

    return when {
        tipo != TipoMovimiento.AJUSTE && cantidad <= 0 -> "Debe ser mayor a 0"
        tipo == TipoMovimiento.SALIDA && cantidad > stockActual ->
            "Stock insuficiente (hay $stockActual un.)"
        else -> null
    }
}
//  CATEGORIAS

fun validarNombreCategoria(nombre: String): String? = when {
    nombre.isBlank() -> "El nombre de la categoría es obligatorio"
    nombre.trim().length < 3 -> "Debe tener al menos 3 letras"
    nombre.trim().length > 30 -> "Máximo 30 caracteres"
    else -> null
}

fun validarPrefijo(prefijo: String): String? = when {
    prefijo.isBlank() -> "El prefijo es obligatorio"
    !prefijo.matches(Regex("^[A-Z]{2,3}$")) -> "Usa 2 o 3 letras mayúsculas (ej: IN)"
    else -> null
}

// Sugiere el prefijo con las 2 primeras letras: "Optica" -> "OP", "Insumos" -> "IN"
fun sugerirPrefijo(nombre: String): String {
    val sinTildes = Normalizer.normalize(nombre, Normalizer.Form.NFD)
        .replace(Regex("\\p{Mn}+"), "")          // quita tildes: Ó -> O
    return sinTildes.uppercase().filter { it in 'A'..'Z' }.take(2)
}

// ----- PRODUCTOS -----

fun validarObligatorio(texto: String, campo: String): String? =
    if (texto.isBlank()) "$campo es obligatorio" else null

// Para stock y stock mínimo: número entero, puede ser 0
fun validarEnteroNoNegativo(texto: String, campo: String): String? {
    val numero = texto.toIntOrNull()
        ?: return if (texto.isBlank()) "$campo es obligatorio" else "Solo números enteros"
    return if (numero < 0) "$campo no puede ser negativo" else null
}

// Para el precio: número entero mayor a 0
fun validarPrecio(texto: String): String? {
    val numero = texto.toIntOrNull()
        ?: return if (texto.isBlank()) "El precio es obligatorio" else "Solo números enteros"
    return if (numero <= 0) "El precio debe ser mayor a 0" else null
}