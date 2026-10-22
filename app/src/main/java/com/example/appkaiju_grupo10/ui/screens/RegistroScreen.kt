package com.example.appkaiju_grupo10.ui.screens

import android.widget.Toast
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.example.appkaiju_grupo10.model.Rol
import com.example.appkaiju_grupo10.ui.utils.validarConfirmacion
import com.example.appkaiju_grupo10.ui.utils.validarEmail
import com.example.appkaiju_grupo10.ui.utils.validarNombre
import com.example.appkaiju_grupo10.ui.utils.validarPassword
import com.example.appkaiju_grupo10.ui.utils.validarRut
import com.example.appkaiju_grupo10.viewmodels.MainViewModel
import com.example.appkaiju_grupo10.viewmodels.RegistroViewModel
import com.example.appkaiju_grupo10.viewmodels.ResultadoRegistro

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RegistroScreen(
    navController: NavController,
    viewModel: MainViewModel,
    registroViewModel: RegistroViewModel = viewModel()
) {
    // ----- ESTADO del formulario -----
    var nombre by rememberSaveable { mutableStateOf("") }
    var rut by rememberSaveable { mutableStateOf("") }
    var email by rememberSaveable { mutableStateOf("") }
    var password by rememberSaveable { mutableStateOf("") }
    var confirmacion by rememberSaveable { mutableStateOf("") }
    var verPassword by rememberSaveable { mutableStateOf(false) }
    var rol by rememberSaveable { mutableStateOf<Rol?>(null) }
    var menuRolAbierto by remember { mutableStateOf(false) }
    var intentoEnviar by rememberSaveable { mutableStateOf(false) }

    // ----- ESTADO que viene del ViewModel -----
    val isLoading by registroViewModel.isLoading.collectAsState()
    val resultado by registroViewModel.resultado.collectAsState()
    val context = LocalContext.current

    // ----- Errores de formato (solo después del primer intento) -----
    val errorNombre = if (intentoEnviar) validarNombre(nombre) else null
    val errorRut = if (intentoEnviar) validarRut(rut) else null
    val errorEmail = if (intentoEnviar) validarEmail(email) else null
    val errorPassword = if (intentoEnviar) validarPassword(password) else null
    val errorConfirmacion = if (intentoEnviar) validarConfirmacion(password, confirmacion) else null
    val errorRol = if (intentoEnviar && rol == null) "Selecciona un rol" else null

    // la misma transformación para los dos campos de contraseña
    val transformacionPassword =
        if (verPassword) VisualTransformation.None else PasswordVisualTransformation()

    // Cuando el guardado sale bien: aviso + volver al Login
    LaunchedEffect(resultado) {
        if (resultado is ResultadoRegistro.Exito) {
            Toast.makeText(context, "Operador registrado correctamente", Toast.LENGTH_SHORT).show()
            registroViewModel.limpiarResultado()
            viewModel.navigateBack()
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Card(
            modifier = Modifier.widthIn(max = 420.dp).fillMaxWidth(),
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surfaceVariant
            )
        ) {
            Column(
                modifier = Modifier.padding(24.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "Registrar operador",
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold
                )

                // ----- Nombre completo -----
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

                // ----- RUT -----
                OutlinedTextField(
                    value = rut,
                    onValueChange = { rut = it.trim() },
                    label = { Text("RUT / ID") },
                    placeholder = { Text("12.345.678-5") },
                    singleLine = true,
                    isError = errorRut != null,
                    supportingText = { if (errorRut != null) Text(errorRut) },
                    keyboardOptions = KeyboardOptions(imeAction = ImeAction.Next),
                    modifier = Modifier.fillMaxWidth()
                )

                // ----- Email -----
                OutlinedTextField(
                    value = email,
                    onValueChange = { email = it.trim() },
                    label = { Text("Email") },
                    singleLine = true,
                    isError = errorEmail != null,
                    supportingText = { if (errorEmail != null) Text(errorEmail) },
                    keyboardOptions = KeyboardOptions(
                        keyboardType = KeyboardType.Email,
                        imeAction = ImeAction.Next
                    ),
                    modifier = Modifier.fillMaxWidth()
                )

                // ----- Contraseña (NUEVO) -----
                OutlinedTextField(
                    value = password,
                    onValueChange = { password = it },
                    label = { Text("Contraseña") },
                    singleLine = true,
                    isError = errorPassword != null,
                    supportingText = { if (errorPassword != null) Text(errorPassword) },
                    visualTransformation = transformacionPassword,
                    trailingIcon = {
                        IconButton(onClick = { verPassword = !verPassword }) {
                            Icon(
                                imageVector = if (verPassword) Icons.Default.VisibilityOff
                                else Icons.Default.Visibility,
                                contentDescription = if (verPassword) "Ocultar contraseña"
                                else "Mostrar contraseña"
                            )
                        }
                    },
                    keyboardOptions = KeyboardOptions(
                        keyboardType = KeyboardType.Password,
                        imeAction = ImeAction.Next
                    ),
                    modifier = Modifier.fillMaxWidth()
                )

                // ----- Confirmar contraseña (NUEVO) -----
                OutlinedTextField(
                    value = confirmacion,
                    onValueChange = { confirmacion = it },
                    label = { Text("Confirmar contraseña") },
                    singleLine = true,
                    isError = errorConfirmacion != null,
                    supportingText = { if (errorConfirmacion != null) Text(errorConfirmacion) },
                    visualTransformation = transformacionPassword,
                    keyboardOptions = KeyboardOptions(
                        keyboardType = KeyboardType.Password,
                        imeAction = ImeAction.Done
                    ),
                    modifier = Modifier.fillMaxWidth()
                )

                // ----- Rol (lista desplegable) -----
                ExposedDropdownMenuBox(
                    expanded = menuRolAbierto,
                    onExpandedChange = { menuRolAbierto = it }
                ) {
                    OutlinedTextField(
                        value = rol?.etiqueta ?: "",
                        onValueChange = {},
                        readOnly = true,
                        label = { Text("Rol") },
                        placeholder = { Text("Bodega / Ventas / Admin") },
                        isError = errorRol != null,
                        supportingText = { if (errorRol != null) Text(errorRol) },
                        trailingIcon = {
                            ExposedDropdownMenuDefaults.TrailingIcon(expanded = menuRolAbierto)
                        },
                        modifier = Modifier
                            .menuAnchor(ExposedDropdownMenuAnchorType.PrimaryNotEditable)
                            .fillMaxWidth()
                    )
                    ExposedDropdownMenu(
                        expanded = menuRolAbierto,
                        onDismissRequest = { menuRolAbierto = false }
                    ) {
                        Rol.entries.forEach { opcion ->
                            DropdownMenuItem(
                                text = { Text(opcion.etiqueta) },
                                onClick = {
                                    rol = opcion
                                    menuRolAbierto = false
                                }
                            )
                        }
                    }
                }

                // ----- Error que viene de la base de datos -----
                val errorBD = resultado as? ResultadoRegistro.Error
                if (errorBD != null) {
                    Text(
                        text = errorBD.mensaje,
                        color = MaterialTheme.colorScheme.error,
                        style = MaterialTheme.typography.bodyMedium
                    )
                }

                // ----- Botón Guardar -----
                Button(
                    onClick = {
                        intentoEnviar = true
                        val rolElegido = rol
                        val formularioValido = validarNombre(nombre) == null &&
                                validarRut(rut) == null &&
                                validarEmail(email) == null &&
                                validarPassword(password) == null &&
                                validarConfirmacion(password, confirmacion) == null &&
                                rolElegido != null
                        if (formularioValido && rolElegido != null) {
                            registroViewModel.registrar(nombre, rut, email, password, rolElegido)
                        }
                    },
                    enabled = !isLoading,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    if (isLoading) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(20.dp),
                            strokeWidth = 2.dp
                        )
                    } else {
                        Text("Guardar usuario")
                    }
                }

                TextButton(onClick = { viewModel.navigateBack() }) {
                    Text("Volver al login")
                }
            }
        }
    }
}