package co.edu.univalle.puzzle.datos.progreso

import android.content.Context
import co.edu.univalle.puzzle.datos.local.BaseDatosPuzzle
import co.edu.univalle.puzzle.datos.local.PartidaGuardada
import co.edu.univalle.puzzle.datos.local.ResultadoLocal
import co.edu.univalle.puzzle.datos.local.SesionLocal
import co.edu.univalle.puzzle.motor.FinPartida
import co.edu.univalle.puzzle.motor.ResultadoPartida
import kotlinx.coroutines.flow.Flow

/**
 * Progreso del jugador en el teléfono (T-17). Sirve igual para el invitado y para una cuenta:
 * todo se guarda con la cuenta activa de [SesionLocal]. El invitado nunca sube nada a la nube,
 * así que no aparece en ningún ranking (RF-03).
 */
class RepositorioProgreso(context: Context) {

    private val bd = BaseDatosPuzzle.obtener(context.applicationContext)

    /** Guarda el resultado de una partida terminada y borra la partida en curso. */
    suspend fun registrar(modo: String, dificultad: Int, resultado: ResultadoPartida, ahoraMs: Long = System.currentTimeMillis()) {
        val cuenta = SesionLocal.cuentaId.value
        bd.resultados().insertar(resultadoLocal(cuenta, modo, dificultad, resultado, ahoraMs))
        bd.partidas().borrar(cuenta)
    }

    suspend fun guardarEnCurso(partida: PartidaGuardada) = bd.partidas().guardar(partida)

    suspend fun partidaEnCurso(): PartidaGuardada? = bd.partidas().deCuenta(SesionLocal.cuentaId.value)

    fun historial(): Flow<List<ResultadoLocal>> = bd.resultados().deCuenta(SesionLocal.cuentaId.value)

    suspend fun completadas(): Int = bd.resultados().completadas(SesionLocal.cuentaId.value)

    /** Verdadero si se puede subir el progreso a la nube: solo con cuenta, nunca el invitado. */
    fun puedeSubirALaNube(): Boolean = !SesionLocal.esInvitado
}

/** Convierte el resultado del motor en la fila que se guarda en el teléfono. */
fun resultadoLocal(cuentaId: String, modo: String, dificultad: Int, r: ResultadoPartida, ahoraMs: Long) = ResultadoLocal(
    cuentaId = cuentaId,
    modo = modo,
    dificultad = dificultad,
    completada = r.fin == FinPartida.COMPLETADA,
    movimientos = r.movimientos,
    tiempoMs = r.tiempoMs,
    fechaMs = ahoraMs
)
