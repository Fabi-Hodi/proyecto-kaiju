package com.example.appkaiju_grupo10.ui.screens

import android.widget.Toast
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.example.appkaiju_grupo10.model.formatearSku
import com.example.appkaiju_grupo10.ui.components.MensajeAnimado
import com.example.appkaiju_grupo10.ui.utils.sugerirPrefijo
import com.example.appkaiju_grupo10.ui.utils.validarNombreCategoria
import com.example.appkaiju_grupo10.ui.utils.validarPrefijo
import com.example.appkaiju_grupo10.viewmodels.CatalogoViewModel
import com.example.appkaiju_grupo10.viewmodels.MainViewModel
import com.example.appkaiju_grupo10.viewmodels.ResultadoCatalogo

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AgregarCategoriaScreen(
    navController: NavController,
    viewModel: MainViewModel,
    catalogoViewModel: CatalogoViewModel = viewModel()
) {
    var nombre by rememberSaveable { mutableStateOf("") }
    var prefijo by rememberSaveable { mutableStateOf("") }
    var prefijoEditado by rememberSaveable { mutableStateOf(false) }   // ¿el usuario lo cambió a mano?
    var intentoEnviar by rememberSaveable { mutableStateOf(false) }

    val isLoading by catalogoViewModel.isLoading.collectAsState()
    val resultado by catalogoViewModel.resultado.collectAsState()
    val context = LocalContext.current

    val errorNombre = if (intentoEnviar) validarNombreCategoria(nombre) else null
    val errorPrefijo = if (intentoEnviar) validarPrefijo(prefijo) else null

    // Si se guardó bien: aviso y volver a la pantalla anterior
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
                title = { Text("Nueva categoría") },
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
            Card(
                modifier = Modifier.widthIn(max = 480.dp).fillMaxWidth(),
                shape = RoundedCornerShape(24.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
            ) {
                Column(
                    modifier = Modifier.padding(24.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    // ----- Nombre: al escribir, sugiere el prefijo -----
                    OutlinedTextField(
                        value = nombre,
                        onValueChange = { nuevo ->
                            nombre = nuevo
                            if (!prefijoEditado) prefijo = sugerirPrefijo(nuevo)
                        },
                        label = { Text("Nombre de la categoría") },
                        placeholder = { Text("Ej: Limpieza") },
                        singleLine = true,
                        isError = errorNombre != null,
                        supportingText = { if (errorNombre != null) Text(errorNombre) },
                        keyboardOptions = KeyboardOptions(
                            capitalization = KeyboardCapitalization.Sentences,
                            imeAction = ImeAction.Next
                        ),
                        modifier = Modifier.fillMaxWidth()
                    )

                    // ----- Prefijo: sugerido, pero editable -----
                    OutlinedTextField(
                        value = prefijo,
                        onValueChange = { nuevo ->
                            prefijoEditado = true
                            prefijo = nuevo.uppercase().filter { it in 'A'..'Z' }.take(3)
                        },
                        label = { Text("Prefijo del código") },
                        singleLine = true,
                        isError = errorPrefijo != null,
                        supportingText = {
                            Text(errorPrefijo ?: "Se sugiere solo; puedes cambiarlo (2 o 3 letras)")
                        },
                        keyboardOptions = KeyboardOptions(
                            capitalization = KeyboardCapitalization.Characters,
                            imeAction = ImeAction.Done
                        ),
                        modifier = Modifier.fillMaxWidth()
                    )

                    // ----- Vista previa de los códigos -----
                    if (validarPrefijo(prefijo) == null) {
                        Text(
                            text = "Los productos se codificarán: ${formatearSku(prefijo, 1)}, ${formatearSku(prefijo, 2)}, …",
                            color = MaterialTheme.colorScheme.primary,
                            fontWeight = FontWeight.Bold
                        )
                    }

                    // Error de la base (nombre o prefijo repetido)
                    MensajeAnimado(
                        mensaje = (resultado as? ResultadoCatalogo.Error)?.mensaje,
                        esError = true
                    )

                    Button(
                        onClick = {
                            intentoEnviar = true
                            if (validarNombreCategoria(nombre) == null && validarPrefijo(prefijo) == null) {
                                catalogoViewModel.crearCategoria(nombre, prefijo)
                            }
                        },
                        enabled = !isLoading,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        if (isLoading) {
                            CircularProgressIndicator(modifier = Modifier.size(20.dp), strokeWidth = 2.dp)
                        } else {
                            Text("Guardar categoría")
                        }
                    }
                }
            }
        }
    }
}