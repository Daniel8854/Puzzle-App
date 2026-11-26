package co.edu.univalle.puzzle.datos.amigos

import co.edu.univalle.puzzle.datos.nube.Nube
import co.edu.univalle.puzzle.datos.nube.Nube.Colecciones
import co.edu.univalle.puzzle.dominio.amigos.EstadoSolicitud
import co.edu.univalle.puzzle.dominio.amigos.JugadorEncontrado
import co.edu.univalle.puzzle.dominio.amigos.SolicitudInvalida
import co.edu.univalle.puzzle.dominio.amigos.Solicitudes
import co.edu.univalle.puzzle.dominio.amigos.Solicitudes.Busqueda
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestoreException
import com.google.firebase.firestore.Query
import kotlinx.coroutines.launch
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await

/** Resultado de enviar una solicitud. */
sealed interface EnvioSolicitud {
    data object Enviada : EnvioSolicitud
    data class Invalida(val motivo: SolicitudInvalida) : EnvioSolicitud
    data object Fallo : EnvioSolicitud
}

/** Solicitud pendiente que llegó de [de]. */
data class SolicitudRecibida(val id: String, val de: JugadorEncontrado)

/** Amigos en Firestore (T-51, T-52, T-53). Solo se usa con cuenta: el invitado no llega a estas pantallas. */
/**
 * Lo que necesitan los ViewModels de amigos. Es una interfaz para poder probarlos con datos
 * falsos, sin Firebase.
 */
interface FuenteAmigos {
    /** Código de amigo propio; null mientras se genera (RF-39). */
    fun miCodigo(): Flow<String?>

    /** Busca por código exacto o por el inicio del nombre (RF-40). */
    suspend fun buscar(texto: String): List<JugadorEncontrado>

    /** Envía la solicitud de amistad a [para] (RF-40). */
    suspend fun enviarSolicitud(para: String): EnvioSolicitud

    /** Solicitudes pendientes que me llegaron (RF-41). */
    fun recibidas(): Flow<List<SolicitudRecibida>>

    /** Acepta o rechaza una solicitud. Devuelve si se pudo (RF-41). */
    suspend fun responder(solicitudId: String, aceptar: Boolean): Boolean

    /** Amigos aceptados, en orden alfabético. */
    fun amigos(): Flow<List<JugadorEncontrado>>
}

class RepositorioAmigos : FuenteAmigos {

    private val db get() = Nube.firestore

    /**
     * Código de amigo del jugador (RF-39). Si el perfil todavía no tiene uno, la app lo genera y lo
     * reserva (plan Spark: no hay Cloud Functions). Mientras tanto llega null.
     */
    override fun miCodigo(): Flow<String?> = callbackFlow {
        val uid = Nube.uid
        if (uid == null) {
            trySend(null)
            close()
            return@callbackFlow
        }
        var asignando = false
        val registro = Nube.firestore.collection(Colecciones.USUARIOS).document(uid)
            .addSnapshotListener { doc, _ ->
                val codigo = doc?.getString("codigo")
                trySend(codigo)
                if (doc != null && doc.exists() && codigo == null && !asignando) {
                    asignando = true
                    launch { asignarCodigo(uid) }
                }
            }
        awaitClose { registro.remove() }
    }

    /**
     * Reserva un código libre en «codigos/{codigo}» y lo copia al perfil en la misma transacción
     * (reglas de T-51). Si el código ya está ocupado prueba con otro.
     */
    private suspend fun asignarCodigo(uid: String) {
        val base = Nube.firestore
        repeat(5) {
            val codigo = co.edu.univalle.puzzle.dominio.amigos.CodigoAmigo.generar()
            val reservado = runCatching {
                base.runTransaction { tx ->
                    val ref = base.collection(Colecciones.CODIGOS).document(codigo)
                    if (tx.get(ref).exists()) {
                        false
                    } else {
                        tx.set(ref, mapOf("uid" to uid))
                        tx.update(base.collection(Colecciones.USUARIOS).document(uid), "codigo", codigo)
                        true
                    }
                }.await()
            }.getOrDefault(false)
            if (reservado) return
        }
    }

    /** RF-40 (T-52): busca por código exacto o por el inicio del nombre de jugador. */
    override suspend fun buscar(texto: String): List<JugadorEncontrado> = when (val b = Solicitudes.interpretar(texto)) {
        Busqueda.Vacia -> emptyList()
        is Busqueda.PorCodigo -> {
            val uid = db.collection(Colecciones.CODIGOS).document(b.codigo).get().await().getString("uid")
            if (uid == null) emptyList()
            else listOfNotNull(jugador(db.collection(Colecciones.USUARIOS).document(uid).get().await()))
        }
        is Busqueda.PorNombre -> db.collection(Colecciones.USUARIOS)
            .whereGreaterThanOrEqualTo("nombreMinusculas", b.prefijo)
            .whereLessThan("nombreMinusculas", b.prefijo + "")
            .limit(10).get().await().documents.mapNotNull(::jugador)
    }

