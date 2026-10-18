package com.example.appkaiju_grupo10.model

enum class TipoMovimiento(val etiqueta: String) {
    ENTRADA("Entrada"),
    SALIDA("Salida"),
    AJUSTE("Ajuste")
}

data class Movimiento(
    val sku: String,
    val tipo: TipoMovimiento,
    val cantidad: Int,
    val notas: String = "",
    val fecha: Long = System.currentTimeMillis()  // fecha y hora automaticas
)