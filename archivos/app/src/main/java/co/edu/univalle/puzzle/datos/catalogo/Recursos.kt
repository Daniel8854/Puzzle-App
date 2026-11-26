package co.edu.univalle.puzzle.datos.catalogo

import android.content.Context
import android.media.AudioAttributes
import android.media.SoundPool
import co.edu.univalle.puzzle.R

/** Los 8 avatares predefinidos (T-71). El número guardado en el perfil es la posición en esta lista. */
object Avatares {
    val lista = listOf(
        R.drawable.avatar_1, R.drawable.avatar_2, R.drawable.avatar_3, R.drawable.avatar_4,
        R.drawable.avatar_5, R.drawable.avatar_6, R.drawable.avatar_7, R.drawable.avatar_8
    )

    fun de(indice: Int): Int = lista[indice.mod(lista.size)]
}

/** Sonidos del juego (T-71). Respetan la opción «Sonidos» de Configuración (RF-49). */
class Sonidos(context: Context) {
    enum class Tipo { MOVER, ERROR, VICTORIA }

    private val pool = SoundPool.Builder()
        .setMaxStreams(3)
        .setAudioAttributes(AudioAttributes.Builder().setUsage(AudioAttributes.USAGE_GAME).build())
        .build()
    private val ids = mapOf(
        Tipo.MOVER to pool.load(context, R.raw.sonido_mover, 1),
        Tipo.ERROR to pool.load(context, R.raw.sonido_error, 1),
        Tipo.VICTORIA to pool.load(context, R.raw.sonido_victoria, 1)
    )

    fun reproducir(tipo: Tipo, activado: Boolean = true) {
        if (activado) ids[tipo]?.let { pool.play(it, 1f, 1f, 1, 0, 1f) }
    }

    fun liberar() = pool.release()
}
