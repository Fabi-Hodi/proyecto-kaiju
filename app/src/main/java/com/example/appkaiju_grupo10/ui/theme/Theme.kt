package com.example.appkaiju_grupo10.ui.theme

import android.app.Activity
import android.os.Build
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat

// Colores para modo claro (quedan disponibles por si algún día se vuelve a usar)
private val LightColorScheme = lightColorScheme(
    primary = KaijuCianOscuro,
    onPrimary = Color.White,
    primaryContainer = KaijuCianSuave,
    onPrimaryContainer = KaijuNavyBarra,
    secondaryContainer = KaijuCianSuave,      // barra superior
    onSecondaryContainer = KaijuNavyBarra,
    background = KaijuFondoClaro,
    onBackground = KaijuNavyBarra,
    surface = Color.White,
    onSurface = KaijuNavyBarra,
    surfaceVariant = KaijuTarjetaClara,       // tarjetas
    onSurfaceVariant = KaijuTextoGris,
    error = KaijuRojoStock,
    onError = Color.White,
    errorContainer = KaijuRojoStockFondo,     // etiqueta "Reponer"
    onErrorContainer = KaijuRojoStock
)

// Colores para modo oscuro (igual al modelo)
private val DarkColorScheme = darkColorScheme(
    primary = KaijuCian,
    onPrimary = KaijuNavyBarra,               // texto oscuro sobre el botón cian
    primaryContainer = KaijuCianProfundo,
    onPrimaryContainer = KaijuCian,
    secondaryContainer = KaijuNavyBarra,      // barra superior
    onSecondaryContainer = KaijuTextoClaro,
    background = KaijuNavyFondo,
    onBackground = KaijuTextoClaro,
    surface = KaijuNavyFondo,
    onSurface = KaijuTextoClaro,
    surfaceVariant = KaijuTarjetaOscura,      // tarjetas
    onSurfaceVariant = KaijuTextoSuave,
    error = KaijuRojoCoral,
    onError = KaijuNavyBarra,
    errorContainer = KaijuRojoFondo,          // etiqueta "Reponer"
    onErrorContainer = KaijuRojoCoral
)

@Composable
fun AppKaiju_Grupo10Theme(
    darkTheme: Boolean = true,                // la app usa SIEMPRE el tema oscuro del modelo
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

    // Íconos de la barra de estado y de navegación: claros sobre fondo oscuro
    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as Activity).window
            WindowCompat.getInsetsController(window, view).apply {
                isAppearanceLightStatusBars = !darkTheme
                isAppearanceLightNavigationBars = !darkTheme
            }
        }
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}