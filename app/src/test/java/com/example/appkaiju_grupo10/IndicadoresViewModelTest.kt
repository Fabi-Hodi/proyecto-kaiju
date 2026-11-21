package com.example.appkaiju_grupo10

import com.example.appkaiju_grupo10.repository.DolarObservado
import com.example.appkaiju_grupo10.repository.IndicadoresRepository
import com.example.appkaiju_grupo10.viewmodels.EstadoDolar
import com.example.appkaiju_grupo10.viewmodels.IndicadoresViewModel
import io.kotest.core.spec.style.StringSpec
import io.kotest.matchers.shouldBe
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import java.io.IOException

@OptIn(ExperimentalCoroutinesApi::class)
class IndicadoresViewModelTest : StringSpec({

    // viewModelScope usa el hilo "Main" de Android, que no existe en el computador:
    // lo reemplazamos por un dispatcher de prueba que nosotros controlamos
    val dispatcher = StandardTestDispatcher()
    beforeTest { Dispatchers.setMain(dispatcher) }
    afterTest { Dispatchers.resetMain() }

    val dolarDePrueba = DolarObservado(valor = 916.39, fecha = "2026-11-16")

    "empieza cargando y luego muestra el dólar" {
        runTest(dispatcher) {
            val repository = mockk<IndicadoresRepository>()
            coEvery { repository.obtenerDolar() } returns dolarDePrueba

            val viewModel = IndicadoresViewModel(repository)
            viewModel.estado.value shouldBe EstadoDolar.Cargando     // antes de ejecutar la corrutina

            advanceUntilIdle()                                        // ejecuta todo lo pendiente
            viewModel.estado.value shouldBe EstadoDolar.Listo(dolarDePrueba)
        }
    }

    "si la API falla, el estado pasa a Error" {
        runTest(dispatcher) {
            val repository = mockk<IndicadoresRepository>()
            coEvery { repository.obtenerDolar() } throws IOException("sin internet")

            val viewModel = IndicadoresViewModel(repository)
            advanceUntilIdle()

            viewModel.estado.value shouldBe EstadoDolar.Error
        }
    }

    "al reintentar después de un error, se recupera" {
        runTest(dispatcher) {
            val repository = mockk<IndicadoresRepository>()
            coEvery { repository.obtenerDolar() } throws IOException("sin internet")

            val viewModel = IndicadoresViewModel(repository)
            advanceUntilIdle()
            viewModel.estado.value shouldBe EstadoDolar.Error

            // vuelve internet: ahora la API responde bien
            coEvery { repository.obtenerDolar() } returns dolarDePrueba
            viewModel.cargarDolar()                                   // lo que hace el botón "Reintentar"
            advanceUntilIdle()

            viewModel.estado.value shouldBe EstadoDolar.Listo(dolarDePrueba)
            coVerify(exactly = 2) { repository.obtenerDolar() }      // 1 al iniciar + 1 al reintentar
        }
    }
})