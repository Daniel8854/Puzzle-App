package co.edu.univalle.puzzle.motor

import kotlin.math.abs

/**
 * Movimiento de piezas (RF-10): solo se mueve una pieza que comparte lado
 * con la casilla vacía. Un toque ilegal no cambia nada.
 */

/** Verdadero si la pieza en [indice] está junto a la casilla vacía (no en diagonal). */
fun Tablero.esMovible(indice: Int): Boolean {
    if (indice !in 0 until totalCasillas || indice == posicionVacia) return false
    val distanciaFilas = abs(filaDe(indice) - filaDe(posicionVacia))
    val distanciaColumnas = abs(columnaDe(indice) - columnaDe(posicionVacia))
    return distanciaFilas + distanciaColumnas == 1
}

/** Posiciones de las piezas que se pueden mover ahora mismo (2, 3 o 4). */
fun Tablero.posicionesMovibles(): List<Int> =
    (0 until totalCasillas).filter { esMovible(it) }

/**
 * Mueve la pieza de la posición [indice] a la casilla vacía.
 * Devuelve el tablero nuevo, o `null` si el movimiento no es legal
 * (en ese caso el tablero original no cambia).
 */
fun Tablero.mover(indice: Int): Tablero? =
    if (esMovible(indice)) intercambiar(indice, posicionVacia) else null
