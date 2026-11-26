package co.edu.univalle.puzzle.datos.local

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey
import co.edu.univalle.puzzle.motor.Partida
import co.edu.univalle.puzzle.motor.Tablero

/** Partida terminada, para el progreso local (también el del invitado, RF-03). */
@Entity(tableName = "resultados", indices = [Index("cuentaId")])
data class ResultadoLocal(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val cuentaId: String,
    val modo: String,
    val dificultad: Int,
    val completada: Boolean,
    val movimientos: Int,
    val tiempoMs: Long,
    val fechaMs: Long
)

/** Partida en curso: hay una por cuenta y se recupera al volver a abrir la app (RNF-08). */
@Entity(tableName = "partida_en_curso")
data class PartidaGuardada(
    @PrimaryKey val cuentaId: String,
    val modo: String,
    val imagenId: String,
    val tamano: Int,
    /** Casillas del tablero separadas por comas. */
    val casillas: String,
    val movimientos: Int,
    val tiempoMs: Long,
    val limiteMs: Long?
) {
    /** Devuelve la partida en pausa; la pantalla la reanuda al mostrarse. */
    fun aPartida(): Partida = Partida(
        tablero = Tablero.desde(tamano, casillas.split(",").map { it.trim().toInt() }),
        movimientos = movimientos,
        limiteMs = limiteMs,
        acumuladoMs = tiempoMs
    )

    companion object {
        fun desde(cuentaId: String, modo: String, imagenId: String, partida: Partida, ahoraMs: Long) =
            PartidaGuardada(
                cuentaId = cuentaId,
                modo = modo,
                imagenId = imagenId,
                tamano = partida.tablero.tamano,
                casillas = partida.tablero.comoLista().joinToString(","),
                movimientos = partida.movimientos,
                tiempoMs = partida.tiempoMs(ahoraMs),
                limiteMs = partida.limiteMs
            )
    }
}

/** Imagen del catálogo ya descargada, para jugarla sin conexión (RNF-08, RNF-13). */
@Entity(tableName = "imagenes_descargadas")
data class ImagenDescargada(
    @PrimaryKey val id: String,
    val rutaLocal: String,
    val autor: String,
    val origen: String,
    val fechaMs: Long
)

/** Foto propia del jugador: se guarda una copia local (RF-34, RNF-08). */
@Entity(tableName = "fotos_propias", indices = [Index("cuentaId")])
data class FotoPropia(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val cuentaId: String,
    /** Foto ya comprimida (máx. 1080×1080 y 500 KB). */
    val rutaLocal: String,
    /** Recorte cuadrado listo para jugar (T-47); `null` mientras no se recorta. */
    val rutaRecorte: String? = null,
    /** Tamaño de la cuadrícula elegido al recortar (3, 4 o 5). */
    val cuadricula: Int = 3,
    val fechaMs: Long
)
