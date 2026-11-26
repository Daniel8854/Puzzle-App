package co.edu.univalle.puzzle.datos.progreso

import co.edu.univalle.puzzle.datos.local.SesionLocal
import co.edu.univalle.puzzle.motor.FinPartida
import co.edu.univalle.puzzle.motor.ResultadoPartida
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class ProgresoInvitadoTest {

    @After fun limpiar() = SesionLocal.usarInvitado()

    @Test
    fun `el progreso del invitado se guarda con su id local (RF-03)`() {
        val fila = resultadoLocal(SesionLocal.INVITADO, "clasico", 3,
            ResultadoPartida(FinPartida.COMPLETADA, 42, 90_000, 8, 8), ahoraMs = 1_000)
        assertEquals("invitado", fila.cuentaId)
        assertTrue(fila.completada)
        assertEquals(42, fila.movimientos)
    }

    @Test
    fun `tiempo agotado se guarda como no completada`() {
        val fila = resultadoLocal("uid", "contrarreloj", 4,
            ResultadoPartida(FinPartida.TIEMPO_AGOTADO, 10, 360_000, 9, 15), ahoraMs = 1)
        assertFalse(fila.completada)
    }

    @Test
    fun `el invitado esta marcado como tal y una cuenta no`() {
        SesionLocal.usarInvitado()
        assertTrue(SesionLocal.esInvitado)
        SesionLocal.usarCuenta("uid-9")
        assertFalse(SesionLocal.esInvitado)
    }
}
