package co.edu.univalle.puzzle.ui.pantallas.modos

import android.content.Context
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.asImageBitmap
import co.edu.univalle.puzzle.datos.catalogo.ImagenCatalogo
import co.edu.univalle.puzzle.datos.catalogo.RepositorioCatalogo
import co.edu.univalle.puzzle.datos.progreso.RepositorioProgreso
import co.edu.univalle.puzzle.motor.ResultadoPartida

/**
 * Lo que Modos y Partida necesitan del catálogo (T-71). Es una interfaz para probar los
 * ViewModels con datos falsos, sin teléfono ni red.
 */
interface CatalogoJuego {
    suspend fun todas(): List<ImagenCatalogo>
    suspend fun descargar(imagen: ImagenCatalogo): ImagenCatalogo
    suspend fun buscarLocal(id: String): ImagenCatalogo?
    suspend fun cargar(imagen: ImagenCatalogo): ImageBitmap?
}

/** Guarda el resultado de una partida (T-17). */
fun interface RegistroResultados {
    suspend fun registrar(modo: String, dificultad: Int, resultado: ResultadoPartida)
}

/** Catálogo real: el repositorio de T-71. */
fun catalogoJuego(context: Context): CatalogoJuego {
    val repositorio = RepositorioCatalogo(context.applicationContext)
    return object : CatalogoJuego {
        override suspend fun todas() = repositorio.todas()
        override suspend fun descargar(imagen: ImagenCatalogo) = repositorio.descargar(imagen)
        override suspend fun buscarLocal(id: String) = repositorio.buscarLocal(id)
        override suspend fun cargar(imagen: ImagenCatalogo) = repositorio.cargar(imagen)?.asImageBitmap()
    }
}

/** Registro real: el progreso en el teléfono de T-17. */
fun registroResultados(context: Context): RegistroResultados {
    val progreso = RepositorioProgreso(context.applicationContext)
    return RegistroResultados { modo, dificultad, resultado -> progreso.registrar(modo, dificultad, resultado) }
}
