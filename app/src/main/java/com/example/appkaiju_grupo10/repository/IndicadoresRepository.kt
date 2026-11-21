package com.example.appkaiju_grupo10.repository

import com.example.appkaiju_grupo10.data.remote.RetrofitClient

// valor del dolar ya listo para usar en la pantalla
data class DolarObservado(
    val valor: Double,   // CLP por 1 USD
    val fecha: String    // "2026-11-16"
)

// el repositorio es el unico que conoce la API, los ViewModels solo piden datos
class IndicadoresRepository {

    suspend fun obtenerDolar(): DolarObservado {
        val ultimo = RetrofitClient.indicadoresApi.getDolar().serie.firstOrNull()
            ?: throw IllegalStateException("La API no entregó valores del dólar")
        return DolarObservado(
            valor = ultimo.valor,
            fecha = ultimo.fecha.take(10)    // nos quedamos con "yyyy-MM-dd"
        )
    }
}
