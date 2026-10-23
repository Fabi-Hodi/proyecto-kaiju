package com.example.appkaiju_grupo10.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Logout
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material.icons.filled.Build
import androidx.compose.material.icons.filled.Category
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Inventory2
import androidx.compose.material.icons.filled.LocalShipping
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.*
import androidx.compose.material3.windowsizeclass.WindowWidthSizeClass
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.example.appkaiju_grupo10.model.CategoriaResumen
import com.example.appkaiju_grupo10.navigation.Screen
import com.example.appkaiju_grupo10.ui.utils.obtenerWindowSizeClass
import com.example.appkaiju_grupo10.viewmodels.InventarioViewModel
import com.example.appkaiju_grupo10.viewmodels.MainViewModel
import kotlinx.coroutines.launch
import com.example.appkaiju_grupo10.ui.components.iconoCategoria

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    navController: NavController,
    viewModel: MainViewModel,
    inventarioViewModel: InventarioViewModel = viewModel()
) {
    val windowSizeClass = obtenerWindowSizeClass()
    val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)
    val scope = rememberCoroutineScope()

    val categorias by inventarioViewModel.categorias.collectAsState()
    var busqueda by rememberSaveable { mutableStateOf("") }

    // ADAPTABILIDAD (Guía 9): cuántas columnas según el ancho de la pantalla
    val columnas = when (windowSizeClass.widthSizeClass) {
        WindowWidthSizeClass.Compact -> 2      // celular
        WindowWidthSizeClass.Medium -> 3       // tablet pequeña / celular horizontal
        else -> 4                              // tablet grande
    }

    // Acción de buscar: se usa en la lupa y en la tecla "buscar" del teclado
    val buscar = {
        if (busqueda.isNotBlank()) viewModel.navigateToInventario(busqueda = busqueda.trim())
    }

    // ----- MENÚ LATERAL (Guía 10) -----
    ModalNavigationDrawer(
        drawerState = drawerState,
        drawerContent = {
            ModalDrawerSheet {
                Text(
                    text = "Menú Kaiju",
                    modifier = Modifier.padding(16.dp),
                    style = MaterialTheme.typography.titleLarge
                )
                NavigationDrawerItem(
                    label = { Text("Inicio") },
                    icon = { Icon(Icons.Default.Home, contentDescription = null) },
                    selected = true,
                    onClick = { scope.launch { drawerState.close() } }
                )
                NavigationDrawerItem(
                    label = { Text("Inventario") },
                    icon = { Icon(Icons.Default.Inventory2, contentDescription = null) },
                    selected = false,
                    onClick = {
                        scope.launch { drawerState.close() }
                        viewModel.navigateToInventario()
                    }
                )
                NavigationDrawerItem(
                    label = { Text("Perfil") },
                    icon = { Icon(Icons.Default.Person, contentDescription = null) },
                    selected = false,
                    onClick = {
                        scope.launch { drawerState.close() }
                        viewModel.navigateTo(Screen.Profile)
                    }
                )
                NavigationDrawerItem(
                    label = { Text("Configuración") },
                    icon = { Icon(Icons.Default.Settings, contentDescription = null) },
                    selected = false,
                    onClick = {
                        scope.launch { drawerState.close() }
                        viewModel.navigateTo(Screen.Settings)
                    }
                )
                HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))
                NavigationDrawerItem(
                    label = { Text("Cerrar sesión") },
                    icon = { Icon(Icons.AutoMirrored.Filled.Logout, contentDescription = null) },
                    selected = false,
                    onClick = {
                        scope.launch { drawerState.close() }
                        // vuelve al Login y borra el Inicio del historial
                        viewModel.cerrarSesion()
                    }
                )
            }
        }
    ) {
        Scaffold(
            topBar = {
                TopAppBar(
                    title = { Text("Kaiju") },
                    navigationIcon = {
                        IconButton(onClick = { scope.launch { drawerState.open() } }) {
                            Icon(Icons.Default.Menu, contentDescription = "Menú")
                        }
                    },
                    actions = {
                        IconButton(onClick = { viewModel.navigateTo(Screen.Profile) }) {
                            Icon(
                                Icons.Default.AccountCircle,
                                contentDescription = "Perfil",
                                modifier = Modifier.size(32.dp)
                            )
                        }
                    },
                    // barra gris del mockup (color del Paso 2)
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
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // ----- Buscador -----
                OutlinedTextField(
                    value = busqueda,
                    onValueChange = { busqueda = it },
                    placeholder = { Text("Buscar producto por SKU o nombre...") },
                    singleLine = true,
                    trailingIcon = {
                        IconButton(onClick = buscar) {
                            Icon(Icons.Default.Search, contentDescription = "Buscar")
                        }
                    },
                    keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search),
                    keyboardActions = KeyboardActions(onSearch = { buscar() }),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.fillMaxWidth()
                )

                Text(
                    text = "Categoría",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold
                )
                HorizontalDivider()

                // ----- Grilla de categorías -----
                if (categorias.isEmpty()) {
                    // mientras Room responde (o carga los datos la primera vez)
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        CircularProgressIndicator()
                    }
                } else {
                    LazyVerticalGrid(
                        columns = GridCells.Fixed(columnas),
                        horizontalArrangement = Arrangement.spacedBy(12.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        items(items = categorias, key = { it.categoria }) { categoria ->
                            TarjetaCategoria(
                                categoria = categoria,
                                onClick = { viewModel.navigateToInventario(categoria = categoria.categoria) }
                            )
                        }
                    }
                }
            }
        }
    }
}

// Tarjeta reutilizable de una categoría
@Composable
fun TarjetaCategoria(categoria: CategoriaResumen, onClick: () -> Unit) {
    Card(
        onClick = onClick,
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
    ) {
        Column(
            modifier = Modifier.fillMaxWidth().padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            // recuadro cuadrado con el ícono (el "placeholder" gris del mockup)
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .aspectRatio(1f)
                    .clip(RoundedCornerShape(12.dp))
                    .background(MaterialTheme.colorScheme.surface),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = iconoCategoria(categoria.categoria),
                    contentDescription = null,
                    modifier = Modifier.size(48.dp),
                    tint = MaterialTheme.colorScheme.primary
                )
            }
            Text(
                text = categoria.categoria,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
            Text(
                text = "${categoria.cantidad} productos",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

// un icono distinto según la categoría
private fun iconoCategoria(nombre: String): ImageVector = when (nombre) {
    "Insumos" -> Icons.Default.Inventory2
    "Herramientas" -> Icons.Default.Build
    "Embalaje" -> Icons.Default.LocalShipping
    else -> Icons.Default.Category
}