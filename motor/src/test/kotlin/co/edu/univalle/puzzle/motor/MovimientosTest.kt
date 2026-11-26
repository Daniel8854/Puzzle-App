package co.edu.univalle.puzzle.motor

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class MovimientosTest {
    // Tablero 3×3 con la vacía en la posición indicada; las demás piezas dan igual.
    private fun tableroConVaciaEn(vacia: Int): Tablero {
        val casillas = (1..8).toMutableList()
        casillas.add(vacia, Tablero.VACIA)
        return Tablero.desde(3, casillas)
    }

    @Test
    fun `vacia en esquina tiene dos piezas movibles`() {
        assertEquals(listOf(5, 7), tableroConVaciaEn(8).posicionesMovibles())
        assertEquals(listOf(1, 3), tableroConVaciaEn(0).posicionesMovibles())
        assertEquals(listOf(1, 5), tableroConVaciaEn(2).posicionesMovibles())
        assertEquals(listOf(3, 7), tableroConVaciaEn(6).posicionesMovibles())
    }

    @Test
    fun `vacia en borde tiene tres piezas movibles`() {
        assertEquals(listOf(0, 2, 4), tableroConVaciaEn(1).posicionesMovibles())
        assertEquals(listOf(0, 4, 6), tableroConVaciaEn(3).posicionesMovibles())
        assertEquals(listOf(2, 4, 8), tableroConVaciaEn(5).posicionesMovibles())
        assertEquals(listOf(4, 6, 8), tableroConVaciaEn(7).posicionesMovibles())
    }

    @Test
    fun `vacia en el centro tiene cuatro piezas movibles`() {
        assertEquals(listOf(1, 3, 5, 7), tableroConVaciaEn(4).posicionesMovibles())
    }

    @Test
    fun `mover una pieza adyacente la pasa a la casilla vacia`() {
        val inicial = Tablero.resuelto(3)
        val despues = inicial.mover(7)!!

        assertEquals(7, despues.posicionVacia)
        assertEquals(8, despues.piezaEn(8))
        assertFalse(despues.estaResuelto())
        // El tablero original no cambia (es inmutable)
        assertTrue(inicial.estaResuelto())
    }

    @Test
    fun `deshacer el movimiento vuelve al tablero resuelto`() {
        val despues = Tablero.resuelto(3).mover(5)!!
        assertEquals(Tablero.resuelto(3), despues.mover(8))
    }

    @Test
    fun `toques ilegales no cambian nada`() {
        val tablero = Tablero.resuelto(3) // vacía en la esquina inferior derecha
        assertNull(tablero.mover(0)) // lejos
        assertNull(tablero.mover(4)) // diagonal
        assertNull(tablero.mover(8)) // la propia vacía
        assertNull(tablero.mover(-1)) // fuera del tablero
        assertNull(tablero.mover(9)) // fuera del tablero
        assertTrue(tablero.estaResuelto())
    }

    @Test
    fun `no se puede saltar de una fila a la siguiente`() {
        // Vacía en la posición 3 (inicio de la fila 1): la posición 2 (fin de la fila 0)
        // está al lado en el arreglo, pero no en el tablero.
        val tablero = tableroConVaciaEn(3)
        assertFalse(tablero.esMovible(2))
    }

    @Test
    fun `funciona en tableros de 4x4 y 5x5`() {
        assertEquals(listOf(11, 14), Tablero.resuelto(4).posicionesMovibles())
        assertEquals(listOf(19, 23), Tablero.resuelto(5).posicionesMovibles())
    }
}
