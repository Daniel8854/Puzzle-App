package co.edu.univalle.puzzle.ui.pantallas.amigos

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import co.edu.univalle.puzzle.dominio.amigos.JugadorEncontrado
import co.edu.univalle.puzzle.ui.acceso.ColoresAvatar

/** Círculo de color con la inicial del jugador, como en el Figma. */
@Composable
fun AvatarJugador(jugador: JugadorEncontrado) {
    Box(
        Modifier.size(40.dp).clip(CircleShape).background(ColoresAvatar[jugador.avatar.mod(ColoresAvatar.size)]),
        contentAlignment = Alignment.Center
    ) {
        Text(jugador.nombre.firstOrNull()?.uppercase() ?: "?", color = Color.White, fontWeight = FontWeight.Bold)
    }
}

/** Fila blanca con avatar, nombre, una línea debajo y los botones de la derecha. */
@Composable
fun FilaJugador(jugador: JugadorEncontrado, detalle: String, acciones: @Composable RowScope.() -> Unit) {
    Row(
        Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(Color.White)
            .padding(12.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        AvatarJugador(jugador)
        Column(Modifier.weight(1f)) {
            Text(jugador.nombre, fontWeight = FontWeight.SemiBold)
            Text(detalle, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
        acciones()
    }
}

/** Título de sección («Resultado», «Solicitudes recibidas», «Amigos»). */
@Composable
fun TituloSeccion(texto: String) {
    Text(texto, style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)
}
