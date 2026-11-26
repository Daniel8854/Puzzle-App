package co.edu.univalle.puzzle.ui.pantallas.fotos

import co.edu.univalle.puzzle.datos.fotos.Rect
import co.edu.univalle.puzzle.datos.local.FotoPropia
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
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class RecortarViewModelTest {

    private val despachador = StandardTestDispatcher()

    @Before fun antes() = Dispatchers.setMain(despachador)
    @After fun despues() = Dispatchers.resetMain()

    private val foto = FotoPropia(id = 7, cuentaId = "invitado", rutaLocal = "foto.jpg", cuadricula = 4, fechaMs = 0)

    @Test
    fun `carga la foto con la cuadricula que tenia`() = runTest(despachador) {
        val vm = RecortarViewModel(7, AlmacenFotosFalso(foto = foto))
        assertTrue(vm.estado.value.cargando)
        advanceUntilIdle()
        assertFalse(vm.estado.value.cargando)
        assertEquals(foto, vm.estado.value.foto)
        assertEquals(4, vm.estado.value.cuadricula)
    }

    @Test
    fun `rotar da la vuelta completa y voltear se alterna`() = runTest(despachador) {
        val vm = RecortarViewModel(7, AlmacenFotosFalso(foto = foto))
        repeat(3) { vm.rotar() }
        assertEquals(270, vm.estado.value.rotacion)
        vm.rotar()
        assertEquals(0, vm.estado.value.rotacion)
        vm.voltear()
        assertTrue(vm.estado.value.volteada)
        vm.voltear()
        assertFalse(vm.estado.value.volteada)
    }

    @Test
    fun `solo acepta cuadriculas de 3 a 5`() = runTest(despachador) {
        val vm = RecortarViewModel(7, AlmacenFotosFalso(foto = foto))
        advanceUntilIdle()
        vm.elegirCuadricula(5)
        assertEquals(5, vm.estado.value.cuadricula)
        vm.elegirCuadricula(8)
        assertEquals(5, vm.estado.value.cuadricula)
    }

    @Test
    fun `guardar manda el recorte con giro, espejo y cuadricula`() = runTest(despachador) {
        val almacen = AlmacenFotosFalso(foto = foto)
        val vm = RecortarViewModel(7, almacen)
        advanceUntilIdle()
        vm.rotar()
        vm.voltear()
        vm.elegirCuadricula(3)
        vm.guardar(Rect(10, 20, 300))
        advanceUntilIdle()
        assertEquals(listOf(AlmacenFotosFalso.Recorte(90, true, Rect(10, 20, 300), 3)), almacen.recortes)
        assertTrue(vm.estado.value.listo)
    }

    @Test
    fun `si no se puede guardar muestra error y no avanza`() = runTest(despachador) {
        val vm = RecortarViewModel(7, AlmacenFotosFalso(falla = true, foto = foto))
        advanceUntilIdle()
        vm.guardar(Rect(0, 0, 100))
        advanceUntilIdle()
        assertFalse(vm.estado.value.listo)
        assertFalse(vm.estado.value.guardando)
        assertNotNull(vm.estado.value.error)
    }

    @Test
    fun `si la foto no existe avisa`() = runTest(despachador) {
        val vm = RecortarViewModel(99, AlmacenFotosFalso(foto = foto))
        advanceUntilIdle()
        assertNotNull(vm.estado.value.error)
        vm.guardar(Rect(0, 0, 100))
        advanceUntilIdle()
        assertFalse(vm.estado.value.guardando)
    }
}
