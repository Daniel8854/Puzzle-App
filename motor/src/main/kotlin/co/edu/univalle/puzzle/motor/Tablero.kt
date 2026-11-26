package co.edu.univalle.puzzle.motor

// Casillas por filas, de izquierda a derecha. Es inmutable: cada cambio devuelve un tablero nuevo.
class Tablero private constructor(
    val tamano: Int,
    private val casillas: IntArray
) {
    val totalCasillas: Int get() = casillas.size

    val totalPiezas: Int get() = casillas.size - 1

    val posicionVacia: Int = casillas.indexOf(VACIA)

    fun piezaEn(indice: Int): Int {
        require(indice in casillas.indices) { "Índice fuera del tablero: $indice" }
        return casillas[indice]
    }

    fun piezaEn(fila: Int, columna: Int): Int = piezaEn(indiceDe(fila, columna))

    fun filaDe(indice: Int): Int = indice / tamano

    fun columnaDe(indice: Int): Int = indice % tamano

    fun indiceDe(fila: Int, columna: Int): Int {
        require(fila in 0 until tamano && columna in 0 until tamano) {
            "Casilla fuera del tablero: ($fila, $columna)"
        }
        return fila * tamano + columna
    }

    fun estaResuelto(): Boolean =
        (0 until totalPiezas).all { casillas[it] == it + 1 } && casillas.last() == VACIA

    fun piezasEnSuLugar(): Int = (0 until totalPiezas).count { casillas[it] == it + 1 }

    fun comoLista(): List<Int> = casillas.toList()

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

        fun resuelto(tamano: Int): Tablero {
            require(tamano >= TAMANO_MINIMO) { "El tamaño mínimo es $TAMANO_MINIMO" }
            val total = tamano * tamano
            val casillas = IntArray(total) { if (it == total - 1) VACIA else it + 1 }
            return Tablero(tamano, casillas)
        }

        fun resuelto(dificultad: Dificultad): Tablero = resuelto(dificultad.tamano)

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
