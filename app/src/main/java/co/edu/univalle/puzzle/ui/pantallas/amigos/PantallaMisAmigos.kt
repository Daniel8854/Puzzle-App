package co.edu.univalle.puzzle.ui.pantallas.amigos

import androidx.compose.runtime.Composable
import co.edu.univalle.puzzle.ui.pantallas.PantallaEnConstruccion

// Provisional: la reemplaza T-53.
@Composable
fun PantallaMisAmigos(alAgregar: () -> Unit, alVolver: () -> Unit) {
    PantallaEnConstruccion("Mis amigos", alVolver)
}
