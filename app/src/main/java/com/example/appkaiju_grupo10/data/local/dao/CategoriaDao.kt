package com.example.appkaiju_grupo10.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.appkaiju_grupo10.model.Categoria
import kotlinx.coroutines.flow.Flow

@Dao
interface CategoriaDao {

    // Lista para el selector del formulario de productos (se actualiza sola)
    @Query("SELECT * FROM categorias ORDER BY nombre ASC")
    fun obtenerTodas(): Flow<List<Categoria>>

    // COLLATE NOCASE: "insumos" e "Insumos" cuentan como la misma
    @Query("SELECT * FROM categorias WHERE nombre = :nombre COLLATE NOCASE LIMIT 1")
    suspend fun buscarPorNombre(nombre: String): Categoria?

    @Query("SELECT * FROM categorias WHERE prefijo = :prefijo LIMIT 1")
    suspend fun buscarPorPrefijo(prefijo: String): Categoria?

    @Insert
    suspend fun insertar(categoria: Categoria)

    // Para los datos iniciales: si ya existe, la salta
    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertarTodas(categorias: List<Categoria>)

    // Avanza el contador al crear un producto
    @Query("UPDATE categorias SET ultimoNumero = :numero WHERE nombre = :nombre")
    suspend fun actualizarUltimoNumero(nombre: String, numero: Int)
}