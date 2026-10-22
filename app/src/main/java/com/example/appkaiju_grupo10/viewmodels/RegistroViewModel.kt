package com.example.appkaiju_grupo10.viewmodels

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.appkaiju_grupo10.data.local.AppDatabase
import com.example.appkaiju_grupo10.model.Rol
import com.example.appkaiju_grupo10.model.Usuario
import com.example.appkaiju_grupo10.ui.utils.normalizarRut
import com.example.appkaiju_grupo10.utils.generarHash
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

// Los posibles resultados de intentar guardar
sealed class ResultadoRegistro {
    data object Exito : ResultadoRegistro()
    data class Error(val mensaje: String) : ResultadoRegistro()
}

class RegistroViewModel(application: Application) : AndroidViewModel(application) {

    private val usuarioDao = AppDatabase.getDatabase(application).usuarioDao

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _resultado = MutableStateFlow<ResultadoRegistro?>(null)
    val resultado: StateFlow<ResultadoRegistro?> = _resultado.asStateFlow()

    fun registrar(nombre: String, rut: String, email: String, password: String, rol: Rol) {
        viewModelScope.launch {
            if (_isLoading.value) return@launch
            _isLoading.value = true
            _resultado.value = null

            try {
                val rutFinal = normalizarRut(rut)
                val emailFinal = email.trim().lowercase()

                when {
                    usuarioDao.buscarPorRut(rutFinal) != null ->
                        _resultado.value = ResultadoRegistro.Error("Ya existe un operador con ese RUT")

                    usuarioDao.buscarPorEmail(emailFinal) != null ->
                        _resultado.value = ResultadoRegistro.Error("Ese email ya está registrado")

                    else -> {
                        // el hash es un cálculo pesado: lo hacemos fuera del hilo de la pantalla
                        val hash = withContext(Dispatchers.Default) { generarHash(password) }
                        usuarioDao.insertar(
                            Usuario(
                                rut = rutFinal,
                                nombreCompleto = nombre.trim(),
                                email = emailFinal,
                                rol = rol,
                                passwordHash = hash
                            )
                        )
                        _resultado.value = ResultadoRegistro.Exito
                    }
                }
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