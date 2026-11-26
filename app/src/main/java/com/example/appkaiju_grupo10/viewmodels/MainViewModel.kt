package com.example.appkaiju_grupo10.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.appkaiju_grupo10.model.Usuario
import com.example.appkaiju_grupo10.navigation.NavigationEvent
import com.example.appkaiju_grupo10.navigation.Screen
import com.example.appkaiju_grupo10.repository.AuthRepository
import com.example.appkaiju_grupo10.repository.EstadoSesion
import com.example.appkaiju_grupo10.repository.SesionKaiju
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class MainViewModel(
    private val authRepository: AuthRepository = AuthRepository()
) : ViewModel() {

    private val _navigationEvents = MutableSharedFlow<NavigationEvent>()
    val navigationEvents: SharedFlow<NavigationEvent> = _navigationEvents.asSharedFlow()

    // usuario que inició sesión (lo comparten todas las pantallas)
    private val _usuarioActual = MutableStateFlow<Usuario?>(null)
    val usuarioActual: StateFlow<Usuario?> = _usuarioActual.asStateFlow()

    // NUEVO: motivo por el que se cerró la sesión sola (se muestra como aviso)
    private val _avisoSesion = MutableStateFlow<String?>(null)
    val avisoSesion: StateFlow<String?> = _avisoSesion.asStateFlow()

    private var verificando = false

    fun iniciarSesion(usuario: Usuario) {
        _usuarioActual.value = usuario
    }

    // NUEVO: pregunta al backend si la sesión sigue válida.
    // Se llama al volver a la app y al cambiar de pantalla (ver MainActivity).
    fun verificarSesion() {
        if (_usuarioActual.value == null || verificando) return   // nadie conectado o ya preguntando
        verificando = true
        viewModelScope.launch {
            try {
                when (val estado = authRepository.verificarSesion(SesionKaiju.token)) {
                    // datos al día: si el admin cambió el rol, el menú se actualiza solo
                    is EstadoSesion.Vigente -> _usuarioActual.value = estado.usuario.aUsuario()
                    is EstadoSesion.Terminada -> {
                        _avisoSesion.value = estado.mensaje
                        cerrarSesion()
                    }
                    EstadoSesion.SinConexion -> Unit           // sin internet: se sigue trabajando
                }
            } finally {
                verificando = false
            }
        }
    }

    fun limpiarAviso() {
        _avisoSesion.value = null
    }

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

    // ir a editar un usuario (administración)
    fun navigateToEditarUsuario(id: Long) {
        CoroutineScope(Dispatchers.Main).launch {
            _navigationEvents.emit(
                NavigationEvent.NavigateToRuta(Screen.EditarUsuario.crearRuta(id))
            )
        }
    }

    // ir a editar un producto (administración)
    fun navigateToEditarProducto(sku: String) {
        CoroutineScope(Dispatchers.Main).launch {
            _navigationEvents.emit(
                NavigationEvent.NavigateToRuta(Screen.EditarProducto.crearRuta(sku))
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

    // cerrar sesión: olvidar al usuario y el token, e ir al Login sin dejar pantallas atrás
    fun cerrarSesion() {
        _usuarioActual.value = null
        SesionKaiju.cerrar()                      // se borra el token JWT
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