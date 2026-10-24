package com.example.appkaiju_grupo10.ui.components

import android.content.Context
import android.graphics.ImageDecoder
import android.net.Uri
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.produceState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlin.math.max

@Composable
fun ImagenInteligente(
    uri: Uri?,
    modifier: Modifier = Modifier,
    tamano: Dp = 140.dp
) {
    val context = LocalContext.current

    val imagen by produceState<ImageBitmap?>(initialValue = null, uri) {
        value = if (uri == null) null else withContext(Dispatchers.IO) { cargarImagen(context, uri) }
    }

    Box(
        modifier = modifier
            .size(tamano)
            .clip(CircleShape)
            .background(MaterialTheme.colorScheme.surfaceVariant),
        contentAlignment = Alignment.Center
    ) {
        val actual = imagen
        if (actual != null) {
            Image(
                bitmap = actual,
                contentDescription = "Foto de perfil",
                contentScale = ContentScale.Crop,
                modifier = Modifier.fillMaxSize()
            )
        } else {
            Icon(
                imageVector = Icons.Default.Person,
                contentDescription = "Sin foto de perfil",
                modifier = Modifier.size(tamano / 2),
                tint = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}
private fun cargarImagen(context: Context, uri: Uri): ImageBitmap? = try {
    val fuente = ImageDecoder.createSource(context.contentResolver, uri)
    ImageDecoder.decodeBitmap(fuente) { decoder, info, _ ->
        val ladoMayor = max(info.size.width, info.size.height)
        decoder.setTargetSampleSize(max(1, ladoMayor / 512))
    }.asImageBitmap()
} catch (e: Exception) {
    null
}