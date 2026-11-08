package com.example.appkaiju_grupo10.data

import com.example.appkaiju_grupo10.model.Categoria
import com.example.appkaiju_grupo10.model.Producto

// Catálogo ficticio inicial (el caso exige datos ficticios)
object DatosFicticios {

    // ultimoNumero = cuántos códigos ya usa cada categoría abajo
    val categorias = listOf(
        Categoria(nombre = "Insumos", prefijo = "IN", ultimoNumero = 2),
        Categoria(nombre = "Herramientas", prefijo = "HE", ultimoNumero = 2),
        Categoria(nombre = "Embalaje", prefijo = "EM", ultimoNumero = 2)
    )

    val productos = listOf(
        Producto("IN-001", "Filtro Industrial", "Filtro de aire para maquinaria", "Insumos", 15990, 12, 5, "Pasillo 3 - Rack 8"),
        Producto("IN-002", "Guantes de nitrilo", "Caja de 100 unidades", "Insumos", 6490, 2, 10, "Pasillo 1 - Rack 2"),
        Producto("HE-001", "Taladro percutor", "Taladro 750W", "Herramientas", 49990, 4, 2, "Pasillo 5 - Rack 1"),
        Producto("HE-002", "Set de llaves", "Set de 12 llaves combinadas", "Herramientas", 22990, 1, 3, "Pasillo 5 - Rack 4"),
        Producto("EM-001", "Cinta de embalaje", "Rollo transparente 48mm", "Embalaje", 1990, 40, 15, "Pasillo 2 - Rack 6"),
        Producto("EM-002", "Caja de cartón M", "Caja 40x30x30 cm", "Embalaje", 990, 8, 20, "Pasillo 2 - Rack 7")
    )
}