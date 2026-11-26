package co.edu.univalle.puzzle.ui.pantallas.fotos

import android.net.Uri
import co.edu.univalle.puzzle.datos.fotos.AlmacenFotos

/** Almacén de fotos en memoria para probar los ViewModels sin teléfono ni base de datos. */
class AlmacenFotosFalso(var falla: Boolean = false) : AlmacenFotos {
    val guardadas = mutableListOf<String>()

    override fun nuevoArchivoCamara(): Uri = throw UnsupportedOperationException("no se usa en las pruebas")

    override suspend fun guardar(origen: String): Long {
        if (falla) error("foto dañada")
        guardadas += origen
        return guardadas.size.toLong()
    }
}
