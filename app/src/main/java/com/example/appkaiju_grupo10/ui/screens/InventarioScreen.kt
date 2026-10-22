package com.example.appkaiju_grupo10.ui.screens

import android.widget.Toast
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.QrCodeScanner
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.example.appkaiju_grupo10.ui.components.TarjetaProducto
import com.example.appkaiju_grupo10.viewmodels.InventarioViewModel
import com.example.appkaiju_grupo10.viewmodels.MainViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun InventarioScreen(
    navController: NavController,
    viewModel: MainViewModel,
    categoria: String? = null,
    busqueda: String? = null,
    inventarioViewModel: InventarioViewModel = viewModel()
) {
    val productos by inventarioViewModel.productos.collectAsState()
    val categorias by inventarioViewModel.categorias.collectAsState()
    val context = LocalContext.current

    var texto by rememberSaveable { mutableStateOf(busqueda ?: "") }
    var categoriaSeleccionada by rememberSaveable { mutableStateOf(categoria) }
    var soloStockBajo by rememberSaveable { mutableStateOf(false) }

    val productosFiltrados = remember(productos, texto, categoriaSeleccionada, soloStockBajo) {
        productos
            .filter { producto ->
                val coincideCategoria =
                    categoriaSeleccionada == null || producto.categoria == categoriaSeleccionada
                val coincideTexto = texto.isBlank() ||
                        producto.nombre.contains(texto.trim(), ignoreCase = true) ||
                        producto.sku.contains(texto.trim(), ignoreCase = true)
                val coincideStock = !soloStockBajo || producto.stockBajo
                coincideCategoria && coincideTexto && coincideStock
            }
            .sortedByDescending { it.stockBajo }
    }
    val cantidadStockBajo = productos.count { it.stockBajo }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Inventario") },
                navigationIcon = {
                    IconButton(onClick = { viewModel.navigateBack() }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Volver")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.secondaryContainer
                )
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .padding(innerPadding)
                .fillMaxSize()
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Spacer(modifier = Modifier.height(4.dp))

            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                OutlinedTextField(
                    value = texto,
                    onValueChange = { texto = it },
                    placeholder = { Text("Buscar por SKU o nombre...") },
                    singleLine = true,
                    leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
                    trailingIcon = {
                        if (texto.isNotEmpty()) {
                            IconButton(onClick = { texto = "" }) {
                                Icon(Icons.Default.Close, contentDescription = "Limpiar búsqueda")
                            }
                        }
                    },
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.weight(1f)
                )
                FilledTonalIconButton(
                    onClick = {
                        Toast.makeText(context, "Escáner QR: se activa en el Paso 11", Toast.LENGTH_SHORT).show()
                    }
                ) {
                    Icon(Icons.Default.QrCodeScanner, contentDescription = "Escanear código QR")
                }
            }

            LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                item {
                    FilterChip(
                        selected = categoriaSeleccionada == null,
                        onClick = { categoriaSeleccionada = null },
                        label = { Text("Todas") }
                    )
                }
                items(items = categorias, key = { it.categoria }) { cat ->
                    FilterChip(
                        selected = categoriaSeleccionada == cat.categoria,
                        onClick = { categoriaSeleccionada = cat.categoria },
                        label = { Text("${cat.categoria} (${cat.cantidad})") }
                    )
                }
                item {
                    FilterChip(
                        selected = soloStockBajo,
                        onClick = { soloStockBajo = !soloStockBajo },
                        label = { Text("Stock bajo") },
                        leadingIcon = {
                            Icon(
                                Icons.Default.Warning,
                                contentDescription = null,
                                modifier = Modifier.size(18.dp)
                            )
                        }
                    )
                }
            }

            if (cantidadStockBajo > 0 && !soloStockBajo) {
                Card(
                    onClick = { soloStockBajo = true },
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.errorContainer
                    )
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth().padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Icon(
                            Icons.Default.Warning,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.onErrorContainer
                        )
                        Text(
                            text = "$cantidadStockBajo productos bajo el stock mínimo · Ver",
                            color = MaterialTheme.colorScheme.onErrorContainer
                        )
                    }
                }
            }

            Text(
                text = "${productosFiltrados.size} productos",
                style = MaterialTheme.typography.labelLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            if (productosFiltrados.isEmpty()) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text(
                        text = "No se encontraron productos",
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            } else {
                LazyVerticalGrid(
                    // las columnas como quepan de al menos 320 dp
                    columns = GridCells.Adaptive(minSize = 320.dp),
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                    contentPadding = PaddingValues(bottom = 16.dp)
                ) {
                    items(items = productosFiltrados, key = { it.sku }) { producto ->
                        TarjetaProducto(
                            producto = producto,
                            onVerFicha = { viewModel.navigateToMovimiento(producto.sku) }
                        )
                    }
                }
            }
        }
    }
}