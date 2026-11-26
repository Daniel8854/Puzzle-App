package co.edu.univalle.puzzle.ui.pantallas.misfotos

import androidx.compose.runtime.Composable
import co.edu.univalle.puzzle.ui.pantallas.PantallaEnConstruccion

// Provisional: la reemplaza T-49.
@Composable
fun PantallaMisFotos(alAgregarFoto: () -> Unit, alVolver: () -> Unit) {
    PantallaEnConstruccion("Mis fotos", alVolver)
}
