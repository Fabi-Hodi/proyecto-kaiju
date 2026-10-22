package com.example.appkaiju_grupo10.navigation

import android.net.Uri


sealed class Screen(val route: String) {
    data object Login : Screen("login_page")
    data object Registro : Screen("registro_page")
    data object Home : Screen("home_page")
    // Ruta con 2 argumentos OPCIONALES (?): se puede entrar con o sin filtro
    data object Inventario : Screen("inventario_page?categoria={categoria}&busqueda={busqueda}") {
        fun crearRuta(categoria: String? = null, busqueda: String? = null): String {
            val parametros = buildList {
                if (categoria != null) add("categoria=${Uri.encode(categoria)}")
                if (!busqueda.isNullOrBlank()) add("busqueda=${Uri.encode(busqueda)}")
            }
            return if (parametros.isEmpty()) "inventario_page"
            else "inventario_page?" + parametros.joinToString("&")
        }
    }

    // Esta ruta recibe un dato: el SKU del producto
    data object Movimiento : Screen("movimiento_page/{sku}") {
        fun crearRuta(sku: String) = "movimiento_page/$sku"
    }

    data object Profile : Screen("profile_page")
    data object Settings : Screen("settings_page")
}