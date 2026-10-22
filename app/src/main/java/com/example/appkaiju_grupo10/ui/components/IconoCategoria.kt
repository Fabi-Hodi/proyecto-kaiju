package com.example.appkaiju_grupo10.ui.components

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Build
import androidx.compose.material.icons.filled.Category
import androidx.compose.material.icons.filled.Inventory2
import androidx.compose.material.icons.filled.LocalShipping
import androidx.compose.ui.graphics.vector.ImageVector

fun iconoCategoria(nombre: String): ImageVector = when (nombre) {
    "Insumos" -> Icons.Default.Inventory2
    "Herramientas" -> Icons.Default.Build
    "Embalaje" -> Icons.Default.LocalShipping
    else -> Icons.Default.Category
}