package com.example.appkaiju_grupo10.data.remote

import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

// Punto unico donde se crean los clientes HTTP.
// cada API tiene su propia URL base, por eso cada una tiene su propia instancia de Retrofit.
object RetrofitClient {

    private fun crear(baseUrl: String): Retrofit =
        Retrofit.Builder()
            .baseUrl(baseUrl)                                    // debe terminar en "/"
            .addConverterFactory(GsonConverterFactory.create())  // JSON -> data class
            .build()

    //API pública: indicadores económicos de Chile (dólar, UF, etc.)
    val indicadoresApi: IndicadoresApi by lazy {
        crear("https://mindicador.cl/").create(IndicadoresApi::class.java)
    }

    // aqui se agregara la API propia del grupo (backend con base de datos):
    // val kaijuApi: KaijuApi by lazy { crear("http://10.0.2.2:8080/").create(KaijuApi::class.java) }
}
