package co.edu.univalle.puzzle.datos.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

/** Base de datos local de la app (Room, RNF-08). Se guarda al cerrar y reabrir la app. */
@Database(
    entities = [ResultadoLocal::class, PartidaGuardada::class, ImagenDescargada::class, FotoPropia::class],
    version = 1,
    exportSchema = true
)
abstract class BaseDatosPuzzle : RoomDatabase() {
    abstract fun resultados(): ResultadoDao
    abstract fun partidas(): PartidaGuardadaDao
    abstract fun imagenes(): ImagenDescargadaDao
    abstract fun fotos(): FotoPropiaDao

    companion object {
        @Volatile private var instancia: BaseDatosPuzzle? = null

        fun obtener(context: Context): BaseDatosPuzzle =
            instancia ?: synchronized(this) {
                instancia ?: Room.databaseBuilder(context.applicationContext, BaseDatosPuzzle::class.java, "puzzle.db")
                    .build()
                    .also { instancia = it }
            }
    }
}
