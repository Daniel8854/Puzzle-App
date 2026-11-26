package co.edu.univalle.puzzle.ui.pantallas.amigos

import android.content.ClipData
import android.content.ClipboardManager
import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.systemBarsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.OutlinedTextField
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardCapitalization
import co.edu.univalle.puzzle.dominio.amigos.CodigoAmigo
import co.edu.univalle.puzzle.ui.acceso.EncabezadoAcceso

private val Rojo = Color(0xFFE0384C)
private val Rosa = Color(0xFFB07CF7)

/**
 * Pantalla 18 · Agregar amigo. T-51: código propio con Copiar (RF-39).
 * T-52: buscar por código exacto y enviar la solicitud (RF-40).
 * T-53: solicitudes recibidas con Aceptar y Rechazar (RF-41).
 * La pantalla solo dibuja; los datos y las acciones están en los ViewModels.
 */
@Composable
fun PantallaAgregarAmigo(
    alVerAmigos: () -> Unit,
    alVolver: () -> Unit,
    vm: AgregarAmigoViewModel = viewModel(factory = AgregarAmigoViewModel.fabrica()),
) {
    val context = LocalContext.current
    val estado by vm.estado.collectAsStateWithLifecycle()
    val codigo = estado.codigo

    LaunchedEffect(estado.mensaje) {
        estado.mensaje?.let {
            Toast.makeText(context, it, Toast.LENGTH_SHORT).show()
            vm.mensajeMostrado()
        }
    }

    Column(
        Modifier.fillMaxSize().systemBarsPadding().verticalScroll(rememberScrollState()).padding(20.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        EncabezadoAcceso("Agregar amigo", "Juega duelos con tus amigos", alVolver)

        // Tarjeta «TU CÓDIGO» (T-51)
        Column(
            Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(20.dp))
                .background(Brush.linearGradient(listOf(MaterialTheme.colorScheme.primary, Rosa)))
                .padding(18.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text("TU CÓDIGO", color = Color(0xFFFFD27A), style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold)
            Text(codigo ?: "Generando…", color = Color.White, fontSize = 30.sp, fontWeight = FontWeight.Bold, letterSpacing = 1.sp)
            Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                BotonBlanco("Copiar", Modifier.weight(1f), habilitado = codigo != null) {
                    context.getSystemService(ClipboardManager::class.java)
                        .setPrimaryClip(ClipData.newPlainText("Código de amigo", codigo))
                    Toast.makeText(context, "Código copiado", Toast.LENGTH_SHORT).show()
                }
                BotonBlanco("Ver mis amigos", Modifier.weight(1f), habilitado = true, alPulsar = alVerAmigos)
            }
        }

        // Buscar por código exacto (T-52)
        OutlinedTextField(
            value = estado.texto,
            onValueChange = vm::cambiarTexto,
            label = { Text("Buscar por código exacto") },
            placeholder = { Text("Ej. LM4K7XQ2") },
            singleLine = true,
            shape = RoundedCornerShape(14.dp),
            keyboardOptions = KeyboardOptions(capitalization = KeyboardCapitalization.Characters, imeAction = ImeAction.Search),
            keyboardActions = KeyboardActions(onSearch = { vm.buscar() }),
            trailingIcon = {
                if (estado.buscando) CircularProgressIndicator(Modifier.padding(12.dp), strokeWidth = 2.dp)
                else androidx.compose.material3.TextButton(onClick = vm::buscar) { Text("Buscar") }
            },
            modifier = Modifier.fillMaxWidth()
        )
        estado.resultados?.let { resultados ->
            TituloSeccion("Resultado")
            if (resultados.isEmpty()) {
                Text("No hay ningún jugador con ese código", color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
            val buscado = CodigoAmigo.normalizar(estado.texto)
            resultados.forEach { jugador ->
                FilaJugador(jugador, if (CodigoAmigo.esValido(buscado)) "Código $buscado" else "Jugador") {
                    Button(onClick = { vm.enviarSolicitud(jugador) }, shape = RoundedCornerShape(10.dp)) { Text("+ Agregar") }
                }
            }
        }
    }
}

@Composable
private fun BotonBlanco(texto: String, modifier: Modifier, habilitado: Boolean, alPulsar: () -> Unit) {
    Text(
        texto,
        color = MaterialTheme.colorScheme.primary,
        fontWeight = FontWeight.SemiBold,
        textAlign = androidx.compose.ui.text.style.TextAlign.Center,
        modifier = modifier
            .clip(RoundedCornerShape(12.dp))
            .background(Color.White.copy(alpha = if (habilitado) 1f else 0.6f))
            .border(1.dp, Color.White, RoundedCornerShape(12.dp))
            .clickable(enabled = habilitado, role = Role.Button, onClick = alPulsar)
            .padding(vertical = 10.dp)
    )
}
