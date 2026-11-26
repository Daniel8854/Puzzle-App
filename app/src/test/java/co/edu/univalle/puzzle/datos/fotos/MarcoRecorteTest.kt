package co.edu.univalle.puzzle.datos.fotos

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class MarcoRecorteTest {

    private fun dentro(m: MarcoRecorte): Boolean {
        val r = m.cuadrado()
        return r.izquierda >= 0 && r.arriba >= 0 && r.izquierda + r.lado <= m.ancho && r.arriba + r.lado <= m.alto
    }

    @Test
    fun `al empezar toma el cuadrado mas grande centrado (RF-35)`() {
        assertEquals(Rect(135, 0, 810), MarcoRecorte(1080, 810).cuadrado())
        assertEquals(Rect(0, 135, 810), MarcoRecorte(810, 1080).cuadrado())
    }

    @Test
    fun `moverlo nunca lo saca de la imagen`() {
        val m = MarcoRecorte(1080, 810).mover(10_000f, -10_000f)
        assertEquals(Rect(270, 0, 810), m.cuadrado())
        assertTrue(dentro(MarcoRecorte(1080, 810).mover(-99f, 99f)))
    }

    @Test
    fun `acercar reduce el lado y respeta los limites de zoom`() {
        val m = MarcoRecorte(1000, 1000).ampliar(2f)
        assertEquals(500, m.cuadrado().lado)
        assertEquals(250, MarcoRecorte(1000, 1000).ampliar(100f).cuadrado().lado) // máximo 4×
        assertEquals(1000, MarcoRecorte(1000, 1000).ampliar(0.1f).cuadrado().lado) // mínimo 1×
    }

    @Test
    fun `siempre es cuadrado y queda dentro con cualquier combinacion`() {
        var m = MarcoRecorte(1080, 607)
        for (i in 0 until 200) {
            m = m.ampliar(if (i % 3 == 0) 1.3f else 0.9f).mover((i * 37 % 200 - 100).toFloat(), (i * 53 % 160 - 80).toFloat())
            assertTrue(dentro(m))
        }
    }
}
