package co.edu.univalle.puzzle.datos.local

import androidx.datastore.preferences.core.mutablePreferencesOf
import co.edu.univalle.puzzle.motor.Mezcla
import co.edu.univalle.puzzle.motor.Partida
import co.edu.univalle.puzzle.motor.posicionesMovibles
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class DatosLocalesTest {

    @After fun limpiar() = SesionLocal.usarInvitado()

    @Test
    fun `la sesion empieza como invitado y cambia a la cuenta`() {
        assertTrue(SesionLocal.esInvitado)
        SesionLocal.usarCuenta("uid-1")
        assertEquals("uid-1", SesionLocal.cuentaId.value)
        assertFalse(SesionLocal.esInvitado)
    }

    @Test
    fun `la partida en curso se guarda y se recupera igual (RNF-08)`() {
        val partida = Partida.nueva(Mezcla.mezclar(4, 9L), ahoraMs = 0, limiteMs = 360_000)
            .mover(Mezcla.mezclar(4, 9L).posicionesMovibles().first(), 1_000)
        val guardada = PartidaGuardada.desde("uid-1", "contrarreloj", "img-7", partida, ahoraMs = 30_000)
        val recuperada = guardada.aPartida()
        assertEquals(partida.tablero, recuperada.tablero)
        assertEquals(partida.movimientos, recuperada.movimientos)
        assertEquals(30_000, recuperada.tiempoMs(999_999)) // vuelve en pausa
        assertEquals(330_000L, recuperada.restanteMs(999_999))
    }

    @Test
    fun `las preferencias de una cuenta no afectan a la otra`() {
        val guardado = mutablePreferencesOf(Opcion.SONIDOS.clave("uid-1") to false)
        assertFalse(leerPreferencias(guardado, "uid-1").sonidos)
        assertTrue(leerPreferencias(guardado, "uid-2").sonidos)
        assertTrue(leerPreferencias(guardado, SesionLocal.INVITADO).sonidos)
    }

    @Test
    fun `sin nada guardado se usan los valores por defecto`() {
        assertEquals(Preferencias(), leerPreferencias(mutablePreferencesOf(), "uid-1"))
    }
}
