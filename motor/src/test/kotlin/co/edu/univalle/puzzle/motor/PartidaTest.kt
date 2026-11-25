package co.edu.univalle.puzzle.motor

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertSame
import org.junit.Assert.assertTrue
import org.junit.Test

class PartidaTest {

    /** Tablero a un movimiento de resolverse: la pieza 8 está en la posición 8. */
    private val casiResuelto = Tablero.resuelto(3).mover(7)!!

    @Test
    fun `el tiempo corre y la pausa no suma (RF-11)`() {
        var p = Partida.nueva(Tablero.resuelto(3).mover(7)!!, ahoraMs = 0)
        assertEquals(5_000, p.tiempoMs(5_000))
        p = p.pausar(5_000)
        assertTrue(p.enPausa)
        assertEquals(5_000, p.tiempoMs(60_000))
        p = p.reanudar(60_000)
        assertEquals(7_000, p.tiempoMs(62_000))
    }

    @Test
    fun `cada movimiento valido suma uno y uno ilegal no cambia nada (RF-11)`() {
        val inicio = Partida.nueva(Mezcla.mezclar(3, 1L), 0)
        val legal = inicio.tablero.posicionesMovibles().first()
        val ilegal = (0 until 9).first { !inicio.tablero.esMovible(it) && it != inicio.tablero.posicionVacia }
        assertEquals(1, inicio.mover(legal, 100).movimientos)
        assertSame(inicio, inicio.mover(ilegal, 100))
    }

    @Test
    fun `en pausa no se puede mover`() {
        val p = Partida.nueva(casiResuelto, 0).pausar(1_000)
        assertEquals(0, p.mover(8, 2_000).movimientos)
    }

    @Test
    fun `el progreso cuenta las piezas en su lugar (RF-13)`() {
        val p = Partida.nueva(casiResuelto, 0)
        assertEquals(7, p.piezasEnSuLugar)
        assertEquals(8, p.totalPiezas)
        assertEquals(7 / 8f, p.progreso)
    }

    @Test
    fun `al colocar la ultima pieza termina y se registra una sola vez (RF-12)`() {
        val fin = Partida.nueva(casiResuelto, 0).mover(8, 12_000)
        assertTrue(fin.terminada)
        val resultado = fin.resultado!!
        assertEquals(FinPartida.COMPLETADA, resultado.fin)
        assertEquals(1, resultado.movimientos)
        assertEquals(12_000, resultado.tiempoMs)
        // Después de terminar, el tablero queda bloqueado y el reloj detenido.
        assertSame(fin, fin.mover(7, 20_000))
        assertEquals(12_000, fin.tiempoMs(99_000))
    }

    @Test
    fun `contrarreloj termina al agotarse el tiempo (RF-20)`() {
        val limite = Partida.limiteContrarreloj(Dificultad.FACIL)
        val p = Partida.nueva(Mezcla.mezclar(3, 2L), 0, limite)
        assertEquals(limite - 1_000, p.restanteMs(1_000))
        val agotada = p.revisarTiempo(limite)
        assertEquals(FinPartida.TIEMPO_AGOTADO, agotada.resultado?.fin)
        assertEquals(0L, agotada.restanteMs(limite + 5_000))
    }

    @Test
    fun `clasico no tiene limite`() {
        val p = Partida.nueva(Mezcla.mezclar(3, 3L), 0)
        assertNull(p.restanteMs(10_000_000))
        assertFalse(p.revisarTiempo(10_000_000).terminada)
    }

    @Test
    fun `limites de contrarreloj por dificultad`() {
        assertEquals(180_000L, Partida.limiteContrarreloj(Dificultad.FACIL))
        assertEquals(360_000L, Partida.limiteContrarreloj(Dificultad.MEDIO))
        assertEquals(720_000L, Partida.limiteContrarreloj(Dificultad.DIFICIL))
    }
}
