package co.edu.univalle.puzzle.ui.pantallas.amigos

import android.widget.Toast
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import co.edu.univalle.puzzle.ui.acceso.BotonPrincipal
import co.edu.univalle.puzzle.ui.acceso.EncabezadoAcceso

/**
 * Pantalla 18b · Mis amigos (T-53): la lista de amigos aceptados. Las solicitudes recibidas se
 * responden en la 18 · Agregar amigo. Los datos están en [MisAmigosViewModel].
 */
@Composable
fun PantallaMisAmigos(
    alAgregar: () -> Unit,
    alVolver: () -> Unit,
    vm: MisAmigosViewModel = viewModel(factory = MisAmigosViewModel.fabrica())
) {
    val context = LocalContext.current
    val estado by vm.estado.collectAsStateWithLifecycle()
    val amigos = estado.amigos

    LaunchedEffect(estado.mensaje) {
        estado.mensaje?.let {
            Toast.makeText(context, it, Toast.LENGTH_SHORT).show()
            vm.mensajeMostrado()
        }
    }

    Column(
        Modifier.fillMaxSize().systemBarsPadding().verticalScroll(rememberScrollState()).padding(20.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        EncabezadoAcceso("Mis amigos", if (amigos.size == 1) "1 amigo" else "${amigos.size} amigos", alVolver)
        TituloSeccion("Amigos")
        if (amigos.isEmpty()) {
            Text("Aún no tienes amigos. Agrega uno con su código.", color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
        amigos.forEach { amigo -> FilaJugador(amigo, "Amigo") {} }
        BotonPrincipal("+ Agregar amigo", habilitado = true, alPulsar = alAgregar)
        if (estado.solicitudes.isNotEmpty()) {
            val n = estado.solicitudes.size
            Text(
                if (n == 1) "Tienes 1 solicitud nueva en Agregar amigo" else "Tienes $n solicitudes nuevas en Agregar amigo",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center,
                modifier = Modifier.fillMaxWidth()
            )
        }
    }
}
