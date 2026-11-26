package co.edu.univalle.puzzle.motor

import kotlin.math.abs

fun Tablero.esMovible(indice: Int): Boolean {
    if (indice !in 0 until totalCasillas || indice == posicionVacia) return false
    val distanciaFilas = abs(filaDe(indice) - filaDe(posicionVacia))
    val distanciaColumnas = abs(columnaDe(indice) - columnaDe(posicionVacia))
    return distanciaFilas + distanciaColumnas == 1
}

fun Tablero.posicionesMovibles(): List<Int> =
    (0 until totalCasillas).filter { esMovible(it) }

fun Tablero.mover(indice: Int): Tablero? =
    if (esMovible(indice)) intercambiar(indice, posicionVacia) else null
