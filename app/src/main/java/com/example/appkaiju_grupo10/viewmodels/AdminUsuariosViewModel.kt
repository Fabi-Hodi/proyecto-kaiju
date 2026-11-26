package com.example.appkaiju_grupo10.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.appkaiju_grupo10.data.remote.UsuarioKaijuResponse
import com.example.appkaiju_grupo10.model.Permisos
import com.example.appkaiju_grupo10.model.Rol
import com.example.appkaiju_grupo10.repository.AuthException
import com.example.appkaiju_grupo10.repository.UsuariosAdminRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

// ----- LISTA de usuarios (pantalla Usuarios) -----
class AdminUsuariosViewModel(
    private val repository: UsuariosAdminRepository = UsuariosAdminRepository()
) : ViewModel() {

    private val _usuarios = MutableStateFlow<List<UsuarioKaijuResponse>>(emptyList())
    val usuarios: StateFlow<List<UsuarioKaijuResponse>> = _usuarios.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error.asStateFlow()

    // Se llama al abrir la pantalla y al volver de "Editar usuario" (así la lista queda al día)
    fun cargar() {
        viewModelScope.launch {
            if (_isLoading.value) return@launch
            _isLoading.value = true
            _error.value = null
            try {
                _usuarios.value = repository.listar()
            } catch (e: AuthException) {
                _error.value = e.message                       // ej: "Solo un administrador puede hacer esto"
            } catch (e: Exception) {
                _error.value = "No se pudo cargar la lista de usuarios"
            } finally {
                _isLoading.value = false
            }
        }
    }
}

// ----- EDITAR un usuario (pantalla Editar usuario) -----
sealed class ResultadoEdicion {
    data object Guardado : ResultadoEdicion()
    data class Error(val mensaje: String) : ResultadoEdicion()
}

class EditarUsuarioViewModel(
    private val repository: UsuariosAdminRepository = UsuariosAdminRepository()
) : ViewModel() {

    private val _usuario = MutableStateFlow<UsuarioKaijuResponse?>(null)
    val usuario: StateFlow<UsuarioKaijuResponse?> = _usuario.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _errorCarga = MutableStateFlow<String?>(null)
    val errorCarga: StateFlow<String?> = _errorCarga.asStateFlow()

    private val _guardando = MutableStateFlow(false)
    val guardando: StateFlow<Boolean> = _guardando.asStateFlow()

    private val _resultado = MutableStateFlow<ResultadoEdicion?>(null)
    val resultado: StateFlow<ResultadoEdicion?> = _resultado.asStateFlow()

    // El backend no tiene "buscar por id": se pide la lista y se toma el que corresponde
    fun cargar(id: Long) {
        if (_usuario.value?.id == id) return                   // ya está cargado (ej: al girar la pantalla)
        viewModelScope.launch {
            _isLoading.value = true
            _errorCarga.value = null
            try {
                _usuario.value = repository.listar().firstOrNull { it.id == id }
                if (_usuario.value == null) _errorCarga.value = "No se encontró el usuario"
            } catch (e: AuthException) {
                _errorCarga.value = e.message
            } catch (e: Exception) {
                _errorCarga.value = "No se pudo cargar el usuario"
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun guardar(id: Long, nombre: String, email: String, rol: Rol, activo: Boolean, permisos: Permisos) {
        viewModelScope.launch {
            if (_guardando.value) return@launch
            _guardando.value = true
            _resultado.value = null
            try {
                _usuario.value = repository.actualizar(id, nombre, email, rol.name, activo, permisos)
                _resultado.value = ResultadoEdicion.Guardado
            } catch (e: AuthException) {
                // ej: "Ese email ya está registrado" o "No puedes quitarte el rol de administrador..."
                _resultado.value = ResultadoEdicion.Error(e.message ?: "No se pudieron guardar los cambios")
            } catch (e: Exception) {
                _resultado.value = ResultadoEdicion.Error("No se pudieron guardar los cambios")
            } finally {
                _guardando.value = false
            }
        }
    }

    fun limpiarResultado() {
        _resultado.value = null
    }
}