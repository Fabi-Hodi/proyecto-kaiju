package com.example.appkaiju_grupo10.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import com.example.appkaiju_grupo10.model.Movimiento
import kotlinx.coroutines.flow.Flow

@Dao
interface MovimientoDao {

    @Insert
    suspend fun insertar(movimiento: Movimiento)

    // Historial de un producto, del más reciente al más antiguo
    @Query("SELECT * FROM movimientos WHERE sku = :sku ORDER BY fecha DESC")
    fun obtenerPorProducto(sku: String): Flow<List<Movimiento>>
}