package com.example.appkaiju_grupo10.navigation

sealed class NavigationEvent {
    data class NavigateTo(
        val route: Screen,
        val popUpToRoute: Screen? = null,
        val inclusive: Boolean = false,
        val singleTop: Boolean = false
    ) : NavigationEvent()

    // nuevo: sirve para navegar a rutas que llevan datos (ej: "movimiento_page/KAJ-2034")
    data class NavigateToRuta(val ruta: String) : NavigationEvent()

    data object PopBackStack : NavigationEvent()
    data object NavigateUp : NavigationEvent()
}