package co.edu.univalle.puzzle.motor

/**
 * Tablero N×N del rompecabezas deslizante (RF-08).
 *
 * Las casillas se guardan por filas, de izquierda a derecha y de arriba abajo.
 * Cada casilla tiene el número de la pieza que la ocupa (1 a N²−1) o [VACIA].
 * En el tablero resuelto la pieza k está en la posición k−1 y la vacía al final.
 *
 * Es inmutable: cualquier cambio produce un tablero nuevo.
 */
class Tablero private constructor(
    val tamano: Int,
    private val casillas: IntArray
) {

    /** Cantidad total de casillas (N²). */
    val totalCasillas: Int get() = casillas.size

    /** Cantidad de piezas (N²−1). */
    val totalPiezas: Int get() = casillas.size - 1

    /** Posición (índice) de la casilla vacía. */
    val posicionVacia: Int = casillas.indexOf(VACIA)

    /** Pieza que ocupa la posición [indice] o [VACIA]. */
    fun piezaEn(indice: Int): Int {
        require(indice in casillas.indices) { "Índice fuera del tablero: $indice" }
        return casillas[indice]
    }

    /** Pieza que ocupa la casilla de la [fila] y [columna] dadas (desde 0). */
    fun piezaEn(fila: Int, columna: Int): Int = piezaEn(indiceDe(fila, columna))

    fun filaDe(indice: Int): Int = indice / tamano

    fun columnaDe(indice: Int): Int = indice % tamano

    fun indiceDe(fila: Int, columna: Int): Int {
        require(fila in 0 until tamano && columna in 0 until tamano) {
            "Casilla fuera del tablero: ($fila, $columna)"
        }
        return fila * tamano + columna
    }

    /** Verdadero si cada pieza está en su lugar y la vacía en la última casilla. */
    fun estaResuelto(): Boolean =
        (0 until totalPiezas).all { casillas[it] == it + 1 } && casillas.last() == VACIA

    /** Cantidad de piezas que ya están en su lugar (sirve para el progreso). */
    fun piezasEnSuLugar(): Int = (0 until totalPiezas).count { casillas[it] == it + 1 }

    /** Copia de las casillas, para guardar la partida o dibujar el tablero. */
    fun comoLista(): List<Int> = casillas.toList()

    /** Crea un tablero nuevo intercambiando dos casillas. Uso interno del motor. */
    internal fun intercambiar(a: Int, b: Int): Tablero {
        val copia = casillas.copyOf()
        copia[a] = casillas[b]
        copia[b] = casillas[a]
        return Tablero(tamano, copia)
    }

    override fun equals(other: Any?): Boolean =
        other is Tablero && other.tamano == tamano && other.casillas.contentEquals(casillas)

    override fun hashCode(): Int = 31 * tamano + casillas.contentHashCode()

    override fun toString(): String =
        casillas.toList().chunked(tamano).joinToString("\n") { fila ->
            fila.joinToString(" ") { if (it == VACIA) " ." else it.toString().padStart(2) }
        }

    companion object {
        const val VACIA = 0
        const val TAMANO_MINIMO = 2

        /** Tablero resuelto de [tamano] × [tamano]. */
        fun resuelto(tamano: Int): Tablero {
            require(tamano >= TAMANO_MINIMO) { "El tamaño mínimo es $TAMANO_MINIMO" }
            val total = tamano * tamano
            val casillas = IntArray(total) { if (it == total - 1) VACIA else it + 1 }
            return Tablero(tamano, casillas)
        }

        fun resuelto(dificultad: Dificultad): Tablero = resuelto(dificultad.tamano)

        /**
         * Crea un tablero a partir de sus casillas (por ejemplo, al restaurar una partida).
         * Lanza [IllegalArgumentException] si las casillas no forman un tablero válido:
         * deben ser exactamente los números 0 a N²−1, cada uno una sola vez.
         */
        fun desde(tamano: Int, casillas: List<Int>): Tablero {
            require(tamano >= TAMANO_MINIMO) { "El tamaño mínimo es $TAMANO_MINIMO" }
            val total = tamano * tamano
            require(casillas.size == total) {
                "Se esperaban $total casillas y llegaron ${casillas.size}"
            }
            require(casillas.sorted() == (0 until total).toList()) {
                "Las casillas deben contener los números 0 a ${total - 1} sin repetir"
            }
            return Tablero(tamano, casillas.toIntArray())
        }
    }
}
