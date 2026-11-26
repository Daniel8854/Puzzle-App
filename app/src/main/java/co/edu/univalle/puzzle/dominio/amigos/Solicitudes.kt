package co.edu.univalle.puzzle.dominio.amigos

/** Estados de una solicitud de amistad en Firestore. */
enum class EstadoSolicitud(val clave: String) {
    PENDIENTE("pendiente"), ACEPTADA("aceptada"), RECHAZADA("rechazada");

    companion object {
        fun desde(clave: String?): EstadoSolicitud? = entries.firstOrNull { it.clave == clave }
    }
}

/** Por qué no se puede enviar una solicitud (RF-40). */
enum class SolicitudInvalida(val mensaje: String) {
    A_SI_MISMO("No puedes enviarte una solicitud a ti mismo"),
    YA_SON_AMIGOS("Ya son amigos"),
    YA_ENVIADA("Ya le enviaste una solicitud; espera su respuesta")
}

/** Jugador encontrado al buscar. */
data class JugadorEncontrado(val uid: String, val nombre: String, val avatar: Int)

object Solicitudes {

    /** Id del documento de la solicitud: «deUid_paraUid» (reglas de T-14). */
    fun id(de: String, para: String) = "${de}_$para"

    /**
     * Revisa los tres casos inválidos antes de enviar. Si la anterior fue rechazada,
     * se puede volver a enviar.
     */
    fun validar(miUid: String, otroUid: String, yaSonAmigos: Boolean, anterior: EstadoSolicitud?): SolicitudInvalida? = when {
        miUid == otroUid -> SolicitudInvalida.A_SI_MISMO
        yaSonAmigos || anterior == EstadoSolicitud.ACEPTADA -> SolicitudInvalida.YA_SON_AMIGOS
        anterior == EstadoSolicitud.PENDIENTE -> SolicitudInvalida.YA_ENVIADA
        else -> null
    }

    /** Qué buscar: si el texto tiene forma de código se busca el código exacto; si no, el nombre. */
    sealed interface Busqueda {
        data class PorCodigo(val codigo: String) : Busqueda
        data class PorNombre(val prefijo: String) : Busqueda
        data object Vacia : Busqueda
    }

    fun interpretar(texto: String): Busqueda {
        val codigo = CodigoAmigo.normalizar(texto)
        if (CodigoAmigo.esValido(codigo)) return Busqueda.PorCodigo(codigo)
        val prefijo = texto.trim().lowercase()
        return if (prefijo.length >= 2) Busqueda.PorNombre(prefijo) else Busqueda.Vacia
    }
}
