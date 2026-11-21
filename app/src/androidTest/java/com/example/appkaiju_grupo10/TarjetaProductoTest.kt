package com.example.appkaiju_grupo10

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import com.example.appkaiju_grupo10.model.Producto
import com.example.appkaiju_grupo10.ui.components.TarjetaProducto
import com.example.appkaiju_grupo10.ui.theme.AppKaiju_Grupo10Theme
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test

class TarjetaProductoTest {

    @get:Rule
    val composeRule = createComposeRule()

    // stock 2 con mínimo 10: stock bajo
    private val productoBajo = Producto("IN-002", "Guantes de nitrilo", "Caja de 100", "Insumos", 6490, 2, 10, "Pasillo 1 - Rack 2")
    // stock 40 con mínimo 15: stock normal
    private val productoNormal = Producto("EM-001", "Cinta de embalaje", "Rollo 48mm", "Embalaje", 1990, 40, 15, "Pasillo 2 - Rack 6")

    @Test
    fun muestra_los_datos_del_producto() {
        composeRule.setContent {
            AppKaiju_Grupo10Theme { TarjetaProducto(producto = productoNormal, onVerFicha = {}) }
        }
        composeRule.onNodeWithText("Cinta de embalaje").assertIsDisplayed()
        composeRule.onNodeWithText("SKU: EM-001").assertIsDisplayed()
        composeRule.onNodeWithText("Pasillo 2 - Rack 6").assertIsDisplayed()
    }

    @Test
    fun con_stock_bajo_muestra_la_etiqueta_reponer() {
        composeRule.setContent {
            AppKaiju_Grupo10Theme { TarjetaProducto(producto = productoBajo, onVerFicha = {}) }
        }
        composeRule.onNodeWithText("Reponer").assertIsDisplayed()
    }

    @Test
    fun con_stock_normal_no_muestra_reponer() {
        composeRule.setContent {
            AppKaiju_Grupo10Theme { TarjetaProducto(producto = productoNormal, onVerFicha = {}) }
        }
        composeRule.onNodeWithText("Reponer").assertDoesNotExist()
    }

    @Test
    fun el_precio_en_dolares_aparece_solo_si_hay_valor_del_dolar() {
        composeRule.setContent {
            AppKaiju_Grupo10Theme { TarjetaProducto(producto = productoNormal, onVerFicha = {}, dolar = 916.39) }
        }
        composeRule.onNodeWithText("US$", substring = true).assertIsDisplayed()
    }

    @Test
    fun sin_valor_del_dolar_no_muestra_precio_en_dolares() {
        composeRule.setContent {
            AppKaiju_Grupo10Theme { TarjetaProducto(producto = productoNormal, onVerFicha = {}, dolar = null) }
        }
        composeRule.onNodeWithText("US$", substring = true).assertDoesNotExist()
    }

    @Test
    fun el_boton_ver_ficha_avisa_al_tocarlo() {
        var clics = 0
        composeRule.setContent {
            AppKaiju_Grupo10Theme { TarjetaProducto(producto = productoNormal, onVerFicha = { clics++ }) }
        }
        composeRule.onNodeWithText("Ver ficha / Trazabilidad").performClick()
        assertEquals(1, clics)
    }
}