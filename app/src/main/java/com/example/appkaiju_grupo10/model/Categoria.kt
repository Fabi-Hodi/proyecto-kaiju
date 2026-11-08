package com.example.appkaiju_grupo10.model

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

// Tabla "categorias": cada categoría tiene su prefijo y su propio contador
@Entity(
    tableName = "categorias",
    indices = [Index(value = ["prefijo"], unique = true)]   // dos categorías no pueden tener el mismo prefijo
)
data class Categoria(
    @PrimaryKey val nombre: String,      // "Insumos"
    val prefijo: String,                 // "IN"
    val ultimoNumero: Int = 0            // último número usado: el siguiente es ultimoNumero + 1
)

// Arma el código: ("IN", 3) -> "IN-003"
fun formatearSku(prefijo: String, numero: Int): String =
    "$prefijo-${numero.toString().padStart(3, '0')}"

// El código que recibiría el próximo producto de esta categoría
fun Categoria.siguienteSku(): String = formatearSku(prefijo, ultimoNumero + 1)