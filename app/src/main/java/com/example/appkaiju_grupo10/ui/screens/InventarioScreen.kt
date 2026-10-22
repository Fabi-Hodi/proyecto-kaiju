package com.example.appkaiju_grupo10.ui.screens

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.example.appkaiju_grupo10.viewmodels.InventarioViewModel
import com.example.appkaiju_grupo10.viewmodels.MainViewModel

// BORRADOR con datos de Room + filtros - el diseño del mockup se hace en el Paso 8
@Composable
fun InventarioScreen(
    navController: NavController,
    viewModel: MainViewModel,
    categoria: String? = null,     // llega desde la ruta (opcional)
    busqueda: String? = null,      // llega desde la ruta (opcional)
    inventarioViewModel: InventarioViewModel = viewModel()
) {
    val productos by inventarioViewModel.productos.collectAsState()

    // Aplicamos los filtros. remember(...) solo recalcula si cambia alguno de los 3 valores
    val productosFiltrados = remember(productos, categoria, busqueda) {
        productos.filter { producto ->
            val coincideCategoria = categoria == null || producto.categoria == categoria
            val coincideBusqueda = busqueda.isNullOrBlank() ||
                    producto.nombre.contains(busqueda, ignoreCase = true) ||
                    producto.sku.contains(busqueda, ignoreCase = true)
            coincideCategoria && coincideBusqueda
        }
    }

    Column(
        modifier = Modifier.fillMaxSize().padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Text(
            text = "Inventario (${productosFiltrados.size} productos)",
            style = MaterialTheme.typography.titleLarge
        )
        // mostramos qué filtro está activo
        if (categoria != null) Text("Categoría: $categoria")
        if (!busqueda.isNullOrBlank()) Text("Búsqueda: \"$busqueda\"")

        if (productosFiltrados.isEmpty()) {
            Text(
                text = "No se encontraron productos",
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }

        LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            items(items = productosFiltrados, key = { it.sku }) { producto ->
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { viewModel.navigateToMovimiento(producto.sku) }
                ) {
                    Row(
                        modifier = Modifier.padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(producto.nombre, fontWeight = FontWeight.Bold)
                            Text("SKU: ${producto.sku} · ${producto.categoria}")
                            Text(
                                text = "Stock: ${producto.stock} un.",
                                color = if (producto.stockBajo) MaterialTheme.colorScheme.error
                                else MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                        Button(onClick = { inventarioViewModel.sumarStockPrueba(producto) }) {
                            Text("+1")
                        }
                    }
                }
            }
        }
    }
}