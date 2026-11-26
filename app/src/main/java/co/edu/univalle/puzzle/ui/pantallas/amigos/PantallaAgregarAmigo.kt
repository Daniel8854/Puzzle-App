package co.edu.univalle.puzzle.ui.pantallas.amigos

import androidx.compose.runtime.Composable
import co.edu.univalle.puzzle.ui.pantallas.PantallaEnConstruccion

// Provisional: la reemplazan T-51 y T-52.
@Composable
fun PantallaAgregarAmigo(alVerAmigos: () -> Unit, alVolver: () -> Unit) {
    PantallaEnConstruccion("Agregar amigo", alVolver)
}
