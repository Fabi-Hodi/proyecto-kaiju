package com.example.appkaiju_grupo10.viewmodels

import androidx.lifecycle.ViewModel
import com.example.appkaiju_grupo10.navigation.NavigationEvent
import com.example.appkaiju_grupo10.navigation.Screen
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import com.example.appkaiju_grupo10.model.Usuario
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
class MainViewModel : ViewModel() {

    private val _navigationEvents = MutableSharedFlow<NavigationEvent>()
    // usuario que inició sesión (lo comparten todas las pantallas)
    private val _usuarioActual = MutableStateFlow<Usuario?>(null)
    val usuarioActual: StateFlow<Usuario?> = _usuarioActual.asStateFlow()

    fun iniciarSesion(usuario: Usuario) {
        _usuarioActual.value = usuario
    }
    val navigationEvents: SharedFlow<NavigationEvent> = _navigationEvents.asSharedFlow()

    // función para ir a una pantalla especifica
    fun navigateTo(screen: Screen) {
        CoroutineScope(Dispatchers.Main).launch {
            _navigationEvents.emit(NavigationEvent.NavigateTo(route = screen))
        }
    }

    // función para volver a la pantalla anterior
    fun navigateBack() {
        CoroutineScope(Dispatchers.Main).launch {
            _navigationEvents.emit(NavigationEvent.PopBackStack)
        }
    }

    // función para subir en la jerarquia
    fun navigateUp() {
        CoroutineScope(Dispatchers.Main).launch {
            _navigationEvents.emit(NavigationEvent.NavigateUp)
        }
    }
    // funcion para ir al movimiento de stock de un producto especifico
    fun navigateToMovimiento(sku: String) {
        CoroutineScope(Dispatchers.Main).launch {
            _navigationEvents.emit(
                NavigationEvent.NavigateToRuta(Screen.Movimiento.crearRuta(sku))
            )
        }
    }
    // función para ir a una pantalla borrando el historial hasta "popUpTo"
    // (ej: después del login, que "Atrás" no vuelva al Login)
    fun navigateAndClear(screen: Screen, popUpTo: Screen) {
        CoroutineScope(Dispatchers.Main).launch {
            _navigationEvents.emit(
                NavigationEvent.NavigateTo(
                    route = screen,
                    popUpToRoute = popUpTo,
                    inclusive = true,
                    singleTop = true
                )
            )
        }
    }
    // función para ir al inventario, con filtros opcionales
    fun navigateToInventario(categoria: String? = null, busqueda: String? = null) {
        CoroutineScope(Dispatchers.Main).launch {
            _navigationEvents.emit(
                NavigationEvent.NavigateToRuta(Screen.Inventario.crearRuta(categoria, busqueda))
            )
        }
    }
    // cerrar sesión: olvidar al usuario e ir al Login sin dejar pantallas atrás
    fun cerrarSesion() {
        _usuarioActual.value = null
        CoroutineScope(Dispatchers.Main).launch {
            _navigationEvents.emit(NavigationEvent.NavigateAndClearAll(Screen.Login))
        }
    }

    // volver al Inicio que YA existe (sin crear uno nuevo encima)
    fun volverAlInicio() {
        CoroutineScope(Dispatchers.Main).launch {
            _navigationEvents.emit(
                NavigationEvent.NavigateTo(
                    route = Screen.Home,
                    popUpToRoute = Screen.Home,   // saca todo lo que está encima del Inicio
                    inclusive = false,            // pero deja el Inicio
                    singleTop = true              // y no crea otro
                )
            )
        }
    }
}