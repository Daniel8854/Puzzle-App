package co.edu.univalle.puzzle.dominio.duelo

import co.edu.univalle.puzzle.dominio.duelo.EstadoDuelo.CANCELADO
import co.edu.univalle.puzzle.dominio.duelo.EstadoDuelo.EN_CURSO
import co.edu.univalle.puzzle.dominio.duelo.EstadoDuelo.ESPERANDO
import co.edu.univalle.puzzle.dominio.duelo.EstadoDuelo.FINALIZADO
import co.edu.univalle.puzzle.dominio.duelo.EstadoDuelo.SALA
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class DueloTest {

    private fun duelo(estado: EstadoDuelo, actualizado: Long = 0) =
        Duelo("d", "r", "t", estado, 4, "img", 7, actualizado)

    private fun r(completado: Boolean, tiempo: Long = 0, movimientos: Int = 0, piezas: Int = 0) =
        ResultadoDuelo(completado, tiempo, movimientos, piezas)

    @Test
    fun `camino normal del duelo`() {
        assertTrue(ReglasDuelo.permitida(ESPERANDO, SALA, Actor.RETADO))
        assertTrue(ReglasDuelo.permitida(SALA, EN_CURSO, Actor.RETADOR))
        assertTrue(ReglasDuelo.permitida(EN_CURSO, FINALIZADO, Actor.SERVIDOR))
    }

    @Test
    fun `solo el retado acepta y solo el servidor finaliza`() {
        assertFalse(ReglasDuelo.permitida(ESPERANDO, SALA, Actor.RETADOR))
        assertFalse(ReglasDuelo.permitida(EN_CURSO, FINALIZADO, Actor.RETADOR))
        assertFalse(ReglasDuelo.permitida(EN_CURSO, FINALIZADO, Actor.RETADO))
    }

    @Test
    fun `rechazar, cancelar o vencer lleva a cancelado`() {
        assertTrue(ReglasDuelo.permitida(ESPERANDO, CANCELADO, Actor.RETADO))
        assertTrue(ReglasDuelo.permitida(ESPERANDO, CANCELADO, Actor.RETADOR))
        assertTrue(ReglasDuelo.permitida(SALA, CANCELADO, Actor.SERVIDOR))
    }

    @Test
    fun `los estados finales no cambian y no se salta ningun paso`() {
        for (a in EstadoDuelo.entries) for (actor in Actor.entries) {
            assertFalse(ReglasDuelo.permitida(FINALIZADO, a, actor))
            assertFalse(ReglasDuelo.permitida(CANCELADO, a, actor))
        }
        assertFalse(ReglasDuelo.permitida(ESPERANDO, EN_CURSO, Actor.RETADO))
        assertFalse(ReglasDuelo.permitida(ESPERANDO, FINALIZADO, Actor.SERVIDOR))
        assertFalse(ReglasDuelo.permitida(SALA, ESPERANDO, Actor.RETADO))
        assertFalse(ReglasDuelo.permitida(EN_CURSO, CANCELADO, Actor.RETADOR))
    }

    @Test
    fun `sin respuesta en 2 minutos o 30 segundos en la sala se vence`() {
        assertNull(ReglasDuelo.vencido(duelo(ESPERANDO), 119_999))
        assertEquals(MotivoDuelo.SIN_RESPUESTA, ReglasDuelo.vencido(duelo(ESPERANDO), 120_000))
        assertNull(ReglasDuelo.vencido(duelo(SALA, 1_000), 30_999))
        assertEquals(MotivoDuelo.SALA_VENCIDA, ReglasDuelo.vencido(duelo(SALA, 1_000), 31_000))
        assertNull(ReglasDuelo.vencido(duelo(EN_CURSO), 10_000_000))
    }

    @Test
    fun `quien completa gana y entre dos completos gana el menor tiempo (RF-30)`() {
        assertEquals(Actor.RETADO, ReglasDuelo.ganador(r(false, piezas = 14), r(true, 90_000)))
        assertEquals(Actor.RETADOR, ReglasDuelo.ganador(r(true, 60_000, 50), r(true, 61_000, 30)))
        assertEquals(Actor.RETADO, ReglasDuelo.ganador(r(true, 60_000, 50), r(true, 60_000, 49)))
    }

    @Test
    fun `si nadie completa gana quien tiene mas piezas, y si todo es igual es empate`() {
        assertEquals(Actor.RETADOR, ReglasDuelo.ganador(r(false, piezas = 10), r(false, piezas = 8)))
        assertNull(ReglasDuelo.ganador(r(false, piezas = 8), r(false, piezas = 8)))
        assertNull(ReglasDuelo.ganador(r(true, 60_000, 40), r(true, 60_000, 40)))
    }

    @Test
    fun `quien abandona pierde`() {
        assertEquals(Actor.RETADO, ReglasDuelo.ganadorPorAbandono(Actor.RETADOR))
    }

    @Test
    fun `las claves se leen de vuelta`() {
        for (e in EstadoDuelo.entries) assertEquals(e, EstadoDuelo.desde(e.clave))
        assertTrue(FINALIZADO.esFinal && CANCELADO.esFinal && !SALA.esFinal)
    }
}
