package co.edu.univalle.puzzle.ui.pantallas.modos

import co.edu.univalle.puzzle.motor.Dificultad
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class OpcionesJuegoTest {

    @Test
    fun `clasico no tiene limite y contrarreloj usa 3, 6 y 12 minutos (RF-20)`() {
        assertNull(ModoJuego.CLASICO.limiteMs(Dificultad.MEDIO))
        assertEquals(180_000L, ModoJuego.CONTRARRELOJ.limiteMs(Dificultad.FACIL))
        assertEquals(360_000L, ModoJuego.CONTRARRELOJ.limiteMs(Dificultad.MEDIO))
        assertEquals(720_000L, ModoJuego.CONTRARRELOJ.limiteMs(Dificultad.DIFICIL))
    }

    @Test
    fun `el modo y la dificultad se recuperan desde la ruta`() {
        for (m in ModoJuego.entries) assertEquals(m, ModoJuego.desde(m.clave))
        assertEquals(ModoJuego.CLASICO, ModoJuego.desde("otro"))
        assertEquals(Dificultad.DIFICIL, dificultadDe(5))
        assertEquals(Dificultad.FACIL, dificultadDe(9))
    }

    @Test
    fun `sin imagen elegida no se puede jugar`() {
        assertFalse(Eleccion().lista)
        assertTrue(Eleccion(imagenId = "incluida-mar").lista)
    }
}
