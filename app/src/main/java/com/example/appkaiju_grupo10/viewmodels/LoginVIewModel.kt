package com.example.appkaiju_grupo10.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.appkaiju_grupo10.data.remote.UsuarioKaijuResponse
import com.example.appkaiju_grupo10.model.Rol
import com.example.appkaiju_grupo10.model.Usuario
import com.example.appkaiju_grupo10.repository.AuthException
import com.example.appkaiju_grupo10.repository.AuthRepository
import com.example.appkaiju_grupo10.repository.SesionKaiju
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

sealed class ResultadoLogin {
    data class Exito(val usuario: Usuario) : ResultadoLogin()
    data class Error(val mensaje: String) : ResultadoLogin()
}

// Inicia sesión contra el backend (tabla usuario_kaiju en PostgreSQL), ya no contra Room.
// El repositorio se recibe en el constructor: la app usa el real y los tests uno falso (MockK).
class LoginViewModel(
    private val repository: AuthRepository = AuthRepository()
) : ViewModel() {

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _resultado = MutableStateFlow<ResultadoLogin?>(null)
    val resultado: StateFlow<ResultadoLogin?> = _resultado.asStateFlow()

    fun iniciarSesion(email: String, password: String) {
        viewModelScope.launch {
            if (_isLoading.value) return@launch
            _isLoading.value = true
            _resultado.value = null

            try {
                val respuesta = repository.login(email, password)
                SesionKaiju.iniciar(respuesta.accessToken)               // guardamos el token de la sesión
                _resultado.value = ResultadoLogin.Exito(respuesta.usuario.aUsuario())
            } catch (e: AuthException) {
                // mensaje ya listo: "Email o contraseña incorrectos", "No se pudo conectar...", etc.
                _resultado.value = ResultadoLogin.Error(e.message ?: "No se pudo iniciar sesión")
            } catch (e: Exception) {
                _resultado.value = ResultadoLogin.Error("No se pudo iniciar sesión")
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun limpiarResultado() {
        _resultado.value = null
    }
}

// Convierte la respuesta de la API al modelo que ya usan las pantallas (Perfil, sesión).
// passwordHash queda vacío: la contraseña nunca viaja de vuelta desde el servidor.
fun UsuarioKaijuResponse.aUsuario(): Usuario = Usuario(
    rut = rut,
    nombreCompleto = nombreCompleto,
    email = email,
    rol = runCatching { Rol.valueOf(rol) }.getOrDefault(Rol.BODEGA),
    passwordHash = ""
)