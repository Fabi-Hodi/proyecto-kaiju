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
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.example.appkaiju_grupo10.model.Permisos
import com.example.appkaiju_grupo10.model.Rol
import com.example.appkaiju_grupo10.ui.components.MensajeAnimado
import com.example.appkaiju_grupo10.ui.utils.validarEmail
import com.example.appkaiju_grupo10.ui.utils.validarNombre
import com.example.appkaiju_grupo10.viewmodels.EditarUsuarioViewModel
import com.example.appkaiju_grupo10.viewmodels.MainViewModel
import com.example.appkaiju_grupo10.viewmodels.ResultadoEdicion

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditarUsuarioScreen(
    navController: NavController,
    viewModel: MainViewModel,
    id: Long,
    editarViewModel: EditarUsuarioViewModel = viewModel()
) {
    val usuario by editarViewModel.usuario.collectAsState()
    val isLoading by editarViewModel.isLoading.collectAsState()
    val errorCarga by editarViewModel.errorCarga.collectAsState()
    val guardando by editarViewModel.guardando.collectAsState()
    val resultado by editarViewModel.resultado.collectAsState()
    val usuarioActual by viewModel.usuarioActual.collectAsState()
    val context = LocalContext.current

    LaunchedEffect(id) { editarViewModel.cargar(id) }

    // Campos del formulario: se llenan cuando llega el usuario (la clave usuario?.id los reinicia)
    var nombre by rememberSaveable(usuario?.id) { mutableStateOf(usuario?.nombreCompleto ?: "") }
    var email by rememberSaveable(usuario?.id) { mutableStateOf(usuario?.email ?: "") }
    var rol by rememberSaveable(usuario?.id) {
        mutableStateOf(Rol.entries.firstOrNull { it.name == usuario?.rol } ?: Rol.BODEGA)
    }
    var activo by rememberSaveable(usuario?.id) { mutableStateOf(usuario?.activo ?: true) }

    // NUEVO: permisos (uno por interruptor)
    var permEntradas by rememberSaveable(usuario?.id) { mutableStateOf(usuario?.permisoEntradas ?: false) }
    var permSalidas by rememberSaveable(usuario?.id) { mutableStateOf(usuario?.permisoSalidas ?: false) }
    var permPrecios by rememberSaveable(usuario?.id) { mutableStateOf(usuario?.permisoPrecios ?: false) }
    var permReportes by rememberSaveable(usuario?.id) { mutableStateOf(usuario?.permisoReportes ?: false) }

    var intentoGuardar by rememberSaveable { mutableStateOf(false) }

    // ¿El admin está editando su propia cuenta? (se compara el RUT)
    val esSuPropiaCuenta = usuario != null && usuario?.rut == usuarioActual?.rut
    // Un Admin siempre tiene todos los permisos: interruptores encendidos y bloqueados
    val rolEsAdmin = rol == Rol.ADMIN

    val errorNombre = if (intentoGuardar) validarNombre(nombre) else null
    val errorEmail = if (intentoGuardar) validarEmail(email) else null

    // Al cambiar de rol, los permisos vuelven a los de ese rol (luego se pueden ajustar)
    fun cambiarRol(nuevo: Rol) {
        rol = nuevo
        val porDefecto = Permisos.porRol(nuevo)
        permEntradas = porDefecto.entradas
        permSalidas = porDefecto.salidas
        permPrecios = porDefecto.precios
        permReportes = porDefecto.reportes
    }

    // Guardado OK: aviso y volver a la lista
    LaunchedEffect(resultado) {
        if (resultado is ResultadoEdicion.Guardado) {
            Toast.makeText(context, "Cambios guardados", Toast.LENGTH_SHORT).show()
            editarViewModel.limpiarResultado()
            viewModel.navigateBack()
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Editar usuario") },
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
        val u = usuario
        Box(modifier = Modifier.padding(innerPadding).fillMaxSize()) {
            when {
                u == null && isLoading -> {
                    CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
                }
                u == null -> {
                    Column(
                        modifier = Modifier.align(Alignment.Center),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(errorCarga ?: "No se encontró el usuario", color = MaterialTheme.colorScheme.error)
                        Spacer(Modifier.height(12.dp))
                        Button(onClick = { editarViewModel.cargar(id) }) { Text("Reintentar") }
                    }
                }
                else -> {
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .verticalScroll(rememberScrollState())
                            .padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(14.dp)
                    ) {
                        // ----- Encabezado -----
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            AvatarIniciales(nombre = u.nombreCompleto, tamano = 56.dp)
                            Column {
                                Text(
                                    text = u.nombreCompleto,
                                    style = MaterialTheme.typography.titleLarge,
                                    fontWeight = FontWeight.Bold
                                )
                                Text(
                                    text = "RUT ${u.rut} · Creado el ${formatearFechaRegistro(u.fechaRegistro)}",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }

                        // ----- Nombre -----
                        OutlinedTextField(
                            value = nombre,
                            onValueChange = { nombre = it },
                            label = { Text("Nombre completo") },
                            singleLine = true,
                            isError = errorNombre != null,
                            supportingText = { if (errorNombre != null) Text(errorNombre) },
                            keyboardOptions = KeyboardOptions(imeAction = ImeAction.Next),
                            modifier = Modifier.fillMaxWidth()
                        )

                        // ----- Correo -----
                        OutlinedTextField(
                            value = email,
                            onValueChange = { email = it.trim() },
                            label = { Text("Correo") },
                            singleLine = true,
                            isError = errorEmail != null,
                            supportingText = { if (errorEmail != null) Text(errorEmail) },
                            keyboardOptions = KeyboardOptions(
                                keyboardType = KeyboardType.Email,
                                imeAction = ImeAction.Done
                            ),
                            modifier = Modifier.fillMaxWidth()
                        )

                        // ----- Rol -----
                        Text("Rol", style = MaterialTheme.typography.titleSmall)
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            Rol.entries.forEach { opcion ->
                                FilterChip(
                                    selected = rol == opcion,
                                    onClick = { if (rol != opcion) cambiarRol(opcion) },
                                    enabled = !esSuPropiaCuenta,
                                    label = { Text(opcion.etiqueta) }
                                )
                            }
                        }

                        // ----- NUEVO: Permisos -----
                        Text("Permisos", style = MaterialTheme.typography.titleSmall)
                        Card(
                            shape = RoundedCornerShape(16.dp),
                            colors = CardDefaults.cardColors(
                                containerColor = MaterialTheme.colorScheme.surfaceVariant
                            )
                        ) {
                            Column(modifier = Modifier.padding(vertical = 4.dp)) {
                                FilaPermiso(
                                    texto = "Registrar entradas",
                                    activo = rolEsAdmin || permEntradas,
                                    habilitado = !rolEsAdmin,
                                    onCambio = { permEntradas = it }
                                )
                                HorizontalDivider()
                                FilaPermiso(
                                    texto = "Registrar salidas",
                                    activo = rolEsAdmin || permSalidas,
                                    habilitado = !rolEsAdmin,
                                    onCambio = { permSalidas = it }
                                )
                                HorizontalDivider()
                                FilaPermiso(
                                    texto = "Editar precios",
                                    activo = rolEsAdmin || permPrecios,
                                    habilitado = !rolEsAdmin,
                                    onCambio = { permPrecios = it }
                                )
                                HorizontalDivider()
                                FilaPermiso(
                                    texto = "Ver reportes",
                                    activo = rolEsAdmin || permReportes,
                                    habilitado = !rolEsAdmin,
                                    onCambio = { permReportes = it }
                                )
                            }
                        }
                        if (rolEsAdmin) {
                            Text(
                                text = "Un administrador siempre tiene todos los permisos.",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }

                        // ----- Cuenta activa -----
                        Card(
                            shape = RoundedCornerShape(16.dp),
                            colors = CardDefaults.cardColors(
                                containerColor = MaterialTheme.colorScheme.surfaceVariant
                            )
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth().padding(16.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Text("Cuenta activa", style = MaterialTheme.typography.titleSmall)
                                    Text(
                                        text = "Si la desactivas, no podrá iniciar sesión",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                                Switch(
                                    checked = activo,
                                    onCheckedChange = { activo = it },
                                    enabled = !esSuPropiaCuenta
                                )
                            }
                        }

                        if (esSuPropiaCuenta) {
                            Text(
                                text = "Es tu propia cuenta: no puedes cambiar tu rol ni desactivarla.",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }

                        // ----- Error del servidor -----
                        MensajeAnimado(
                            mensaje = (resultado as? ResultadoEdicion.Error)?.mensaje,
                            esError = true
                        )

                        // ----- Guardar -----
                        Button(
                            onClick = {
                                intentoGuardar = true
                                if (validarNombre(nombre) == null && validarEmail(email) == null) {
                                    editarViewModel.guardar(
                                        id = u.id,
                                        nombre = nombre,
                                        email = email,
                                        rol = rol,
                                        activo = activo,
                                        permisos = Permisos(
                                            entradas = permEntradas,
                                            salidas = permSalidas,
                                            precios = permPrecios,
                                            reportes = permReportes
                                        )
                                    )
                                }
                            },
                            enabled = !guardando,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            if (guardando) {
                                CircularProgressIndicator(
                                    modifier = Modifier.size(20.dp),
                                    strokeWidth = 2.dp
                                )
                            } else {
                                Text("Guardar cambios")
                            }
                        }
                    }
                }
            }
        }
    }
}

// Una fila de permiso: texto a la izquierda, interruptor a la derecha
@Composable
private fun FilaPermiso(
    texto: String,
    activo: Boolean,
    habilitado: Boolean,
    onCambio: (Boolean) -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 6.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = texto,
            style = MaterialTheme.typography.bodyLarge,
            modifier = Modifier.weight(1f)
        )
        Switch(checked = activo, onCheckedChange = onCambio, enabled = habilitado)
    }
}

// "2026-11-26T10:15:30.123" -> "26-11-2026"
fun formatearFechaRegistro(fecha: String?): String =
    runCatching {
        val (anio, mes, dia) = fecha!!.substring(0, 10).split("-")
        "$dia-$mes-$anio"
    }.getOrDefault("—")