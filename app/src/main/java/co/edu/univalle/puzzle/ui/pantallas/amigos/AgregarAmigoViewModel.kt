package co.edu.univalle.puzzle.ui.pantallas.amigos

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import co.edu.univalle.puzzle.datos.amigos.EnvioSolicitud
import co.edu.univalle.puzzle.datos.amigos.FuenteAmigos
import co.edu.univalle.puzzle.datos.amigos.RepositorioAmigos
import co.edu.univalle.puzzle.dominio.amigos.JugadorEncontrado
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

/** Estado de la pantalla 13 · Agregar amigo. */
data class EstadoAgregarAmigo(
    /** Código propio; null mientras se genera. */
    val codigo: String? = null,
    val texto: String = "",
    /** null = todavía no se ha buscado; vacía = no se encontró a nadie. */
    val resultados: List<JugadorEncontrado>? = null,
    val buscando: Boolean = false,
    /** Mensaje corto para mostrar una vez (por ejemplo, «Solicitud enviada a Ana»). */
    val mensaje: String? = null
) {
    /** El código se muestra en grupos de 4: ABCD-1234. */
    val codigoVisible: String get() = codigo?.chunked(4)?.joinToString("-") ?: "Generando…"
}

/** Agregar amigo. T-51: código propio (RF-39). T-52: buscar y enviar solicitud (RF-40). */
class AgregarAmigoViewModel(private val fuente: FuenteAmigos) : ViewModel() {

    private val _estado = MutableStateFlow(EstadoAgregarAmigo())
    val estado: StateFlow<EstadoAgregarAmigo> = _estado.asStateFlow()

    init {
        viewModelScope.launch {
            fuente.miCodigo().collect { codigo -> _estado.update { it.copy(codigo = codigo) } }
        }
    }

    fun cambiarTexto(v: String) = _estado.update { it.copy(texto = v) }

    fun buscar() {
        if (_estado.value.buscando) return
        val texto = _estado.value.texto
        _estado.update { it.copy(buscando = true) }
        viewModelScope.launch {
            val encontrados = runCatching { fuente.buscar(texto) }.getOrDefault(emptyList())
            _estado.update { it.copy(buscando = false, resultados = encontrados) }
        }
    }

    fun enviarSolicitud(jugador: JugadorEncontrado) {
        viewModelScope.launch {
            val mensaje = when (val r = fuente.enviarSolicitud(jugador.uid)) {
                EnvioSolicitud.Enviada -> "Solicitud enviada a ${jugador.nombre}"
                is EnvioSolicitud.Invalida -> r.motivo.mensaje
                EnvioSolicitud.Fallo -> "No se pudo enviar. Revisa tu conexión."
            }
            _estado.update { it.copy(mensaje = mensaje) }
        }
    }

    /** La pantalla ya mostró el mensaje. */
    fun mensajeMostrado() = _estado.update { it.copy(mensaje = null) }

    companion object {
        fun fabrica(): ViewModelProvider.Factory = viewModelFactory {
            initializer { AgregarAmigoViewModel(RepositorioAmigos()) }
        }
    }
}
