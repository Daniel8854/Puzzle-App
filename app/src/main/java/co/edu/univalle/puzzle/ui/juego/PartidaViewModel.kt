package co.edu.univalle.puzzle.ui.juego

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import co.edu.univalle.puzzle.motor.Partida
import co.edu.univalle.puzzle.motor.ResultadoPartida
import co.edu.univalle.puzzle.motor.Tablero
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch

/** Lo que muestra la pantalla de partida en cada momento. */
data class EstadoPartida(
    val partida: Partida,
    /** Segundos jugados (Clásico) o que quedan (Contrarreloj). */
    val segundosVisibles: Long
)

/**
 * Lleva la partida en la pantalla (RF-11, RF-12, RF-13): actualiza el reloj cada segundo,
 * aplica los movimientos y avisa una sola vez cuando termina con [alTerminar].
 */
class PartidaViewModel(
    tablero: Tablero,
    limiteMs: Long?,
    private val reloj: () -> Long = System::currentTimeMillis,
    private val alTerminar: (ResultadoPartida) -> Unit = {},
    /** Dónde corre el reloj; en las pruebas se pasa uno que se cancela solo. */
    escopoReloj: CoroutineScope? = null
) : ViewModel() {

    private val _estado = MutableStateFlow(armar(Partida.nueva(tablero, reloj(), limiteMs)))
    val estado: StateFlow<EstadoPartida> = _estado.asStateFlow()
    private var avisado = false

    init {
        (escopoReloj ?: viewModelScope).launch {
            while (isActive) {
                delay(250)
                actualizar(_estado.value.partida.revisarTiempo(reloj()))
                if (_estado.value.partida.terminada) break
            }
        }
    }

    fun mover(posicion: Int) = actualizar(_estado.value.partida.mover(posicion, reloj()))
    fun pausar() = actualizar(_estado.value.partida.pausar(reloj()))
    fun reanudar() = actualizar(_estado.value.partida.reanudar(reloj()))

    private fun actualizar(partida: Partida) {
        _estado.value = armar(partida)
        val resultado = partida.resultado
        if (resultado != null && !avisado) {
            avisado = true
            alTerminar(resultado)
        }
    }

    private fun armar(partida: Partida): EstadoPartida {
        val ahora = reloj()
        val ms = partida.restanteMs(ahora) ?: partida.tiempoMs(ahora)
        return EstadoPartida(partida, ms / 1000)
    }

    companion object {
        fun fabrica(
            tablero: Tablero,
            limiteMs: Long?,
            alTerminar: (ResultadoPartida) -> Unit = {}
        ): ViewModelProvider.Factory = viewModelFactory {
            initializer { PartidaViewModel(tablero, limiteMs, alTerminar = alTerminar) }
        }
    }
}

/** Formato m:ss del cronómetro. */
fun formatoReloj(segundos: Long): String = "%d:%02d".format(segundos / 60, segundos % 60)
