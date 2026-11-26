package co.edu.univalle.puzzle.datos.fotos

import kotlin.math.min
import kotlin.math.roundToInt

data class Rect(val izquierda: Int, val arriba: Int, val lado: Int)

// zoom 1 es el cuadrado más grande posible; con 4 el lado es la cuarta parte.
data class MarcoRecorte(
    val ancho: Int,
    val alto: Int,
    val zoom: Float = 1f,
    val centroX: Float = ancho / 2f,
    val centroY: Float = alto / 2f
) {
    init {
        require(ancho > 0 && alto > 0) { "Imagen vacía" }
    }

    val lado: Float get() = min(ancho, alto) / zoom.coerceIn(ZOOM_MINIMO, ZOOM_MAXIMO)

    fun mover(dx: Float, dy: Float): MarcoRecorte = copy(centroX = centroX + dx, centroY = centroY + dy).ajustado()

    fun ampliar(factor: Float): MarcoRecorte =
        copy(zoom = (zoom * factor).coerceIn(ZOOM_MINIMO, ZOOM_MAXIMO)).ajustado()

    fun ajustado(): MarcoRecorte {
        val mitad = lado / 2
        return copy(
            zoom = zoom.coerceIn(ZOOM_MINIMO, ZOOM_MAXIMO),
            centroX = centroX.coerceIn(mitad, ancho - mitad),
            centroY = centroY.coerceIn(mitad, alto - mitad)
        )
    }

    fun cuadrado(): Rect {
        val a = ajustado()
        val l = a.lado.roundToInt().coerceIn(1, min(ancho, alto))
        val x = (a.centroX - l / 2f).roundToInt().coerceIn(0, ancho - l)
        val y = (a.centroY - l / 2f).roundToInt().coerceIn(0, alto - l)
        return Rect(x, y, l)
    }

    companion object {
        const val ZOOM_MINIMO = 1f
        const val ZOOM_MAXIMO = 4f
    }
}
