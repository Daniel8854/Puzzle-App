package co.edu.univalle.puzzle.motor

import org.junit.Assert.assertEquals
import org.junit.Assert.assertThrows
import org.junit.Assert.assertTrue
import org.junit.Test

class RecorteTest {

    @Test
    fun `cada dificultad divide la imagen en piezas iguales`() {
        for (dificultad in Dificultad.entries) {
            val n = dificultad.tamano
            val recortes = Recortes.todos(n, 1080)
            assertEquals(n * n - 1, recortes.size)
            assertTrue(recortes.all { it.lado == 1080 / n })
        }
    }

    @Test
    fun `las piezas no se salen de la imagen ni se superponen`() {
        val recortes = Recortes.todos(4, 1080)
        assertTrue(recortes.all { it.izquierda + it.lado <= 1080 && it.arriba + it.lado <= 1080 })
        assertEquals(recortes.size, recortes.map { it.izquierda to it.arriba }.toSet().size)
    }

    @Test
    fun `la pieza 1 es la esquina superior izquierda y la ultima va antes del hueco`() {
        assertEquals(Recorte(0, 0, 360), Recortes.de(1, 3, 1080))
        assertEquals(Recorte(360, 720, 360), Recortes.de(8, 3, 1080))
    }

    @Test
    fun `una imagen que no es multiplo descarta el sobrante`() {
        assertEquals(Recorte(1078 / 5 * 4, 0, 1078 / 5), Recortes.de(5, 5, 1078))
    }

    @Test
    fun `pieza invalida o imagen diminuta fallan`() {
        assertThrows(IllegalArgumentException::class.java) { Recortes.de(0, 3, 1080) }
        assertThrows(IllegalArgumentException::class.java) { Recortes.de(9, 3, 1080) }
        assertThrows(IllegalArgumentException::class.java) { Recortes.de(1, 5, 4) }
    }
}
