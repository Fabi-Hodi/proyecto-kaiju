package com.example.appkaiju_grupo10

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.animation.AnimatedContentTransitionScope
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.example.appkaiju_grupo10.navigation.NavigationEvent
import com.example.appkaiju_grupo10.navigation.Screen
import com.example.appkaiju_grupo10.ui.screens.HomeScreen
import com.example.appkaiju_grupo10.ui.screens.InventarioScreen
import com.example.appkaiju_grupo10.ui.screens.LoginScreen
import com.example.appkaiju_grupo10.ui.screens.MovimientoScreen
import com.example.appkaiju_grupo10.ui.screens.ProfileScreen
import com.example.appkaiju_grupo10.ui.screens.RegistroScreen
import com.example.appkaiju_grupo10.ui.screens.SettingsScreen
import com.example.appkaiju_grupo10.ui.theme.AppKaiju_Grupo10Theme
import com.example.appkaiju_grupo10.viewmodels.MainViewModel
import kotlinx.coroutines.flow.collectLatest

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            AppKaiju_Grupo10Theme {

                // inicializamos el Cerebro (ViewModel) y el Controlador de rutas
                val viewModel: MainViewModel = viewModel()
                val navController = rememberNavController()

                // Escuchamos las ordenes de navegación del ViewModel
                LaunchedEffect(key1 = Unit) {
                    viewModel.navigationEvents.collectLatest { event ->
                        when (event) {
                            is NavigationEvent.NavigateTo -> {
                                navController.navigate(event.route.route) {
                                    event.popUpToRoute?.let {
                                        popUpTo(it.route) {
                                            inclusive = event.inclusive
                                        }
                                    }
                                    launchSingleTop = event.singleTop
                                }
                            }
                            is NavigationEvent.NavigateToRuta -> navController.navigate(event.ruta)
                            is NavigationEvent.NavigateAndClearAll -> {
                                navController.navigate(event.route.route) {
                                    // graph.id = la raíz de la navegación: borra todas las pantallas
                                    popUpTo(navController.graph.id) { inclusive = true }
                                }
                            }
                            is NavigationEvent.PopBackStack -> navController.popBackStack()
                            is NavigationEvent.NavigateUp -> navController.navigateUp()
                        }
                    }
                }

                // contenedor principal con las rutas de las pantallas
                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                    NavHost(
                        navController = navController,
                        startDestination = Screen.Login.route,   // la app ahora parte en Login
                        modifier = Modifier.padding(innerPadding),
                        // transiciones entre pantallas (slide + fade)
                        // al avanzar la nueva entra por la derecha, la actual sale por la izquierda
                        enterTransition = {
                            slideIntoContainer(
                                AnimatedContentTransitionScope.SlideDirection.Left,
                                animationSpec = tween(350)
                            ) + fadeIn(tween(350))
                        },
                        exitTransition = {
                            slideOutOfContainer(
                                AnimatedContentTransitionScope.SlideDirection.Left,
                                animationSpec = tween(350)
                            ) + fadeOut(tween(350))
                        },
                        // al volver atrás el movimiento es al reves
                        popEnterTransition = {
                            slideIntoContainer(
                                AnimatedContentTransitionScope.SlideDirection.Right,
                                animationSpec = tween(350)
                            ) + fadeIn(tween(350))
                        },
                        popExitTransition = {
                            slideOutOfContainer(
                                AnimatedContentTransitionScope.SlideDirection.Right,
                                animationSpec = tween(350)
                            ) + fadeOut(tween(350))
                        }
                    ) {
                        composable(route = Screen.Login.route) {
                            LoginScreen(navController = navController, viewModel = viewModel)
                        }
                        composable(route = Screen.Registro.route) {
                            RegistroScreen(navController = navController, viewModel = viewModel)
                        }
                        composable(route = Screen.Home.route) {
                            HomeScreen(navController = navController, viewModel = viewModel)
                        }
                        // ruta con argumentos OPCIONALES: nullable + valor por defecto null
                        composable(
                            route = Screen.Inventario.route,
                            arguments = listOf(
                                navArgument("categoria") {
                                    type = NavType.StringType
                                    nullable = true
                                    defaultValue = null
                                },
                                navArgument("busqueda") {
                                    type = NavType.StringType
                                    nullable = true
                                    defaultValue = null
                                }
                            )
                        ) { backStackEntry ->
                            InventarioScreen(
                                navController = navController,
                                viewModel = viewModel,
                                categoria = backStackEntry.arguments?.getString("categoria"),
                                busqueda = backStackEntry.arguments?.getString("busqueda")
                            )
                        }
                        // ruta con argumento: le decimos que {sku} es un String
                        composable(
                            route = Screen.Movimiento.route,
                            arguments = listOf(navArgument("sku") { type = NavType.StringType })
                        ) { backStackEntry ->
                            val sku = backStackEntry.arguments?.getString("sku") ?: ""
                            MovimientoScreen(navController = navController, viewModel = viewModel, sku = sku)
                        }
                        composable(route = Screen.Profile.route) {
                            ProfileScreen(navController = navController, viewModel = viewModel)
                        }
                        composable(route = Screen.Settings.route) {
                            SettingsScreen(navController = navController, viewModel = viewModel)
                        }
                    }
                }
            }
        }
    }
}