    /** RF-40 (T-52): envía la solicitud si no cae en ninguno de los casos inválidos. */
    override suspend fun enviarSolicitud(para: String): EnvioSolicitud {
        val yo = Nube.uid ?: return EnvioSolicitud.Fallo
        return try {
            val yaSonAmigos = db.collection(Colecciones.USUARIOS).document(yo)
                .collection(Colecciones.AMIGOS).document(para).get().await().exists()
            val ref = db.collection(Colecciones.SOLICITUDES).document(Solicitudes.id(yo, para))
            val anterior = estadoDe(ref)
            Solicitudes.validar(yo, para, yaSonAmigos, anterior)?.let { return EnvioSolicitud.Invalida(it) }
            if (anterior == EstadoSolicitud.RECHAZADA) {
                ref.update(mapOf("estado" to EstadoSolicitud.PENDIENTE.clave, "creadaEn" to FieldValue.serverTimestamp())).await()
            } else {
                ref.set(mapOf(
                    "de" to yo, "para" to para,
                    "estado" to EstadoSolicitud.PENDIENTE.clave,
                    "creadaEn" to FieldValue.serverTimestamp()
                )).await()
            }
            EnvioSolicitud.Enviada
        } catch (e: Exception) {
            EnvioSolicitud.Fallo
        }
    }

    /** RF-41 (T-53): solicitudes pendientes que me llegaron, con el nombre de quien la envía. */
    override fun recibidas(): Flow<List<SolicitudRecibida>> = callbackFlow {
        val yo = Nube.uid ?: run { trySend(emptyList()); close(); return@callbackFlow }
        val registro = db.collection(Colecciones.SOLICITUDES)
            .whereEqualTo("para", yo)
            .whereEqualTo("estado", EstadoSolicitud.PENDIENTE.clave)
            .orderBy("creadaEn", Query.Direction.DESCENDING)
            .addSnapshotListener { consulta, _ ->
                val docs = consulta?.documents.orEmpty()
                launch {
                    trySend(docs.mapNotNull { d ->
                        val de = d.getString("de") ?: return@mapNotNull null
                        val perfil = runCatching { db.collection(Colecciones.USUARIOS).document(de).get().await() }.getOrNull()
                        SolicitudRecibida(d.id, perfil?.let(::jugador) ?: JugadorEncontrado(de, "Jugador", 0))
                    })
                }
            }
        awaitClose { registro.remove() }
    }

    /**
     * Acepta o rechaza (RF-41). Al aceptar, en la misma operación se crea la amistad en las dos
     * cuentas con el nombre y el avatar de cada uno; las reglas de T-53 solo lo permiten si la
     * solicitud queda aceptada.
     */
    override suspend fun responder(solicitudId: String, aceptar: Boolean): Boolean = runCatching {
        val ref = db.collection(Colecciones.SOLICITUDES).document(solicitudId)
        if (aceptar) aceptarSolicitud(ref) else ref.update("estado", EstadoSolicitud.RECHAZADA.clave).await()
        Unit
    }.isSuccess

    private suspend fun aceptarSolicitud(ref: com.google.firebase.firestore.DocumentReference) {
        val yo = Nube.uid ?: error("sin sesión")
        val otro = ref.get().await().getString("de") ?: error("solicitud sin remitente")
        val usuarios = db.collection(Colecciones.USUARIOS)
        val perfilYo = usuarios.document(yo).get().await()
        val perfilOtro = usuarios.document(otro).get().await()
        fun amistad(perfil: DocumentSnapshot) = mapOf(
            "nombre" to perfil.getString("nombre").orEmpty(),
            "avatar" to (perfil.getLong("avatar") ?: 0L),
            "desde" to FieldValue.serverTimestamp()
        )
        db.batch().apply {
            update(ref, "estado", EstadoSolicitud.ACEPTADA.clave)
            set(usuarios.document(yo).collection(Colecciones.AMIGOS).document(otro), amistad(perfilOtro))
            set(usuarios.document(otro).collection(Colecciones.AMIGOS).document(yo), amistad(perfilYo))
        }.commit().await()
    }

    /** Amigos aceptados, en orden alfabético. */
    override fun amigos(): Flow<List<JugadorEncontrado>> = callbackFlow {
        val yo = Nube.uid ?: run { trySend(emptyList()); close(); return@callbackFlow }
        val registro = db.collection(Colecciones.USUARIOS).document(yo).collection(Colecciones.AMIGOS)
            .orderBy("nombre")
            .addSnapshotListener { consulta, _ -> trySend(consulta?.documents.orEmpty().mapNotNull(::jugador)) }
        awaitClose { registro.remove() }
    }

    /** Estado de una solicitud propia; si no existe, las reglas niegan la lectura y se toma como null. */
    private suspend fun estadoDe(ref: com.google.firebase.firestore.DocumentReference): EstadoSolicitud? = try {
        EstadoSolicitud.desde(ref.get().await().getString("estado"))
    } catch (e: FirebaseFirestoreException) {
        if (e.code == FirebaseFirestoreException.Code.PERMISSION_DENIED) null else throw e
    }

    private fun jugador(doc: DocumentSnapshot): JugadorEncontrado? {
        if (!doc.exists()) return null
        return JugadorEncontrado(doc.id, doc.getString("nombre").orEmpty(), (doc.getLong("avatar") ?: 0).toInt())
    }
}
