package co.edu.univalle.puzzle.ui.navegacion

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.systemBarsPadding
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import co.edu.univalle.puzzle.datos.local.SesionLocal
import co.edu.univalle.puzzle.ui.acceso.BotonPrincipal
import co.edu.univalle.puzzle.ui.acceso.EncabezadoAcceso
import co.edu.univalle.puzzle.ui.tema.PuzzleTheme

/**
 * Envuelve las secciones que requieren cuenta: Amigos, Duelo VS y Ranking (T-17, RF-03).
 * Al invitado le muestra la pantalla 26 con la invitación a registrarse; con cuenta muestra [contenido].
 */
@Composable
fun SeccionProtegida(
    titulo: String,
    alCrearCuenta: () -> Unit,
    alVolver: () -> Unit,
    contenido: @Composable () -> Unit
) {
    val cuenta by SesionLocal.cuentaId.collectAsStateWithLifecycle()
    if (cuenta == SesionLocal.INVITADO) {
        FuncionBloqueada(titulo, alCrearCuenta, alVolver)
    } else {
        contenido()
    }
}

/** Subtítulo de cada sección bloqueada, como en el encabezado del diseño. */
private fun subtituloDe(titulo: String) = when (titulo) {
    "Duelo VS" -> "Reta a un amigo al mismo puzzle"
    "Amigos" -> "Juega duelos con tus amigos"
    "Ranking" -> "Compite con los mejores tiempos"
    else -> null
}

/** Pantalla 26 · Invitado · Función bloqueada. */
@Composable
fun FuncionBloqueada(titulo: String, alCrearCuenta: () -> Unit, alVolver: () -> Unit) {
    Column(
        Modifier.fillMaxSize().systemBarsPadding().padding(horizontal = 24.dp, vertical = 20.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Box(Modifier.fillMaxWidth()) { EncabezadoAcceso(titulo, subtituloDe(titulo), alVolver) }
        Spacer(Modifier.height(40.dp))
        Box(
            Modifier.size(190.dp).clip(CircleShape).background(MaterialTheme.colorScheme.primaryContainer),
            contentAlignment = Alignment.Center
        ) {
            Row(horizontalArrangement = Arrangement.spacedBy(10.dp), verticalAlignment = Alignment.CenterVertically) {
                Box(
                    Modifier.size(52.dp).clip(CircleShape).background(Color(0xFFF2A93B)),
                    contentAlignment = Alignment.Center
                ) { Text("J", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 20.sp) }
                val morado = MaterialTheme.colorScheme.primary
                Box(
                    Modifier
                        .size(52.dp)
                        .clip(CircleShape)
                        .background(Color.White)
                        .drawBehind {
                            drawCircle(
                                morado,
                                style = Stroke(2.dp.toPx(), pathEffect = PathEffect.dashPathEffect(floatArrayOf(6f, 6f)))
                            )
                        },
                    contentAlignment = Alignment.Center
                ) { Text("?", color = morado, fontWeight = FontWeight.Bold, fontSize = 20.sp) }
            }
        }
        Spacer(Modifier.height(28.dp))
        Text("Necesitas una cuenta", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
        Spacer(Modifier.height(8.dp))
        Text(
            "Amigos, duelos y ranking requieren una cuenta. Tu progreso de invitado se queda en este teléfono.",
            textAlign = TextAlign.Center,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Spacer(Modifier.height(24.dp))
        BotonPrincipal("Crear cuenta", habilitado = true, alPulsar = alCrearCuenta)
    }
}

@Preview(showBackground = true, widthDp = 360, heightDp = 760, name = "26 · Invitado · Función bloqueada")
@Composable
private fun VistaBloqueada() = PuzzleTheme { FuncionBloqueada("Duelo VS", {}, {}) }
