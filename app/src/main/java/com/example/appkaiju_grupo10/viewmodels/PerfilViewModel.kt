package com.example.appkaiju_grupo10.viewmodels

import android.app.Application
import android.graphics.Bitmap
import android.graphics.ImageDecoder
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
import java.io.FileOutputStream

class PerfilViewModel(application: Application) : AndroidViewModel(application) {

    private val _fotoUri = MutableStateFlow<Uri?>(null)
    val fotoUri: StateFlow<Uri?> = _fotoUri.asStateFlow()

    private val _errorImagen = MutableStateFlow<String?>(null)
    val errorImagen: StateFlow<String?> = _errorImagen.asStateFlow()

    // archivo temporal donde la camara esta guardando la foto
    private var archivoCamaraPendiente: File? = null

    private val carpetaFotos: File
        get() = File(getApplication<Application>().filesDir, "fotos")

    init {
        cargarFotoGuardada()
    }

    // al abrir el perfil, recupera la ultima foto guardada (si existe)
    private fun cargarFotoGuardada() {
        val archivo = carpetaFotos.listFiles()?.maxByOrNull { it.lastModified() }
        _fotoUri.value = archivo?.let { Uri.fromFile(it) }
    }

    // ---------- CAMARA ----------

    // crea el archivo temporal y devuelve su Uri (via FileProvider) para pasarselo a TakePicture.
    fun prepararFotoCamara(): Uri {
        val app = getApplication<Application>()
        val carpeta = File(app.cacheDir, "camara").apply { mkdirs() }
        val archivo = File(carpeta, "captura_${System.currentTimeMillis()}.jpg")
        archivoCamaraPendiente = archivo
        return FileProvider.getUriForFile(app, "${app.packageName}.fileprovider", archivo)
    }

    // resultado de TakePicture: exito = true si el usuario acepto la foto, false si cancelo.
    fun actualizarDesdeCamara(exito: Boolean) {
        val archivo = archivoCamaraPendiente ?: return
        archivoCamaraPendiente = null
        if (!exito) {
            archivo.delete() // cancelo: no hacemos nada mas, sin crash
            return
        }
        viewModelScope.launch {
            guardar { ImageDecoder.createSource(archivo) }
            withContext(Dispatchers.IO) { archivo.delete() }
        }
    }

    // ---------- GALERIA ----------

    // resultado de GetContent: uri = null si el usuario cancelo.
    fun actualizarDesdeGaleria(uri: Uri?) {
        if (uri == null) return // cancelo
        val resolver = getApplication<Application>().contentResolver
        viewModelScope.launch {
            guardar { ImageDecoder.createSource(resolver, uri) }
        }
    }

    fun limpiarError() {
        _errorImagen.value = null
    }

    // ---------- Interno ----------

    // reduce la imagen (max. ~1024 px), la guarda en filesDir y actualiza el StateFlow
    private suspend fun guardar(origen: () -> ImageDecoder.Source) {
        val nuevo = withContext(Dispatchers.IO) {
            try {
                val bitmap = ImageDecoder.decodeBitmap(origen()) { decoder, info, _ ->
                    decoder.allocator = ImageDecoder.ALLOCATOR_SOFTWARE
                    val lado = maxOf(info.size.width, info.size.height)
                    if (lado > 1024) decoder.setTargetSampleSize(lado / 1024)
                }
                val carpeta = carpetaFotos.apply { mkdirs() }
                val archivo = File(carpeta, "perfil_${System.currentTimeMillis()}.jpg")
                FileOutputStream(archivo).use { bitmap.compress(Bitmap.CompressFormat.JPEG, 90, it) }
                // borramos las fotos anteriores
                carpeta.listFiles()?.filter { it != archivo }?.forEach { it.delete() }
                archivo
            } catch (e: Exception) {
                null
            }
        }
        if (nuevo != null) {
            _errorImagen.value = null
            _fotoUri.value = Uri.fromFile(nuevo)
        } else {
            _errorImagen.value = "No se pudo cargar la imagen. Intenta con otra."
        }
    }
}
