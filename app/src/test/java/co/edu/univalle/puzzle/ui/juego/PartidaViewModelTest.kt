package co.edu.univalle.puzzle.ui.juego

import co.edu.univalle.puzzle.motor.FinPartida
import co.edu.univalle.puzzle.motor.ResultadoPartida
import co.edu.univalle.puzzle.motor.Tablero
import co.edu.univalle.puzzle.motor.mover
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.advanceTimeBy
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class PartidaViewModelTest {

    private val despachador = StandardTestDispatcher()
    private var ahora = 0L
    private val resultados = mutableListOf<ResultadoPartida>()

    @Before fun antes() = Dispatchers.setMain(despachador)
    @After fun despues() = Dispatchers.resetMain()

    private fun TestScope.vm(limite: Long? = null) =
        PartidaViewModel(Tablero.resuelto(3).mover(7)!!, limite, { ahora }, { resultados += it }, backgroundScope)

    @Test
    fun `el reloj visible avanza cada segundo`() = runTest(despachador) {
        val vm = vm()
        ahora = 3_000
        advanceTimeBy(300)
        assertEquals(3, vm.estado.value.segundosVisibles)
        vm.pausar()
        ahora = 10_000
        advanceTimeBy(300)
        assertEquals(3, vm.estado.value.segundosVisibles)
        vm.reanudar()
        vm.mover(0) // ilegal: no cuenta
        assertEquals(0, vm.estado.value.partida.movimientos)
    }

    @Test
    fun `al ganar avisa una sola vez`() = runTest(despachador) {
        val vm = vm()
        ahora = 5_000
        vm.mover(8)
        vm.mover(7)
        advanceTimeBy(1_000)
        assertTrue(vm.estado.value.partida.terminada)
        assertEquals(1, resultados.size)
        assertEquals(FinPartida.COMPLETADA, resultados.single().fin)
    }

    @Test
    fun `contrarreloj muestra lo que queda y avisa al agotarse`() = runTest(despachador) {
        val vm = vm(limite = 60_000)
        ahora = 20_000
        advanceTimeBy(300)
        assertEquals(40, vm.estado.value.segundosVisibles)
        ahora = 61_000
        advanceTimeBy(300)
        assertEquals(FinPartida.TIEMPO_AGOTADO, resultados.single().fin)
    }

    @Test
    fun `formato del reloj`() {
        assertEquals("0:05", formatoReloj(5))
        assertEquals("12:00", formatoReloj(720))
    }
}
