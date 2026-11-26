package co.edu.univalle.puzzle.notificaciones

import android.Manifest
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.ContextCompat
import co.edu.univalle.puzzle.MainActivity
import co.edu.univalle.puzzle.R
import co.edu.univalle.puzzle.datos.local.Preferencias

/** Tipos de aviso que el jugador puede activar o desactivar en Configuración (RF-49). */
enum class TipoAviso { SOLICITUDES, DESAFIOS, RESULTADOS }

/** Cómo le fue al jugador en un duelo, para el aviso de resultado. */
enum class ResultadoAviso { GANE, PERDI, EMPATE }

/** Texto de un aviso y la pantalla que abre al tocarlo. */
data class Aviso(val titulo: String, val texto: String, val ruta: String)

/**
 * Notificaciones (T-62, RF-51).
 *
 * Con el plan Spark no hay servidor que envíe push: [AvisosEnVivo] escucha Firestore y llama a
 * [mostrar]. Al tocar la notificación se abre la pantalla indicada en el dato «ruta».
 */
object Notificaciones {

    const val CANAL = "puzzle_avisos"
    const val EXTRA_RUTA = "ruta"

    /** Rutas que puede abrir una notificación; cualquier otra se ignora. */
    val rutasPermitidas = setOf("mis_amigos", "duelo", "ranking", "inicio")

    fun rutaSegura(ruta: String?): String? = ruta?.takeIf { it in rutasPermitidas }

    /** Respeta lo que el jugador desactivó en Configuración (RF-49). */
    fun quiereAviso(preferencias: Preferencias, tipo: TipoAviso): Boolean = when (tipo) {
        TipoAviso.SOLICITUDES -> preferencias.notifSolicitudes
        TipoAviso.DESAFIOS -> preferencias.notifDesafios
        TipoAviso.RESULTADOS -> preferencias.notifResultados
    }

    /** Texto de cada aviso; [nombre] es el otro jugador. */
    fun armarAviso(tipo: TipoAviso, nombre: String, resultado: ResultadoAviso? = null): Aviso = when (tipo) {
        TipoAviso.SOLICITUDES -> Aviso("Nueva solicitud de amistad", "$nombre quiere ser tu amigo", "mis_amigos")
        TipoAviso.DESAFIOS -> Aviso("¡Te retaron a un duelo!", "$nombre te desafía. Tienes 2 minutos para aceptar", "duelo")
        TipoAviso.RESULTADOS -> Aviso(
            "Resultado del duelo",
            when (resultado) {
                ResultadoAviso.GANE -> "Le ganaste a $nombre"
                ResultadoAviso.PERDI -> "$nombre ganó el duelo"
                else -> "Empate con $nombre"
            },
            "duelo"
        )
    }

    /**
     * Solo se avisa lo que pasó después del último aviso mostrado, así no se repite cada vez que
     * se abre la app. Lo que no tiene fecha todavía (se está guardando) se deja para después.
     */
    fun esNuevo(fechaMs: Long?, ultimoAvisadoMs: Long): Boolean = fechaMs != null && fechaMs > ultimoAvisadoMs

    fun crearCanal(context: Context) {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O) return
        val canal = NotificationChannel(CANAL, "Desafíos y amigos", NotificationManager.IMPORTANCE_HIGH).apply {
            description = "Desafíos de duelo, solicitudes de amistad y resultados"
        }
        context.getSystemService(NotificationManager::class.java).createNotificationChannel(canal)
    }

    fun mostrar(context: Context, aviso: Aviso) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU &&
            ContextCompat.checkSelfPermission(context, Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED
        ) return
        crearCanal(context)
        val abrir = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP
            rutaSegura(aviso.ruta)?.let { putExtra(EXTRA_RUTA, it) }
        }
        val pendiente = PendingIntent.getActivity(
            context, aviso.ruta.hashCode(), abrir, PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        )
        val notificacion = NotificationCompat.Builder(context, CANAL)
            .setSmallIcon(R.drawable.ic_launcher_foreground)
            .setContentTitle(aviso.titulo)
            .setContentText(aviso.texto)
            .setAutoCancel(true)
            .setContentIntent(pendiente)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .build()
        NotificationManagerCompat.from(context).notify(System.currentTimeMillis().toInt(), notificacion)
    }
}
