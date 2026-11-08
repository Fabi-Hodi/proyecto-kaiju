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
import com.example.appkaiju_grupo10.model.formatearSku
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

sealed class ResultadoCatalogo {
    data class Exito(val mensaje: String) : ResultadoCatalogo()
    data class Error(val mensaje: String) : ResultadoCatalogo()
}

class CatalogoViewModel(application: Application) : AndroidViewModel(application) {

    private val database = AppDatabase.getDatabase(application)
    private val categoriaDao = database.categoriaDao
    private val productoDao = database.productoDao
    private val movimientoDao = database.movimientoDao

    // Categorías para el selector del formulario (se actualiza sola)
    val categorias: StateFlow<List<Categoria>> = categoriaDao.obtenerTodas().stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _resultado = MutableStateFlow<ResultadoCatalogo?>(null)
    val resultado: StateFlow<ResultadoCatalogo?> = _resultado.asStateFlow()

    // ----- CREAR CATEGORÍA -----
    fun crearCategoria(nombre: String, prefijo: String) {
        viewModelScope.launch {
            if (_isLoading.value) return@launch
            _isLoading.value = true
            _resultado.value = null

            try {
                val nombreFinal = nombre.trim()
                val prefijoFinal = prefijo.trim().uppercase()

                val mismoNombre = categoriaDao.buscarPorNombre(nombreFinal)
                val mismoPrefijo = categoriaDao.buscarPorPrefijo(prefijoFinal)

                _resultado.value = when {
                    mismoNombre != null ->
                        ResultadoCatalogo.Error("Ya existe la categoría ${mismoNombre.nombre}")
                    mismoPrefijo != null ->
                        ResultadoCatalogo.Error("El prefijo $prefijoFinal ya lo usa ${mismoPrefijo.nombre}")
                    else -> {
                        categoriaDao.insertar(Categoria(nombre = nombreFinal, prefijo = prefijoFinal))
                        ResultadoCatalogo.Exito(
                            "Categoría $nombreFinal creada. Sus códigos serán ${formatearSku(prefijoFinal, 1)}, ${formatearSku(prefijoFinal, 2)}…"
                        )
                    }
                }
            } catch (e: Exception) {
                _resultado.value = ResultadoCatalogo.Error("No se pudo crear la categoría")
            } finally {
                _isLoading.value = false
            }
        }
    }

    // ----- CREAR PRODUCTO (el código se genera solo) -----
    fun crearProducto(
        nombreCategoria: String,
        nombre: String,
        descripcion: String,
        precio: Int,
        stockInicial: Int,
        stockMinimo: Int,
        ubicacion: String
    ) {
        viewModelScope.launch {
            if (_isLoading.value) return@launch
            _isLoading.value = true
            _resultado.value = null

            try {
                // TRANSACCIÓN: leer contador + avanzarlo + guardar producto (+ movimiento inicial)
                val skuNuevo = database.withTransaction {
                    val categoria = categoriaDao.buscarPorNombre(nombreCategoria)
                        ?: throw IllegalStateException("Categoría no encontrada")

                    val numero = categoria.ultimoNumero + 1               // 2 -> 3
                    val sku = formatearSku(categoria.prefijo, numero)     // "IN-003"

                    categoriaDao.actualizarUltimoNumero(categoria.nombre, numero)
                    productoDao.insertar(
                        Producto(
                            sku = sku,
                            nombre = nombre.trim(),
                            descripcion = descripcion.trim(),
                            categoria = categoria.nombre,
                            precio = precio,
                            stock = stockInicial,
                            stockMinimo = stockMinimo,
                            ubicacion = ubicacion.trim()
                        )
                    )
                    // Trazabilidad: el stock inicial queda como primera Entrada del historial
                    if (stockInicial > 0) {
                        movimientoDao.insertar(
                            Movimiento(
                                sku = sku,
                                tipo = TipoMovimiento.ENTRADA,
                                cantidad = stockInicial,
                                stockResultante = stockInicial,
                                notas = "Stock inicial al crear el producto"
                            )
                        )
                    }
                    sku   // lo que devuelve la transacción
                }
                _resultado.value = ResultadoCatalogo.Exito("Producto creado con el código $skuNuevo")
            } catch (e: Exception) {
                _resultado.value = ResultadoCatalogo.Error("No se pudo crear el producto")
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun limpiarResultado() {
        _resultado.value = null
    }
}