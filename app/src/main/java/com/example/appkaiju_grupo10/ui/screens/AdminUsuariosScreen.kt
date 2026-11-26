package com.example.appkaiju_grupo10.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
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
import com.example.appkaiju_grupo10.data.remote.UsuarioKaijuResponse
import com.example.appkaiju_grupo10.model.Rol
import com.example.appkaiju_grupo10.viewmodels.AdminUsuariosViewModel
import com.example.appkaiju_grupo10.viewmodels.MainViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AdminUsuariosScreen(
    navController: NavController,
    viewModel: MainViewModel,
    adminViewModel: AdminUsuariosViewModel = viewModel()
) {
    val usuarios by adminViewModel.usuarios.collectAsState()
    val isLoading by adminViewModel.isLoading.collectAsState()
    val error by adminViewModel.error.collectAsState()

    // null = "Todos"; si no, el nombre del rol ("ADMIN", "BODEGA", "VENTAS")
    var filtro by rememberSaveable { mutableStateOf<String?>(null) }

    // Se ejecuta al abrir la pantalla y cada vez que se vuelve desde "Editar usuario"
    LaunchedEffect(Unit) { adminViewModel.cargar() }

    val visibles = remember(usuarios, filtro) {
        if (filtro == null) usuarios else usuarios.filter { it.rol == filtro }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Usuarios") },
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
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // ----- Filtros por rol -----
            Row(
                modifier = Modifier.horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                FilterChip(
                    selected = filtro == null,
                    onClick = { filtro = null },
                    label = { Text("Todos") }
                )
                Rol.entries.forEach { rol ->
                    FilterChip(
                        selected = filtro == rol.name,
                        onClick = { filtro = rol.name },
                        label = { Text(rol.etiqueta) }
                    )
                }
            }

            Text(
                text = "${visibles.size} usuarios",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            when {
                // cargando por primera vez
                isLoading && usuarios.isEmpty() -> {
                    Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        CircularProgressIndicator()
                    }
                }
                // error y nada que mostrar: mensaje + reintentar
                error != null && usuarios.isEmpty() -> {
                    Column(
                        modifier = Modifier.fillMaxSize(),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        Text(error ?: "", color = MaterialTheme.colorScheme.error)
                        Spacer(Modifier.height(12.dp))
                        Button(onClick = { adminViewModel.cargar() }) { Text("Reintentar") }
                    }
                }
                else -> {
                    LazyColumn(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                        items(items = visibles, key = { it.id }) { usuario ->
                            TarjetaUsuario(
                                usuario = usuario,
                                onClick = { viewModel.navigateToEditarUsuario(usuario.id) }
                            )
                        }
                    }
                }
            }
        }
    }
}

// Tarjeta de un usuario: iniciales, nombre, correo, rol y estado
@Composable
fun TarjetaUsuario(usuario: UsuarioKaijuResponse, onClick: () -> Unit) {
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
            AvatarIniciales(nombre = usuario.nombreCompleto)

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = usuario.nombreCompleto,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    text = usuario.email,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }

            Column(
                horizontalAlignment = Alignment.End,
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                EtiquetaRol(rol = usuario.rol)
                Text(
                    text = if (usuario.activo) "● Activo" else "○ Inactivo",
                    style = MaterialTheme.typography.labelSmall,
                    color = if (usuario.activo) MaterialTheme.colorScheme.primary
                    else MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

// Círculo con las iniciales del nombre ("Ana Pérez" -> "AP")
@Composable
fun AvatarIniciales(nombre: String, tamano: Dp = 44.dp) {
    Box(
        modifier = Modifier
            .size(tamano)
            .clip(CircleShape)
            .background(MaterialTheme.colorScheme.primaryContainer),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = iniciales(nombre),
            color = MaterialTheme.colorScheme.onPrimaryContainer,
            fontWeight = FontWeight.Bold
        )
    }
}

// Etiqueta pequeña con el nombre del rol ("BODEGA" -> "Bodega")
@Composable
fun EtiquetaRol(rol: String) {
    val texto = Rol.entries.firstOrNull { it.name == rol }?.etiqueta ?: rol
    Surface(
        shape = RoundedCornerShape(8.dp),
        color = MaterialTheme.colorScheme.primaryContainer
    ) {
        Text(
            text = texto,
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.onPrimaryContainer,
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp)
        )
    }
}

fun iniciales(nombre: String): String =
    nombre.trim()
        .split(" ")
        .filter { it.isNotBlank() }
        .take(2)
        .joinToString("") { it.first().uppercase() }
        .ifEmpty { "?" }