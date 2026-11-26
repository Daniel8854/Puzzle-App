package co.edu.univalle.puzzle.ui.pantallas.fotos

import android.content.Context
import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import co.edu.univalle.puzzle.datos.fotos.AlmacenFotos
import co.edu.univalle.puzzle.datos.fotos.RepositorioFotos
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

/** Estado de la pantalla Nueva foto. Cuando [fotoGuardada] deja de ser null se pasa a Recortar. */
data class EstadoNuevaFoto(
    val guardando: Boolean = false,
    val error: String? = null,
    val fotoGuardada: Long? = null
)

/** Nueva foto (RF-34): recibe la foto elegida o tomada, la manda a comprimir y guardar. */
class NuevaFotoViewModel(private val almacen: AlmacenFotos) : ViewModel() {

    private val _estado = MutableStateFlow(EstadoNuevaFoto())
    val estado: StateFlow<EstadoNuevaFoto> = _estado.asStateFlow()

    /** Uri donde la cámara guarda la foto. */
    fun nuevoArchivoCamara(): Uri = almacen.nuevoArchivoCamara()

    /** Foto elegida de la galería o tomada con la cámara (en texto). Si es null, el usuario canceló. */
    fun fotoElegida(origen: String?) {
        if (origen == null || _estado.value.guardando) return
        _estado.value = EstadoNuevaFoto(guardando = true)
        viewModelScope.launch {
            val id = runCatching { almacen.guardar(origen) }.getOrNull()
            _estado.value = if (id != null) EstadoNuevaFoto(fotoGuardada = id)
            else EstadoNuevaFoto(error = "No se pudo usar esa foto. Prueba con otra.")
        }
    }

    /** La pantalla ya navegó a Recortar: se limpia para no volver a navegar al regresar. */
    fun navegacionHecha() = _estado.update { it.copy(fotoGuardada = null) }

    companion object {
        fun fabrica(context: Context): ViewModelProvider.Factory = viewModelFactory {
            initializer { NuevaFotoViewModel(RepositorioFotos(context.applicationContext)) }
        }
    }
}
