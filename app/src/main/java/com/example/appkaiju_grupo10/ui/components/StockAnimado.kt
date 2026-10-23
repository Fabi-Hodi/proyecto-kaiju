package com.example.appkaiju_grupo10.ui.components

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.layout.Row
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight


@Composable
fun StockAnimado(
    stock: Int,
    stockBajo: Boolean,
    modifier: Modifier = Modifier,
    prefijo: String = "Stock: ",
    style: TextStyle = MaterialTheme.typography.bodyMedium
) {
    val color by animateColorAsState(
        targetValue = if (stockBajo) MaterialTheme.colorScheme.error
        else MaterialTheme.colorScheme.onSurface,
        animationSpec = tween(durationMillis = 500),
        label = "colorStock"
    )

    Row(modifier = modifier, verticalAlignment = Alignment.CenterVertically) {
        Text(text = prefijo, style = style, fontWeight = FontWeight.Bold, color = color)

        AnimatedContent(
            targetState = stock,
            transitionSpec = {
                // +1 si el stock sube (entra desde abajo), -1 si baja (entra desde arriba)
                val direccion = if (targetState > initialState) 1 else -1
                (slideInVertically(tween(300)) { alto -> direccion * alto } + fadeIn(tween(300))) togetherWith
                        (slideOutVertically(tween(300)) { alto -> -direccion * alto } + fadeOut(tween(300)))
            },
            label = "numeroStock"
        ) { valor ->
            Text(text = "$valor un.", style = style, fontWeight = FontWeight.Bold, color = color)
        }
    }
}
