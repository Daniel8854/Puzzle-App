package co.edu.univalle.puzzle.notificaciones

import co.edu.univalle.puzzle.datos.local.Preferencias
import co.edu.univalle.puzzle.ui.navegacion.Rutas
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class NotificacionesTest {

    @Test
    fun `una notificacion solo abre pantallas conocidas`() {
        assertEquals("duelo", Notificaciones.rutaSegura("duelo"))
        assertEquals("mis_amigos", Notificaciones.rutaSegura("mis_amigos"))
        assertNull(Notificaciones.rutaSegura("recortar/1"))
        assertNull(Notificaciones.rutaSegura("https://otro.sitio"))
        assertNull(Notificaciones.rutaSegura(null))
    }

    @Test
    fun `las rutas permitidas existen en la navegacion`() {
        val rutas = setOf(Rutas.MIS_AMIGOS, Rutas.DUELO, Rutas.RANKING, Rutas.INICIO)
        assertEquals(rutas, Notificaciones.rutasPermitidas)
    }

    @Test
    fun `se respeta lo que el jugador desactivo (RF-49)`() {
        val sinDesafios = Preferencias(notifDesafios = false)
        assertFalse(Notificaciones.quiereAviso(sinDesafios, TipoAviso.DESAFIOS))
        assertTrue(Notificaciones.quiereAviso(sinDesafios, TipoAviso.SOLICITUDES))
        assertTrue(Notificaciones.quiereAviso(sinDesafios, TipoAviso.RESULTADOS))
    }

    @Test
    fun `cada aviso dice quien y abre su pantalla`() {
        val solicitud = Notificaciones.armarAviso(TipoAviso.SOLICITUDES, "Ana")
        assertEquals("Ana quiere ser tu amigo", solicitud.texto)
        assertEquals(Rutas.MIS_AMIGOS, solicitud.ruta)
        assertEquals(Rutas.DUELO, Notificaciones.armarAviso(TipoAviso.DESAFIOS, "Ana").ruta)
        assertEquals("Le ganaste a Ana", Notificaciones.armarAviso(TipoAviso.RESULTADOS, "Ana", ResultadoAviso.GANE).texto)
        assertEquals("Ana ganó el duelo", Notificaciones.armarAviso(TipoAviso.RESULTADOS, "Ana", ResultadoAviso.PERDI).texto)
        assertEquals("Empate con Ana", Notificaciones.armarAviso(TipoAviso.RESULTADOS, "Ana", ResultadoAviso.EMPATE).texto)
    }

    @Test
    fun `no se repite un aviso ya mostrado`() {
        assertTrue(Notificaciones.esNuevo(2_000, 1_000))
        assertFalse(Notificaciones.esNuevo(1_000, 1_000))
        assertFalse(Notificaciones.esNuevo(500, 1_000))
        assertFalse(Notificaciones.esNuevo(null, 0))
    }
}
