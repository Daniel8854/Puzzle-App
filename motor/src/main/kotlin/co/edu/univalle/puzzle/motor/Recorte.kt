package co.edu.univalle.puzzle.motor

/**
 * Parte de la imagen que le toca a cada pieza (RF-08).
 *
 * La imagen siempre es cuadrada (lado × lado): el catálogo y las fotos propias se recortan
 * en cuadrado antes de jugar, así las piezas son iguales y nada se deforma.
 */
data class Recorte(val izquierda: Int, val arriba: Int, val lado: Int)

object Recortes {

    /**
     * Recorte de la imagen que muestra la [pieza] (1 a N²−1) en un tablero de [tamano] × [tamano].
     * Si [ladoImagen] no es múltiplo de [tamano], se ignoran los píxeles sobrantes del borde
     * derecho e inferior para que todas las piezas midan lo mismo.
     */
    fun de(pieza: Int, tamano: Int, ladoImagen: Int): Recorte {
        require(tamano >= Tablero.TAMANO_MINIMO) { "El tamaño mínimo es ${Tablero.TAMANO_MINIMO}" }
        require(pieza in 1 until tamano * tamano) { "Pieza fuera de rango: $pieza" }
        val ladoPieza = ladoImagen / tamano
        require(ladoPieza > 0) { "La imagen es demasiado pequeña para un tablero de $tamano×$tamano" }
        val posicion = pieza - 1
        return Recorte(
            izquierda = (posicion % tamano) * ladoPieza,
            arriba = (posicion / tamano) * ladoPieza,
            lado = ladoPieza
        )
    }

    /** Recortes de todas las piezas, en orden (pieza 1 primero). */
    fun todos(tamano: Int, ladoImagen: Int): List<Recorte> =
        (1 until tamano * tamano).map { de(it, tamano, ladoImagen) }
}
