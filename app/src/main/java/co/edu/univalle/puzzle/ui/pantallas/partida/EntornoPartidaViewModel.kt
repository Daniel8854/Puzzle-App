package co.edu.univalle.puzzle.ui.pantallas.partida

import android.content.Context
import androidx.compose.ui.graphics.ImageBitmap
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import co.edu.univalle.puzzle.motor.ResultadoPartida
import co.edu.univalle.puzzle.ui.pantallas.modos.CatalogoJuego
import co.edu.univalle.puzzle.ui.pantallas.modos.RegistroResultados
import co.edu.univalle.puzzle.ui.pantallas.modos.catalogoJuego
import co.edu.univalle.puzzle.ui.pantallas.modos.registroResultados
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

/** Lo que rodea al tablero: título e imagen elegida, y el resultado cuando la partida termina. */
data class EstadoEntornoPartida(
    val titulo: String = "Partida",
    val imagen: ImageBitmap? = null,
    val final: ResultadoPartida? = null
)

/**
 * Entorno de la partida (T-73): carga la imagen del catálogo y guarda el resultado en el teléfono
 * (T-17). El tablero y el reloj los lleva [co.edu.univalle.puzzle.ui.juego.PartidaViewModel].
 */
class EntornoPartidaViewModel(
    imagenId: String,
    private val modo: String,
    private val dificultad: Int,
    private val catalogo: CatalogoJuego,
    private val registro: RegistroResultados
) : ViewModel() {

    private val _estado = MutableStateFlow(EstadoEntornoPartida())
    val estado: StateFlow<EstadoEntornoPartida> = _estado.asStateFlow()

    init {
        viewModelScope.launch {
            val img = runCatching { catalogo.buscarLocal(imagenId) }.getOrNull() ?: return@launch
            _estado.update { it.copy(titulo = img.titulo) }
            val imagen = runCatching { catalogo.cargar(img) }.getOrNull()
            _estado.update { it.copy(imagen = imagen) }
        }
    }

    /** La llama el tablero al terminar (una sola vez): muestra el resumen y guarda el resultado. */
    fun terminar(resultado: ResultadoPartida) {
        _estado.update { it.copy(final = resultado) }
        viewModelScope.launch { runCatching { registro.registrar(modo, dificultad, resultado) } }
    }

    companion object {
        fun fabrica(imagenId: String, modo: String, dificultad: Int, context: Context): ViewModelProvider.Factory =
            viewModelFactory {
                initializer {
                    EntornoPartidaViewModel(imagenId, modo, dificultad, catalogoJuego(context), registroResultados(context))
                }
            }
    }
}
