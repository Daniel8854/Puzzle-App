package co.edu.univalle.puzzle.ui.pantallas.partida

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import co.edu.univalle.puzzle.motor.FinPartida
import co.edu.univalle.puzzle.motor.Mezcla
import co.edu.univalle.puzzle.ui.juego.PartidaViewModel
import co.edu.univalle.puzzle.ui.juego.TableroPuzzle
import co.edu.univalle.puzzle.ui.juego.formatoReloj
import co.edu.univalle.puzzle.ui.pantallas.BarraSuperior
import co.edu.univalle.puzzle.ui.pantallas.modos.ModoJuego
import co.edu.univalle.puzzle.ui.pantallas.modos.dificultadDe
import kotlin.random.Random

/**
 * Pantalla 05 · Partida (T-73, versión básica): tablero con la imagen elegida, reloj,
 * movimientos y progreso. Al terminar guarda el resultado en el teléfono (T-17).
 * Pistas, reiniciar y abandonar llegan en el Sprint 2.
 * Dos ViewModels: [PartidaViewModel] lleva el tablero y el reloj, y [EntornoPartidaViewModel]
 * carga la imagen y guarda el resultado.
 */
@Composable
fun PantallaPartida(modo: String, dificultad: Int, imagenId: String, alVolver: () -> Unit) {
    val context = LocalContext.current
    val modoJuego = ModoJuego.desde(modo)
    val nivel = dificultadDe(dificultad)
    val semilla = remember { Random.nextLong() }

    val entorno: EntornoPartidaViewModel = viewModel(
        factory = EntornoPartidaViewModel.fabrica(imagenId, modoJuego.clave, nivel.tamano, context)
    )
    val vm: PartidaViewModel = viewModel(
        factory = PartidaViewModel.fabrica(
            tablero = Mezcla.mezclar(nivel, semilla),
            limiteMs = modoJuego.limiteMs(nivel),
            alTerminar = entorno::terminar
        )
    )
    val estado by vm.estado.collectAsStateWithLifecycle()
    val entornoActual by entorno.estado.collectAsStateWithLifecycle()

    Scaffold(topBar = { BarraSuperior(entornoActual.titulo, alVolver) }) { relleno ->
        Column(
            Modifier.fillMaxSize().padding(relleno).padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text("${modoJuego.nombre} · ${nivel.tamano}×${nivel.tamano} ${nivel.nombre}",
                color = MaterialTheme.colorScheme.onSurfaceVariant)
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceEvenly) {
                Dato(formatoReloj(estado.segundosVisibles), if (modoJuego == ModoJuego.CONTRARRELOJ) "Quedan" else "Tiempo")
                Dato("${estado.partida.movimientos}", "Movimientos")
                Dato("${estado.partida.piezasEnSuLugar}/${estado.partida.totalPiezas}", "Piezas OK")
            }
            LinearProgressIndicator(progress = { estado.partida.progreso }, modifier = Modifier.fillMaxWidth())
            TableroPuzzle(
                tablero = estado.partida.tablero,
                alMover = vm::mover,
                imagen = entornoActual.imagen,
                bloqueado = estado.partida.terminada,
                modifier = Modifier.fillMaxWidth()
            )
        }
    }

    entornoActual.final?.let { r ->
        AlertDialog(
            onDismissRequest = {},
            title = { Text(if (r.fin == FinPartida.COMPLETADA) "¡Puzzle completado!" else "Se acabó el tiempo") },
            text = {
                Text("Movimientos: ${r.movimientos}\nTiempo: ${formatoReloj(r.tiempoMs / 1000)}\n" +
                    "Piezas en su lugar: ${r.piezasEnSuLugar}/${r.totalPiezas}")
            },
            confirmButton = { TextButton(onClick = alVolver) { Text("Volver") } }
        )
    }
}

@Composable
private fun Dato(valor: String, etiqueta: String) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(valor, style = MaterialTheme.typography.titleLarge)
        Text(etiqueta, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
    }
}
