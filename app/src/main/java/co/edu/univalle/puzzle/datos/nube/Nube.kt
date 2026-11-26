package co.edu.univalle.puzzle.datos.nube

import android.content.Context
import com.google.firebase.FirebaseApp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

// Plan Spark: sin Cloud Storage ni Cloud Functions; las reglas de Firestore validan lo que escribe la app.
object Nube {
    // Sin google-services.json Firebase no arranca; las pantallas en línea deben revisar esto.
    fun disponible(context: Context): Boolean = FirebaseApp.getApps(context).isNotEmpty()

    val auth: FirebaseAuth get() = FirebaseAuth.getInstance()
    val firestore: FirebaseFirestore get() = FirebaseFirestore.getInstance()

    val uid: String? get() = auth.currentUser?.uid

    object Colecciones {
        const val USUARIOS = "usuarios"
        const val NOMBRES = "nombres"
        const val CODIGOS = "codigos"
        const val SOLICITUDES = "solicitudes"
        const val AMIGOS = "amigos"
        const val DUELOS = "duelos"
    }
}
