package co.edu.univalle.puzzle.ui.acceso

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.systemBarsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle

/**
 * Primera vez con Google (T-16): se pide el nombre de jugador, el avatar y la aceptación del aviso,
 * igual que en el registro con correo (RF-01). La lógica está en [CompletarPerfilViewModel].
 */
@Composable
fun PantallaCompletarPerfil(
    vm: CompletarPerfilViewModel,
    alListo: () -> Unit,
    alVerPrivacidad: () -> Unit
) {
    val estado by vm.estado.collectAsStateWithLifecycle()
    LaunchedEffect(estado.listo) { if (estado.listo) alListo() }

    Column(
        Modifier.fillMaxSize().systemBarsPadding().imePadding().verticalScroll(rememberScrollState())
            .padding(horizontal = 24.dp, vertical = 20.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        Text("Completa tu perfil", style = MaterialTheme.typography.titleLarge)
        Text("Elige cómo te verán los demás jugadores.", color = MaterialTheme.colorScheme.onSurfaceVariant)
        SelectorAvatar(estado.avatar, estado.nombre.trim().firstOrNull() ?: 'J', vm::elegirAvatar)
        CampoAcceso("Nombre de jugador", estado.nombre, vm::cambiarNombre,
            ayuda = "De 3 a 20 caracteres", error = estado.error)
        CasillaAcceso("Soy mayor de 18 años", estado.mayorDeEdad, vm::cambiarMayorDeEdad)
        CasillaAcceso("Acepto el aviso de privacidad", estado.aceptoPrivacidad, vm::cambiarAceptoPrivacidad,
            enlace = "Ver" to alVerPrivacidad)
        BotonPrincipal("Continuar", habilitado = estado.puedeContinuar, cargando = estado.cargando, alPulsar = vm::continuar)
    }
}
