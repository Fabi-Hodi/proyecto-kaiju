package com.example.appkaiju_grupo10.data.remote

import retrofit2.http.Body
import retrofit2.http.POST

// ----- DTO: mismos nombres de campo que el JSON del backend (KaijuDtos.java) -----

data class RegistroKaijuRequest(
    val nombreCompleto: String,
    val rut: String,
    val email: String,
    val password: String,
    val rol: String                 // "BODEGA", "VENTAS" o "ADMIN"
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
    val rol: String
)

data class LoginKaijuResponse(
    val accessToken: String,        // el JWT de la sesión
    val tokenType: String,          // "Bearer"
    val expiresIn: Long,            // segundos de vigencia
    val usuario: UsuarioKaijuResponse
)

// ----- Endpoints del backend (/api/v1/kaiju/auth/...) -----
interface KaijuAuthApi {

    @POST("api/v1/kaiju/auth/registro")
    suspend fun registrar(@Body request: RegistroKaijuRequest): UsuarioKaijuResponse

    @POST("api/v1/kaiju/auth/login")
    suspend fun login(@Body request: LoginKaijuRequest): LoginKaijuResponse
}