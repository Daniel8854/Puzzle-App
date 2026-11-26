package co.edu.univalle.puzzle.ui.pantallas.fotos

import android.content.Context
import android.graphics.Bitmap
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import co.edu.univalle.puzzle.datos.fotos.AlmacenFotos
import co.edu.univalle.puzzle.datos.fotos.Rect
import co.edu.univalle.puzzle.datos.fotos.RepositorioFotos
import co.edu.univalle.puzzle.datos.local.FotoPropia
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

/** Estado de la pantalla Recortar. El marco (lo que se arrastra con el dedo) vive en la pantalla. */
data class EstadoRecortar(
    val cargando: Boolean = true,
    val foto: FotoPropia? = null,
    val original: Bitmap? = null,
    val rotacion: Int = 0,
    val volteada: Boolean = false,
    val cuadricula: Int = 3,
    val guardando: Boolean = false,
    val error: String? = null,
    val listo: Boolean = false
)

/** Recortar la foto en cuadrado (RF-35): rotar, voltear, elegir cuadrícula y guardar el recorte. */
class RecortarViewModel(private val fotoId: Long, private val almacen: AlmacenFotos) : ViewModel() {

    private val _estado = MutableStateFlow(EstadoRecortar())
    val estado: StateFlow<EstadoRecortar> = _estado.asStateFlow()

    init {
        viewModelScope.launch {
            val foto = runCatching { almacen.porId(fotoId) }.getOrNull()
            val original = foto?.let { runCatching { almacen.cargarImagen(it) }.getOrNull() }
            _estado.update {
                it.copy(
                    cargando = false,
                    foto = foto,
                    original = original,
                    cuadricula = foto?.cuadricula ?: 3,
                    error = if (foto == null) "No se pudo abrir la foto" else null
                )
            }
        }
    }

    fun rotar() = _estado.update { it.copy(rotacion = (it.rotacion + 90) % 360) }
    fun voltear() = _estado.update { it.copy(volteada = !it.volteada) }
    fun elegirCuadricula(n: Int) {
        if (n in 3..5) _estado.update { it.copy(cuadricula = n) }
    }

    /** Guarda el [recorte] que marcó el jugador sobre la foto ya girada y volteada. */
    fun guardar(recorte: Rect) {
        val e = _estado.value
        val foto = e.foto ?: return
        if (e.guardando) return
        _estado.update { it.copy(guardando = true, error = null) }
        viewModelScope.launch {
            val ok = runCatching {
                almacen.guardarRecorte(foto, e.rotacion, e.volteada, recorte, e.cuadricula)
            }.isSuccess
            _estado.update {
                if (ok) it.copy(guardando = false, listo = true)
                else it.copy(guardando = false, error = "No se pudo guardar el recorte. Intenta de nuevo.")
            }
        }
    }

    companion object {
        fun fabrica(fotoId: Long, context: Context): ViewModelProvider.Factory = viewModelFactory {
            initializer { RecortarViewModel(fotoId, RepositorioFotos(context.applicationContext)) }
        }
    }
}
