package co.edu.univalle.puzzle.ui.pantallas.amigos

import co.edu.univalle.puzzle.datos.amigos.EnvioSolicitud
import co.edu.univalle.puzzle.datos.amigos.FuenteAmigos
import co.edu.univalle.puzzle.dominio.amigos.JugadorEncontrado
import kotlinx.coroutines.flow.MutableStateFlow

/** Amigos en memoria para probar los ViewModels sin Firebase. */
class FuenteAmigosFalsa(
    private val jugadores: List<JugadorEncontrado> = emptyList(),
    var respuestaEnvio: EnvioSolicitud = EnvioSolicitud.Enviada
) : FuenteAmigos {
    val codigo = MutableStateFlow<String?>(null)
    val solicitudesEnviadas = mutableListOf<String>()

    override fun miCodigo() = codigo

    override suspend fun buscar(texto: String) =
        jugadores.filter { it.nombre.startsWith(texto.trim(), ignoreCase = true) }

    override suspend fun enviarSolicitud(para: String): EnvioSolicitud {
        solicitudesEnviadas += para
        return respuestaEnvio
    }
}
