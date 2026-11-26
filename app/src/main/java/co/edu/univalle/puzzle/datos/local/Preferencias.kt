package co.edu.univalle.puzzle.datos.local

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

/** Configuración del jugador (RF-49). Cada cuenta tiene la suya. */
data class Preferencias(
    val sonidos: Boolean = true,
    val vibracion: Boolean = true,
    val flechasAyuda: Boolean = true,
    val numerosEnPiezas: Boolean = false,
    val notifDesafios: Boolean = true,
    val notifSolicitudes: Boolean = true,
    val notifResultados: Boolean = true
)

/** Cada opción se guarda con el id de la cuenta delante: «uid.sonidos». */
enum class Opcion { SONIDOS, VIBRACION, FLECHAS_AYUDA, NUMEROS_EN_PIEZAS, NOTIF_DESAFIOS, NOTIF_SOLICITUDES, NOTIF_RESULTADOS;

    fun clave(cuentaId: String) = booleanPreferencesKey("$cuentaId.${name.lowercase()}")
}

/** Arma las preferencias de una cuenta a partir de lo guardado (lo que falta toma el valor por defecto). */
fun leerPreferencias(guardado: Preferences, cuentaId: String): Preferencias {
    val porDefecto = Preferencias()
    fun valor(opcion: Opcion, defecto: Boolean) = guardado[opcion.clave(cuentaId)] ?: defecto
    return Preferencias(
        sonidos = valor(Opcion.SONIDOS, porDefecto.sonidos),
        vibracion = valor(Opcion.VIBRACION, porDefecto.vibracion),
        flechasAyuda = valor(Opcion.FLECHAS_AYUDA, porDefecto.flechasAyuda),
        numerosEnPiezas = valor(Opcion.NUMEROS_EN_PIEZAS, porDefecto.numerosEnPiezas),
        notifDesafios = valor(Opcion.NOTIF_DESAFIOS, porDefecto.notifDesafios),
        notifSolicitudes = valor(Opcion.NOTIF_SOLICITUDES, porDefecto.notifSolicitudes),
        notifResultados = valor(Opcion.NOTIF_RESULTADOS, porDefecto.notifResultados)
    )
}

private val Context.almacen: DataStore<Preferences> by preferencesDataStore(name = "preferencias")

/** Guarda y lee la configuración con DataStore; se conserva al cerrar y abrir la app (RNF-08). */
class RepositorioPreferencias(context: Context) {
    private val almacen = context.applicationContext.almacen

    fun de(cuentaId: String): Flow<Preferencias> = almacen.data.map { leerPreferencias(it, cuentaId) }

    suspend fun cambiar(cuentaId: String, opcion: Opcion, activa: Boolean) {
        almacen.edit { it[opcion.clave(cuentaId)] = activa }
    }
}
