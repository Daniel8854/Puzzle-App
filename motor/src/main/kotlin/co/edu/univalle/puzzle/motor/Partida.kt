package co.edu.univalle.puzzle.motor

/** Cómo terminó una partida. */
enum class FinPartida { COMPLETADA, TIEMPO_AGOTADO }

/** Resultado que se registra una sola vez al terminar (RF-12). */
data class ResultadoPartida(
    val fin: FinPartida,
    val movimientos: Int,
    val tiempoMs: Long,
    val piezasEnSuLugar: Int,
    val totalPiezas: Int
)

/**
 * Estado de una partida (RF-11, RF-12, RF-13). Es inmutable y no lee el reloj:
 * cada operación recibe el instante actual en milisegundos ([ahoraMs]), así se prueba sin esperar.
 *
 * - El tiempo solo corre mientras la partida no está en pausa ni terminada.
 * - Cada movimiento válido suma 1; un toque ilegal no cambia nada.
 * - Al colocar la última pieza termina y guarda el [resultado] una sola vez.
 * - Con [limiteMs] (Contrarreloj, RF-20) termina como [FinPartida.TIEMPO_AGOTADO] al llegar a 0.
 */
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

    /** Piezas en su lugar sobre el total, para la barra de progreso (RF-13). */
    val piezasEnSuLugar: Int get() = tablero.piezasEnSuLugar()
    val totalPiezas: Int get() = tablero.totalPiezas
    val progreso: Float get() = piezasEnSuLugar.toFloat() / totalPiezas

    /** Tiempo jugado hasta [ahoraMs], sin contar las pausas. */
    fun tiempoMs(ahoraMs: Long): Long {
        if (resultado != null) return resultado.tiempoMs
        val corriendo = desdeMs?.let { (ahoraMs - it).coerceAtLeast(0) } ?: 0
        val total = acumuladoMs + corriendo
        return limiteMs?.let { minOf(total, it) } ?: total
    }

    /** Tiempo que queda en Contrarreloj, o `null` en Clásico. */
    fun restanteMs(ahoraMs: Long): Long? = limiteMs?.let { (it - tiempoMs(ahoraMs)).coerceAtLeast(0) }

    fun pausar(ahoraMs: Long): Partida =
        if (desdeMs == null || terminada) this
        else copy(acumuladoMs = tiempoMs(ahoraMs), desdeMs = null)

    fun reanudar(ahoraMs: Long): Partida =
        if (desdeMs != null || terminada) this else copy(desdeMs = ahoraMs)

    /** Revisa si se acabó el tiempo de Contrarreloj; si no, devuelve la misma partida. */
    fun revisarTiempo(ahoraMs: Long): Partida {
        if (terminada || limiteMs == null || tiempoMs(ahoraMs) < limiteMs) return this
        return terminar(FinPartida.TIEMPO_AGOTADO, limiteMs)
    }

    /** Mueve la pieza de [posicion] si es legal. En pausa o terminada no hace nada. */
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
        /** Empieza una partida ya corriendo desde [ahoraMs]. */
        fun nueva(tablero: Tablero, ahoraMs: Long, limiteMs: Long? = null) =
            Partida(tablero = tablero, limiteMs = limiteMs, desdeMs = ahoraMs)

        /** Límites de Contrarreloj por dificultad (RF-20): 3, 6 y 12 minutos. */
        fun limiteContrarreloj(dificultad: Dificultad): Long = when (dificultad) {
            Dificultad.FACIL -> 3 * 60_000L
            Dificultad.MEDIO -> 6 * 60_000L
            Dificultad.DIFICIL -> 12 * 60_000L
        }
    }
}
