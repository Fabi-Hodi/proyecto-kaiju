package com.example.appkaiju_grupo10.data.remote

import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

// Punto unico donde se crean los clientes HTTP.
// cada API tiene su propia URL base, por eso cada una tiene su propia instancia de Retrofit.
object RetrofitClient {

    // IP pública del EC2 (cambia cada vez que se reinicia el laboratorio de AWS Academy).
    // Si cambia, actualizarla aquí y en res/xml/network_security_config.xml
    private const val URL_BACKEND = "http://34.228.244.224:8080/"

    private fun crear(baseUrl: String): Retrofit =
        Retrofit.Builder()
            .baseUrl(baseUrl)                                    // debe terminar en "/"
            .addConverterFactory(GsonConverterFactory.create())  // JSON -> data class
            .build()

    // API pública: indicadores económicos de Chile (dólar, UF, etc.)
    val indicadoresApi: IndicadoresApi by lazy {
        crear("https://mindicador.cl/").create(IndicadoresApi::class.java)
    }

    // API propia: login y registro de Kaiju (backend Spring Boot de Sonido Vivo, rama alonso/kaiju-auth)
    val kaijuAuthApi: KaijuAuthApi by lazy {
        crear(URL_BACKEND).create(KaijuAuthApi::class.java)
    }

    // API propia: administración de usuarios (solo rol ADMIN)
    val kaijuAdminApi: KaijuAdminApi by lazy {
        crear(URL_BACKEND).create(KaijuAdminApi::class.java)
    }
}