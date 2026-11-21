package com.example.appkaiju_grupo10

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import com.example.appkaiju_grupo10.repository.DolarObservado
import com.example.appkaiju_grupo10.ui.components.BannerDolar
import com.example.appkaiju_grupo10.ui.theme.AppKaiju_Grupo10Theme
import com.example.appkaiju_grupo10.viewmodels.EstadoDolar
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test

class BannerDolarTest {

    // Regla que crea una pantalla vacía donde dibujamos el componente a probar
    @get:Rule
    val composeRule = createComposeRule()

    @Test
    fun mientras_carga_muestra_el_mensaje_de_consulta() {
        composeRule.setContent {
            AppKaiju_Grupo10Theme {
                BannerDolar(estado = EstadoDolar.Cargando, valorInventarioClp = 0, onReintentar = {})
            }
        }
        composeRule.onNodeWithText("Consultando el dólar", substring = true).assertIsDisplayed()
    }

    @Test
    fun si_hay_error_muestra_el_mensaje_y_el_boton_reintentar_funciona() {
        var reintentos = 0
        composeRule.setContent {
            AppKaiju_Grupo10Theme {
                BannerDolar(
                    estado = EstadoDolar.Error,
                    valorInventarioClp = 0,
                    onReintentar = { reintentos++ }
                )
            }
        }
        composeRule.onNodeWithText("No se pudo obtener el valor del dólar").assertIsDisplayed()

        composeRule.onNodeWithText("Reintentar").performClick()   // "toca" el botón

        assertEquals(1, reintentos)                                // el botón avisó una vez
    }

    @Test
    fun con_el_dolar_listo_muestra_el_valor_y_el_inventario_en_dolares() {
        composeRule.setContent {
            AppKaiju_Grupo10Theme {
                BannerDolar(
                    estado = EstadoDolar.Listo(DolarObservado(valor = 916.39, fecha = "2026-11-16")),
                    valorInventarioClp = 1_000_000,
                    onReintentar = {}
                )
            }
        }
        composeRule.onNodeWithText("916,39", substring = true).assertIsDisplayed()       // formato chileno
        composeRule.onNodeWithText("2026-11-16", substring = true).assertIsDisplayed()
        composeRule.onNodeWithText("US$ 1.091,24", substring = true).assertIsDisplayed() // 1.000.000 / 916,39
    }
}