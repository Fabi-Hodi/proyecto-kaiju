package com.example.appkaiju_grupo10.ui.screens

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.provider.Settings
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.PhotoCamera
import androidx.compose.material.icons.filled.PhotoLibrary
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.example.appkaiju_grupo10.navigation.Screen
import com.example.appkaiju_grupo10.ui.components.ImagenInteligente
import com.example.appkaiju_grupo10.ui.components.MensajeAnimado
import com.example.appkaiju_grupo10.viewmodels.MainViewModel
import com.example.appkaiju_grupo10.viewmodels.PerfilViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfileScreen(
    navController: NavController,
    viewModel: MainViewModel,
    perfilViewModel: PerfilViewModel = viewModel()
) {
    val items = listOf(Screen.Home, Screen.Profile)
    var selectedItem by remember { mutableIntStateOf(1) }

    val context = LocalContext.current
    val fotoUri by perfilViewModel.fotoUri.collectAsState()
    val errorImagen by perfilViewModel.errorImagen.collectAsState()
    val usuario by viewModel.usuarioActual.collectAsState()
    var permisoDenegado by remember { mutableStateOf(false) }

    // galeria: GetContent devuelve el Uri elegido (o null si se cancela)
    val galeriaLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.GetContent()
    ) { uri -> perfilViewModel.actualizarDesdeGaleria(uri) }

    // camara: TakePicture devuelve true si se tomo la foto (false si se cancela)
    val camaraLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.TakePicture()
    ) { exito -> perfilViewModel.actualizarDesdeCamara(exito) }

    // permiso de camara en tiempo de ejecucion
    val permisoLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { concedido ->
        permisoDenegado = !concedido
        if (concedido) camaraLauncher.launch(perfilViewModel.prepararFotoCamara())
    }

    val abrirCamara = {
        val tienePermiso = ContextCompat.checkSelfPermission(
            context, Manifest.permission.CAMERA
        ) == PackageManager.PERMISSION_GRANTED
        if (tienePermiso) {
            permisoDenegado = false
            camaraLauncher.launch(perfilViewModel.prepararFotoCamara())
        } else {
            permisoLauncher.launch(Manifest.permission.CAMERA)
        }
    }

    Scaffold(
        bottomBar = {
            NavigationBar {
                items.forEachIndexed { index, screen ->
                    NavigationBarItem(
                        selected = selectedItem == index,
                        onClick = {
                            selectedItem = index
                            if (screen == Screen.Home) viewModel.volverAlInicio()
                            else viewModel.navigateTo(screen)
                        },
                        label = { Text(if (screen == Screen.Home) "Inicio" else "Perfil") },
                        icon = {
                            Icon(
                                imageVector = if (screen == Screen.Home) Icons.Default.Home else Icons.Default.Person,
                                contentDescription = if (screen == Screen.Home) "Inicio" else "Perfil"
                            )
                        }
                    )
                }
            }
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .padding(innerPadding)
                .fillMaxSize()
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            ImagenInteligente(uri = fotoUri)
            // datos del operador que inició sesión
            usuario?.let { u ->
                Spacer(modifier = Modifier.height(16.dp))
                Text(u.nombreCompleto, style = MaterialTheme.typography.headlineSmall)
                Text(u.email, color = MaterialTheme.colorScheme.onSurfaceVariant)
                Text("RUT: ${u.rut} · Rol: ${u.rol.etiqueta}")
            }

            Spacer(modifier = Modifier.height(16.dp))
            Text("Foto de perfil", style = MaterialTheme.typography.titleLarge)
            Text(
                "Elige una imagen de tu galería o toma una nueva",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Spacer(modifier = Modifier.height(24.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                FilledTonalButton(
                    onClick = {
                        perfilViewModel.limpiarError()
                        galeriaLauncher.launch("image/*")
                    },
                    modifier = Modifier.weight(1f)
                ) {
                    Icon(Icons.Default.PhotoLibrary, contentDescription = null)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Galería")
                }
                Button(
                    onClick = {
                        perfilViewModel.limpiarError()
                        abrirCamara()
                    },
                    modifier = Modifier.weight(1f)
                ) {
                    Icon(Icons.Default.PhotoCamera, contentDescription = null)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Cámara")
                }
            }

            Spacer(modifier = Modifier.height(12.dp))
            // mensaje claro si se denego el permiso (o si fallo la imagen)
            MensajeAnimado(
                mensaje = if (permisoDenegado) "Sin permiso de cámara. Actívalo en Ajustes para tomar fotos."
                else errorImagen,
                esError = true
            )
            if (permisoDenegado) {
                TextButton(onClick = {
                    context.startActivity(
                        Intent(
                            Settings.ACTION_APPLICATION_DETAILS_SETTINGS,
                            Uri.fromParts("package", context.packageName, null)
                        )
                    )
                }) {
                    Text("Abrir ajustes")
                }
            }
        }
    }
}
