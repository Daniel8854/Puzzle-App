package co.edu.univalle.puzzle.datos.fotos

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class CompresionTest {

    @Test
    fun `una foto grande queda con el lado mayor en 1080 sin deformarse (RNF-05)`() {
        assertEquals(1080 to 810, Compresion.tamanoFinal(4000, 3000))
        assertEquals(810 to 1080, Compresion.tamanoFinal(3000, 4000))
        assertEquals(1080 to 1080, Compresion.tamanoFinal(3024, 3024))
    }

    @Test
    fun `una foto pequena no se agranda`() {
        assertEquals(800 to 600, Compresion.tamanoFinal(800, 600))
    }

    @Test
    fun `el submuestreo nunca baja de 1080 px`() {
        assertEquals(2, Compresion.submuestreo(4000, 3000)) // 2000 px
        assertEquals(1, Compresion.submuestreo(2000, 1500))
        assertEquals(4, Compresion.submuestreo(8000, 6000))
        for (lado in listOf(1080, 2159, 4320, 9000)) {
            assertTrue(lado / Compresion.submuestreo(lado, lado) >= 1080)
        }
    }

    @Test
    fun `las calidades bajan de 90 a 40`() {
        assertEquals(listOf(90, 80, 70, 60, 50, 40), Compresion.calidades())
    }

    @Test
    fun `la orientacion EXIF se convierte en grados`() {
        assertEquals(90, Compresion.gradosPorExif(6))
        assertEquals(180, Compresion.gradosPorExif(3))
        assertEquals(270, Compresion.gradosPorExif(8))
        assertEquals(0, Compresion.gradosPorExif(1))
    }
}
