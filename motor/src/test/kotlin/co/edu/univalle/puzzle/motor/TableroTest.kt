package co.edu.univalle.puzzle.motor

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotEquals
import org.junit.Assert.assertThrows
import org.junit.Assert.assertTrue
import org.junit.Test

class TableroTest {

    @Test
    fun `cada dificultad crea un tablero con N al cuadrado menos uno piezas`() {
        assertEquals(8, Tablero.resuelto(Dificultad.FACIL).totalPiezas)
        assertEquals(15, Tablero.resuelto(Dificultad.MEDIO).totalPiezas)
        assertEquals(24, Tablero.resuelto(Dificultad.DIFICIL).totalPiezas)
    }

    @Test
    fun `el tablero resuelto tiene las piezas en orden y la vacia al final`() {
        val tablero = Tablero.resuelto(3)
        assertEquals(listOf(1, 2, 3, 4, 5, 6, 7, 8, 0), tablero.comoLista())
        assertEquals(8, tablero.posicionVacia)
        assertTrue(tablero.estaResuelto())
        assertEquals(8, tablero.piezasEnSuLugar())
    }

    @Test
    fun `filas columnas e indices se corresponden`() {
        val tablero = Tablero.resuelto(4)
        assertEquals(6, tablero.indiceDe(1, 2))
        assertEquals(1, tablero.filaDe(6))
        assertEquals(2, tablero.columnaDe(6))
        assertEquals(7, tablero.piezaEn(1, 2))
    }

    @Test
    fun `un tablero desordenado no esta resuelto`() {
        val tablero = Tablero.desde(3, listOf(1, 2, 3, 4, 5, 6, 7, 0, 8))
        assertFalse(tablero.estaResuelto())
        assertEquals(7, tablero.posicionVacia)
        assertEquals(7, tablero.piezasEnSuLugar())
    }

    @Test
    fun `desde rechaza casillas repetidas, faltantes o de otro tamano`() {
        assertThrows(IllegalArgumentException::class.java) {
            Tablero.desde(3, listOf(1, 1, 3, 4, 5, 6, 7, 8, 0))
        }
        assertThrows(IllegalArgumentException::class.java) {
            Tablero.desde(3, listOf(1, 2, 3, 4, 5, 6, 7, 8))
        }
        assertThrows(IllegalArgumentException::class.java) {
            Tablero.desde(3, listOf(1, 2, 3, 4, 5, 6, 7, 8, 9))
        }
        assertThrows(IllegalArgumentException::class.java) { Tablero.resuelto(1) }
    }

    @Test
    fun `consultar fuera del tablero lanza error`() {
        val tablero = Tablero.resuelto(3)
        assertThrows(IllegalArgumentException::class.java) { tablero.piezaEn(9) }
        assertThrows(IllegalArgumentException::class.java) { tablero.piezaEn(3, 0) }
    }

    @Test
    fun `dos tableros con las mismas casillas son iguales`() {
        assertEquals(Tablero.resuelto(3), Tablero.desde(3, listOf(1, 2, 3, 4, 5, 6, 7, 8, 0)))
        assertEquals(Tablero.resuelto(3).hashCode(), Tablero.resuelto(3).hashCode())
        assertNotEquals(Tablero.resuelto(3), Tablero.resuelto(4))
    }
}
