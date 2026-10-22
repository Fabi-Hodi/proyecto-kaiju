package com.example.appkaiju_grupo10.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.example.appkaiju_grupo10.navigation.Screen
import com.example.appkaiju_grupo10.viewmodels.MainViewModel

@Composable
fun SettingsScreen(
    navController: NavController,
    viewModel: MainViewModel
) {
    Column(
        modifier = Modifier.fillMaxSize().padding(16.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(text = "Pantalla de Configuración (Settings)")
        Spacer(modifier = Modifier.height(24.dp))

        Button(onClick = { viewModel.volverAlInicio() }) {
            Text(text = "Volver al Inicio")
        }

        Spacer(modifier = Modifier.height(16.dp))

        Button(onClick = { viewModel.navigateTo(Screen.Profile) }) {
            Text(text = "Ir al Perfil")
        }
    }
}