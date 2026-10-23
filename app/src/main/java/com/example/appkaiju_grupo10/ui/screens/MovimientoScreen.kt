package com.example.appkaiju_grupo10.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListScope
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.selection.selectableGroup
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.AddCircle
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material.icons.filled.RemoveCircle
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material3.*
import androidx.compose.material3.windowsizeclass.WindowWidthSizeClass
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.example.appkaiju_grupo10.model.Movimiento
import com.example.appkaiju_grupo10.model.Producto
import com.example.appkaiju_grupo10.model.TipoMovimiento
import com.example.appkaiju_grupo10.model.calcularStockResultante
import com.example.appkaiju_grupo10.ui.components.MensajeAnimado
import com.example.appkaiju_grupo10.ui.components.StockAnimado
import com.example.appkaiju_grupo10.ui.components.iconoCategoria
import com.example.appkaiju_grupo10.ui.utils.obtenerWindowSizeClass
import com.example.appkaiju_grupo10.ui.utils.validarCantidad
import com.example.appkaiju_grupo10.viewmodels.MainViewModel
import com.example.appkaiju_grupo10.viewmodels.MovimientoViewModel
import com.example.appkaiju_grupo10.viewmodels.ResultadoMovimiento
import kotlinx.coroutines.delay
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.TimeZone

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MovimientoScreen(
    navController: NavController,
    viewModel: MainViewModel,
    sku: String,
    movimientoViewModel: MovimientoViewModel = viewModel()
) {
    val producto by movimientoViewModel.producto.collectAsState()
    val historial by movimientoViewModel.historial.collectAsState()
    val isLoading by movimientoViewModel.isLoading.collectAsState()
    val resultado by movimientoViewModel.resultado.collectAsState()

    val windowSizeClass = obtenerWindowSizeClass()
    var tipo by rememberSaveable { mutableStateOf(TipoMovimiento.ENTRADA) }
    var cantidadTexto by rememberSaveable { mutableStateOf("") }
    var notas by rememberSaveable { mutableStateOf("") }
    var intentoEnviar by rememberSaveable { mutableStateOf(false) }
    // mensaje de éxito que se muestra animado unos segundos
    var mensajeExito by remember { mutableStateOf<String?>(null) }

    LaunchedEffect(resultado) {
        val r = resultado
        if (r is ResultadoMovimiento.Exito) {
            cantidadTexto = ""
            notas = ""
            intentoEnviar = false
            mensajeExito = r.mensaje
            movimientoViewModel.limpiarResultado()
        }
    }

    // se oculta solo a los 3 segundos (efecto separado para que limpiarResultado no lo cancele)
    LaunchedEffect(mensajeExito) {
        if (mensajeExito != null) {
            delay(3000)
            mensajeExito = null
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Movimiento de stock") },
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
        val p = producto

        if (p == null) {
            Box(
                modifier = Modifier.padding(innerPadding).fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator()
            }
        } else {
            val cantidad = cantidadTexto.toIntOrNull()
            val errorCantidad = if (intentoEnviar) validarCantidad(cantidadTexto, tipo, p.stock) else null

            val formulario: @Composable () -> Unit = {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Text(
                        text = "Registrar movimiento",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )

                    Row(modifier = Modifier.selectableGroup()) {
                        TipoMovimiento.entries.forEach { opcion ->
                            Row(
                                modifier = Modifier
                                    .selectable(
                                        selected = tipo == opcion,
                                        onClick = { tipo = opcion },
                                        role = Role.RadioButton
                                    )
                                    .padding(end = 12.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                RadioButton(selected = tipo == opcion, onClick = null)
                                Text(opcion.etiqueta)
                            }
                        }
                    }

                    Row(
                        verticalAlignment = Alignment.Top,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        OutlinedTextField(
                            value = cantidadTexto,
                            onValueChange = { nuevo ->
                                if (nuevo.all { it.isDigit() } && nuevo.length <= 5) cantidadTexto = nuevo
                            },
                            label = { Text(if (tipo == TipoMovimiento.AJUSTE) "Stock contado" else "Cantidad") },
                            singleLine = true,
                            isError = errorCantidad != null,
                            supportingText = { if (errorCantidad != null) Text(errorCantidad) },
                            keyboardOptions = KeyboardOptions(
                                keyboardType = KeyboardType.Number,
                                imeAction = ImeAction.Next
                            ),
                            modifier = Modifier.weight(1f)
                        )
                        FilledTonalIconButton(
                            onClick = { cantidadTexto = ((cantidad ?: 0) + 1).toString() },
                            modifier = Modifier.padding(top = 8.dp)
                        ) {
                            Icon(Icons.Default.Add, contentDescription = "Sumar 1")
                        }
                        FilledTonalIconButton(
                            onClick = { cantidadTexto = ((cantidad ?: 0) - 1).coerceAtLeast(0).toString() },
                            modifier = Modifier.padding(top = 8.dp)
                        ) {
                            Icon(Icons.Default.Remove, contentDescription = "Restar 1")
                        }
                    }

                    if (cantidad != null) {
                        val resultante = calcularStockResultante(p.stock, tipo, cantidad)
                        Text(
                            text = "Stock resultante: ${p.stock} → $resultante un.",
                            fontWeight = FontWeight.Bold,
                            color = if (resultante < 0 || resultante <= p.stockMinimo)
                                MaterialTheme.colorScheme.error
                            else MaterialTheme.colorScheme.primary
                        )
                    }

                    OutlinedTextField(
                        value = notas,
                        onValueChange = { notas = it },
                        label = { Text("Notas (opcional)") },
                        placeholder = { Text("Ej: venta boleta 123, llegada proveedor...") },
                        minLines = 2,
                        maxLines = 4,
                        modifier = Modifier.fillMaxWidth()
                    )

                    val errorVM = resultado as? ResultadoMovimiento.Error
                    MensajeAnimado(mensaje = errorVM?.mensaje, esError = true)
                    MensajeAnimado(mensaje = mensajeExito, esError = false)

                    Button(
                        onClick = {
                            intentoEnviar = true
                            val c = cantidad
                            if (validarCantidad(cantidadTexto, tipo, p.stock) == null && c != null) {
                                movimientoViewModel.registrarMovimiento(tipo, c, notas)
                            }
                        },
                        enabled = !isLoading,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        if (isLoading) {
                            CircularProgressIndicator(modifier = Modifier.size(20.dp), strokeWidth = 2.dp)
                        } else {
                            Text("Confirmar movimiento")
                        }
                    }
                }
            }

            // ----- ADAPTABILIDAD: 1 columna en celular, 2 paneles en pantallas anchas -----
            if (windowSizeClass.widthSizeClass == WindowWidthSizeClass.Compact) {
                LazyColumn(
                    modifier = Modifier.padding(innerPadding).fillMaxSize(),
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    item { CabeceraProducto(p) }
                    item { formulario() }
                    item { HorizontalDivider() }
                    seccionHistorial(historial)
                }
            } else {
                Row(
                    modifier = Modifier.padding(innerPadding).fillMaxSize(),
                    horizontalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    LazyColumn(
                        modifier = Modifier.weight(1f),
                        contentPadding = PaddingValues(16.dp),
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        item { CabeceraProducto(p) }
                        item { formulario() }
                    }
                    LazyColumn(
                        modifier = Modifier.weight(1f),
                        contentPadding = PaddingValues(16.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        seccionHistorial(historial)
                    }
                }
            }
        }
    }
}

@Composable
private fun CabeceraProducto(producto: Producto) {
    Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
        Box(
            modifier = Modifier
                .size(96.dp)
                .clip(RoundedCornerShape(16.dp))
                .background(MaterialTheme.colorScheme.surfaceVariant),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = iconoCategoria(producto.categoria),
                contentDescription = null,
                modifier = Modifier.size(48.dp),
                tint = MaterialTheme.colorScheme.primary
            )
        }
        Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
            Text(
                text = producto.nombre,
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold
            )
            Text("SKU: ${producto.sku}")
            Text("Ubicación: ${producto.ubicacion}")
            StockAnimado(
                stock = producto.stock,
                stockBajo = producto.stockBajo,
                prefijo = "Stock actual: ",
                style = MaterialTheme.typography.titleMedium
            )
            Text(
                text = "Stock mínimo: ${producto.stockMinimo} un.",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

private fun LazyListScope.seccionHistorial(historial: List<Movimiento>) {
    item {
        Text(
            text = "Historial / trazabilidad (${historial.size})",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold
        )
    }
    if (historial.isEmpty()) {
        item {
            Text(
                text = "Aún no hay movimientos para este producto",
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    } else {
        items(items = historial, key = { it.id }) { movimiento ->
            FilaMovimiento(movimiento)
        }
    }
}

@Composable
private fun FilaMovimiento(movimiento: Movimiento) {
    val (icono, color, cantidadTexto) = when (movimiento.tipo) {
        TipoMovimiento.ENTRADA -> Triple(Icons.Default.AddCircle, MaterialTheme.colorScheme.primary, "+${movimiento.cantidad}")
        TipoMovimiento.SALIDA -> Triple(Icons.Default.RemoveCircle, MaterialTheme.colorScheme.error, "−${movimiento.cantidad}")
        TipoMovimiento.AJUSTE -> Triple(Icons.Default.Tune, MaterialTheme.colorScheme.onSurfaceVariant, "= ${movimiento.cantidad}")
    }

    Card(
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Icon(icono, contentDescription = movimiento.tipo.etiqueta, tint = color)
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = "${movimiento.tipo.etiqueta}  $cantidadTexto",
                    fontWeight = FontWeight.Bold,
                    color = color
                )
                Text(
                    text = formatearFecha(movimiento.fecha),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                if (movimiento.notas.isNotBlank()) {
                    Text(movimiento.notas, style = MaterialTheme.typography.bodySmall)
                }
            }
            Text(
                text = "Stock: ${movimiento.stockResultante}",
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.Bold
            )
        }
    }
}

// 1729612200000 -> 22/10/2026 15:30 en hora de Chile
private fun formatearFecha(timestamp: Long): String {
    val formato = SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.forLanguageTag("es-CL"))
    formato.timeZone = TimeZone.getTimeZone("America/Santiago")
    return formato.format(Date(timestamp))
}