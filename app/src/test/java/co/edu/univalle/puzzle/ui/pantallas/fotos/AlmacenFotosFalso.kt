package co.edu.univalle.puzzle.ui.pantallas.fotos

import android.graphics.Bitmap
import android.net.Uri
import co.edu.univalle.puzzle.datos.fotos.AlmacenFotos
import co.edu.univalle.puzzle.datos.fotos.Rect
import co.edu.univalle.puzzle.datos.local.FotoPropia

class AlmacenFotosFalso(var falla: Boolean = false, private val foto: FotoPropia? = null) : AlmacenFotos {
    data class Recorte(val rotacion: Int, val volteada: Boolean, val recorte: Rect, val cuadricula: Int)

    val guardadas = mutableListOf<String>()
    val recortes = mutableListOf<Recorte>()

    override fun nuevoArchivoCamara(): Uri = throw UnsupportedOperationException("no se usa en las pruebas")

    override suspend fun guardar(origen: String): Long {
        if (falla) error("foto dañada")
        guardadas += origen
        return guardadas.size.toLong()
    }

    override suspend fun porId(id: Long): FotoPropia? = foto?.takeIf { it.id == id }

    override suspend fun cargarImagen(foto: FotoPropia): Bitmap? = null

    override suspend fun guardarRecorte(foto: FotoPropia, rotacion: Int, volteada: Boolean, recorte: Rect, cuadricula: Int) {
        if (falla) error("disco lleno")
        recortes += Recorte(rotacion, volteada, recorte, cuadricula)
    }
}
