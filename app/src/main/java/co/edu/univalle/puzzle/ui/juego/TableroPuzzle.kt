package co.edu.univalle.puzzle.ui.juego

import androidx.compose.animation.core.animateIntOffsetAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.dp
import co.edu.univalle.puzzle.motor.Mezcla
import co.edu.univalle.puzzle.motor.Recortes
import co.edu.univalle.puzzle.motor.Tablero
import co.edu.univalle.puzzle.ui.tema.PuzzleTheme
import kotlin.math.abs

/** Dirección de un deslizamiento sobre el tablero. */
enum class Direccion { ARRIBA, ABAJO, IZQUIERDA, DERECHA }

/**
 * Posición de la pieza que se mueve al deslizar en [direccion] (RF-10): la que está al lado
 * opuesto de la casilla vacía. Devuelve `null` si no hay pieza en ese lado.
 */
fun Tablero.piezaParaDeslizar(direccion: Direccion): Int? {
    val fila = filaDe(posicionVacia)
    val columna = columnaDe(posicionVacia)
    val (f, c) = when (direccion) {
        Direccion.IZQUIERDA -> fila to columna + 1
        Direccion.DERECHA -> fila to columna - 1
        Direccion.ARRIBA -> fila + 1 to columna
        Direccion.ABAJO -> fila - 1 to columna
    }
    return if (f in 0 until tamano && c in 0 until tamano) indiceDe(f, c) else null
}

/**
 * Tablero del puzzle (RF-10): se juega tocando una pieza junto al hueco o deslizando el dedo.
 * Las piezas se deslizan con una animación corta (RNF-01). Si no hay [imagen], muestra números.
 *
 * [alMover] recibe la posición de la pieza que el jugador quiere mover; quien lo usa decide
 * si es legal (con `Tablero.mover`). Con [bloqueado] el tablero no responde (partida terminada).
 */
@Composable
fun TableroPuzzle(
    tablero: Tablero,
    alMover: (posicion: Int) -> Unit,
    modifier: Modifier = Modifier,
    imagen: ImageBitmap? = null,
    mostrarNumeros: Boolean = imagen == null,
    bloqueado: Boolean = false
) {
    val tableroActual by rememberUpdatedState(tablero)
    val moverActual by rememberUpdatedState(alMover)
    val bloqueadoActual by rememberUpdatedState(bloqueado)

    BoxWithConstraints(
        modifier = modifier
            .fillMaxWidth()
            .aspectRatio(1f)
            .clip(RoundedCornerShape(16.dp))
            .background(MaterialTheme.colorScheme.surfaceVariant)
    ) {
        val n = tablero.tamano
        val ladoPx = with(LocalDensity.current) { maxWidth.toPx() }
        val ladoPiezaPx = ladoPx / n
        val ladoPiezaDp = maxWidth / n

        Box(
            Modifier
                .matchParentSize()
                .pointerInput(n) {
                    detectTapGestures { toque ->
                        if (bloqueadoActual) return@detectTapGestures
                        val fila = (toque.y / ladoPiezaPx).toInt().coerceIn(0, n - 1)
                        val columna = (toque.x / ladoPiezaPx).toInt().coerceIn(0, n - 1)
                        moverActual(tableroActual.indiceDe(fila, columna))
                    }
                }
                .pointerInput(n) {
                    var total = Offset.Zero
                    detectDragGestures(
                        onDragStart = { total = Offset.Zero },
                        onDrag = { cambio, arrastre ->
                            cambio.consume()
                            total += arrastre
                        },
                        onDragEnd = {
                            if (!bloqueadoActual) {
                                val minimo = ladoPiezaPx / 4
                                val direccion = when {
                                    abs(total.x) < minimo && abs(total.y) < minimo -> null
                                    abs(total.x) > abs(total.y) ->
                                        if (total.x < 0) Direccion.IZQUIERDA else Direccion.DERECHA
                                    else -> if (total.y < 0) Direccion.ARRIBA else Direccion.ABAJO
                                }
                                direccion?.let { tableroActual.piezaParaDeslizar(it) }?.let(moverActual)
                            }
                        }
                    )
                }
        ) {
            for (posicion in 0 until tablero.totalCasillas) {
                val pieza = tablero.piezaEn(posicion)
                if (pieza == Tablero.VACIA) continue
                key(pieza) {
                    val destino = IntOffset(
                        (tablero.columnaDe(posicion) * ladoPiezaPx).toInt(),
                        (tablero.filaDe(posicion) * ladoPiezaPx).toInt()
                    )
                    val desplazamiento by animateIntOffsetAsState(destino, tween(90), label = "pieza")
                    Pieza(
                        pieza = pieza,
                        tamano = n,
                        imagen = imagen,
                        mostrarNumero = mostrarNumeros,
                        modifier = Modifier.offset { desplazamiento }.size(ladoPiezaDp).padding(1.5.dp)
                    )
                }
            }
        }
    }
}

@Composable
private fun Pieza(pieza: Int, tamano: Int, imagen: ImageBitmap?, mostrarNumero: Boolean, modifier: Modifier) {
    Box(
        modifier
            .clip(RoundedCornerShape(6.dp))
            .background(if (imagen == null) MaterialTheme.colorScheme.primary else Color.Transparent),
        contentAlignment = Alignment.Center
    ) {
        if (imagen != null) {
            val recorte = Recortes.de(pieza, tamano, minOf(imagen.width, imagen.height))
            Canvas(Modifier.matchParentSize()) {
                drawImage(
                    image = imagen,
                    srcOffset = IntOffset(recorte.izquierda, recorte.arriba),
                    srcSize = IntSize(recorte.lado, recorte.lado),
                    dstSize = IntSize(size.width.toInt(), size.height.toInt())
                )
            }
        }
        if (mostrarNumero) {
            Text(
                "$pieza",
                color = Color.White,
                fontWeight = FontWeight.Bold,
                style = MaterialTheme.typography.titleMedium,
                modifier = if (imagen != null) Modifier.align(Alignment.TopStart).padding(4.dp) else Modifier
            )
        }
    }
}

@Preview(showBackground = true, widthDp = 360)
@Composable
private fun VistaTablero() = PuzzleTheme {
    TableroPuzzle(Mezcla.mezclar(4, 7L), alMover = {}, modifier = Modifier.padding(16.dp))
}
