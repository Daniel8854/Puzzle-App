package co.edu.univalle.puzzle.datos.local

import androidx.room.Room
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

/** Se ejecuta en un teléfono o emulador: Android Studio → clic derecho → Run. */
@RunWith(AndroidJUnit4::class)
class BaseDatosPuzzleTest {

    private lateinit var bd: BaseDatosPuzzle

    @Before fun crear() {
        bd = Room.inMemoryDatabaseBuilder(
            InstrumentationRegistry.getInstrumentation().targetContext, BaseDatosPuzzle::class.java
        ).build()
    }

    @After fun cerrar() = bd.close()

    @Test fun dosCuentasNoVenLosDatosDeLaOtra() = runBlocking {
        bd.resultados().insertar(ResultadoLocal(cuentaId = "a", modo = "clasico", dificultad = 3,
            completada = true, movimientos = 40, tiempoMs = 60_000, fechaMs = 1))
        bd.fotos().insertar(FotoPropia(cuentaId = "a", rutaLocal = "/a.jpg", fechaMs = 1))
        assertEquals(1, bd.resultados().deCuenta("a").first().size)
        assertEquals(0, bd.resultados().deCuenta("b").first().size)
        assertEquals(0, bd.fotos().deCuenta("b").first().size)
    }

    @Test fun hayUnaSolaPartidaEnCursoPorCuenta() = runBlocking {
        val p = PartidaGuardada("a", "clasico", "img", 3, "1,2,3,4,5,6,7,0,8", 1, 1_000, null)
        bd.partidas().guardar(p)
        bd.partidas().guardar(p.copy(movimientos = 2))
        assertEquals(2, bd.partidas().deCuenta("a")?.movimientos)
        bd.partidas().borrar("a")
        assertNull(bd.partidas().deCuenta("a"))
    }
}
