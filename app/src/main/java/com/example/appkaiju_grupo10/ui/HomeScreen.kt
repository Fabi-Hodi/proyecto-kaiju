package com.example.appkaiju_grupo10.ui

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.* // Importamos todo layout (Column, Row, Modifier, etc.)
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Text
import androidx.compose.material3.Scaffold
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.tooling.preview.Preview
import com.example.appkaiju_grupo10.R // Importa los recursos de TU proyecto

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen() {
    // Scaffold nos da la estructura base de la pantalla (barra superior, contenido, etc.)
    Scaffold(
        topBar = {
            TopAppBar(title = { Text("Mi App Kaiju") })
        }
    ) { innerPadding ->
        // Column organiza los elementos verticalmente, uno debajo del otro
        Column(
            modifier = Modifier
                .padding(innerPadding) // Respeta el espacio de la barra superior
                .fillMaxSize() // Ocupa toda la pantalla
                .padding(16.dp), // Agrega márgenes a los lados
            verticalArrangement = Arrangement.spacedBy(20.dp) // Espaciado uniforme entre los elementos (Parte 2 de la guía)
        ) {
            Text(text = "¡Bienvenido a Kaiju!")

            Button(onClick = { /* Acción futura para el botón */ }) {
                Text("Presióname")
            }

            // Imagen que agregamos en la carpeta drawable
            Image(
                painter = painterResource(id = R.drawable.icono_kaiju), // Snombre de la imagen
                contentDescription = "Logo App",
                modifier = Modifier
                    .fillMaxWidth() // Ocupa todo el ancho
                    .height(150.dp), // Altura fija
                contentScale = ContentScale.Fit
            )
        }
    }
}

// Preview nos permite ver la pantalla en el panel de diseño sin correr el emulador
@Preview(showBackground = true)
@Composable
fun HomeScreenPreview() {
    HomeScreen()
}