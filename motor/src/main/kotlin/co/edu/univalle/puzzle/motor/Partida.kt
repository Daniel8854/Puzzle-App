package co.edu.univalle.puzzle.motor

enum class FinPartida { COMPLETADA, TIEMPO_AGOTADO }

data class ResultadoPartida(
    val fin: FinPartida,
    val movimientos: Int,
    val tiempoMs: Long,
    val piezasEnSuLugar: Int,
    val totalPiezas: Int
)

// No lee el reloj: cada operación recibe el instante actual (ahoraMs), así se prueba sin esperar.
data class Partida(
    val tablero: Tablero,
    val movimientos: Int = 0,
    val limiteMs: Long? = null,
    private val acumuladoMs: Long = 0,
    private val desdeMs: Long? = null,
    val resultado: ResultadoPartida? = null
) {
    val terminada: Boolean get() = resultado != null
    val enPausa: Boolean get() = desdeMs == null && !terminada

    val piezasEnSuLugar: Int get() = tablero.piezasEnSuLugar()
    val totalPiezas: Int get() = tablero.totalPiezas
    val progreso: Float get() = piezasEnSuLugar.toFloat() / totalPiezas

    fun tiempoMs(ahoraMs: Long): Long {
        if (resultado != null) return resultado.tiempoMs
        val corriendo = desdeMs?.let { (ahoraMs - it).coerceAtLeast(0) } ?: 0
        val total = acumuladoMs + corriendo
        return limiteMs?.let { minOf(total, it) } ?: total
    }

    fun restanteMs(ahoraMs: Long): Long? = limiteMs?.let { (it - tiempoMs(ahoraMs)).coerceAtLeast(0) }

    fun pausar(ahoraMs: Long): Partida =
        if (desdeMs == null || terminada) this
        else copy(acumuladoMs = tiempoMs(ahoraMs), desdeMs = null)

    fun reanudar(ahoraMs: Long): Partida =
        if (desdeMs != null || terminada) this else copy(desdeMs = ahoraMs)

    fun revisarTiempo(ahoraMs: Long): Partida {
        if (terminada || limiteMs == null || tiempoMs(ahoraMs) < limiteMs) return this
        return terminar(FinPartida.TIEMPO_AGOTADO, limiteMs)
    }

    fun mover(posicion: Int, ahoraMs: Long): Partida {
        val vigente = revisarTiempo(ahoraMs)
        if (vigente.terminada || vigente.enPausa) return vigente
        val nuevo = vigente.tablero.mover(posicion) ?: return vigente
        val movida = vigente.copy(tablero = nuevo, movimientos = vigente.movimientos + 1)
        return if (nuevo.estaResuelto()) movida.terminar(FinPartida.COMPLETADA, movida.tiempoMs(ahoraMs)) else movida
    }

    private fun terminar(fin: FinPartida, tiempo: Long) = copy(
        acumuladoMs = tiempo,
        desdeMs = null,
        resultado = ResultadoPartida(fin, movimientos, tiempo, tablero.piezasEnSuLugar(), tablero.totalPiezas)
    )

    companion object {
        fun nueva(tablero: Tablero, ahoraMs: Long, limiteMs: Long? = null) =
            Partida(tablero = tablero, limiteMs = limiteMs, desdeMs = ahoraMs)

        fun limiteContrarreloj(dificultad: Dificultad): Long = when (dificultad) {
            Dificultad.FACIL -> 3 * 60_000L
            Dificultad.MEDIO -> 6 * 60_000L
            Dificultad.DIFICIL -> 12 * 60_000L
        }
    }
}
