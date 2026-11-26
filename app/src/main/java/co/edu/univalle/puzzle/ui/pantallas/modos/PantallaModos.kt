package co.edu.univalle.puzzle.ui.pantallas.modos

import androidx.compose.runtime.Composable
import co.edu.univalle.puzzle.ui.pantallas.PantallaEnConstruccion

// Provisional: la reemplaza T-73.
@Composable
fun PantallaModos(alVolver: () -> Unit, alJugar: (modo: String, dificultad: Int, imagenId: String) -> Unit) {
    PantallaEnConstruccion("Modos de juego", alVolver)
}
