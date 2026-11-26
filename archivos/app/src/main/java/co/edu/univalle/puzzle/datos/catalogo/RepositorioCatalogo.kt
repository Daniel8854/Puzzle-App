package co.edu.univalle.puzzle.datos.catalogo

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

/**
 * Catálogo de imágenes del juego (T-71).
 *
 * - Las imágenes vienen de Pixabay, elegidas por el equipo, con su autor y origen en
 *   assets/catalogo/incluidas.json. La app no consulta la API de Pixabay.
 * - Todas van dentro de la app (plan Spark: no hay Cloud Storage), así se juega sin conexión
 *   (RNF-13) y los dos jugadores de un duelo tienen siempre la misma imagen.
 * - herramientas/catalogo/preparar.js las deja en 1080×1080 y con máximo 500 KB (RNF-06).
 */
class RepositorioCatalogo(context: Context) {

    private val app = context.applicationContext

    fun incluidas(): List<ImagenCatalogo> =
        ImagenCatalogo.leerIncluidas(app.assets.open("catalogo/incluidas.json").bufferedReader().use { it.readText() })

    /** Todo lo que se puede elegir: siempre está disponible, con o sin conexión. */
    suspend fun todas(): List<ImagenCatalogo> = withContext(Dispatchers.IO) { incluidas() }

    /** Las imágenes ya están en el teléfono: no hay nada que descargar. */
    suspend fun descargar(imagen: ImagenCatalogo): ImagenCatalogo = imagen

    /** Busca una imagen por id. */
    suspend fun buscarLocal(id: String): ImagenCatalogo? = todas().firstOrNull { it.id == id }

    /** Carga la imagen para dibujar el tablero. */
    suspend fun cargar(imagen: ImagenCatalogo): Bitmap? = withContext(Dispatchers.IO) {
        when (imagen.ubicacion) {
            Ubicacion.INCLUIDA -> app.assets.open(imagen.ruta).use { BitmapFactory.decodeStream(it) }
            Ubicacion.DESCARGADA -> BitmapFactory.decodeFile(imagen.ruta)
            Ubicacion.EN_LA_NUBE -> null
        }
    }
}
