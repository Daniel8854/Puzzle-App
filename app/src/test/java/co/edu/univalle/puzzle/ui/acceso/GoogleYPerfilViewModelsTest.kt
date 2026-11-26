package co.edu.univalle.puzzle.ui.acceso

import co.edu.univalle.puzzle.datos.cuenta.RespuestaGoogle
import co.edu.univalle.puzzle.dominio.cuenta.ErrorCuenta
import co.edu.univalle.puzzle.dominio.cuenta.ResultadoCuenta
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

/** T-16: entrar con Google desde el registro y completar el perfil la primera vez. */
@OptIn(ExperimentalCoroutinesApi::class)
class GoogleYPerfilViewModelsTest {

    private val despachador = StandardTestDispatcher()

    @Before fun antes() = Dispatchers.setMain(despachador)
    @After fun despues() = Dispatchers.resetMain()

    // ---------- Registro con Google ----------

    @Test
    fun `registro con Google manda el token y marca registrado`() = runTest(despachador) {
        val servicio = ServicioCuentaFalso()
        val vm = RegistroViewModel(servicio)
        vm.entrarConGoogle("token-123")
        assertTrue(vm.estado.value.cargando)
        advanceUntilIdle()
        assertEquals("token-123", servicio.ultimoTokenGoogle)
        assertTrue(vm.estado.value.registrado)
        assertFalse(vm.estado.value.cargando)
    }

    @Test
    fun `si Google falla muestra el error general`() = runTest(despachador) {
        val vm = RegistroViewModel(ServicioCuentaFalso(respuesta = ResultadoCuenta.Error(ErrorCuenta.SIN_CONEXION)))
        vm.entrarConGoogle("token")
        advanceUntilIdle()
        assertFalse(vm.estado.value.registrado)
        assertEquals(ErrorCuenta.SIN_CONEXION.mensaje, vm.estado.value.errorGeneral)
        vm.mostrarError("No hay cuentas de Google")
        assertEquals("No hay cuentas de Google", vm.estado.value.errorGeneral)
    }

    // ---------- Respuesta del selector de Google (la maneja el ViewModel) ----------

    @Test
    fun `login con el token del selector entra`() = runTest(despachador) {
        val servicio = ServicioCuentaFalso()
        val vm = LoginViewModel(servicio)
        vm.continuarConGoogle { RespuestaGoogle.Token("token-abc") }
        advanceUntilIdle()
        assertEquals("token-abc", servicio.ultimoTokenGoogle)
        assertTrue(vm.estado.value.entro)
    }

    @Test
    fun `si se cierra el selector no pasa nada`() = runTest(despachador) {
        val vm = LoginViewModel(ServicioCuentaFalso())
        vm.continuarConGoogle { RespuestaGoogle.Cancelado }
        advanceUntilIdle()
        assertFalse(vm.estado.value.entro)
        assertNull(vm.estado.value.error)
    }

    @Test
    fun `si el selector falla el registro muestra el mensaje`() = runTest(despachador) {
        val vm = RegistroViewModel(ServicioCuentaFalso())
        vm.continuarConGoogle { RespuestaGoogle.Falla("No hay cuentas de Google en este teléfono") }
        advanceUntilIdle()
        assertFalse(vm.estado.value.registrado)
        assertEquals("No hay cuentas de Google en este teléfono", vm.estado.value.errorGeneral)
    }

    // ---------- Completar perfil ----------

    private class PerfilFalso(var respuesta: ResultadoCuenta = ResultadoCuenta.Exito) : GuardarPerfil {
        var guardado: List<Any>? = null
        override suspend fun guardar(nombre: String, avatar: Int, mayorDeEdad: Boolean, aceptoPrivacidad: Boolean): ResultadoCuenta {
            guardado = listOf(nombre, avatar, mayorDeEdad, aceptoPrivacidad)
            return respuesta
        }
    }

    private fun CompletarPerfilViewModel.llenar(nombre: String) {
        cambiarNombre(nombre)
        elegirAvatar(2)
        cambiarMayorDeEdad(true)
        cambiarAceptoPrivacidad(true)
    }

    @Test
    fun `sin las dos casillas no se puede continuar`() = runTest(despachador) {
        val perfil = PerfilFalso()
        val vm = CompletarPerfilViewModel(perfil)
        vm.cambiarNombre("Jugador1")
        vm.cambiarMayorDeEdad(true)
        assertFalse(vm.estado.value.puedeContinuar)
        vm.continuar()
        advanceUntilIdle()
        assertNull(perfil.guardado)
    }

    @Test
    fun `nombre invalido muestra el error y no guarda`() = runTest(despachador) {
        val perfil = PerfilFalso()
        val vm = CompletarPerfilViewModel(perfil)
        vm.llenar("ab")
        vm.continuar()
        advanceUntilIdle()
        assertNotNull(vm.estado.value.error)
        assertNull(perfil.guardado)
    }

    @Test
    fun `perfil valido se guarda sin espacios y queda listo`() = runTest(despachador) {
        val perfil = PerfilFalso()
        val vm = CompletarPerfilViewModel(perfil)
        vm.llenar("  Jugador1  ")
        vm.continuar()
        advanceUntilIdle()
        assertEquals(listOf("Jugador1", 2, true, true), perfil.guardado)
        assertTrue(vm.estado.value.listo)
    }

    @Test
    fun `si el nombre ya existe muestra el error del servicio`() = runTest(despachador) {
        val vm = CompletarPerfilViewModel(PerfilFalso(ResultadoCuenta.Error(ErrorCuenta.NOMBRE_EN_USO)))
        vm.llenar("Jugador1")
        vm.continuar()
        advanceUntilIdle()
        assertFalse(vm.estado.value.listo)
        assertEquals(ErrorCuenta.NOMBRE_EN_USO.mensaje, vm.estado.value.error)
    }
}
