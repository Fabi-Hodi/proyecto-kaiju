package com.example.appkaiju_grupo10.model

// Lo que un usuario puede hacer en la app. Viene del backend (lo edita el administrador).
data class Permisos(
    val entradas: Boolean = false,    // registrar entradas de stock
    val salidas: Boolean = false,     // registrar salidas de stock
    val precios: Boolean = false,     // editar precios
    val reportes: Boolean = false     // ver reportes
) {
    companion object {
        // Valores por defecto de cada rol (iguales a UsuarioKaiju.aplicarPermisosDeRol del backend)
        fun porRol(rol: Rol): Permisos = when (rol) {
            Rol.ADMIN -> Permisos(entradas = true, salidas = true, precios = true, reportes = true)
            Rol.BODEGA -> Permisos(entradas = true, salidas = true)
            Rol.VENTAS -> Permisos(salidas = true, reportes = true)
        }
    }
}