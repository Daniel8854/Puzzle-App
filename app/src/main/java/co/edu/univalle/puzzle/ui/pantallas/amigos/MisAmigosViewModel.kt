package co.edu.univalle.puzzle.ui.pantallas.amigos

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import co.edu.univalle.puzzle.datos.amigos.FuenteAmigos
import co.edu.univalle.puzzle.datos.amigos.RepositorioAmigos
import co.edu.univalle.puzzle.datos.amigos.SolicitudRecibida
import co.edu.univalle.puzzle.dominio.amigos.JugadorEncontrado
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

/** Estado de la pantalla 14 · Mis amigos. */
data class EstadoMisAmigos(
    val solicitudes: List<SolicitudRecibida> = emptyList(),
    val amigos: List<JugadorEncontrado> = emptyList(),
    /** Mensaje corto para mostrar una vez. */
    val mensaje: String? = null
)

/** Mis amigos (T-53): solicitudes recibidas con Aceptar y Rechazar (RF-41) y la lista de amigos. */
class MisAmigosViewModel(private val fuente: FuenteAmigos) : ViewModel() {

    private val _estado = MutableStateFlow(EstadoMisAmigos())
    val estado: StateFlow<EstadoMisAmigos> = _estado.asStateFlow()

    init {
        viewModelScope.launch { fuente.recibidas().collect { l -> _estado.update { it.copy(solicitudes = l) } } }
        viewModelScope.launch { fuente.amigos().collect { l -> _estado.update { it.copy(amigos = l) } } }
    }

    fun aceptar(solicitud: SolicitudRecibida) {
        viewModelScope.launch {
            val ok = fuente.responder(solicitud.id, aceptar = true)
            _estado.update {
                it.copy(mensaje = if (ok) "Ahora eres amigo de ${solicitud.de.nombre}" else "No se pudo aceptar")
            }
        }
    }

    fun rechazar(solicitud: SolicitudRecibida) {
        viewModelScope.launch {
            if (!fuente.responder(solicitud.id, aceptar = false)) {
                _estado.update { it.copy(mensaje = "No se pudo rechazar") }
            }
        }
    }

    /** La pantalla ya mostró el mensaje. */
    fun mensajeMostrado() = _estado.update { it.copy(mensaje = null) }

    companion object {
        fun fabrica(): ViewModelProvider.Factory = viewModelFactory {
            initializer { MisAmigosViewModel(RepositorioAmigos()) }
        }
    }
}
