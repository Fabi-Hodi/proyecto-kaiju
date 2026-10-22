package com.example.appkaiju_grupo10.viewmodels

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.viewModelScope
import androidx.room.withTransaction
import com.example.appkaiju_grupo10.data.local.AppDatabase
import com.example.appkaiju_grupo10.model.Movimiento
import com.example.appkaiju_grupo10.model.Producto
import com.example.appkaiju_grupo10.model.TipoMovimiento
import com.example.appkaiju_grupo10.model.calcularStockResultante
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

sealed class ResultadoMovimiento {
    data class Exito(val mensaje: String) : ResultadoMovimiento()
    data class Error(val mensaje: String) : ResultadoMovimiento()
}

class MovimientoViewModel(
    application: Application,
    savedStateHandle: SavedStateHandle
) : AndroidViewModel(application) {

    private val database = AppDatabase.getDatabase(application)
    private val productoDao = database.productoDao
    private val movimientoDao = database.movimientoDao

    private val sku: String = savedStateHandle.get<String>("sku") ?: ""

    val producto: StateFlow<Producto?> = productoDao.obtenerPorSku(sku).stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = null
    )

    val historial: StateFlow<List<Movimiento>> = movimientoDao.obtenerPorProducto(sku).stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _resultado = MutableStateFlow<ResultadoMovimiento?>(null)
    val resultado: StateFlow<ResultadoMovimiento?> = _resultado.asStateFlow()

    fun registrarMovimiento(tipo: TipoMovimiento, cantidad: Int, notas: String) {
        viewModelScope.launch {
            if (_isLoading.value) return@launch
            _isLoading.value = true
            _resultado.value = null

            try {
                val actual = productoDao.buscarPorSku(sku)
                if (actual == null) {
                    _resultado.value = ResultadoMovimiento.Error("Producto no encontrado")
                    return@launch
                }

                val nuevoStock = calcularStockResultante(actual.stock, tipo, cantidad)
                if (nuevoStock < 0) {
                    _resultado.value = ResultadoMovimiento.Error("Stock insuficiente: hay ${actual.stock} un.")
                    return@launch
                }

                database.withTransaction {
                    productoDao.actualizarStock(sku, nuevoStock)
                    movimientoDao.insertar(
                        Movimiento(
                            sku = sku,
                            tipo = tipo,
                            cantidad = cantidad,
                            stockResultante = nuevoStock,
                            notas = notas.trim()
                        )
                    )
                }

                val aviso = if (nuevoStock <= actual.stockMinimo) " ⚠ Quedó bajo el stock mínimo" else ""
                _resultado.value = ResultadoMovimiento.Exito(
                    "${tipo.etiqueta} registrada: ${actual.stock} → $nuevoStock un.$aviso"
                )
            } catch (e: Exception) {
                _resultado.value = ResultadoMovimiento.Error("No se pudo registrar el movimiento")
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun limpiarResultado() {
        _resultado.value = null
    }
}