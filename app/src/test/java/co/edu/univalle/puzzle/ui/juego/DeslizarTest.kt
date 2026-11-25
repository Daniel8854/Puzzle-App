package co.edu.univalle.puzzle.ui.juego

import co.edu.univalle.puzzle.motor.Tablero
import co.edu.univalle.puzzle.motor.mover
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Test

class DeslizarTest {

    private val resuelto = Tablero.resuelto(3) // hueco en la esquina inferior derecha (posición 8)

    @Test
    fun `deslizar mueve la pieza que esta al otro lado del hueco`() {
        assertEquals(7, resuelto.piezaParaDeslizar(Direccion.DERECHA))
        assertEquals(5, resuelto.piezaParaDeslizar(Direccion.ABAJO))
    }

    @Test
    fun `en la esquina no hay pieza al otro lado`() {
        assertNull(resuelto.piezaParaDeslizar(Direccion.IZQUIERDA))
        assertNull(resuelto.piezaParaDeslizar(Direccion.ARRIBA))
    }

    @Test
    fun `la pieza elegida por deslizamiento siempre es un movimiento legal`() {
        val centro = resuelto.mover(7)!!.mover(4)!! // hueco en el centro
        for (direccion in Direccion.entries) {
            val pieza = centro.piezaParaDeslizar(direccion)!!
            assertNotNull(centro.mover(pieza))
        }
    }
}
