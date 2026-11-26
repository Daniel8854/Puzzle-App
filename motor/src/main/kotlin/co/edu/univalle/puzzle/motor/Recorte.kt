package co.edu.univalle.puzzle.motor

data class Recorte(val izquierda: Int, val arriba: Int, val lado: Int)

object Recortes {
    // Si el lado de la imagen no es múltiplo del tamaño, se ignoran los píxeles sobrantes del borde derecho e inferior.
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

    fun todos(tamano: Int, ladoImagen: Int): List<Recorte> =
        (1 until tamano * tamano).map { de(it, tamano, ladoImagen) }
}
