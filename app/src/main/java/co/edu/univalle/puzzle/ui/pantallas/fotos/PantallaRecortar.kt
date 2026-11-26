package co.edu.univalle.puzzle.ui.pantallas.fotos

import android.graphics.Bitmap
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTransformGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.systemBarsPadding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import co.edu.univalle.puzzle.datos.fotos.MarcoRecorte
import co.edu.univalle.puzzle.datos.fotos.transformarFoto

private val FondoOscuro = Color(0xFF15141D)
private val BotonOscuro = Color(0xFF2A2935)
private val TextoTenue = Color(0xFFB4B2C4)
private val Naranja = Color(0xFFFFB020)

/**
 * Pantalla 23 · Recortar foto (RF-35): el puzzle siempre es cuadrado. Se mueve con un dedo,
 * se acerca con dos y se cambia la cuadrícula 3×3, 4×4 o 5×5, que se dibuja sobre la foto.
 * La pantalla solo dibuja y mueve el marco; cargar y guardar lo hace [RecortarViewModel].
 */
@Composable
fun PantallaRecortar(
    fotoId: Long,
    alListo: () -> Unit,
    alVolver: () -> Unit,
    vm: RecortarViewModel = viewModel(factory = RecortarViewModel.fabrica(fotoId, LocalContext.current))
) {
    val estado by vm.estado.collectAsStateWithLifecycle()
    LaunchedEffect(estado.listo) { if (estado.listo) alListo() }

    val original = estado.original
    val imagen = remember(original, estado.rotacion, estado.volteada) {
        original?.let { transformarFoto(it, estado.rotacion, estado.volteada) }
    }
    var marco by remember(imagen) { mutableStateOf(imagen?.let { MarcoRecorte(it.width, it.height) }) }
    val n = estado.cuadricula

    Column(Modifier.fillMaxSize().background(FondoOscuro).systemBarsPadding()) {
        Row(
            Modifier.fillMaxWidth().padding(horizontal = 8.dp, vertical = 4.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            TextButton(onClick = alVolver) { Text("Cancelar", color = Color.White) }
            Text(
                "Recortar",
                color = Color.White,
                fontWeight = FontWeight.Bold,
                fontSize = 17.sp,
                textAlign = TextAlign.Center,
                modifier = Modifier.weight(1f)
            )
            TextButton(
                onClick = { marco?.cuadrado()?.let(vm::guardar) },
                enabled = imagen != null && !estado.guardando
            ) {
                Text(if (estado.guardando) "Guardando…" else "Listo", color = Naranja, fontWeight = FontWeight.Bold)
            }
        }

        Box(Modifier.fillMaxWidth().weight(1f).padding(24.dp), contentAlignment = Alignment.Center) {
            val bmp = imagen
            val m = marco
            if (bmp == null || m == null) {
                if (estado.cargando) CircularProgressIndicator(color = Color.White)
                else Text("No se pudo abrir la foto", color = Color.White)
            } else {
                Box(contentAlignment = Alignment.BottomCenter) {
                    VisorRecorte(bmp, m, n) { marco = it }
                    Text(
                        "$n×$n · ${n * n - 1} piezas",
                        color = Color.White,
                        style = androidx.compose.material3.MaterialTheme.typography.labelMedium,
                        modifier = Modifier
                            .padding(bottom = 12.dp)
                            .clip(RoundedCornerShape(50))
                            .background(Color.Black.copy(alpha = 0.55f))
                            .padding(horizontal = 12.dp, vertical = 4.dp)
                    )
                }
            }
        }

        estado.error?.takeIf { imagen != null }?.let {
            Text(it, color = Color(0xFFFF8A80), textAlign = TextAlign.Center, modifier = Modifier.fillMaxWidth())
        }
        Text(
            "El puzzle siempre es cuadrado. Arrastra y pellizca para encuadrar.",
            color = TextoTenue,
            textAlign = TextAlign.Center,
            style = androidx.compose.material3.MaterialTheme.typography.bodySmall,
            modifier = Modifier.fillMaxWidth().padding(horizontal = 32.dp)
        )
        Spacer(Modifier.height(20.dp))
        Row(
            Modifier.fillMaxWidth().padding(bottom = 28.dp),
            horizontalArrangement = Arrangement.spacedBy(40.dp, Alignment.CenterHorizontally)
        ) {
            BotonHerramienta("⌗", "Cuadrícula") { vm.elegirCuadricula(if (n >= 5) 3 else n + 1) }
            BotonHerramienta("↺", "Restablecer") { marco = imagen?.let { MarcoRecorte(it.width, it.height) } }
        }
    }
}

@Composable
private fun BotonHerramienta(icono: String, texto: String, alPulsar: () -> Unit) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier.clip(RoundedCornerShape(12.dp)).clickable(role = Role.Button, onClick = alPulsar).padding(4.dp)
    ) {
        Box(
            Modifier.size(48.dp).clip(RoundedCornerShape(12.dp)).background(BotonOscuro),
            contentAlignment = Alignment.Center
        ) { Text(icono, color = Color.White, fontSize = 20.sp) }
        Spacer(Modifier.height(6.dp))
        Text(texto, color = TextoTenue, style = androidx.compose.material3.MaterialTheme.typography.labelSmall)
    }
}

/** Muestra la parte de la foto que queda dentro del marco, la cuadrícula de cortes y las esquinas. */
@Composable
private fun VisorRecorte(imagen: Bitmap, marco: MarcoRecorte, cuadricula: Int, alCambiar: (MarcoRecorte) -> Unit) {
    val bitmap = remember(imagen) { imagen.asImageBitmap() }
    var actual by remember(marco) { mutableStateOf(marco) }
    Canvas(
        Modifier
            .fillMaxWidth()
            .aspectRatio(1f)
            .clipToBounds()
            .pointerInput(imagen) {
                detectTransformGestures { _, desplazamiento: Offset, zoom, _ ->
                    val pixelesPorPunto = actual.lado / size.width
                    actual = actual.ampliar(zoom).mover(-desplazamiento.x * pixelesPorPunto, -desplazamiento.y * pixelesPorPunto)
                    alCambiar(actual)
                }
            }
    ) {
        val r = marco.cuadrado()
        drawImage(
            image = bitmap,
            srcOffset = IntOffset(r.izquierda, r.arriba),
            srcSize = IntSize(r.lado, r.lado),
            dstSize = IntSize(size.width.toInt(), size.height.toInt())
        )
        val linea = Color.White.copy(alpha = 0.7f)
        val paso = size.width / cuadricula
        for (i in 1 until cuadricula) {
            drawLine(linea, Offset(paso * i, 0f), Offset(paso * i, size.height), strokeWidth = 1.dp.toPx())
            drawLine(linea, Offset(0f, paso * i), Offset(size.width, paso * i), strokeWidth = 1.dp.toPx())
        }
        // Esquinas del marco
        val largo = 22.dp.toPx()
        val grosor = 3.dp.toPx()
        val w = size.width
        val h = size.height
        listOf(
            Offset(0f, 0f) to Offset(1f, 1f), Offset(w, 0f) to Offset(-1f, 1f),
            Offset(0f, h) to Offset(1f, -1f), Offset(w, h) to Offset(-1f, -1f)
        ).forEach { (p, d) ->
            drawLine(Color.White, p, Offset(p.x + largo * d.x, p.y), strokeWidth = grosor)
            drawLine(Color.White, p, Offset(p.x, p.y + largo * d.y), strokeWidth = grosor)
        }
    }
}
