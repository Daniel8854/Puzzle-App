package co.edu.univalle.puzzle.ui.pantallas.amigos

import co.edu.univalle.puzzle.datos.amigos.EnvioSolicitud
import co.edu.univalle.puzzle.datos.amigos.FuenteAmigos
import co.edu.univalle.puzzle.datos.amigos.SolicitudRecibida
import co.edu.univalle.puzzle.dominio.amigos.JugadorEncontrado
import kotlinx.coroutines.flow.MutableStateFlow

/** Amigos en memoria para probar los ViewModels sin Firebase. */
class FuenteAmigosFalsa(
    private val jugadores: List<JugadorEncontrado> = emptyList(),
    var respuestaEnvio: EnvioSolicitud = EnvioSolicitud.Enviada,
    var responderFunciona: Boolean = true
) : FuenteAmigos {
    val codigo = MutableStateFlow<String?>(null)
    val solicitudesEnviadas = mutableListOf<String>()
    val pendientes = MutableStateFlow<List<SolicitudRecibida>>(emptyList())
    val listaAmigos = MutableStateFlow<List<JugadorEncontrado>>(emptyList())

    override fun miCodigo() = codigo

    override suspend fun buscar(texto: String) =
        jugadores.filter { it.nombre.startsWith(texto.trim(), ignoreCase = true) }

    override suspend fun enviarSolicitud(para: String): EnvioSolicitud {
        solicitudesEnviadas += para
        return respuestaEnvio
    }

    override fun recibidas() = pendientes

    /** Como el repositorio real: al aceptar, el que envió pasa a la lista de amigos. */
    override suspend fun responder(solicitudId: String, aceptar: Boolean): Boolean {
        if (!responderFunciona) return false
        val s = pendientes.value.first { it.id == solicitudId }
        pendientes.value = pendientes.value - s
        if (aceptar) listaAmigos.value = (listaAmigos.value + s.de).sortedBy { it.nombre }
        return true
    }

    override fun amigos() = listaAmigos
}
