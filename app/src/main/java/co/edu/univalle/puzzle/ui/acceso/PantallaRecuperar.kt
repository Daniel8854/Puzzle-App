package co.edu.univalle.puzzle.ui.acceso

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.systemBarsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import co.edu.univalle.puzzle.ui.tema.PuzzleTheme

private val VerdeExito = Color(0xFF1FA67A)
private val FondoExito = Color(0xFFE6F7F0)

/** Pantalla 21 · Recuperar contraseña (RF-04). */
@Composable
fun PantallaRecuperar(vm: RecuperarViewModel, alVolver: () -> Unit) {
    val estado by vm.estado.collectAsStateWithLifecycle()
    ContenidoRecuperar(estado, vm::cambiarCorreo, vm::enviar, alVolver)
}

@Composable
fun ContenidoRecuperar(
    estado: EstadoRecuperar,
    alCambiarCorreo: (String) -> Unit,
    alEnviar: () -> Unit,
    alVolver: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .systemBarsPadding()
            .imePadding()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 24.dp, vertical = 20.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier
                .size(44.dp)
                .clip(RoundedCornerShape(12.dp))
                .background(Color.White)
                .clickable(role = Role.Button, onClickLabel = "Volver", onClick = alVolver)
        ) { Text("←", style = MaterialTheme.typography.titleLarge) }

        Column(Modifier.fillMaxWidth(), horizontalAlignment = Alignment.CenterHorizontally) {
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier.size(104.dp).clip(CircleShape).background(MaterialTheme.colorScheme.primaryContainer)
            ) { Candado(MaterialTheme.colorScheme.primary) }
            Spacer(Modifier.height(20.dp))
            Text(
                "¿Olvidaste tu contraseña?",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center
            )
            Spacer(Modifier.height(6.dp))
            Text(
                "Escribe tu correo y te enviaremos un enlace para crear una nueva.",
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center
            )
        }

        CampoAcceso("Correo electrónico", estado.correo, alCambiarCorreo, "jugador@correo.com",
            error = estado.error, teclado = KeyboardType.Email)

        BotonPrincipal(
            "Enviar enlace",
            habilitado = estado.correo.isNotBlank() && !estado.enviado,
            cargando = estado.cargando,
            alPulsar = alEnviar
        )

        if (estado.enviado) {
            Row(
                Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(14.dp))
                    .background(FondoExito)
                    .padding(16.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Box(
                    contentAlignment = Alignment.Center,
                    modifier = Modifier.size(28.dp).clip(CircleShape).background(VerdeExito)
                ) { Text("✓", color = Color.White, fontWeight = FontWeight.Bold) }
                Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                    Text("¡Revisa tu correo!", fontWeight = FontWeight.SemiBold)
                    Text(
                        "Enviamos el enlace a ${estado.correo.trim()}. Puede tardar unos minutos.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
            TextButton(onClick = alEnviar, enabled = estado.puedeReenviar, modifier = Modifier.align(Alignment.CenterHorizontally)) {
                val s = estado.segundosParaReenviar
                Text(
                    if (s > 0) "¿No te llegó? Reenviar en 0:${s.toString().padStart(2, '0')}" else "¿No te llegó? Reenviar",
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }

        Spacer(Modifier.height(24.dp))
        EnlaceInferior("¿Recordaste tu contraseña?", "Inicia sesión", alVolver)
    }
}

/** Candado del diseño 21, dibujado para no depender de una librería de íconos. */
@Composable
private fun Candado(color: Color) {
    Canvas(Modifier.size(width = 44.dp, height = 50.dp)) {
        val grosor = 5.dp.toPx()
        val ancho = size.width
        val arcoAncho = ancho * 0.6f
        drawArc(
            color = color,
            startAngle = 180f,
            sweepAngle = 180f,
            useCenter = false,
            topLeft = Offset((ancho - arcoAncho) / 2, grosor / 2),
            size = Size(arcoAncho, arcoAncho),
            style = Stroke(grosor)
        )
        drawRect(color, Offset((ancho - arcoAncho) / 2 - grosor / 2, arcoAncho / 2), Size(grosor, size.height * 0.2f))
        drawRect(color, Offset((ancho + arcoAncho) / 2 - grosor / 2, arcoAncho / 2), Size(grosor, size.height * 0.2f))
        drawRoundRect(
            color = color,
            topLeft = Offset(0f, size.height * 0.42f),
            size = Size(ancho, size.height * 0.58f),
            cornerRadius = CornerRadius(8.dp.toPx())
        )
        drawCircle(Color.White, radius = 4.dp.toPx(), center = Offset(ancho / 2, size.height * 0.68f))
    }
}

@Preview(showBackground = true, widthDp = 360, heightDp = 800, name = "21 · Recuperar")
@Composable
private fun VistaRecuperar() = PuzzleTheme {
    ContenidoRecuperar(EstadoRecuperar(correo = "jugador@correo.com", enviado = true, segundosParaReenviar = 45), {}, {}, {})
}
