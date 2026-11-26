package co.edu.univalle.puzzle.dominio.duelo

/**
 * Estados del duelo VS (T-75). Así se guardan en «duelos/{id}.estado».
 *
 *   ESPERANDO ──aceptar──▶ SALA ──los dos listos──▶ EN_CURSO ──▶ FINALIZADO
 *       │                    │                         │
 *       └──rechazar/cancelar/2 min──▶ CANCELADO ◀──30 s─┘ (sala)
 *
 * FINALIZADO y CANCELADO son finales. El paso a FINALIZADO lo define la tarjeta que valida los
 * resultados; mientras tanto las reglas de Firestore no se lo permiten a ningún jugador (RNF-16).
 */
enum class EstadoDuelo(val clave: String) {
    ESPERANDO("esperando"),
    SALA("sala"),
    EN_CURSO("en_curso"),
    FINALIZADO("finalizado"),
    CANCELADO("cancelado");

    val esFinal: Boolean get() = this == FINALIZADO || this == CANCELADO

    companion object {
        fun desde(clave: String?): EstadoDuelo? = entries.firstOrNull { it.clave == clave }
    }
}

/** Por qué terminó o se canceló el duelo. */
enum class MotivoDuelo(val clave: String) {
    VICTORIA("victoria"),
    EMPATE("empate"),
    ABANDONO("abandono"),
    RECHAZADO("rechazado"),
    CANCELADO_POR_RETADOR("cancelado_por_retador"),
    SIN_RESPUESTA("sin_respuesta"),
    SALA_VENCIDA("sala_vencida"),
    SIN_SENAL("sin_senal")
}

/** Quién hace el cambio de estado. */
enum class Actor { RETADOR, RETADO, SERVIDOR }

/** Resultado de un jugador en el duelo. */
data class ResultadoDuelo(
    val completado: Boolean,
    val tiempoMs: Long,
    val movimientos: Int,
    val piezasEnSuLugar: Int
)

/**
 * Duelo entre dos amigos (RF-25). Los dos juegan la misma imagen, la misma dificultad y la misma
 * mezcla ([semilla]), así el tablero es idéntico.
 */
data class Duelo(
    val id: String,
    val retador: String,
    val retado: String,
    val estado: EstadoDuelo,
    val dificultad: Int,
    val imagenId: String,
    val semilla: Long,
    val actualizadoEnMs: Long,
    val motivo: MotivoDuelo? = null,
    val ganador: String? = null
)

object ReglasDuelo {

    /** Tiempo que tiene el retado para responder el desafío. */
    const val ESPERA_RESPUESTA_MS = 2 * 60_000L

    /** Tiempo máximo en la sala antes de empezar. */
    const val ESPERA_SALA_MS = 30_000L

    /** Transiciones permitidas y quién puede hacer cada una. Lo demás se rechaza. */
    fun permitida(de: EstadoDuelo, a: EstadoDuelo, actor: Actor): Boolean = when (de to a) {
        EstadoDuelo.ESPERANDO to EstadoDuelo.SALA -> actor == Actor.RETADO
        EstadoDuelo.ESPERANDO to EstadoDuelo.CANCELADO -> true
        EstadoDuelo.SALA to EstadoDuelo.EN_CURSO -> actor != Actor.SERVIDOR
        EstadoDuelo.SALA to EstadoDuelo.CANCELADO -> true
        EstadoDuelo.EN_CURSO to EstadoDuelo.FINALIZADO -> actor == Actor.SERVIDOR
        else -> false
    }

    /**
     * Motivo para cancelar un duelo que se quedó esperando demasiado, o null si sigue a tiempo.
     * Lo aplica cualquiera de los dos jugadores; las reglas de Firestore revisan los mismos tiempos.
     */
    fun vencido(duelo: Duelo, ahoraMs: Long): MotivoDuelo? {
        val transcurrido = ahoraMs - duelo.actualizadoEnMs
        return when {
            duelo.estado == EstadoDuelo.ESPERANDO && transcurrido >= ESPERA_RESPUESTA_MS -> MotivoDuelo.SIN_RESPUESTA
            duelo.estado == EstadoDuelo.SALA && transcurrido >= ESPERA_SALA_MS -> MotivoDuelo.SALA_VENCIDA
            else -> null
        }
    }

    /**
     * Quién gana (RF-30):
     * 1. Quien completa gana a quien no completa.
     * 2. Si los dos completan, gana el menor tiempo; con el mismo tiempo, menos movimientos.
     * 3. Si ninguno completa, gana quien tiene más piezas en su lugar.
     * Si todo es igual es empate y devuelve null.
     */
    fun ganador(retador: ResultadoDuelo, retado: ResultadoDuelo): Actor? {
        val comparacion = compareBy<ResultadoDuelo>(
            { if (it.completado) 0 else 1 },
            { if (it.completado) it.tiempoMs else -it.piezasEnSuLugar.toLong() },
            { if (it.completado) it.movimientos else 0 }
        ).compare(retador, retado)
        return when {
            comparacion < 0 -> Actor.RETADOR
            comparacion > 0 -> Actor.RETADO
            else -> null
        }
    }

    /** Si un jugador abandona, gana el otro. */
    fun ganadorPorAbandono(quienAbandona: Actor): Actor =
        if (quienAbandona == Actor.RETADOR) Actor.RETADO else Actor.RETADOR
}
