package com.example.appkaiju_grupo10.viewmodels

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.appkaiju_grupo10.data.local.AppDatabase
import com.example.appkaiju_grupo10.model.Usuario
import com.example.appkaiju_grupo10.utils.verificarPassword
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

sealed class ResultadoLogin {
    data class Exito(val usuario: Usuario) : ResultadoLogin()
    data class Error(val mensaje: String) : ResultadoLogin()
}

class LoginViewModel(application: Application) : AndroidViewModel(application) {

    private val usuarioDao = AppDatabase.getDatabase(application).usuarioDao

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
                // 1. ¿Existe un operador con ese email?
                val usuario = usuarioDao.buscarPorEmail(email.trim().lowercase())

                // 2. ¿La contraseña coincide con la huella guardada?
                val passwordCorrecta = usuario != null && withContext(Dispatchers.Default) {
                    verificarPassword(password, usuario.passwordHash)
                }

                _resultado.value = if (usuario != null && passwordCorrecta) {
                    ResultadoLogin.Exito(usuario)
                } else {
                    // mismo mensaje en los dos casos (ver explicación)
                    ResultadoLogin.Error("Email o contraseña incorrectos")
                }
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