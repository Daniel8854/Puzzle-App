package co.edu.univalle.puzzle.ui.pantallas.modos

import android.content.Context
import androidx.compose.ui.graphics.ImageBitmap
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import co.edu.univalle.puzzle.datos.catalogo.ImagenCatalogo
import co.edu.univalle.puzzle.motor.Dificultad
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

/** Lo que se le pasa a la pantalla de partida al pulsar «Jugar». */
data class Jugada(val modo: String, val dificultad: Int, val imagenId: String)

/** Estado de la pantalla 04 · Modos de juego. */
data class EstadoModos(
    val imagenes: List<ImagenCatalogo> = emptyList(),
    val miniaturas: Map<String, ImageBitmap> = emptyMap(),
    val eleccion: Eleccion = Eleccion(),
    val descargando: Boolean = false,
    val error: String? = null,
    /** false: lista de modos (04). true: elegir imagen del catálogo (04c). */
    val viendoImagenes: Boolean = false,
    /** Cuando deja de ser null, la pantalla abre la partida. */
    val jugada: Jugada? = null
)

/**
 * Modos de juego (T-73): pantalla 04 para elegir Clásico o Contrarreloj y 04c para elegir la
 * dificultad y una imagen del catálogo (T-71). Si la imagen está en la nube se descarga antes de jugar.
 */
class ModosViewModel(private val catalogo: CatalogoJuego) : ViewModel() {

    private val _estado = MutableStateFlow(EstadoModos())
    val estado: StateFlow<EstadoModos> = _estado.asStateFlow()

    init {
        viewModelScope.launch {
            val imagenes = runCatching { catalogo.todas() }.getOrDefault(emptyList())
            _estado.update {
                it.copy(imagenes = imagenes, eleccion = it.eleccion.copy(imagenId = imagenes.firstOrNull()?.id))
            }
            imagenes.filter { it.disponibleSinConexion }.forEach { img ->
                runCatching { catalogo.cargar(img) }.getOrNull()?.let { mini ->
                    _estado.update { it.copy(miniaturas = it.miniaturas + (img.id to mini)) }
                }
            }
        }
    }

    /** Al tocar un modo se pasa a elegir la imagen (04 → 04c). */
    fun elegirModo(modo: ModoJuego) =
        _estado.update { it.copy(eleccion = it.eleccion.copy(modo = modo), viendoImagenes = true) }

    fun volverAModos() = _estado.update { it.copy(viendoImagenes = false, error = null) }
    fun elegirDificultad(d: Dificultad) = _estado.update { it.copy(eleccion = it.eleccion.copy(dificultad = d)) }
    fun elegirImagen(id: String) = _estado.update { it.copy(eleccion = it.eleccion.copy(imagenId = id)) }

    fun jugar() {
        val e = _estado.value
        if (!e.eleccion.lista || e.descargando) return
        val img = e.imagenes.firstOrNull { it.id == e.eleccion.imagenId } ?: return
        _estado.update { it.copy(descargando = true, error = null) }
        viewModelScope.launch {
            val lista = runCatching { catalogo.descargar(img) }.getOrNull()
            _estado.update {
                if (lista == null) it.copy(descargando = false, error = "No se pudo descargar la imagen. Revisa tu conexión.")
                else it.copy(
                    descargando = false,
                    jugada = Jugada(it.eleccion.modo.clave, it.eleccion.dificultad.tamano, lista.id)
                )
            }
        }
    }

    /** La pantalla ya abrió la partida: se limpia para no abrirla otra vez al volver. */
    fun navegacionHecha() = _estado.update { it.copy(jugada = null) }

    companion object {
        fun fabrica(context: Context): ViewModelProvider.Factory = viewModelFactory {
            initializer { ModosViewModel(catalogoJuego(context)) }
        }
    }
}
