package co.edu.univalle.puzzle.motor

/** Dificultades del juego: el tablero es de [tamano] × [tamano] casillas. */
enum class Dificultad(val tamano: Int, val nombre: String) {
    FACIL(3, "Fácil"),
    MEDIO(4, "Medio"),
    DIFICIL(5, "Difícil")
}
