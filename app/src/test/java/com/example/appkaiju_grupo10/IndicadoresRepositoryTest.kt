package com.example.appkaiju_grupo10

import com.example.appkaiju_grupo10.data.remote.IndicadorResponse
import com.example.appkaiju_grupo10.data.remote.IndicadoresApi
import com.example.appkaiju_grupo10.data.remote.ValorIndicador
import com.example.appkaiju_grupo10.repository.IndicadoresRepository
import io.kotest.assertions.throwables.shouldThrow
import io.kotest.core.spec.style.StringSpec
import io.kotest.matchers.shouldBe
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk

class IndicadoresRepositoryTest : StringSpec({

    "entrega el valor más reciente de la serie y recorta la fecha" {
        // API falsa: responde exactamente lo que definimos (sin internet)
        val api = mockk<IndicadoresApi>()
        coEvery { api.getDolar() } returns IndicadorResponse(
            codigo = "dolar",
            nombre = "Dólar observado",
            serie = listOf(
                ValorIndicador(fecha = "2026-11-16T03:00:00.000Z", valor = 916.39),   // el más reciente
                ValorIndicador(fecha = "2026-11-15T03:00:00.000Z", valor = 920.10)
            )
        )

        val dolar = IndicadoresRepository(api).obtenerDolar()

        dolar.valor shouldBe 916.39
        dolar.fecha shouldBe "2026-11-16"
        coVerify(exactly = 1) { api.getDolar() }     // se llamó a la API una sola vez
    }

    "lanza un error si la API no entrega valores" {
        val api = mockk<IndicadoresApi>()
        coEvery { api.getDolar() } returns IndicadorResponse("dolar", "Dólar observado", emptyList())

        shouldThrow<IllegalStateException> {
            IndicadoresRepository(api).obtenerDolar()
        }
    }
})