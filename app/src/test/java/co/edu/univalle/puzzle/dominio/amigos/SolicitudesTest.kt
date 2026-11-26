package co.edu.univalle.puzzle.dominio.amigos

import co.edu.univalle.puzzle.dominio.amigos.Solicitudes.Busqueda
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class SolicitudesTest {

    @Test
    fun `los tres casos invalidos (RF-40)`() {
        assertEquals(SolicitudInvalida.A_SI_MISMO, Solicitudes.validar("a", "a", false, null))
        assertEquals(SolicitudInvalida.YA_SON_AMIGOS, Solicitudes.validar("a", "b", true, null))
        assertEquals(SolicitudInvalida.YA_ENVIADA, Solicitudes.validar("a", "b", false, EstadoSolicitud.PENDIENTE))
    }

    @Test
    fun `se puede enviar la primera vez y despues de un rechazo`() {
        assertNull(Solicitudes.validar("a", "b", false, null))
        assertNull(Solicitudes.validar("a", "b", false, EstadoSolicitud.RECHAZADA))
    }

    @Test
    fun `el id sigue el formato de las reglas`() {
        assertEquals("a_b", Solicitudes.id("a", "b"))
    }

    @Test
    fun `un texto con forma de codigo busca el codigo exacto`() {
        assertEquals(Busqueda.PorCodigo("ABCD2345"), Solicitudes.interpretar(" abcd-2345 "))
    }

    @Test
    fun `cualquier otro texto busca por el inicio del nombre`() {
        assertEquals(Busqueda.PorNombre("lau"), Solicitudes.interpretar("Lau"))
        assertEquals(Busqueda.Vacia, Solicitudes.interpretar("a"))
    }
}
