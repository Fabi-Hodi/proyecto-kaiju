package com.example.appkaiju_grupo10.navigation


sealed class Screen(val route: String) {
    data object Login : Screen("login_page")
    data object Registro : Screen("registro_page")
    data object Home : Screen("home_page")
    data object Inventario : Screen("inventario_page")

    // Esta ruta recibe un dato: el SKU del producto
    data object Movimiento : Screen("movimiento_page/{sku}") {
        fun crearRuta(sku: String) = "movimiento_page/$sku"
    }

    data object Profile : Screen("profile_page")
    data object Settings : Screen("settings_page")
}