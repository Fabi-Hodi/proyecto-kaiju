package com.example.appkaiju_grupo10.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.appkaiju_grupo10.repository.AuthException
import com.example.appkaiju_grupo10.repository.AuthRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

// Los posibles resultados de intentar guardar
sealed class ResultadoRegistro {
    data object Exito : ResultadoRegistro()
    data class Error(val mensaje: String) : ResultadoRegistro()
}

// Registra al operador en el backend (tabla usuario_kaiju en PostgreSQL).
// El backend valida el RUT, evita RUT/email repetidos, guarda la contraseña con BCrypt
// y le asigna el rol BODEGA (solo un administrador puede cambiarlo después).
class RegistroViewModel(
    private val repository: AuthRepository = AuthRepository()
) : ViewModel() {

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _resultado = MutableStateFlow<ResultadoRegistro?>(null)
    val resultado: StateFlow<ResultadoRegistro?> = _resultado.asStateFlow()

    fun registrar(nombre: String, rut: String, email: String, password: String) {
        viewModelScope.launch {
            if (_isLoading.value) return@launch
            _isLoading.value = true
            _resultado.value = null

            try {
                repository.registrar(
                    nombreCompleto = nombre,
                    rut = rut,
                    email = email,
                    password = password
                )
                _resultado.value = ResultadoRegistro.Exito
            } catch (e: AuthException) {
                // ej: "Ya existe un operador con ese RUT" (mensaje que manda el backend)
                _resultado.value = ResultadoRegistro.Error(e.message ?: "No se pudo guardar el operador")
            } catch (e: Exception) {
                _resultado.value = ResultadoRegistro.Error("No se pudo guardar el operador")
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun limpiarResultado() {
        _resultado.value = null
    }
}