package com.example.appkaiju_grupo10.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.example.appkaiju_grupo10.viewmodels.MainViewModel

// Borrador - se diseña en el Paso 7
@Composable
fun InventarioScreen(navController: NavController, viewModel: MainViewModel) {
    Column(
        modifier = Modifier.fillMaxSize().padding(16.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text("Inventario (en construcción)", style = MaterialTheme.typography.titleLarge)
        Spacer(modifier = Modifier.height(16.dp))
        // prueba de paso de información: mandamos el SKU a la otra pantalla
        Button(onClick = { viewModel.navigateToMovimiento("KAJ-2034") }) {
            Text("Ver Filtro Industrial (KAJ-2034)")
        }
    }
}