package co.edu.univalle.puzzle.datos.progreso

import android.content.Context
import co.edu.univalle.puzzle.datos.local.BaseDatosPuzzle
import co.edu.univalle.puzzle.datos.local.PartidaGuardada
import co.edu.univalle.puzzle.datos.local.ResultadoLocal
import co.edu.univalle.puzzle.datos.local.SesionLocal
import co.edu.univalle.puzzle.motor.FinPartida
import co.edu.univalle.puzzle.motor.ResultadoPartida
import kotlinx.coroutines.flow.Flow

// El invitado nunca sube nada a la nube, así que no aparece en ningún ranking.
class RepositorioProgreso(context: Context) {
    private val bd = BaseDatosPuzzle.obtener(context.applicationContext)

    suspend fun registrar(modo: String, dificultad: Int, resultado: ResultadoPartida, ahoraMs: Long = System.currentTimeMillis()) {
        val cuenta = SesionLocal.cuentaId.value
        bd.resultados().insertar(resultadoLocal(cuenta, modo, dificultad, resultado, ahoraMs))
        bd.partidas().borrar(cuenta)
    }

    suspend fun guardarEnCurso(partida: PartidaGuardada) = bd.partidas().guardar(partida)

    suspend fun partidaEnCurso(): PartidaGuardada? = bd.partidas().deCuenta(SesionLocal.cuentaId.value)

    fun historial(): Flow<List<ResultadoLocal>> = bd.resultados().deCuenta(SesionLocal.cuentaId.value)

    suspend fun completadas(): Int = bd.resultados().completadas(SesionLocal.cuentaId.value)

    fun puedeSubirALaNube(): Boolean = !SesionLocal.esInvitado
}

fun resultadoLocal(cuentaId: String, modo: String, dificultad: Int, r: ResultadoPartida, ahoraMs: Long) = ResultadoLocal(
    cuentaId = cuentaId,
    modo = modo,
    dificultad = dificultad,
    completada = r.fin == FinPartida.COMPLETADA,
    movimientos = r.movimientos,
    tiempoMs = r.tiempoMs,
    fechaMs = ahoraMs
)
