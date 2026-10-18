package com.example.appkaiju_grupo10.model
// los roles permitidos
enum class Rol(val etiqueta: String) {
    BODEGA("Bodega"),
    VENTAS("Ventas"),
    ADMIN("Admin")
}

data class Usuario(
    val nombreCompleto: String,
    val rut: String,
    val email: String,
    val rol: Rol
)