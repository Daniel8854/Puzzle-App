package co.edu.univalle.puzzle.motor

import kotlin.random.Random

// Parte del tablero resuelto y aplica movimientos válidos, así siempre tiene solución.
// La misma semilla da siempre la misma mezcla (los duelos lo necesitan).
object Mezcla {
    const val MOVIMIENTOS_MINIMOS = 50

    fun mezclar(tamano: Int, semilla: Long, movimientos: Int = MOVIMIENTOS_MINIMOS * tamano): Tablero {
        require(movimientos >= MOVIMIENTOS_MINIMOS) { "Se necesitan al menos $MOVIMIENTOS_MINIMOS movimientos" }
        val aleatorio = Random(semilla)
        var tablero = Tablero.resuelto(tamano)
        var anteriorVacia = -1
        repeat(movimientos) {
            // No se deshace el movimiento anterior, para que la mezcla avance.
            val opciones = tablero.posicionesMovibles().filter { it != anteriorVacia }
            val elegida = opciones[aleatorio.nextInt(opciones.size)]
            anteriorVacia = tablero.posicionVacia
            tablero = tablero.mover(elegida)!!
        }
        // Nunca se entrega un tablero ya resuelto.
        if (tablero.estaResuelto()) tablero = tablero.mover(tablero.posicionesMovibles().first())!!
        return tablero
    }

    fun mezclar(dificultad: Dificultad, semilla: Long): Tablero = mezclar(dificultad.tamano, semilla)

    // Paridad de inversiones.
    fun tieneSolucion(tablero: Tablero): Boolean {
        val piezas = tablero.comoLista().filter { it != Tablero.VACIA }
        var inversiones = 0
        for (i in piezas.indices) for (j in i + 1 until piezas.size) if (piezas[i] > piezas[j]) inversiones++
        val n = tablero.tamano
        return if (n % 2 == 1) {
            inversiones % 2 == 0
        } else {
            val filaVaciaDesdeAbajo = n - tablero.filaDe(tablero.posicionVacia)
            (inversiones + filaVaciaDesdeAbajo) % 2 == 1
        }
    }
}
