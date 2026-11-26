package co.edu.univalle.puzzle.ui.pantallas.modos

import androidx.compose.ui.graphics.ImageBitmap
import co.edu.univalle.puzzle.datos.catalogo.ImagenCatalogo
import co.edu.univalle.puzzle.datos.catalogo.Ubicacion
import co.edu.univalle.puzzle.motor.Dificultad
import co.edu.univalle.puzzle.motor.FinPartida
import co.edu.univalle.puzzle.motor.ResultadoPartida
import co.edu.univalle.puzzle.ui.pantallas.partida.EntornoPartidaViewModel
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
class ModosYPartidaViewModelsTest {

    private val despachador = StandardTestDispatcher()

    @Before fun antes() = Dispatchers.setMain(despachador)
    @After fun despues() = Dispatchers.resetMain()

    private fun imagen(id: String, ubicacion: Ubicacion) =
        ImagenCatalogo(id, "Imagen $id", "Autor", "https://pixabay.com", "Pixabay", ubicacion, "$id.jpg")

    /** Catálogo en memoria: una incluida en la app y otra en la nube. */
    private class CatalogoFalso(val imagenes: List<ImagenCatalogo>, var sinRed: Boolean = false) : CatalogoJuego {
        val descargadas = mutableListOf<String>()
        override suspend fun todas() = imagenes
        override suspend fun descargar(imagen: ImagenCatalogo): ImagenCatalogo {
            if (sinRed && imagen.ubicacion == Ubicacion.EN_LA_NUBE) error("sin red")
            descargadas += imagen.id
            return imagen.copy(ubicacion = Ubicacion.DESCARGADA)
        }
        override suspend fun buscarLocal(id: String) = imagenes.firstOrNull { it.id == id }
        override suspend fun cargar(imagen: ImagenCatalogo): ImageBitmap? = null
    }

    private val catalogo = listOf(imagen("gato", Ubicacion.INCLUIDA), imagen("playa", Ubicacion.EN_LA_NUBE))

    // ---------- Modos ----------

    @Test
    fun `al abrir elige la primera imagen y Clasico Facil`() = runTest(despachador) {
        val vm = ModosViewModel(CatalogoFalso(catalogo))
        advanceUntilIdle()
        assertEquals(2, vm.estado.value.imagenes.size)
        assertEquals(Eleccion(ModoJuego.CLASICO, Dificultad.FACIL, "gato"), vm.estado.value.eleccion)
    }

    @Test
    fun `jugar descarga la imagen y arma la jugada elegida`() = runTest(despachador) {
        val fuente = CatalogoFalso(catalogo)
        val vm = ModosViewModel(fuente)
        advanceUntilIdle()
        vm.elegirModo(ModoJuego.CONTRARRELOJ)
        vm.elegirDificultad(Dificultad.DIFICIL)
        vm.elegirImagen("playa")
        vm.jugar()
        advanceUntilIdle()
        assertEquals(listOf("playa"), fuente.descargadas)
        assertEquals(Jugada(ModoJuego.CONTRARRELOJ.clave, Dificultad.DIFICIL.tamano, "playa"), vm.estado.value.jugada)
        vm.navegacionHecha()
        assertNull(vm.estado.value.jugada)
    }

    @Test
    fun `elegir un modo abre el catalogo y volver regresa a los modos`() = runTest(despachador) {
        val vm = ModosViewModel(CatalogoFalso(catalogo))
        advanceUntilIdle()
        assertFalse(vm.estado.value.viendoImagenes)
        vm.elegirModo(ModoJuego.CONTRARRELOJ)
        assertTrue(vm.estado.value.viendoImagenes)
        assertEquals(ModoJuego.CONTRARRELOJ, vm.estado.value.eleccion.modo)
        vm.volverAModos()
        assertFalse(vm.estado.value.viendoImagenes)
    }

    @Test
    fun `sin red la imagen de la nube no se puede jugar`() = runTest(despachador) {
        val vm = ModosViewModel(CatalogoFalso(catalogo, sinRed = true))
        advanceUntilIdle()
        vm.elegirImagen("playa")
        vm.jugar()
        advanceUntilIdle()
        assertNull(vm.estado.value.jugada)
        assertNotNull(vm.estado.value.error)
        assertFalse(vm.estado.value.descargando)
    }

    @Test
    fun `sin imagenes no hay jugada`() = runTest(despachador) {
        val vm = ModosViewModel(CatalogoFalso(emptyList()))
        advanceUntilIdle()
        vm.jugar()
        advanceUntilIdle()
        assertNull(vm.estado.value.jugada)
    }

    // ---------- Entorno de la partida ----------

    @Test
    fun `la partida muestra el titulo de la imagen y guarda el resultado al terminar`() = runTest(despachador) {
        val guardados = mutableListOf<Triple<String, Int, ResultadoPartida>>()
        val vm = EntornoPartidaViewModel("gato", "clasico", 3, CatalogoFalso(catalogo)) { m, d, r -> guardados += Triple(m, d, r) }
        advanceUntilIdle()
        assertEquals("Imagen gato", vm.estado.value.titulo)

        val resultado = ResultadoPartida(FinPartida.COMPLETADA, movimientos = 30, tiempoMs = 45_000, piezasEnSuLugar = 8, totalPiezas = 8)
        vm.terminar(resultado)
        assertEquals(resultado, vm.estado.value.final)
        advanceUntilIdle()
        assertEquals(listOf(Triple("clasico", 3, resultado)), guardados)
    }
}
