package co.edu.univalle.puzzle.motor

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class MezclaTest {

    @Test
    fun `1000 mezclas tienen solucion y ninguna empieza resuelta (RF-09)`() {
        for (semilla in 0L until 1000L) {
            for (dificultad in Dificultad.entries) {
                val tablero = Mezcla.mezclar(dificultad, semilla)
                assertFalse(tablero.estaResuelto())
                assertTrue(Mezcla.tieneSolucion(tablero))
            }
        }
    }

    @Test
    fun `la misma semilla da la misma mezcla (RF-25)`() {
        assertEquals(Mezcla.mezclar(Dificultad.MEDIO, 42L), Mezcla.mezclar(Dificultad.MEDIO, 42L))
        assertNotEquals(Mezcla.mezclar(Dificultad.MEDIO, 42L), Mezcla.mezclar(Dificultad.MEDIO, 43L))
    }

    @Test
    fun `detecta un tablero sin solucion`() {
        // Intercambiar dos piezas del tablero resuelto lo vuelve imposible.
        val imposible = Tablero.desde(3, listOf(2, 1, 3, 4, 5, 6, 7, 8, 0))
        assertFalse(Mezcla.tieneSolucion(imposible))
        assertTrue(Mezcla.tieneSolucion(Tablero.resuelto(4)))
    }
}
