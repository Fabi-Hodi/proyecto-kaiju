package com.example.appkaiju_grupo10.data.remote

import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.PUT
import retrofit2.http.Path

// Lo que envía el administrador al editar un usuario (igual a ActualizarUsuarioRequest del backend)
data class ActualizarUsuarioRequest(
    val nombreCompleto: String,
    val email: String,
    val rol: String,        // "BODEGA", "VENTAS" o "ADMIN"
    val activo: Boolean,
    val permisoEntradas: Boolean,
    val permisoSalidas: Boolean,
    val permisoPrecios: Boolean,
    val permisoReportes: Boolean
)

// ----- Endpoints protegidos (/api/v1/kaiju/admin/...) -----
// @Header("Authorization"): cada llamada lleva "Bearer <token>" del admin que inició sesión
interface KaijuAdminApi {

    @GET("api/v1/kaiju/admin/usuarios")
    suspend fun listarUsuarios(
        @Header("Authorization") token: String
    ): List<UsuarioKaijuResponse>

    @PUT("api/v1/kaiju/admin/usuarios/{id}")
    suspend fun actualizarUsuario(
        @Header("Authorization") token: String,
        @Path("id") id: Long,
        @Body request: ActualizarUsuarioRequest
    ): UsuarioKaijuResponse
}