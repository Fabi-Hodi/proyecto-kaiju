package com.example.appkaiju_grupo10.ui.screens

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.material3.windowsizeclass.WindowWidthSizeClass
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import com.example.appkaiju_grupo10.R
import com.example.appkaiju_grupo10.ui.utils.obtenerWindowSizeClass

// DECIDE QUE PANTALLA MOSTRAR
@Composable
fun HomeScreenAdaptativa() {
    val windowSizeClass = obtenerWindowSizeClass()

    when (windowSizeClass.widthSizeClass) {
        WindowWidthSizeClass.Compact -> HomeScreenCompacta()
        WindowWidthSizeClass.Medium -> HomeScreenMediana()
        WindowWidthSizeClass.Expanded -> HomeScreenExpandida()
    }
}

// DISEÑO PARA CELULARES (Compacta)
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreenCompacta() {
    Scaffold(
        topBar = { TopAppBar(title = { Text("Kaiju - Celular") }) }
    ) { innerPadding ->
        Column(
            modifier = Modifier.padding(innerPadding).fillMaxSize().padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(20.dp)
        ) {
            Text("¡Bienvenido a Kaiju!", style = MaterialTheme.typography.titleLarge)
            Button(onClick = { }) { Text("Presióname") }
            Image(
                painter = painterResource(id = R.drawable.icono_kaiju),
                contentDescription = "Logo App",
                modifier = Modifier.fillMaxWidth().height(150.dp),
                contentScale = ContentScale.Fit
            )
        }
    }
}

// DISEÑO PARA TABLETS PEQUEÑAS (Mediana)
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreenMediana() {
    Scaffold(
        topBar = { TopAppBar(title = { Text("Kaiju - Tablet Pequeña") }) }
    ) { innerPadding ->
        Row( // Usamos Row para poner el texto y la imagen uno al lado del otro
            modifier = Modifier.padding(innerPadding).fillMaxSize().padding(16.dp),
            horizontalArrangement = Arrangement.spacedBy(20.dp)
        ) {
            Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(20.dp)) {
                Text("¡Bienvenido a Kaiju!", style = MaterialTheme.typography.titleLarge)
                Button(onClick = { }) { Text("Presióname") }
            }
            Image(
                painter = painterResource(id = R.drawable.icono_kaiju),
                contentDescription = "Logo",
                modifier = Modifier.weight(1f).height(200.dp)
            )
        }
    }
}

// DISEÑO PARA TABLETS GRANDES O PC (Expandida)
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreenExpandida() {
    Scaffold(
        topBar = { TopAppBar(title = { Text("Kaiju - Pantalla Gigante") }) }
    ) { innerPadding ->
        Row(
            modifier = Modifier.padding(innerPadding).fillMaxSize().padding(32.dp),
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            Text("Vista expandida para Kaiju", style = MaterialTheme.typography.headlineLarge)
            Button(onClick = { }) { Text("Botón Expandido") }
        }
    }
}