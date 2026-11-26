package co.edu.univalle.puzzle.ui.pantallas.modos

import co.edu.univalle.puzzle.motor.Dificultad
import co.edu.univalle.puzzle.motor.Partida

/** Modos de juego individuales (RF-10). [clave] es lo que viaja en la ruta de la partida. */
enum class ModoJuego(val clave: String, val nombre: String, val descripcion: String) {
    CLASICO("clasico", "Clásico", "Sin límite de tiempo. Relájate y arma."),
    CONTRARRELOJ("contrarreloj", "Contrarreloj", "Termina antes de que acabe el reloj.");

    /** Límite de tiempo en ms; null en Clásico (RF-20). */
    fun limiteMs(dificultad: Dificultad): Long? =
        if (this == CONTRARRELOJ) Partida.limiteContrarreloj(dificultad) else null

    companion object {
        fun desde(clave: String): ModoJuego = entries.firstOrNull { it.clave == clave } ?: CLASICO
    }
}

/** Dificultad a partir del tamaño del tablero que viaja en la ruta (3, 4 o 5). */
fun dificultadDe(tamano: Int): Dificultad = Dificultad.entries.firstOrNull { it.tamano == tamano } ?: Dificultad.FACIL

/** Lo que el jugador eligió antes de empezar. Solo se puede jugar con una imagen elegida. */
data class Eleccion(
    val modo: ModoJuego = ModoJuego.CLASICO,
    val dificultad: Dificultad = Dificultad.FACIL,
    val imagenId: String? = null
) {
    val lista: Boolean get() = imagenId != null
}
