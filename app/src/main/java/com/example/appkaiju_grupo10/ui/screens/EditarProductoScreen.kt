package com.example.appkaiju_grupo10.ui.screens

import android.widget.Toast
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.example.appkaiju_grupo10.model.Movimiento
import com.example.appkaiju_grupo10.model.TipoMovimiento
import com.example.appkaiju_grupo10.ui.components.MensajeAnimado
import com.example.appkaiju_grupo10.ui.utils.validarEnteroNoNegativo
import com.example.appkaiju_grupo10.ui.utils.validarObligatorio
import com.example.appkaiju_grupo10.ui.utils.validarPrecio
import com.example.appkaiju_grupo10.viewmodels.AdminProductosViewModel
import com.example.appkaiju_grupo10.viewmodels.MainViewModel
import com.example.appkaiju_grupo10.viewmodels.ResultadoAdminProducto

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditarProductoScreen(
    navController: NavController,
    viewModel: MainViewModel,
    sku: String,
    adminViewModel: AdminProductosViewModel = viewModel()
) {
    val producto by adminViewModel.producto.collectAsState()
    val movimientos by adminViewModel.movimientos.collectAsState()
    val categorias by adminViewModel.categorias.collectAsState()
    val guardando by adminViewModel.guardando.collectAsState()
    val resultado by adminViewModel.resultado.collectAsState()
    val context = LocalContext.current

    LaunchedEffect(sku) { adminViewModel.seleccionar(sku) }

    // Campos del formulario: se llenan cuando Room entrega el producto (clave = su SKU)
    val clave = producto?.sku
    var nombre by rememberSaveable(clave) { mutableStateOf(producto?.nombre ?: "") }
    var categoria by rememberSaveable(clave) { mutableStateOf(producto?.categoria ?: "") }
    var precio by rememberSaveable(clave) { mutableStateOf(producto?.precio?.toString() ?: "") }
    var stockMinimo by rememberSaveable(clave) { mutableStateOf(producto?.stockMinimo?.toString() ?: "") }
    var ubicacion by rememberSaveable(clave) { mutableStateOf(producto?.ubicacion ?: "") }
    var stockNuevo by rememberSaveable(clave) { mutableStateOf(producto?.stock ?: 0) }
    var menuCategoria by remember { mutableStateOf(false) }
    var confirmarEliminar by remember { mutableStateOf(false) }
    var intentoGuardar by rememberSaveable { mutableStateOf(false) }

    val errorNombre = if (intentoGuardar) validarObligatorio(nombre, "El nombre") else null
    val errorPrecio = if (intentoGuardar) validarPrecio(precio) else null
    val errorMinimo = if (intentoGuardar) validarEnteroNoNegativo(stockMinimo, "El stock mínimo") else null
    val errorUbicacion = if (intentoGuardar && ubicacion.isBlank()) "La ubicación es obligatoria" else null

    // Guardado o eliminado: aviso y volver a la lista
    LaunchedEffect(resultado) {
        when (resultado) {
            is ResultadoAdminProducto.Guardado -> {
                Toast.makeText(context, "Producto actualizado", Toast.LENGTH_SHORT).show()
                adminViewModel.limpiarResultado()
                viewModel.navigateBack()
            }
            is ResultadoAdminProducto.Eliminado -> {
                Toast.makeText(context, "Producto eliminado", Toast.LENGTH_SHORT).show()
                adminViewModel.limpiarResultado()
                viewModel.navigateBack()
            }
            else -> Unit
        }
    }

    // ----- Confirmación antes de eliminar -----
    if (confirmarEliminar) {
        AlertDialog(
            onDismissRequest = { confirmarEliminar = false },
            title = { Text("¿Eliminar producto?") },
            text = {
                Text("Se eliminará \"${producto?.nombre}\" ($sku). Su historial de movimientos se conserva.")
            },
            confirmButton = {
                TextButton(onClick = {
                    confirmarEliminar = false
                    adminViewModel.eliminar(sku)
                }) {
                    Text("Eliminar", color = MaterialTheme.colorScheme.error)
                }
            },
            dismissButton = {
                TextButton(onClick = { confirmarEliminar = false }) { Text("Cancelar") }
            }
        )
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Editar producto") },
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
            if (producto != null) {
                Row(
                    modifier = Modifier.fillMaxWidth().padding(16.dp),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    OutlinedButton(
                        onClick = { confirmarEliminar = true },
                        enabled = !guardando,
                        colors = ButtonDefaults.outlinedButtonColors(
                            contentColor = MaterialTheme.colorScheme.error
                        ),
                        border = BorderStroke(1.dp, MaterialTheme.colorScheme.error),
                        modifier = Modifier.weight(1f)
                    ) {
                        Text("Eliminar")
                    }
                    Button(
                        onClick = {
                            intentoGuardar = true
                            val valido = validarObligatorio(nombre, "El nombre") == null &&
                                    validarPrecio(precio) == null &&
                                    validarEnteroNoNegativo(stockMinimo, "El stock mínimo") == null &&
                                    ubicacion.isNotBlank() &&
                                    categoria.isNotBlank()
                            if (valido) {
                                adminViewModel.guardar(
                                    sku = sku,
                                    nombre = nombre,
                                    categoria = categoria,
                                    precio = precio.toInt(),
                                    stockMinimo = stockMinimo.toInt(),
                                    ubicacion = ubicacion,
                                    stockNuevo = stockNuevo
                                )
                            }
                        },
                        enabled = !guardando,
                        modifier = Modifier.weight(2f)
                    ) {
                        if (guardando) {
                            CircularProgressIndicator(modifier = Modifier.size(20.dp), strokeWidth = 2.dp)
                        } else {
                            Text("Guardar cambios")
                        }
                    }
                }
            }
        }
    ) { innerPadding ->
        val p = producto
        if (p == null) {
            // mientras Room entrega el producto
            Box(
                modifier = Modifier.padding(innerPadding).fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator()
            }
        } else {
            val minimoActual = stockMinimo.toIntOrNull() ?: p.stockMinimo
            val bajoMinimo = stockNuevo <= minimoActual

            Column(
                modifier = Modifier
                    .padding(innerPadding)
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // ----- Encabezado -----
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    CajaIconoProducto(categoria = p.categoria, tamano = 56.dp)
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = p.nombre,
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "SKU ${p.sku}",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    if (bajoMinimo) {
                        Surface(
                            color = MaterialTheme.colorScheme.errorContainer,
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Text(
                                text = "Bajo mínimo",
                                color = MaterialTheme.colorScheme.onErrorContainer,
                                style = MaterialTheme.typography.labelSmall,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp)
                            )
                        }
                    }
                }

                // ----- Nombre -----
                OutlinedTextField(
                    value = nombre,
                    onValueChange = { nombre = it },
                    label = { Text("Nombre") },
                    singleLine = true,
                    isError = errorNombre != null,
                    supportingText = { if (errorNombre != null) Text(errorNombre) },
                    keyboardOptions = KeyboardOptions(imeAction = ImeAction.Next),
                    modifier = Modifier.fillMaxWidth()
                )

                // ----- SKU (solo lectura) + Categoría -----
                Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    OutlinedTextField(
                        value = p.sku,
                        onValueChange = {},
                        label = { Text("SKU") },
                        readOnly = true,
                        enabled = false,
                        singleLine = true,
                        modifier = Modifier.weight(1f)
                    )
                    ExposedDropdownMenuBox(
                        expanded = menuCategoria,
                        onExpandedChange = { menuCategoria = it },
                        modifier = Modifier.weight(1f)
                    ) {
                        OutlinedTextField(
                            value = categoria,
                            onValueChange = {},
                            readOnly = true,
                            singleLine = true,
                            label = { Text("Categoría") },
                            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = menuCategoria) },
                            modifier = Modifier
                                .menuAnchor(ExposedDropdownMenuAnchorType.PrimaryNotEditable)
                                .fillMaxWidth()
                        )
                        ExposedDropdownMenu(
                            expanded = menuCategoria,
                            onDismissRequest = { menuCategoria = false }
                        ) {
                            categorias.forEach { opcion ->
                                DropdownMenuItem(
                                    text = { Text(opcion.nombre) },
                                    onClick = {
                                        categoria = opcion.nombre
                                        menuCategoria = false
                                    }
                                )
                            }
                        }
                    }
                }

                // ----- Precio + Stock mínimo -----
                Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    OutlinedTextField(
                        value = precio,
                        onValueChange = { precio = it.filter { c -> c.isDigit() } },
                        label = { Text("Precio") },
                        prefix = { Text("$") },
                        singleLine = true,
                        isError = errorPrecio != null,
                        supportingText = { if (errorPrecio != null) Text(errorPrecio) },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number, imeAction = ImeAction.Next),
                        modifier = Modifier.weight(1f)
                    )
                    OutlinedTextField(
                        value = stockMinimo,
                        onValueChange = { stockMinimo = it.filter { c -> c.isDigit() } },
                        label = { Text("Stock mínimo") },
                        singleLine = true,
                        isError = errorMinimo != null,
                        supportingText = { if (errorMinimo != null) Text(errorMinimo) },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number, imeAction = ImeAction.Next),
                        modifier = Modifier.weight(1f)
                    )
                }

                // ----- Ubicación -----
                OutlinedTextField(
                    value = ubicacion,
                    onValueChange = { ubicacion = it },
                    label = { Text("Ubicación (pasillo / rack)") },
                    singleLine = true,
                    isError = errorUbicacion != null,
                    supportingText = { if (errorUbicacion != null) Text(errorUbicacion) },
                    keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
                    modifier = Modifier.fillMaxWidth()
                )

                // ----- Stock actual con − / + -----
                Card(
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth().padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "Stock actual",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Text(
                                text = "$stockNuevo un.",
                                style = MaterialTheme.typography.headlineSmall,
                                fontWeight = FontWeight.Bold,
                                color = if (bajoMinimo) MaterialTheme.colorScheme.error
                                else MaterialTheme.colorScheme.onSurface
                            )
                            if (stockNuevo != p.stock) {
                                Text(
                                    text = "Antes: ${p.stock} un. · se registrará un ajuste",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                        FilledTonalIconButton(
                            onClick = { if (stockNuevo > 0) stockNuevo -= 1 },
                            enabled = stockNuevo > 0
                        ) {
                            Icon(Icons.Default.Remove, contentDescription = "Restar 1")
                        }
                        Spacer(Modifier.width(8.dp))
                        FilledTonalIconButton(onClick = { stockNuevo += 1 }) {
                            Icon(Icons.Default.Add, contentDescription = "Sumar 1")
                        }
                    }
                }

                // ----- Últimos movimientos -----
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Últimos movimientos",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold
                    )
                    TextButton(onClick = { viewModel.navigateToMovimiento(p.sku) }) {
                        Text("Ver trazabilidad")
                    }
                }

                if (movimientos.isEmpty()) {
                    Text(
                        text = "Sin movimientos todavía",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                } else {
                    Card(
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
                    ) {
                        Column {
                            movimientos.take(3).forEachIndexed { indice, movimiento ->
                                if (indice > 0) HorizontalDivider()
                                FilaMovimientoAdmin(movimiento)
                            }
                        }
                    }
                }

                // ----- Error al guardar/eliminar -----
                MensajeAnimado(
                    mensaje = (resultado as? ResultadoAdminProducto.Error)?.mensaje,
                    esError = true
                )
            }
        }
    }
}

// Una fila del historial: tipo, nota y cantidad con signo
@Composable
private fun FilaMovimientoAdmin(movimiento: Movimiento) {
    val (texto, color) = when (movimiento.tipo) {
        TipoMovimiento.ENTRADA -> "+${movimiento.cantidad}" to MaterialTheme.colorScheme.primary
        TipoMovimiento.SALIDA -> "−${movimiento.cantidad}" to MaterialTheme.colorScheme.error
        TipoMovimiento.AJUSTE -> "= ${movimiento.cantidad}" to MaterialTheme.colorScheme.onSurfaceVariant
    }
    Row(
        modifier = Modifier.fillMaxWidth().padding(12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(movimiento.tipo.etiqueta, style = MaterialTheme.typography.bodyMedium)
            if (movimiento.notas.isNotBlank()) {
                Text(
                    text = movimiento.notas,
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }
        Text(
            text = texto,
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.Bold,
            color = color
        )
    }
}