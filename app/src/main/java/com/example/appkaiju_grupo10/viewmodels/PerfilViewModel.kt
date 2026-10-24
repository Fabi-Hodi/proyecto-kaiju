package com.example.appkaiju_grupo10.viewmodels

import android.app.Application
import android.content.Context
import android.net.Uri
import androidx.core.content.FileProvider
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File

class PerfilViewModel(application: Application) : AndroidViewModel(application) {

    private val carpetaFotos = File(application.filesDir, "fotos").apply { mkdirs() }
    private val archivoTemporal = File(carpetaFotos, "temp_camara.jpg")
    private val preferencias = application.getSharedPreferences("perfil", Context.MODE_PRIVATE)

    private val _fotoUri = MutableStateFlow(cargarFotoGuardada())
    val fotoUri: StateFlow<Uri?> = _fotoUri.asStateFlow()

    private val _errorImagen = MutableStateFlow<String?>(null)
    val errorImagen: StateFlow<String?> = _errorImagen.asStateFlow()

    fun prepararFotoCamara(): Uri {
        val app = getApplication<Application>()
        return FileProvider.getUriForFile(app, "${app.packageName}.fileprovider", archivoTemporal)
    }

    fun actualizarDesdeCamara(exito: Boolean) {
        if (!exito) return                    // el usuario canceló
        viewModelScope.launch {
            try {
                val nuevo = withContext(Dispatchers.IO) {
                    val destino = nuevoArchivo()
                    archivoTemporal.copyTo(destino, overwrite = true)
                    archivoTemporal.delete()
                    destino
                }
                guardarComoActual(nuevo)
            } catch (e: Exception) {
                _errorImagen.value = "No se pudo guardar la foto"
            }
        }
    }

    fun actualizarDesdeGaleria(uri: Uri?) {
        if (uri == null) return
        viewModelScope.launch {
            try {
                val nuevo = withContext(Dispatchers.IO) {
                    val destino = nuevoArchivo()
                    getApplication<Application>().contentResolver.openInputStream(uri)?.use { entrada ->
                        destino.outputStream().use { salida -> entrada.copyTo(salida) }
                    } ?: error("No se pudo leer la imagen")
                    destino
                }
                guardarComoActual(nuevo)
            } catch (e: Exception) {
                _errorImagen.value = "No se pudo cargar la imagen seleccionada"
            }
        }
    }

    fun limpiarError() {
        _errorImagen.value = null
    }
    private fun nuevoArchivo() = File(carpetaFotos, "perfil_${System.currentTimeMillis()}.jpg")

    private fun guardarComoActual(archivo: File) {
        carpetaFotos.listFiles()
            ?.filter { it.name.startsWith("perfil_") && it != archivo }
            ?.forEach { it.delete() }
        preferencias.edit().putString(CLAVE_FOTO, archivo.name).apply()
        _fotoUri.value = Uri.fromFile(archivo)
    }

    private fun cargarFotoGuardada(): Uri? {
        val nombre = preferencias.getString(CLAVE_FOTO, null) ?: return null
        val archivo = File(carpetaFotos, nombre)
        return if (archivo.exists()) Uri.fromFile(archivo) else null
    }

    companion object {
        private const val CLAVE_FOTO = "foto_actual"
    }
}