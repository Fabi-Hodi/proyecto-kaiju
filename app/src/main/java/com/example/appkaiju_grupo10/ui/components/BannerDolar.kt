package com.example.appkaiju_grupo10.ui.components

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.appkaiju_grupo10.viewmodels.EstadoDolar
import java.text.NumberFormat
import java.util.Locale

// tarjeta con el dólar del día (API pública) y el valor total del inventario en dólares.
// muestra los 3 estados de la consulta: cargando, error (con reintento) y listo.
@Composable
fun BannerDolar(
    estado: EstadoDolar,
    valorInventarioClp: Long,
    onReintentar: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
    ) {
        Column(
            modifier = Modifier.fillMaxWidth().padding(12.dp),
            verticalArrangement = Arrangement.spacedBy(2.dp)
        ) {
            when (estado) {
                is EstadoDolar.Cargando -> {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        CircularProgressIndicator(modifier = Modifier.size(16.dp), strokeWidth = 2.dp)
                        Text("Consultando el dólar…", style = MaterialTheme.typography.bodySmall)
                    }
                }

                is EstadoDolar.Error -> {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = "No se pudo obtener el valor del dólar",
                            style = MaterialTheme.typography.bodySmall,
                            modifier = Modifier.weight(1f)
                        )
                        TextButton(onClick = onReintentar) { Text("Reintentar") }
                    }
                }

                is EstadoDolar.Listo -> {
                    val dolar = estado.dolar
                    Text(
                        text = "Dólar observado: ${formatearDecimal(dolar.valor)} CLP (${dolar.fecha})",
                        style = MaterialTheme.typography.labelLarge
                    )
                    Text(
                        text = "Valor del inventario: ${formatearClp(valorInventarioClp)}" +
                                " ≈ US$ ${formatearDecimal(valorInventarioClp / dolar.valor)}",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
    }
}

// 916.39 -> "916,39" (formato chile)
private fun formatearDecimal(valor: Double): String {
    val formato = NumberFormat.getNumberInstance(Locale.forLanguageTag("es-CL"))
    formato.minimumFractionDigits = 2
    formato.maximumFractionDigits = 2
    return formato.format(valor)
}

private fun formatearClp(valor: Long): String {
    val formato = NumberFormat.getCurrencyInstance(Locale.forLanguageTag("es-CL"))
    formato.maximumFractionDigits = 0
    return formato.format(valor)
}
