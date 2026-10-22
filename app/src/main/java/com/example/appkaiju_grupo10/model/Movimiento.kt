package com.example.appkaiju_grupo10.model

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

enum class TipoMovimiento(val etiqueta: String) {
    ENTRADA("Entrada"),
    SALIDA("Salida"),
    AJUSTE("Ajuste")
}
@Entity(
    tableName = "movimientos",
    indices = [Index(value = ["sku"])]
)
data class Movimiento(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val sku: String,
    val tipo: TipoMovimiento,
    val cantidad: Int,
    val notas: String = "",
    val fecha: Long = System.currentTimeMillis()  // fecha y hora automatica
)