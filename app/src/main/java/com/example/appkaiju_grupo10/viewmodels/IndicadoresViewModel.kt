package com.example.appkaiju_grupo10.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.appkaiju_grupo10.repository.DolarObservado
import com.example.appkaiju_grupo10.repository.IndicadoresRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

// los estados posibles de la consulta del dólar
sealed class EstadoDolar {
    data object Cargando : EstadoDolar()
    data class Listo(val dolar: DolarObservado) : EstadoDolar()
    data object Error : EstadoDolar()
}

class IndicadoresViewModel : ViewModel() {

    private val repository = IndicadoresRepository()

    private val _estado = MutableStateFlow<EstadoDolar>(EstadoDolar.Cargando)
    val estado: StateFlow<EstadoDolar> = _estado.asStateFlow()

    init {
        cargarDolar()
    }

    // tambien se llama desde el botón "Reintentar" cuando falla
    fun cargarDolar() {
        viewModelScope.launch {
            _estado.value = EstadoDolar.Cargando
            _estado.value = try {
                EstadoDolar.Listo(repository.obtenerDolar())
            } catch (e: Exception) {
                // sin internet, servidor caído, respuesta inesperada...
                EstadoDolar.Error
            }
        }
    }
}
