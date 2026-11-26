package co.edu.univalle.puzzle.ui.pantallas.amigos

import co.edu.univalle.puzzle.datos.amigos.SolicitudRecibida
import co.edu.univalle.puzzle.dominio.amigos.JugadorEncontrado
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class MisAmigosViewModelTest {

    private val despachador = UnconfinedTestDispatcher()

    @Before fun antes() = Dispatchers.setMain(despachador)
    @After fun despues() = Dispatchers.resetMain()

    private val ana = JugadorEncontrado("uid-ana", "Ana", 1)
    private val beto = JugadorEncontrado("uid-beto", "Beto", 2)
    private val deAna = SolicitudRecibida("ana_yo", ana)
    private val deBeto = SolicitudRecibida("beto_yo", beto)

    private fun fuenteConSolicitudes() = FuenteAmigosFalsa().apply { pendientes.value = listOf(deAna, deBeto) }

    @Test
    fun `muestra las solicitudes pendientes y los amigos`() = runTest(despachador) {
        val fuente = fuenteConSolicitudes()
        val vm = MisAmigosViewModel(fuente)
        assertEquals(listOf(deAna, deBeto), vm.estado.value.solicitudes)
        assertTrue(vm.estado.value.amigos.isEmpty())
    }

    @Test
    fun `aceptar la quita de pendientes, suma el amigo y avisa (RF-41)`() = runTest(despachador) {
        val vm = MisAmigosViewModel(fuenteConSolicitudes())
        vm.aceptar(deAna)
        assertEquals(listOf(deBeto), vm.estado.value.solicitudes)
        assertEquals(listOf(ana), vm.estado.value.amigos)
        assertEquals("Ahora eres amigo de Ana", vm.estado.value.mensaje)
        vm.mensajeMostrado()
        assertNull(vm.estado.value.mensaje)
    }

    @Test
    fun `rechazar la quita sin sumar amigo ni mostrar mensaje`() = runTest(despachador) {
        val vm = MisAmigosViewModel(fuenteConSolicitudes())
        vm.rechazar(deBeto)
        assertEquals(listOf(deAna), vm.estado.value.solicitudes)
        assertTrue(vm.estado.value.amigos.isEmpty())
        assertNull(vm.estado.value.mensaje)
    }

    @Test
    fun `si falla la conexion avisa y no cambia nada`() = runTest(despachador) {
        val fuente = fuenteConSolicitudes().apply { responderFunciona = false }
        val vm = MisAmigosViewModel(fuente)
        vm.aceptar(deAna)
        assertEquals("No se pudo aceptar", vm.estado.value.mensaje)
        assertEquals(2, vm.estado.value.solicitudes.size)
    }
}
