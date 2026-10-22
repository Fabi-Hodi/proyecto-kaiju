package com.example.appkaiju_grupo10.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.example.appkaiju_grupo10.data.local.dao.MovimientoDao
import com.example.appkaiju_grupo10.data.local.dao.ProductoDao
import com.example.appkaiju_grupo10.data.local.dao.UsuarioDao
import com.example.appkaiju_grupo10.model.Movimiento
import com.example.appkaiju_grupo10.model.Producto
import com.example.appkaiju_grupo10.model.Usuario

@Database(
    entities = [Producto::class, Movimiento::class, Usuario::class],
    version = 3,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {

    abstract val productoDao: ProductoDao
    abstract val movimientoDao: MovimientoDao
    abstract val usuarioDao: UsuarioDao

    companion object {
        // @Volatile: todos los hilos ven siempre el mismo valor actualizado
        @Volatile
        private var INSTANCE: AppDatabase? = null

        // Devuelve siemprela misma base de datos
        fun getDatabase(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "kaiju_database"
                )
                    .fallbackToDestructiveMigration(dropAllTables = true)
                    .build()
                    .also { INSTANCE = it }
            }
        }
    }
}