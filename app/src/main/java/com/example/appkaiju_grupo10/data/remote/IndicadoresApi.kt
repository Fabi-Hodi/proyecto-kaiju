package com.example.appkaiju_grupo10.data.remote

import retrofit2.http.GET

// respuesta de https://mindicador.cl/api/dolar
// la API devuelve el indicador y una "serie" con los últimos valores (el primero es el más reciente)
data class IndicadorResponse(
    val codigo: String,
    val nombre: String,
    val serie: List<ValorIndicador>
)

data class ValorIndicador(
    val fecha: String,   // ejemplo: "2026-11-16T03:00:00.000Z"
    val valor: Double    // pesos chilenos por 1 dólar
)

// endpoints de la API publica (la URL base se define en RetrofitClient)
interface IndicadoresApi {
    @GET("api/dolar")
    suspend fun getDolar(): IndicadorResponse
}
