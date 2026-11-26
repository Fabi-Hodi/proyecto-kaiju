package com.example.appkaiju_grupo10.repository

import com.example.appkaiju_grupo10.data.remote.KaijuAuthApi
import com.example.appkaiju_grupo10.data.remote.LoginKaijuRequest
import com.example.appkaiju_grupo10.data.remote.LoginKaijuResponse
import com.example.appkaiju_grupo10.data.remote.RegistroKaijuRequest
import com.example.appkaiju_grupo10.data.remote.RetrofitClient
import com.example.appkaiju_grupo10.data.remote.UsuarioKaijuResponse
import retrofit2.HttpException
import java.io.IOException

// Resultado de preguntarle al backend si la sesión sigue válida
sealed class EstadoSesion {
    data class Vigente(val usuario: UsuarioKaijuResponse) : EstadoSesion()   // todo bien (datos al día)
    data class Terminada(val mensaje: String) : EstadoSesion()                // desactivada o token vencido
    data object SinConexion : EstadoSesion()                                  // no se pudo preguntar
}

// Único punto que conoce la API de login/registro/sesión.
// La API se recibe en el constructor: la app usa la real y los tests una falsa (MockK).
class AuthRepository(
    private val api: KaijuAuthApi = RetrofitClient.kaijuAuthApi
) {

    suspend fun login(email: String, password: String): LoginKaijuResponse =
        llamarApi(mensaje401 = "Email o contraseña incorrectos") {
            api.login(LoginKaijuRequest(email.trim().lowercase(), password))
        }

    // Sin rol: el backend registra siempre como BODEGA (solo el admin puede cambiarlo)
    suspend fun registrar(
        nombreCompleto: String,
        rut: String,
        email: String,
        password: String
    ): UsuarioKaijuResponse =
        llamarApi {
            api.registrar(
                RegistroKaijuRequest(
                    nombreCompleto = nombreCompleto.trim(),
                    rut = rut.trim(),
                    email = email.trim().lowercase(),
                    password = password
                )
            )
        }

    // NUEVO: ¿la sesión sigue válida? Sin internet NO se considera terminada.
    suspend fun verificarSesion(token: String?): EstadoSesion {
        if (token == null) return EstadoSesion.Terminada("Debes iniciar sesión")
        return try {
            EstadoSesion.Vigente(api.yo("Bearer $token"))
        } catch (e: HttpException) {
            when (e.code()) {
                401 -> EstadoSesion.Terminada(mensajeDelServidor(e) ?: "Tu sesión venció. Inicia sesión otra vez")
                403 -> EstadoSesion.Terminada(mensajeDelServidor(e) ?: "Tu cuenta fue desactivada")
                else -> EstadoSesion.SinConexion        // error del servidor: no se echa al usuario
            }
        } catch (e: IOException) {
            EstadoSesion.SinConexion
        }
    }
}