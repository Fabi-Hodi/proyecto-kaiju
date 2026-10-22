package com.example.appkaiju_grupo10.model

// Resultado de la consulta "categorías con su cantidad de productos"
// NO es una tabla. no lleva @Entity: es solo la forma de la respuesta
data class CategoriaResumen(
    val categoria: String,
    val cantidad: Int
)