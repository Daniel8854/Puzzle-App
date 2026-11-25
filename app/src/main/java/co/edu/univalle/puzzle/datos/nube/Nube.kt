package co.edu.univalle.puzzle.datos.nube

import android.content.Context
import com.google.firebase.FirebaseApp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

/**
 * Acceso a los servicios de Firebase (T-13): Authentication y Firestore. Todo viaja cifrado por
 * HTTPS con TLS 1.2 o superior (RNF-18).
 *
 * El proyecto usa el plan Spark, que no tiene costo: no se usan Cloud Storage ni Cloud Functions.
 * Las imágenes van dentro de la app o en el teléfono, y lo que antes haría el servidor lo hace la
 * app con las reglas de Firestore validándolo (T-14).
 *
 * Las contraseñas las maneja Firebase Authentication: la app nunca las guarda ni las escribe
 * en Firestore (RNF-14).
 */
object Nube {

    /**
     * Verdadero si la app tiene google-services.json. Sin ese archivo Firebase no arranca;
     * las pantallas en línea deben revisar esto y mostrar un aviso en lugar de cerrarse.
     */
    fun disponible(context: Context): Boolean = FirebaseApp.getApps(context).isNotEmpty()

    val auth: FirebaseAuth get() = FirebaseAuth.getInstance()
    val firestore: FirebaseFirestore get() = FirebaseFirestore.getInstance()

    /** Uid del jugador con sesión iniciada, o `null` (invitado o sin sesión). */
    val uid: String? get() = auth.currentUser?.uid

    /** Nombres de las colecciones de Firestore (las mismas de las reglas de T-14). */
    object Colecciones {
        const val USUARIOS = "usuarios"
        const val NOMBRES = "nombres"
        const val CODIGOS = "codigos"
        const val SOLICITUDES = "solicitudes"
        const val AMIGOS = "amigos"
        const val DUELOS = "duelos"
    }
}
