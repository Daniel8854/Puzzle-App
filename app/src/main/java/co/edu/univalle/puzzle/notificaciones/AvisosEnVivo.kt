package co.edu.univalle.puzzle.notificaciones

import android.content.Context
import co.edu.univalle.puzzle.datos.local.RepositorioPreferencias
import co.edu.univalle.puzzle.datos.nube.Nube
import co.edu.univalle.puzzle.datos.nube.Nube.Colecciones
import co.edu.univalle.puzzle.dominio.duelo.ReglasDuelo
import com.google.firebase.firestore.DocumentChange
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.ListenerRegistration
import com.google.firebase.firestore.Query
import kotlinx.coroutines.awaitCancellation
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

/**
 * Avisos sin servidor (T-62). Con el plan Spark no hay Cloud Functions que envíen push, así que
 * la app escucha en Firestore lo que le llega al jugador y muestra una notificación local:
 *
 * - solicitud de amistad nueva,
 * - desafío de duelo nuevo,
 * - resultado de un duelo en el que jugó.
 *
 * Funciona mientras la app está abierta o en segundo plano. Lo que llegó con la app cerrada se
 * avisa al volver a abrirla; para no repetir avisos se guarda la fecha del último de cada tipo.
 */
class AvisosEnVivo(context: Context) {

    private val app = context.applicationContext
    private val preferencias = RepositorioPreferencias(app)
    private val vistos = app.getSharedPreferences("avisos_vistos", Context.MODE_PRIVATE)

    /** Escucha los avisos de [uid] hasta que se cancele la corrutina (al cerrar sesión o la app). */
    suspend fun escuchar(uid: String): Unit = coroutineScope {
        val db = Nube.firestore
        val solicitudes = db.collection(Colecciones.SOLICITUDES)
        val duelos = db.collection(Colecciones.DUELOS)

        fun alLlegar(consulta: Query, cambio: DocumentChange.Type, accion: suspend (DocumentSnapshot) -> Unit) =
            consulta.addSnapshotListener { resultado, _ ->
                resultado?.documentChanges?.filter { it.type == cambio }?.forEach { c -> launch { accion(c.document) } }
            }

        val registros: List<ListenerRegistration> = listOf(
            alLlegar(solicitudes.whereEqualTo("para", uid).whereEqualTo("estado", "pendiente"), DocumentChange.Type.ADDED) { d ->
                avisar(uid, TipoAviso.SOLICITUDES, d.getString("de"), d.getTimestamp("creadaEn")?.toDate()?.time)
            },
            alLlegar(duelos.whereEqualTo("retado", uid).whereEqualTo("estado", "esperando"), DocumentChange.Type.ADDED) { d ->
                // Un desafío de hace más de 2 minutos ya venció (T-75): no se avisa.
                val creado = d.getTimestamp("creadoEn")?.toDate()?.time
                if (creado != null && System.currentTimeMillis() - creado < ReglasDuelo.ESPERA_RESPUESTA_MS) {
                    avisar(uid, TipoAviso.DESAFIOS, d.getString("retador"), creado)
                }
            },
            alLlegar(duelos.whereEqualTo("retador", uid).whereEqualTo("estado", "finalizado"), DocumentChange.Type.ADDED) { d ->
                avisarResultado(uid, d, rival = d.getString("retado"))
            },
            alLlegar(duelos.whereEqualTo("retado", uid).whereEqualTo("estado", "finalizado"), DocumentChange.Type.ADDED) { d ->
                avisarResultado(uid, d, rival = d.getString("retador"))
            }
        )
        try {
            awaitCancellation()
        } finally {
            registros.forEach { it.remove() }
        }
    }

    private suspend fun avisarResultado(uid: String, duelo: DocumentSnapshot, rival: String?) {
        val ganador = duelo.getString("ganador")
        val resultado = when (ganador) {
            null -> ResultadoAviso.EMPATE
            uid -> ResultadoAviso.GANE
            else -> ResultadoAviso.PERDI
        }
        avisar(uid, TipoAviso.RESULTADOS, rival, duelo.getTimestamp("actualizadoEn")?.toDate()?.time, resultado)
    }

    private suspend fun avisar(uid: String, tipo: TipoAviso, otro: String?, fechaMs: Long?, resultado: ResultadoAviso? = null) {
        val clave = "$uid.${tipo.name}"
        if (otro == null || !Notificaciones.esNuevo(fechaMs, vistos.getLong(clave, 0L))) return
        vistos.edit().putLong(clave, fechaMs!!).apply()
        if (!Notificaciones.quiereAviso(preferencias.de(uid).first(), tipo)) return
        Notificaciones.mostrar(app, Notificaciones.armarAviso(tipo, nombreDe(otro), resultado))
    }

    private suspend fun nombreDe(uid: String): String = runCatching {
        Nube.firestore.collection(Colecciones.USUARIOS).document(uid).get().await().getString("nombre")
    }.getOrNull() ?: "Un jugador"
}
