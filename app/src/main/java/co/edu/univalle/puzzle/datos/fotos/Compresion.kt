package co.edu.univalle.puzzle.datos.fotos

import kotlin.math.max
import kotlin.math.roundToInt

/** Límites de una foto antes de guardarla o subirla (RNF-05). */
object Compresion {
    const val LADO_MAXIMO = 1080
    const val BYTES_MAXIMOS = 500 * 1024
    const val CALIDAD_INICIAL = 90
    const val CALIDAD_MINIMA = 40
    const val PASO_CALIDAD = 10

    /** Tamaño final manteniendo la proporción: el lado mayor queda en máximo 1080 px. */
    fun tamanoFinal(ancho: Int, alto: Int): Pair<Int, Int> {
        require(ancho > 0 && alto > 0) { "Imagen vacía" }
        val mayor = max(ancho, alto)
        if (mayor <= LADO_MAXIMO) return ancho to alto
        val escala = LADO_MAXIMO.toDouble() / mayor
        return (ancho * escala).roundToInt().coerceAtLeast(1) to (alto * escala).roundToInt().coerceAtLeast(1)
    }

    /**
     * Factor de submuestreo para leer la foto sin cargarla completa en memoria
     * (potencia de 2 que deja el lado mayor en al menos 1080 px).
     */
    fun submuestreo(ancho: Int, alto: Int): Int {
        var factor = 1
        while (max(ancho, alto) / (factor * 2) >= LADO_MAXIMO) factor *= 2
        return factor
    }

    /** Calidades JPEG que se prueban, de mayor a menor, hasta quedar en 500 KB o menos. */
    fun calidades(): List<Int> = (CALIDAD_INICIAL downTo CALIDAD_MINIMA step PASO_CALIDAD).toList()

    /** Grados que hay que girar según la orientación EXIF de la cámara. */
    fun gradosPorExif(orientacion: Int): Int = when (orientacion) {
        6 -> 90   // ORIENTATION_ROTATE_90
        3 -> 180  // ORIENTATION_ROTATE_180
        8 -> 270  // ORIENTATION_ROTATE_270
        else -> 0
    }
}
