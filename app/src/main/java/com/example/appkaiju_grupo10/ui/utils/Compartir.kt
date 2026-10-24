package com.example.appkaiju_grupo10.ui.utils

import android.content.Context
import android.content.Intent
import com.example.appkaiju_grupo10.model.Producto
import java.text.NumberFormat
import java.util.Locale

private fun compartirTexto(context: Context, asunto: String, texto: String) {
    val intent = Intent(Intent.ACTION_SEND).apply {
        type = "text/plain"
        putExtra(Intent.EXTRA_SUBJECT, asunto)
        putExtra(Intent.EXTRA_TEXT, texto)
    }
    context.startActivity(Intent.createChooser(intent, "Compartir con..."))
}

fun compartirProducto(context: Context, producto: Producto) {
    val precio = NumberFormat.getCurrencyInstance(Locale.forLanguageTag("es-CL"))
        .apply { maximumFractionDigits = 0 }
        .format(producto.precio)

    val texto = buildString {
        appendLine("📦 Kaiju · Ficha de producto")
        appendLine()
        appendLine("${producto.nombre} (${producto.sku})")
        appendLine("Categoría: ${producto.categoria}")
        appendLine("Ubicación: ${producto.ubicacion}")
        appendLine("Precio: $precio")
        appendLine("Stock: ${producto.stock} un. (mínimo ${producto.stockMinimo})")
        if (producto.stockBajo) appendLine("⚠ Requiere reposición")
    }
    compartirTexto(context, "Producto ${producto.sku}", texto)
}

fun compartirReporteReposicion(context: Context, productos: List<Producto>) {
    val criticos = productos.filter { it.stockBajo }
    val texto = buildString {
        appendLine("📦 Kaiju · Productos para reponer (${criticos.size})")
        appendLine()
        criticos.forEach {
            appendLine("• ${it.nombre} (${it.sku}): ${it.stock} un. / mín. ${it.stockMinimo} · ${it.ubicacion}")
        }
    }
    compartirTexto(context, "Reposición de stock Kaiju", texto)
}