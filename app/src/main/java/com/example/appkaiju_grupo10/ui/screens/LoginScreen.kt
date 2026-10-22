package com.example.appkaiju_grupo10.ui.screens

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.example.appkaiju_grupo10.R
import com.example.appkaiju_grupo10.navigation.Screen
import com.example.appkaiju_grupo10.ui.utils.validarEmail
import com.example.appkaiju_grupo10.ui.utils.validarPassword
import com.example.appkaiju_grupo10.viewmodels.LoginViewModel
import com.example.appkaiju_grupo10.viewmodels.MainViewModel
import com.example.appkaiju_grupo10.viewmodels.ResultadoLogin

@Composable
fun LoginScreen(
    navController: NavController,
    viewModel: MainViewModel,
    loginViewModel: LoginViewModel = viewModel()
) {
    var email by rememberSaveable { mutableStateOf("") }
    var password by rememberSaveable { mutableStateOf("") }
    var verPassword by rememberSaveable { mutableStateOf(false) }
    var intentoEnviar by rememberSaveable { mutableStateOf(false) }

    val isLoading by loginViewModel.isLoading.collectAsState()
    val resultado by loginViewModel.resultado.collectAsState()

    val errorEmail = if (intentoEnviar) validarEmail(email) else null
    val errorPassword = if (intentoEnviar) validarPassword(password) else null

    // Si las credenciales son correctas: ir al Home sin poder volver al Login
    LaunchedEffect(resultado) {
        if (resultado is ResultadoLogin.Exito) {
            loginViewModel.limpiarResultado()
            viewModel.navigateAndClear(Screen.Home, popUpTo = Screen.Login)
        }
    }

    Column(
        modifier = Modifier.fillMaxSize().padding(24.dp),
        verticalArrangement = Arrangement.Center,
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
                Image(
                    painter = painterResource(id = R.drawable.icono_kaiju),
                    contentDescription = "Logo Kaiju",
                    modifier = Modifier.size(80.dp)
                )

                Text(
                    text = "Bienvenido",
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold
                )

                // para el correo
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

                // para contraseña
                OutlinedTextField(
                    value = password,
                    onValueChange = { password = it },
                    label = { Text("Contraseña") },
                    singleLine = true,
                    isError = errorPassword != null,
                    supportingText = { if (errorPassword != null) Text(errorPassword) },
                    visualTransformation = if (verPassword) VisualTransformation.None
                    else PasswordVisualTransformation(),
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
                        imeAction = ImeAction.Done
                    ),
                    modifier = Modifier.fillMaxWidth()
                )

                // error de credenciales (viene de la BD)
                val errorLogin = resultado as? ResultadoLogin.Error
                if (errorLogin != null) {
                    Text(
                        text = errorLogin.mensaje,
                        color = MaterialTheme.colorScheme.error,
                        style = MaterialTheme.typography.bodyMedium
                    )
                }

                // iniciar sesion
                Button(
                    onClick = {
                        intentoEnviar = true
                        val formatoValido =
                            validarEmail(email) == null && validarPassword(password) == null
                        if (formatoValido) {
                            loginViewModel.iniciarSesion(email, password)   // ahora consulta Room
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
                        Text("Iniciar sesión")
                    }
                }

                TextButton(onClick = { viewModel.navigateTo(Screen.Registro) }) {
                    Text("Registrar operador")
                }
            }
        }
    }
}