package com.example.appkaiju_grupo10.repository

import com.google.gson.Gson
import retrofit2.HttpException
import java.io.IOException

// Error con un mensaje listo para mostrar en pantalla
class AuthException(mensaje: String) : Exception(mensaje)

// Ejecuta una llamada a la API y convierte cualquier falla en un AuthException con un mensaje claro.
// La usan AuthRepository (login/registro) y UsuariosAdminRepository (administración).
suspend fun <T> llamarApi(
    mensaje401: String = "Tu sesión venció. Inicia sesión otra vez",
    bloque: suspend () -> T
): T =
    try {
        bloque()
    } catch (e: HttpException) {                     // el servidor respondió con error (400, 401, 403, 409...)
        throw AuthException(mensajeDeError(e, mensaje401))
    } catch (e: IOException) {                       // sin internet, servidor apagado, IP equivocada
        throw AuthException("No se pudo conectar con el servidor. Revisa tu conexión.")
    }

private fun mensajeDeError(e: HttpException, mensaje401: String): String {
    if (e.code() == 401) return mensaje401
    // si el backend mandó un mensaje (ej: "Solo un administrador puede hacer esto"), se usa ese
    return mensajeDelServidor(e) ?: when (e.code()) {
        400 -> "Datos inválidos"
        403 -> "No tienes permiso para hacer esto"
        404 -> "No se encontró el registro"
        409 -> "El RUT o el email ya están registrados"
        else -> "Error del servidor (${e.code()})"
    }
}

// Lee el campo "message" (o "mensaje") del JSON de error del backend
fun mensajeDelServidor(e: HttpException): String? =
    try {
        val json = Gson().fromJson(e.response()?.errorBody()?.string(), Map::class.java)
        (json?.get("message") ?: json?.get("mensaje"))?.toString()
    } catch (ex: Exception) {
        null
    }