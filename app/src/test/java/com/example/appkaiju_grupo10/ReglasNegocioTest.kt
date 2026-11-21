package com.example.appkaiju_grupo10

import com.example.appkaiju_grupo10.model.Categoria
import com.example.appkaiju_grupo10.model.Producto
import com.example.appkaiju_grupo10.model.TipoMovimiento
import com.example.appkaiju_grupo10.model.calcularStockResultante
import com.example.appkaiju_grupo10.model.formatearSku
import com.example.appkaiju_grupo10.model.siguienteSku
import com.example.appkaiju_grupo10.ui.utils.normalizarRut
import com.example.appkaiju_grupo10.ui.utils.sugerirPrefijo
import com.example.appkaiju_grupo10.ui.utils.validarCantidad
import com.example.appkaiju_grupo10.ui.utils.validarPrefijo
import com.example.appkaiju_grupo10.ui.utils.validarRut
import io.kotest.core.spec.style.StringSpec
import io.kotest.matchers.nulls.shouldBeNull
import io.kotest.matchers.nulls.shouldNotBeNull
import io.kotest.matchers.shouldBe
import io.kotest.matchers.string.shouldContain

// Pruebas de la lógica pura de Kaiju: no necesitan emulador ni base de datos
class ReglasNegocioTest : StringSpec({

    // ----- Stock -----
    "una entrada suma al stock" {
        calcularStockResultante(10, TipoMovimiento.ENTRADA, 5) shouldBe 15
    }

    "una salida resta del stock" {
        calcularStockResultante(10, TipoMovimiento.SALIDA, 3) shouldBe 7
    }

    "un ajuste reemplaza el stock por lo contado" {
        calcularStockResultante(10, TipoMovimiento.AJUSTE, 4) shouldBe 4
    }

    "un producto con stock igual al mínimo tiene stock bajo" {
        val producto = Producto("IN-001", "Filtro", "", "Insumos", 1000, 5, 5, "Pasillo 1")
        producto.stockBajo shouldBe true
    }

    // ----- Código automático (SKU) -----
    "el SKU se arma con prefijo y número de 3 dígitos" {
        formatearSku("IN", 3) shouldBe "IN-003"
        formatearSku("HE", 12) shouldBe "HE-012"
    }

    "el siguiente SKU usa el contador de la categoría + 1" {
        Categoria(nombre = "Insumos", prefijo = "IN", ultimoNumero = 2).siguienteSku() shouldBe "IN-003"
    }

    "el prefijo se sugiere sin tildes y en mayúsculas" {
        sugerirPrefijo("Óptica") shouldBe "OP"
        sugerirPrefijo("insumos") shouldBe "IN"
    }

    "el prefijo debe tener 2 o 3 letras mayúsculas" {
        validarPrefijo("IN").shouldBeNull()
        validarPrefijo("INSU").shouldNotBeNull()
        validarPrefijo("i1").shouldNotBeNull()
    }

    // ----- RUT (módulo 11) -----
    "un RUT con dígito verificador correcto es válido" {
        validarRut("12.345.678-5").shouldBeNull()
        validarRut("11.111.111-1").shouldBeNull()
    }

    "un RUT con dígito verificador incorrecto es inválido" {
        validarRut("12.345.678-9").shouldNotBeNull()
    }

    "el RUT se guarda siempre en el mismo formato" {
        normalizarRut("12.345.678-5") shouldBe "12345678-5"
        normalizarRut("123456785") shouldBe "12345678-5"
    }

    // ----- Cantidad de un movimiento -----
    "no se puede sacar más de lo que hay en stock" {
        validarCantidad("11", TipoMovimiento.SALIDA, 10)
            .shouldNotBeNull()
            .shouldContain("Stock insuficiente")
    }

    "una entrada de 0 no es válida, pero un ajuste a 0 sí" {
        validarCantidad("0", TipoMovimiento.ENTRADA, 10).shouldNotBeNull()
        validarCantidad("0", TipoMovimiento.AJUSTE, 10).shouldBeNull()
    }
})