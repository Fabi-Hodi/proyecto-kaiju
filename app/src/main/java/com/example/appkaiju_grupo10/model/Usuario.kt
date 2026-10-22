package com.example.appkaiju_grupo10.model

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

// los roles permitidos
enum class Rol(val etiqueta: String) {
    BODEGA("Bodega"),
    VENTAS("Ventas"),
    ADMIN("Admin")
}
@Entity(
    tableName = "usuarios",
    indices = [Index(value = ["email"], unique = true)]
)
data class Usuario(
    @PrimaryKey val rut: String,
    val nombreCompleto: String,
    val email: String,
    val rol: Rol,
    val passwordHash: String
)