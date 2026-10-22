package com.example.appkaiju_grupo10.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "productos")
data class Producto(
    @PrimaryKey val sku: String,
    val nombre: String,
    val descripcion: String,
    val categoria: String,
    val precio: Int,
    val stock: Int,
    val stockMinimo: Int,
    val ubicacion: String
) {
    val stockBajo: Boolean
        get() = stock <= stockMinimo
}