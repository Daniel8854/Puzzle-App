package co.edu.univalle.puzzle.ui.pantallas.fotos

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

@OptIn(ExperimentalCoroutinesApi::class)
class NuevaFotoViewModelTest {

    private val despachador = StandardTestDispatcher()

    @Before fun antes() = Dispatchers.setMain(despachador)
    @After fun despues() = Dispatchers.resetMain()

    @Test
    fun `al elegir una foto la guarda y avisa el id`() = runTest(despachador) {
        val almacen = AlmacenFotosFalso()
        val vm = NuevaFotoViewModel(almacen)
        vm.fotoElegida("content://galeria/1")
        assertTrue(vm.estado.value.guardando)
        advanceUntilIdle()
        assertEquals(listOf("content://galeria/1"), almacen.guardadas)
        assertEquals(1L, vm.estado.value.fotoGuardada)
        assertFalse(vm.estado.value.guardando)
        vm.navegacionHecha()
        assertNull(vm.estado.value.fotoGuardada)
    }

    @Test
    fun `si el usuario cancela no pasa nada`() = runTest(despachador) {
        val almacen = AlmacenFotosFalso()
        val vm = NuevaFotoViewModel(almacen)
        vm.fotoElegida(null)
        advanceUntilIdle()
        assertTrue(almacen.guardadas.isEmpty())
        assertEquals(EstadoNuevaFoto(), vm.estado.value)
    }

    @Test
    fun `si la foto no sirve muestra un error`() = runTest(despachador) {
        val vm = NuevaFotoViewModel(AlmacenFotosFalso(falla = true))
        vm.fotoElegida("content://galeria/2")
        advanceUntilIdle()
        assertNotNull(vm.estado.value.error)
        assertNull(vm.estado.value.fotoGuardada)
        assertFalse(vm.estado.value.guardando)
    }
}
