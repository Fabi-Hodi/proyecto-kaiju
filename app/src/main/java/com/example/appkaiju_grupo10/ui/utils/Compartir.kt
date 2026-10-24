package com.example.appkaiju_grupo10.ui.utils

import android.content.Context
import android.content.Intent
import com.example.appkaiju_grupo10.model.Producto
import java.text.NumberFormat
import java.util.Locale

// abre el menu nativo de Android para compartir la ficha de un producto como texto.
fun compartirProducto(context: Context, producto: Producto) {
    val precio = NumberFormat.getInstance(Locale("es", "CL")).format(producto.precio)
    val texto = buildString {
        appendLine("📦 ${producto.nombre}")
        appendLine("SKU: ${producto.sku}")
        appendLine("Categoría: ${producto.categoria}")
        appendLine("Precio: $$precio")
        appendLine("Stock: ${producto.stock} un." + if (producto.stockBajo) " (stock bajo)" else "")
        append("Ubicación: ${producto.ubicacion}")
    }
    val intent = Intent(Intent.ACTION_SEND).apply {
        type = "text/plain"
        putExtra(Intent.EXTRA_SUBJECT, "Producto ${producto.sku}")
        putExtra(Intent.EXTRA_TEXT, texto)
    }
    context.startActivity(Intent.createChooser(intent, "Compartir producto"))
}
