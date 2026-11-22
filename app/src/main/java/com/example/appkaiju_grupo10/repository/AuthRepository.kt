package com.example.appkaiju_grupo10.repository

import com.example.appkaiju_grupo10.data.remote.KaijuAuthApi
import com.example.appkaiju_grupo10.data.remote.LoginKaijuRequest
import com.example.appkaiju_grupo10.data.remote.LoginKaijuResponse
import com.example.appkaiju_grupo10.data.remote.RegistroKaijuRequest
import com.example.appkaiju_grupo10.data.remote.RetrofitClient
import com.example.appkaiju_grupo10.data.remote.UsuarioKaijuResponse
import com.google.gson.Gson
import retrofit2.HttpException
import java.io.IOException

// Error con un mensaje listo para mostrar en pantalla
class AuthException(mensaje: String) : Exception(mensaje)

// Único punto que conoce la API de login/registro.
// La API se recibe en el constructor: la app usa la real y los tests una falsa (MockK).
class AuthRepository(
    private val api: KaijuAuthApi = RetrofitClient.kaijuAuthApi
) {

    suspend fun login(email: String, password: String): LoginKaijuResponse =
        llamar(mensaje401 = "Email o contraseña incorrectos") {
            api.login(LoginKaijuRequest(email.trim().lowercase(), password))
        }

    suspend fun registrar(
        nombreCompleto: String,
        rut: String,
        email: String,
        password: String,
        rol: String
    ): UsuarioKaijuResponse =
        llamar {
            api.registrar(
                RegistroKaijuRequest(
                    nombreCompleto = nombreCompleto.trim(),
                    rut = rut.trim(),
                    email = email.trim().lowercase(),
                    password = password,
                    rol = rol
                )
            )
        }

    // Ejecuta la llamada y convierte cualquier falla en un AuthException con un mensaje claro
    private suspend fun <T> llamar(mensaje401: String = "No autorizado", bloque: suspend () -> T): T =
        try {
            bloque()
        } catch (e: HttpException) {                     // el servidor respondió con error (400, 401, 409...)
            throw AuthException(mensajeDeError(e, mensaje401))
        } catch (e: IOException) {                       // sin internet, servidor apagado, IP equivocada
            throw AuthException("No se pudo conectar con el servidor. Revisa tu conexión.")
        }

    private fun mensajeDeError(e: HttpException, mensaje401: String): String {
        if (e.code() == 401) return mensaje401
        // si el backend mandó un mensaje (ej: "Ya existe un operador con ese RUT"), se usa ese
        val delServidor = leerMensaje(e.response()?.errorBody()?.string())
        return delServidor ?: when (e.code()) {
            400 -> "Datos inválidos"
            409 -> "El RUT o el email ya están registrados"
            else -> "Error del servidor (${e.code()})"
        }
    }

    // Lee el campo "message" (o "mensaje") del JSON de error del backend
    private fun leerMensaje(cuerpo: String?): String? =
        try {
            val json = Gson().fromJson(cuerpo, Map::class.java)
            (json?.get("message") ?: json?.get("mensaje"))?.toString()
        } catch (e: Exception) {
            null
        }
}