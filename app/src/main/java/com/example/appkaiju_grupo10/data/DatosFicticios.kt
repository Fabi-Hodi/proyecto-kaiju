package com.example.appkaiju_grupo10.data

import com.example.appkaiju_grupo10.model.Producto

// Catalogo ficticio (solo por el momento)
object DatosFicticios {

    val productos = listOf(
        Producto("KAJ-2034", "Filtro Industrial", "Filtro de aire para maquinaria", "Insumos", 15990, 12, 5, "Pasillo 3 - Rack 8"),
        Producto("KAJ-1049", "Guantes de nitrilo", "Caja de 100 unidades", "Insumos", 6490, 2, 10, "Pasillo 1 - Rack 2"),
        Producto("KAJ-3001", "Taladro percutor", "Taladro 750W", "Herramientas", 49990, 4, 2, "Pasillo 5 - Rack 1"),
        Producto("KAJ-3015", "Set de llaves", "Set de 12 llaves combinadas", "Herramientas", 22990, 1, 3, "Pasillo 5 - Rack 4"),
        Producto("KAJ-4100", "Cinta de embalaje", "Rollo transparente 48mm", "Embalaje", 1990, 40, 15, "Pasillo 2 - Rack 6"),
        Producto("KAJ-4120", "Caja de cartón M", "Caja 40x30x30 cm", "Embalaje", 990, 8, 20, "Pasillo 2 - Rack 7")
    )

    val categorias: List<String>
        get() = productos.map { it.categoria }.distinct()
}