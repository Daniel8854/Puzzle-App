package co.edu.univalle.puzzle.datos.local

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey
import co.edu.univalle.puzzle.motor.Partida
import co.edu.univalle.puzzle.motor.Tablero

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

@Entity(tableName = "partida_en_curso")
data class PartidaGuardada(
    @PrimaryKey val cuentaId: String,
    val modo: String,
    val imagenId: String,
    val tamano: Int,
    // Casillas separadas por comas.
    val casillas: String,
    val movimientos: Int,
    val tiempoMs: Long,
    val limiteMs: Long?
) {
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

@Entity(tableName = "imagenes_descargadas")
data class ImagenDescargada(
    @PrimaryKey val id: String,
    val rutaLocal: String,
    val autor: String,
    val origen: String,
    val fechaMs: Long
)

@Entity(tableName = "fotos_propias", indices = [Index("cuentaId")])
data class FotoPropia(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val cuentaId: String,
    val rutaLocal: String,
    // null mientras la foto no se recorta.
    val rutaRecorte: String? = null,
    val cuadricula: Int = 3,
    val fechaMs: Long
)
