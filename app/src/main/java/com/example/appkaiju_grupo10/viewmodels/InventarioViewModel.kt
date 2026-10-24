package com.example.appkaiju_grupo10.viewmodels

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.appkaiju_grupo10.data.DatosFicticios
import com.example.appkaiju_grupo10.data.local.AppDatabase
import com.example.appkaiju_grupo10.model.Producto
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import com.example.appkaiju_grupo10.model.CategoriaResumen

class InventarioViewModel(application: Application) : AndroidViewModel(application) {

    // Pedimos la base de datos unica y su DAO de productos
    private val productoDao = AppDatabase.getDatabase(application).productoDao

    // Lista reactiva: cada vez que cambia la tabla, la pantalla se actualiza sola
    val productos: StateFlow<List<Producto>> = productoDao.obtenerTodos().stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )
    // Categorías con su cantidad (se actualiza sola, igual que productos)
    val categorias: StateFlow<List<CategoriaResumen>> = productoDao.obtenerCategorias().stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )

    // true mientras se cargan los datos iniciales (la pantalla muestra el loader)
    private val _cargando = MutableStateFlow(true)
    val cargando: StateFlow<Boolean> = _cargando.asStateFlow()

    init {
        cargarDatosIniciales()
    }

    // Si la tabla esta vacia (primera vez que se abre la app), copiamos el catalogo ficticio por ahora
    private fun cargarDatosIniciales() {
        viewModelScope.launch {
            if (productoDao.contar() == 0) {
                productoDao.insertarTodos(DatosFicticios.productos)
            }
            // Simula una pequeña demora para que se aprecie el loader (como en la guía 12)
            delay(800)
            _cargando.value = false
        }
    }

}