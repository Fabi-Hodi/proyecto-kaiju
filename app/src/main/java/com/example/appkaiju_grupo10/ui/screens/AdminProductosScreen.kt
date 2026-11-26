package com.example.appkaiju_grupo10.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.example.appkaiju_grupo10.model.Producto
import com.example.appkaiju_grupo10.navigation.Screen
import com.example.appkaiju_grupo10.ui.components.iconoCategoria
import com.example.appkaiju_grupo10.viewmodels.AdminProductosViewModel
import com.example.appkaiju_grupo10.viewmodels.MainViewModel
import java.text.NumberFormat
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AdminProductosScreen(
    navController: NavController,
    viewModel: MainViewModel,
    adminViewModel: AdminProductosViewModel = viewModel()
) {
    val productos by adminViewModel.productos.collectAsState()
    val categorias by adminViewModel.categorias.collectAsState()

    // null = "Todas"; si no, el nombre de la categoría
    var filtro by rememberSaveable { mutableStateOf<String?>(null) }

    val visibles = remember(productos, filtro) {
        if (filtro == null) productos else productos.filter { it.categoria == filtro }
    }
    val bajoMinimo = visibles.count { it.stockBajo }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Productos") },
                navigationIcon = {
                    IconButton(onClick = { viewModel.navigateBack() }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Volver")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.secondaryContainer
                )
            )
        },
        bottomBar = {
            Button(
                onClick = { viewModel.navigateTo(Screen.AgregarProducto) },
                modifier = Modifier.fillMaxWidth().padding(16.dp)
            ) {
                Icon(Icons.Default.Add, contentDescription = null)
                Spacer(Modifier.width(8.dp))
                Text("Nuevo producto")
            }
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .padding(innerPadding)
                .fillMaxSize()
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Spacer(Modifier.height(4.dp))

            // ----- Filtros por categoría -----
            Row(
                modifier = Modifier.horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                FilterChip(
                    selected = filtro == null,
                    onClick = { filtro = null },
                    label = { Text("Todas") }
                )
                categorias.forEach { categoria ->
                    FilterChip(
                        selected = filtro == categoria.nombre,
                        onClick = { filtro = categoria.nombre },
                        label = { Text(categoria.nombre) }
                    )
                }
            }

            // ----- Resumen + acceso a categorías -----
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "${visibles.size} productos · $bajoMinimo bajo mínimo",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                TextButton(onClick = { viewModel.navigateTo(Screen.AgregarCategoria) }) {
                    Text("Nueva categoría")
                }
            }

            if (visibles.isEmpty()) {
                Text(
                    text = "No hay productos en esta categoría",
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            } else {
                LazyColumn(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    items(items = visibles, key = { it.sku }) { producto ->
                        FilaProductoAdmin(
                            producto = producto,
                            onClick = { viewModel.navigateToEditarProducto(producto.sku) }
                        )
                    }
                }
            }
        }
    }
}

// Fila de un producto: ícono, nombre, "SKU · categoría · ubicación", precio y stock
@Composable
fun FilaProductoAdmin(producto: Producto, onClick: () -> Unit) {
    Card(
        onClick = onClick,
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(14.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            CajaIconoProducto(categoria = producto.categoria)

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = producto.nombre,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    text = "${producto.sku} · ${producto.categoria} · ${producto.ubicacion}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }

            Column(horizontalAlignment = Alignment.End) {
                Text(
                    text = precioClp(producto.precio),
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "Stock ${producto.stock} · mín. ${producto.stockMinimo}",
                    style = MaterialTheme.typography.labelSmall,
                    color = if (producto.stockBajo) MaterialTheme.colorScheme.error
                    else MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

// Cuadro con el ícono de la categoría (fondo azul profundo e ícono cian, como el modelo)
@Composable
fun CajaIconoProducto(categoria: String, tamano: Dp = 48.dp) {
    Box(
        modifier = Modifier
            .size(tamano)
            .clip(RoundedCornerShape(12.dp))
            .background(MaterialTheme.colorScheme.primaryContainer),
        contentAlignment = Alignment.Center
    ) {
        Icon(
            imageVector = iconoCategoria(categoria),
            contentDescription = null,
            modifier = Modifier.size(tamano / 2),
            tint = MaterialTheme.colorScheme.primary
        )
    }
}

// 22990 -> "$22.990"
fun precioClp(precio: Int): String {
    val formato = NumberFormat.getCurrencyInstance(Locale.forLanguageTag("es-CL"))
    formato.maximumFractionDigits = 0
    return formato.format(precio)
}