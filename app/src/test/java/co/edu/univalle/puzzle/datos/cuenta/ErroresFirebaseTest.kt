package co.edu.univalle.puzzle.datos.cuenta

import co.edu.univalle.puzzle.dominio.cuenta.ErrorCuenta
import org.junit.Assert.assertEquals
import org.junit.Test

class ErroresFirebaseTest {

    @Test
    fun `correo repetido y contrasena corta tienen su mensaje (RF-01)`() {
        assertEquals(ErrorCuenta.CORREO_EN_USO, errorDeCodigo("ERROR_EMAIL_ALREADY_IN_USE", enLogin = false))
        assertEquals(ErrorCuenta.CONTRASENA_DEBIL, errorDeCodigo("ERROR_WEAK_PASSWORD", enLogin = false))
        assertEquals(ErrorCuenta.CORREO_INVALIDO, errorDeCodigo("ERROR_INVALID_EMAIL", enLogin = false))
    }

    @Test
    fun `en el login todo error de credenciales da el mismo mensaje (RF-02)`() {
        for (codigo in listOf("ERROR_WRONG_PASSWORD", "ERROR_USER_NOT_FOUND", "ERROR_INVALID_CREDENTIAL",
            "ERROR_INVALID_EMAIL", "ERROR_DESCONOCIDO_X")) {
            assertEquals(ErrorCuenta.CREDENCIALES_INCORRECTAS, errorDeCodigo(codigo, enLogin = true))
        }
        assertEquals("Correo o contraseña incorrectos", ErrorCuenta.CREDENCIALES_INCORRECTAS.mensaje)
    }

    @Test
    fun `sin red se avisa que no hay conexion`() {
        assertEquals(ErrorCuenta.SIN_CONEXION, errorDeCodigo("ERROR_NETWORK_REQUEST_FAILED", enLogin = true))
    }
}
