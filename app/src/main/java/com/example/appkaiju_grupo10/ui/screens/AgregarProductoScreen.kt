package com.example.appkaiju_grupo10.ui.screens

import android.widget.Toast
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.example.appkaiju_grupo10.model.siguienteSku
import com.example.appkaiju_grupo10.navigation.Screen
import com.example.appkaiju_grupo10.ui.components.MensajeAnimado
import com.example.appkaiju_grupo10.ui.utils.validarEnteroNoNegativo
import com.example.appkaiju_grupo10.ui.utils.validarObligatorio
import com.example.appkaiju_grupo10.ui.utils.validarPrecio
import com.example.appkaiju_grupo10.viewmodels.CatalogoViewModel
import com.example.appkaiju_grupo10.viewmodels.MainViewModel
import com.example.appkaiju_grupo10.viewmodels.ResultadoCatalogo

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AgregarProductoScreen(
    navController: NavController,
    viewModel: MainViewModel,
    catalogoViewModel: CatalogoViewModel = viewModel()
) {
    val categorias by catalogoViewModel.categorias.collectAsState()
    val isLoading by catalogoViewModel.isLoading.collectAsState()
    val resultado by catalogoViewModel.resultado.collectAsState()
    val context = LocalContext.current

    // ----- ESTADO del formulario -----
    var categoria by rememberSaveable { mutableStateOf<String?>(null) }
    var menuAbierto by remember { mutableStateOf(false) }
    var nombre by rememberSaveable { mutableStateOf("") }
    var descripcion by rememberSaveable { mutableStateOf("") }
    var precio by rememberSaveable { mutableStateOf("") }
    var stock by rememberSaveable { mutableStateOf("") }
    var stockMinimo by rememberSaveable { mutableStateOf("") }
    var ubicacion by rememberSaveable { mutableStateOf("") }
    var intentoEnviar by rememberSaveable { mutableStateOf(false) }

    // La categoría elegida, con su contador actual (se actualiza sola desde Room)
    val categoriaElegida = categorias.find { it.nombre == categoria }

    // ----- Errores (solo después del primer intento) -----
    val errorCategoria = if (intentoEnviar && categoriaElegida == null) "Elige una categoría" else null
    val errorNombre = if (intentoEnviar) validarObligatorio(nombre, "El nombre") else null
    val errorPrecio = if (intentoEnviar) validarPrecio(precio) else null
    val errorStock = if (intentoEnviar) validarEnteroNoNegativo(stock, "El stock inicial") else null
    val errorMinimo = if (intentoEnviar) validarEnteroNoNegativo(stockMinimo, "El stock mínimo") else null
    val errorUbicacion = if (intentoEnviar && ubicacion.isBlank()) "La ubicación es obligatoria" else null

    // Deja solo dígitos y máximo 7 cifras
    val soloNumeros: (String) -> String = { texto -> texto.filter { it.isDigit() }.take(7) }

    LaunchedEffect(resultado) {
        val r = resultado
        if (r is ResultadoCatalogo.Exito) {
            Toast.makeText(context, r.mensaje, Toast.LENGTH_LONG).show()
            catalogoViewModel.limpiarResultado()
            viewModel.navigateBack()
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Nuevo producto") },
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
                .verticalScroll(rememberScrollState())
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Column(
                modifier = Modifier.widthIn(max = 560.dp).fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // ----- Código automático (vista previa en vivo) -----
                Card(
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(20.dp)) {
                        Text(
                            text = "Código asignado",
                            style = MaterialTheme.typography.labelLarge,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Text(
                            text = categoriaElegida?.siguienteSku() ?: "Elige una categoría",
                            style = MaterialTheme.typography.headlineSmall,
                            fontWeight = FontWeight.Bold,
                            color = if (categoriaElegida != null) MaterialTheme.colorScheme.primary
                            else MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Text(
                            text = "Se genera automáticamente y nunca se repite",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                // ----- Categoría (lista desplegable) -----
                ExposedDropdownMenuBox(
                    expanded = menuAbierto,
                    onExpandedChange = { menuAbierto = it }
                ) {
                    OutlinedTextField(
                        value = categoriaElegida?.let { "${it.nombre} (${it.prefijo})" } ?: "",
                        onValueChange = {},
                        readOnly = true,
                        label = { Text("Categoría") },
                        isError = errorCategoria != null,
                        supportingText = { if (errorCategoria != null) Text(errorCategoria) },
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = menuAbierto) },
                        modifier = Modifier
                            .menuAnchor(ExposedDropdownMenuAnchorType.PrimaryNotEditable)
                            .fillMaxWidth()
                    )
                    ExposedDropdownMenu(
                        expanded = menuAbierto,
                        onDismissRequest = { menuAbierto = false }
                    ) {
                        categorias.forEach { cat ->
                            DropdownMenuItem(
                                text = { Text("${cat.nombre} (${cat.prefijo})") },
                                onClick = {
                                    categoria = cat.nombre
                                    menuAbierto = false
                                }
                            )
                        }
                    }
                }

                // Acceso rápido para crear una categoría que no existe
                TextButton(onClick = { viewModel.navigateTo(Screen.AgregarCategoria) }) {
                    Icon(Icons.Default.Add, contentDescription = null)
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Crear nueva categoría")
                }

                // ----- Datos del producto -----
                OutlinedTextField(
                    value = nombre,
                    onValueChange = { nombre = it },
                    label = { Text("Nombre del producto") },
                    singleLine = true,
                    isError = errorNombre != null,
                    supportingText = { if (errorNombre != null) Text(errorNombre) },
                    keyboardOptions = KeyboardOptions(
                        capitalization = KeyboardCapitalization.Sentences,
                        imeAction = ImeAction.Next
                    ),
                    modifier = Modifier.fillMaxWidth()
                )

                OutlinedTextField(
                    value = descripcion,
                    onValueChange = { descripcion = it },
                    label = { Text("Descripción (opcional)") },
                    minLines = 2,
                    maxLines = 3,
                    modifier = Modifier.fillMaxWidth()
                )

                OutlinedTextField(
                    value = precio,
                    onValueChange = { precio = soloNumeros(it) },
                    label = { Text("Precio (CLP)") },
                    prefix = { Text("$ ") },
                    singleLine = true,
                    isError = errorPrecio != null,
                    supportingText = { if (errorPrecio != null) Text(errorPrecio) },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number, imeAction = ImeAction.Next),
                    modifier = Modifier.fillMaxWidth()
                )

                Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    OutlinedTextField(
                        value = stock,
                        onValueChange = { stock = soloNumeros(it) },
                        label = { Text("Stock inicial") },
                        singleLine = true,
                        isError = errorStock != null,
                        supportingText = { if (errorStock != null) Text(errorStock) },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number, imeAction = ImeAction.Next),
                        modifier = Modifier.weight(1f)
                    )
                    OutlinedTextField(
                        value = stockMinimo,
                        onValueChange = { stockMinimo = soloNumeros(it) },
                        label = { Text("Stock mínimo") },
                        singleLine = true,
                        isError = errorMinimo != null,
                        supportingText = { if (errorMinimo != null) Text(errorMinimo) },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number, imeAction = ImeAction.Next),
                        modifier = Modifier.weight(1f)
                    )
                }

                OutlinedTextField(
                    value = ubicacion,
                    onValueChange = { ubicacion = it },
                    label = { Text("Ubicación") },
                    placeholder = { Text("Ej: Pasillo 1 - Rack 2") },
                    singleLine = true,
                    isError = errorUbicacion != null,
                    supportingText = { if (errorUbicacion != null) Text(errorUbicacion) },
                    keyboardOptions = KeyboardOptions(
                        capitalization = KeyboardCapitalization.Sentences,
                        imeAction = ImeAction.Done
                    ),
                    modifier = Modifier.fillMaxWidth()
                )

                // Error de la base
                MensajeAnimado(
                    mensaje = (resultado as? ResultadoCatalogo.Error)?.mensaje,
                    esError = true
                )

                // ----- Guardar -----
                Button(
                    onClick = {
                        intentoEnviar = true
                        val cat = categoriaElegida
                        val formularioValido = cat != null &&
                                validarObligatorio(nombre, "El nombre") == null &&
                                validarPrecio(precio) == null &&
                                validarEnteroNoNegativo(stock, "El stock inicial") == null &&
                                validarEnteroNoNegativo(stockMinimo, "El stock mínimo") == null &&
                                ubicacion.isNotBlank()
                        if (formularioValido && cat != null) {
                            catalogoViewModel.crearProducto(
                                nombreCategoria = cat.nombre,
                                nombre = nombre,
                                descripcion = descripcion,
                                precio = precio.toInt(),
                                stockInicial = stock.toInt(),
                                stockMinimo = stockMinimo.toInt(),
                                ubicacion = ubicacion
                            )
                        }
                    },
                    enabled = !isLoading,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    if (isLoading) {
                        CircularProgressIndicator(modifier = Modifier.size(20.dp), strokeWidth = 2.dp)
                    } else {
                        Text("Guardar producto")
                    }
                }
            }
        }
    }
}