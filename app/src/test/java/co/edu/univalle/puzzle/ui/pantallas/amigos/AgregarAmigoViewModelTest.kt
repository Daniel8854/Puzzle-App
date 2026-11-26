package co.edu.univalle.puzzle.ui.pantallas.amigos

import co.edu.univalle.puzzle.datos.amigos.EnvioSolicitud
import co.edu.univalle.puzzle.dominio.amigos.JugadorEncontrado
import co.edu.univalle.puzzle.dominio.amigos.SolicitudInvalida
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class AgregarAmigoViewModelTest {

    private val despachador = UnconfinedTestDispatcher()

    @Before fun antes() = Dispatchers.setMain(despachador)
    @After fun despues() = Dispatchers.resetMain()

    private val ana = JugadorEncontrado("uid-ana", "Ana", 1)
    private val andres = JugadorEncontrado("uid-andres", "Andrés", 2)
    private val beto = JugadorEncontrado("uid-beto", "Beto", 3)

    @Test
    fun `mientras se genera el codigo muestra Generando`() = runTest(despachador) {
        val vm = AgregarAmigoViewModel(FuenteAmigosFalsa())
        assertNull(vm.estado.value.codigo)
        assertEquals("Generando…", vm.estado.value.codigoVisible)
    }

    @Test
    fun `cuando llega el codigo se muestra en grupos de 4 (RF-39)`() = runTest(despachador) {
        val fuente = FuenteAmigosFalsa()
        val vm = AgregarAmigoViewModel(fuente)
        fuente.codigo.value = "ABCD1234"
        assertEquals("ABCD1234", vm.estado.value.codigo)
        assertEquals("ABCD-1234", vm.estado.value.codigoVisible)
    }

    @Test
    fun `buscar muestra los jugadores encontrados (RF-40)`() = runTest(despachador) {
        val vm = AgregarAmigoViewModel(FuenteAmigosFalsa(listOf(ana, andres, beto)))
        assertNull(vm.estado.value.resultados)
        vm.cambiarTexto("an")
        vm.buscar()
        assertEquals(listOf(ana, andres), vm.estado.value.resultados)
        assertFalse(vm.estado.value.buscando)
    }

    @Test
    fun `si no hay nadie la lista queda vacia`() = runTest(despachador) {
        val vm = AgregarAmigoViewModel(FuenteAmigosFalsa(listOf(beto)))
        vm.cambiarTexto("zz")
        vm.buscar()
        assertEquals(emptyList<JugadorEncontrado>(), vm.estado.value.resultados)
    }

    @Test
    fun `enviar solicitud avisa con un mensaje que se muestra una vez`() = runTest(despachador) {
        val fuente = FuenteAmigosFalsa(listOf(ana))
        val vm = AgregarAmigoViewModel(fuente)
        vm.enviarSolicitud(ana)
        assertEquals(listOf("uid-ana"), fuente.solicitudesEnviadas)
        assertEquals("Solicitud enviada a Ana", vm.estado.value.mensaje)
        vm.mensajeMostrado()
        assertNull(vm.estado.value.mensaje)
    }

    @Test
    fun `solicitud invalida o sin conexion muestra el motivo`() = runTest(despachador) {
        val fuente = FuenteAmigosFalsa(respuestaEnvio = EnvioSolicitud.Invalida(SolicitudInvalida.entries.first()))
        val vm = AgregarAmigoViewModel(fuente)
        vm.enviarSolicitud(ana)
        assertEquals(SolicitudInvalida.entries.first().mensaje, vm.estado.value.mensaje)
        fuente.respuestaEnvio = EnvioSolicitud.Fallo
        vm.enviarSolicitud(ana)
        assertEquals("No se pudo enviar. Revisa tu conexión.", vm.estado.value.mensaje)
    }
}
