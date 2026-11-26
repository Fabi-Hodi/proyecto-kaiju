package com.example.appkaiju_grupo10.viewmodels

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import androidx.room.withTransaction
import com.example.appkaiju_grupo10.data.local.AppDatabase
import com.example.appkaiju_grupo10.model.Categoria
import com.example.appkaiju_grupo10.model.Movimiento
import com.example.appkaiju_grupo10.model.Producto
import com.example.appkaiju_grupo10.model.TipoMovimiento
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

sealed class ResultadoAdminProducto {
    data object Guardado : ResultadoAdminProducto()
    data object Eliminado : ResultadoAdminProducto()
    data class Error(val mensaje: String) : ResultadoAdminProducto()
}

// Administración de productos (Room). Lo usan la lista y la pantalla de edición.
@OptIn(ExperimentalCoroutinesApi::class)
class AdminProductosViewModel(application: Application) : AndroidViewModel(application) {

    private val database = AppDatabase.getDatabase(application)
    private val productoDao = database.productoDao
    private val movimientoDao = database.movimientoDao
    private val categoriaDao = database.categoriaDao

    // ----- LISTA -----
    val productos: StateFlow<List<Producto>> = productoDao.obtenerTodos().stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )

    val categorias: StateFlow<List<Categoria>> = categoriaDao.obtenerTodas().stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )

    // ----- EDICIÓN: el producto elegido y sus movimientos (se actualizan solos) -----
    private val skuSeleccionado = MutableStateFlow<String?>(null)

    // flatMapLatest: cada vez que cambia el SKU elegido, se escucha el Flow de ESE producto
    val producto: StateFlow<Producto?> = skuSeleccionado
        .flatMapLatest { sku ->
            if (sku == null) flowOf<Producto?>(null) else productoDao.obtenerPorSku(sku)
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    val movimientos: StateFlow<List<Movimiento>> = skuSeleccionado
        .flatMapLatest { sku ->
            if (sku == null) flowOf<List<Movimiento>>(emptyList()) else movimientoDao.obtenerPorProducto(sku)
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    private val _guardando = MutableStateFlow(false)
    val guardando: StateFlow<Boolean> = _guardando.asStateFlow()

    private val _resultado = MutableStateFlow<ResultadoAdminProducto?>(null)
    val resultado: StateFlow<ResultadoAdminProducto?> = _resultado.asStateFlow()

    fun seleccionar(sku: String) {
        skuSeleccionado.value = sku
    }

    // TRANSACCIÓN: actualizar el producto y, si cambió el stock, registrar un Ajuste en el historial
    fun guardar(
        sku: String,
        nombre: String,
        categoria: String,
        precio: Int,
        stockMinimo: Int,
        ubicacion: String,
        stockNuevo: Int
    ) {
        viewModelScope.launch {
            if (_guardando.value) return@launch
            _guardando.value = true
            _resultado.value = null
            try {
                database.withTransaction {
                    // se lee el producto ACTUAL de la base (el stock pudo cambiar mientras se editaba)
                    val actual = productoDao.buscarPorSku(sku)
                        ?: throw IllegalStateException("Producto no encontrado")

                    productoDao.actualizar(
                        actual.copy(
                            nombre = nombre.trim(),
                            categoria = categoria,
                            precio = precio,
                            stockMinimo = stockMinimo,
                            ubicacion = ubicacion.trim(),
                            stock = stockNuevo
                        )
                    )
                    if (stockNuevo != actual.stock) {
                        movimientoDao.insertar(
                            Movimiento(
                                sku = sku,
                                tipo = TipoMovimiento.AJUSTE,
                                cantidad = stockNuevo,            // en un Ajuste, la cantidad es el nuevo total
                                stockResultante = stockNuevo,
                                notas = "Ajuste del administrador (antes ${actual.stock} un.)"
                            )
                        )
                    }
                }
                _resultado.value = ResultadoAdminProducto.Guardado
            } catch (e: Exception) {
                _resultado.value = ResultadoAdminProducto.Error("No se pudieron guardar los cambios")
            } finally {
                _guardando.value = false
            }
        }
    }

    fun eliminar(sku: String) {
        viewModelScope.launch {
            if (_guardando.value) return@launch
            _guardando.value = true
            _resultado.value = null
            try {
                productoDao.buscarPorSku(sku)?.let { productoDao.eliminar(it) }
                _resultado.value = ResultadoAdminProducto.Eliminado
            } catch (e: Exception) {
                _resultado.value = ResultadoAdminProducto.Error("No se pudo eliminar el producto")
            } finally {
                _guardando.value = false
            }
        }
    }

    fun limpiarResultado() {
        _resultado.value = null
    }
}