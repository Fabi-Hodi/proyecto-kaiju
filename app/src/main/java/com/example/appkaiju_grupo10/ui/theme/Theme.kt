package com.example.appkaiju_grupo10.ui.theme

import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext

// Colores para modo claro
private val LightColorScheme = lightColorScheme(
    primary = KaijuAzul,
    onPrimary = Color.White,
    secondaryContainer = KaijuGrisBarra,
    onSecondaryContainer = Color.Black,
    background = KaijuGrisFondo,
    onBackground = Color.Black,
    surface = Color.White,
    onSurface = Color.Black,
    surfaceVariant = KaijuGrisTarjeta,
    onSurfaceVariant = KaijuGrisTexto,
    error = KaijuRojoStock,
    onError = Color.White
)

// Colores para modo oscuro
private val DarkColorScheme = darkColorScheme(
    primary = KaijuAzulClaro,
    onPrimary = KaijuAzulOscuro,
    secondaryContainer = KaijuBarraOscura,
    onSecondaryContainer = Color.White,
    background = KaijuFondoOscuro,
    onBackground = Color.White,
    surface = KaijuSuperficieOscura,
    onSurface = Color.White,
    surfaceVariant = KaijuTarjetaOscura,
    onSurfaceVariant = Color(0xFFBDBDBD),
    error = KaijuRojoStockClaro,
    onError = Color.Black
)

@Composable
fun AppKaiju_Grupo10Theme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    dynamicColor: Boolean = false,
    content: @Composable () -> Unit
) {
    val colorScheme = when {
        dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
            val context = LocalContext.current
            if (darkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
        }
        darkTheme -> DarkColorScheme
        else -> LightColorScheme
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}