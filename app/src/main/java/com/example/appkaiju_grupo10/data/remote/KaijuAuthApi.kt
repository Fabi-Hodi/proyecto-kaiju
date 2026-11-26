package com.example.appkaiju_grupo10.data.remote

import com.example.appkaiju_grupo10.model.Permisos
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.POST

// ----- DTO: mismos nombres de campo que el JSON del backend (KaijuDtos.java) -----

// Sin rol: el backend asigna BODEGA a todo usuario nuevo
data class RegistroKaijuRequest(
    val nombreCompleto: String,
    val rut: String,
    val email: String,
    val password: String
)

data class LoginKaijuRequest(
    val email: String,
    val password: String
)

data class UsuarioKaijuResponse(
    val id: Long,
    val rut: String,
    val nombreCompleto: String,
    val email: String,
    val rol: String,                    // "BODEGA", "VENTAS" o "ADMIN"
    val activo: Boolean,                // false = cuenta desactivada por el admin
    val permisoEntradas: Boolean,
    val permisoSalidas: Boolean,
    val permisoPrecios: Boolean,
    val permisoReportes: Boolean,
    val fechaRegistro: String? = null   // "2026-11-26T10:15:30"
)

// Los 4 permisos juntos, en el modelo que usan las pantallas
fun UsuarioKaijuResponse.permisos(): Permisos =
    Permisos(
        entradas = permisoEntradas,
        salidas = permisoSalidas,
        precios = permisoPrecios,
        reportes = permisoReportes
    )

data class LoginKaijuResponse(
    val accessToken: String,        // el JWT de la sesión
    val tokenType: String,          // "Bearer"
    val expiresIn: Long,            // segundos de vigencia
    val usuario: UsuarioKaijuResponse
)

// ----- Endpoints de sesión (/api/v1/kaiju/auth/...) -----
interface KaijuAuthApi {

    @POST("api/v1/kaiju/auth/registro")
    suspend fun registrar(@Body request: RegistroKaijuRequest): UsuarioKaijuResponse

    @POST("api/v1/kaiju/auth/login")
    suspend fun login(@Body request: LoginKaijuRequest): LoginKaijuResponse

    // "¿quién soy?". 403 = cuenta desactivada, 401 = token vencido
    @GET("api/v1/kaiju/auth/yo")
    suspend fun yo(@Header("Authorization") token: String): UsuarioKaijuResponse
}