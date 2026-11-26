package co.edu.univalle.puzzle.ui.pantallas.fotos

import org.junit.Assert.assertEquals
import org.junit.Test

class PermisoCamaraTest {

    @Test
    fun `antes de pedirlo se explica para que sirve (RF-33)`() {
        assertEquals(PasoPermiso.EXPLICAR, pasoPermiso(concedido = false, yaSePidio = false, debeExplicar = false))
    }

    @Test
    fun `concedido sigue el flujo`() {
        assertEquals(PasoPermiso.CONCEDIDO, pasoPermiso(concedido = true, yaSePidio = true, debeExplicar = false))
        assertEquals(PasoPermiso.CONCEDIDO, pasoPermiso(concedido = true, yaSePidio = false, debeExplicar = false))
    }

    @Test
    fun `negado una vez permite intentar de nuevo`() {
        assertEquals(PasoPermiso.NEGADO, pasoPermiso(concedido = false, yaSePidio = true, debeExplicar = true))
    }

    @Test
    fun `negado para siempre ofrece abrir los ajustes`() {
        assertEquals(PasoPermiso.IR_A_AJUSTES, pasoPermiso(concedido = false, yaSePidio = true, debeExplicar = false))
    }
}
