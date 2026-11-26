package com.example.appkaiju_grupo10.repository

import com.example.appkaiju_grupo10.data.remote.ActualizarUsuarioRequest
import com.example.appkaiju_grupo10.data.remote.KaijuAdminApi
import com.example.appkaiju_grupo10.data.remote.RetrofitClient
import com.example.appkaiju_grupo10.data.remote.UsuarioKaijuResponse
import com.example.appkaiju_grupo10.model.Permisos

// Único punto que conoce la API de administración de usuarios.
// Toma el token guardado en SesionKaiju y lo envía como "Bearer <token>".
// Ambas cosas se reciben en el constructor para poder usar versiones falsas en los tests.
class UsuariosAdminRepository(
    private val api: KaijuAdminApi = RetrofitClient.kaijuAdminApi,
    private val tokenActual: () -> String? = { SesionKaiju.token }
) {

    private fun bearer(): String =
        tokenActual()?.let { "Bearer $it" } ?: throw AuthException("Debes iniciar sesión")

    suspend fun listar(): List<UsuarioKaijuResponse> =
        llamarApi { api.listarUsuarios(bearer()) }

    suspend fun actualizar(
        id: Long,
        nombreCompleto: String,
        email: String,
        rol: String,
        activo: Boolean,
        permisos: Permisos
    ): UsuarioKaijuResponse =
        llamarApi {
            api.actualizarUsuario(
                token = bearer(),
                id = id,
                request = ActualizarUsuarioRequest(
                    nombreCompleto = nombreCompleto.trim(),
                    email = email.trim().lowercase(),
                    rol = rol,
                    activo = activo,
                    permisoEntradas = permisos.entradas,
                    permisoSalidas = permisos.salidas,
                    permisoPrecios = permisos.precios,
                    permisoReportes = permisos.reportes
                )
            )
        }
}