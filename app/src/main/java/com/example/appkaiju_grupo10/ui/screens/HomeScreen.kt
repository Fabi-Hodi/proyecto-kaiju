package com.example.appkaiju_grupo10.ui.screens

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material3.*
import androidx.compose.material3.windowsizeclass.WindowWidthSizeClass
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.example.appkaiju_grupo10.R
import com.example.appkaiju_grupo10.navigation.Screen
import com.example.appkaiju_grupo10.ui.utils.obtenerWindowSizeClass
import com.example.appkaiju_grupo10.viewmodels.MainViewModel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch

//EL CEREBRO AHORA INCLUYE EL MENÚ LATERAL
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    navController: NavController,
    viewModel: MainViewModel
) {
    val windowSizeClass = obtenerWindowSizeClass()
    val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)
    val scope = rememberCoroutineScope()

    // el menu lateral envuelve toda la pantalla
    ModalNavigationDrawer(
        drawerState = drawerState,
        drawerContent = {
            ModalDrawerSheet {
                Text(text = "Menú Kaiju", modifier = Modifier.padding(16.dp), style = MaterialTheme.typography.titleLarge)
                NavigationDrawerItem(
                    label = { Text(text = "Ir a Perfil") },
                    selected = false,
                    onClick = {
                        scope.launch { drawerState.close() }
                        viewModel.navigateTo(Screen.Profile)
                    }
                )
            }
        }
    ) {
        // Aquí decidimos que diseño mostrar, pero ahora le pasamos las herramientas de navegación
        when (windowSizeClass.widthSizeClass) {
            WindowWidthSizeClass.Compact -> HomeScreenCompacta(viewModel, drawerState, scope)
            WindowWidthSizeClass.Medium -> HomeScreenMediana(viewModel, drawerState, scope)
            WindowWidthSizeClass.Expanded -> HomeScreenExpandida(viewModel, drawerState, scope)
        }
    }
}

//DISEÑO PARA CELULARES
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreenCompacta(viewModel: MainViewModel, drawerState: DrawerState, scope: CoroutineScope) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Kaiju - Celular") },
                navigationIcon = {
                    IconButton(onClick = { scope.launch { drawerState.open() } }) {
                        Icon(imageVector = Icons.Default.Menu, contentDescription = "Menú")
                    }
                }
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier.padding(innerPadding).fillMaxSize().padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(20.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text("¡Bienvenido a Kaiju!", style = MaterialTheme.typography.titleLarge)
            Button(onClick = { viewModel.navigateTo(Screen.Settings) }) {
                Text("Ir a Configuración")
            }
            Image(
                painter = painterResource(id = R.drawable.icono_kaiju),
                contentDescription = "Logo App",
                modifier = Modifier.fillMaxWidth().height(150.dp),
                contentScale = ContentScale.Fit
            )
        }
    }
}

//DISEÑO PARA TABLETS PEQUEÑAS
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreenMediana(viewModel: MainViewModel, drawerState: DrawerState, scope: CoroutineScope) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Kaiju - Tablet Pequeña") },
                navigationIcon = {
                    IconButton(onClick = { scope.launch { drawerState.open() } }) {
                        Icon(imageVector = Icons.Default.Menu, contentDescription = "Menú")
                    }
                }
            )
        }
    ) { innerPadding ->
        Row(
            modifier = Modifier.padding(innerPadding).fillMaxSize().padding(16.dp),
            horizontalArrangement = Arrangement.spacedBy(20.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(20.dp)) {
                Text("¡Bienvenido a Kaiju!", style = MaterialTheme.typography.titleLarge)
                Button(onClick = { viewModel.navigateTo(Screen.Settings) }) {
                    Text("Ir a Configuración")
                }
            }
            Image(
                painter = painterResource(id = R.drawable.icono_kaiju),
                contentDescription = "Logo",
                modifier = Modifier.weight(1f).height(200.dp)
            )
        }
    }
}

//DISEÑO PARA TABLETS GRANDES O PC
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreenExpandida(viewModel: MainViewModel, drawerState: DrawerState, scope: CoroutineScope) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Kaiju - Pantalla Gigante") },
                navigationIcon = {
                    IconButton(onClick = { scope.launch { drawerState.open() } }) {
                        Icon(imageVector = Icons.Default.Menu, contentDescription = "Menú")
                    }
                }
            )
        }
    ) { innerPadding ->
        Row(
            modifier = Modifier.padding(innerPadding).fillMaxSize().padding(32.dp),
            horizontalArrangement = Arrangement.SpaceEvenly,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text("Vista expandida para Kaiju", style = MaterialTheme.typography.headlineLarge)
            Button(onClick = { viewModel.navigateTo(Screen.Settings) }) {
                Text("Ir a Configuración")
            }
        }
    }
}