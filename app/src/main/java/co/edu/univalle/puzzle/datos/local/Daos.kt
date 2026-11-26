package co.edu.univalle.puzzle.datos.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Upsert
import kotlinx.coroutines.flow.Flow

// Todas las consultas filtran por cuenta.
@Dao
interface ResultadoDao {
    @Insert
    suspend fun insertar(resultado: ResultadoLocal): Long

    @Query("SELECT * FROM resultados WHERE cuentaId = :cuentaId ORDER BY fechaMs DESC")
    fun deCuenta(cuentaId: String): Flow<List<ResultadoLocal>>

    @Query("SELECT COUNT(*) FROM resultados WHERE cuentaId = :cuentaId AND completada = 1")
    suspend fun completadas(cuentaId: String): Int

    @Query("DELETE FROM resultados WHERE cuentaId = :cuentaId")
    suspend fun borrarDeCuenta(cuentaId: String)
}

@Dao
interface PartidaGuardadaDao {
    @Upsert
    suspend fun guardar(partida: PartidaGuardada)

    @Query("SELECT * FROM partida_en_curso WHERE cuentaId = :cuentaId")
    suspend fun deCuenta(cuentaId: String): PartidaGuardada?

    @Query("DELETE FROM partida_en_curso WHERE cuentaId = :cuentaId")
    suspend fun borrar(cuentaId: String)
}

@Dao
interface ImagenDescargadaDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun guardar(imagen: ImagenDescargada)

    @Query("SELECT * FROM imagenes_descargadas ORDER BY fechaMs DESC")
    fun todas(): Flow<List<ImagenDescargada>>

    @Query("SELECT * FROM imagenes_descargadas WHERE id = :id")
    suspend fun porId(id: String): ImagenDescargada?
}

@Dao
interface FotoPropiaDao {
    @Insert
    suspend fun insertar(foto: FotoPropia): Long

    @Upsert
    suspend fun actualizar(foto: FotoPropia)

    @Query("SELECT * FROM fotos_propias WHERE cuentaId = :cuentaId ORDER BY fechaMs DESC")
    fun deCuenta(cuentaId: String): Flow<List<FotoPropia>>

    @Query("SELECT * FROM fotos_propias WHERE id = :id AND cuentaId = :cuentaId")
    suspend fun porId(id: Long, cuentaId: String): FotoPropia?

    @Query("DELETE FROM fotos_propias WHERE id = :id AND cuentaId = :cuentaId")
    suspend fun borrar(id: Long, cuentaId: String)
}
