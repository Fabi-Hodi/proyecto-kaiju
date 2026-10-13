package com.example.appkaiju_grupo10.navigation

// Sealed class para definir rutas seguras
sealed class Screen(val route: String) {
    data object Home : Screen("home_page")
    data object Profile : Screen("profile_page")
    data object Settings : Screen("settings_page")
}