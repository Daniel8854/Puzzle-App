package co.edu.univalle.puzzle.datos.catalogo

import org.json.JSONArray

/** Dónde está la imagen para jugarla. */
enum class Ubicacion { INCLUIDA, DESCARGADA, EN_LA_NUBE }

/**
 * Imagen del catálogo (T-71). Siempre es cuadrada de 1080 px y pesa máximo 500 KB (RNF-06).
 * Se guarda el autor y el enlace de origen para respetar la licencia.
 */
data class ImagenCatalogo(
    val id: String,
    val titulo: String,
    val autor: String,
    val origen: String,
    val licencia: String,
    val ubicacion: Ubicacion,
    /** Archivo en assets o ruta local, según [ubicacion]. */
    val ruta: String
) {
    val disponibleSinConexion: Boolean get() = ubicacion != Ubicacion.EN_LA_NUBE

    companion object {
        /** Lee el listado de imágenes incluidas en la app (assets/catalogo/incluidas.json). */
        fun leerIncluidas(json: String): List<ImagenCatalogo> {
            val lista = JSONArray(json)
            return (0 until lista.length()).map { i ->
                val o = lista.getJSONObject(i)
                ImagenCatalogo(
                    id = o.getString("id"),
                    titulo = o.getString("titulo"),
                    autor = o.getString("autor"),
                    origen = o.getString("origen"),
                    licencia = o.getString("licencia"),
                    ubicacion = Ubicacion.INCLUIDA,
                    ruta = "catalogo/" + o.getString("archivo")
                )
            }
        }

        /**
         * Une las imágenes incluidas, las descargadas y las de la nube sin repetir:
         * si una imagen de la nube ya está descargada, se usa la copia local.
         */
        fun combinar(
            incluidas: List<ImagenCatalogo>,
            descargadas: List<ImagenCatalogo>,
            enLaNube: List<ImagenCatalogo>
        ): List<ImagenCatalogo> {
            val vistas = mutableSetOf<String>()
            return (incluidas + descargadas + enLaNube).filter { vistas.add(it.id) }
        }
    }
}
