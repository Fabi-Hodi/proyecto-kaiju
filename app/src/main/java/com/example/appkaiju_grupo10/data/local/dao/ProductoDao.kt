package com.example.appkaiju_grupo10.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.appkaiju_grupo10.model.Producto
import kotlinx.coroutines.flow.Flow
import com.example.appkaiju_grupo10.model.CategoriaResumen

@Dao
interface ProductoDao {

    // Lista completa, se actualiza sola cuando cambian los datos (Flow)
    @Query("SELECT * FROM productos ORDER BY nombre ASC")
    fun obtenerTodos(): Flow<List<Producto>>

    // Un producto por su SKU (para la pantalla movimiento)
    @Query("SELECT * FROM productos WHERE sku = :sku")
    fun obtenerPorSku(sku: String): Flow<Producto?>

    // Buscador "por SKU o nombre" del mockup
    @Query("""
        SELECT * FROM productos
        WHERE nombre LIKE '%' || :texto || '%' OR sku LIKE '%' || :texto || '%'
        ORDER BY nombre ASC
    """)
    fun buscar(texto: String): Flow<List<Producto>>

    // Cuantos productos hay (para saber si la BD esta vacia)
    @Query("SELECT COUNT(*) FROM productos")
    suspend fun contar(): Int

    // Insertar varios a la vez; si el SKU/codigo ya existe, lo ignora
    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertarTodos(productos: List<Producto>)

    // Cambiar solo el stock de un producto
    @Query("UPDATE productos SET stock = :nuevoStock WHERE sku = :sku")
    suspend fun actualizarStock(sku: String, nuevoStock: Int)

    @Update
    suspend fun actualizar(producto: Producto)
    // Categorías con cuántos productos tiene cada una (pantalla Inicio)
    @Query("""
        SELECT categoria, COUNT(*) AS cantidad
        FROM productos
        GROUP BY categoria
        ORDER BY categoria ASC
    """)
    fun obtenerCategorias(): Flow<List<CategoriaResumen>>
}