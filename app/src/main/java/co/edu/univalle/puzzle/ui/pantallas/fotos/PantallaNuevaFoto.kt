package co.edu.univalle.puzzle.ui.pantallas.fotos

import android.Manifest
import android.content.pm.PackageManager
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import co.edu.univalle.puzzle.ui.pantallas.BarraSuperior
import co.edu.univalle.puzzle.ui.tema.PuzzleTheme

@Composable
fun PantallaNuevaFoto(
    alFotoGuardada: (fotoId: Long) -> Unit,
    alVolver: () -> Unit,
    vm: NuevaFotoViewModel = viewModel(factory = NuevaFotoViewModel.fabrica(LocalContext.current))
) {
    val context = LocalContext.current
    val estado by vm.estado.collectAsStateWithLifecycle()
    var uriCamara by rememberSaveable { mutableStateOf<Uri?>(null) }
    val puedeUsarCamara = ContextCompat.checkSelfPermission(context, Manifest.permission.CAMERA) ==
        PackageManager.PERMISSION_GRANTED

    LaunchedEffect(estado.fotoGuardada) {
        estado.fotoGuardada?.let {
            vm.navegacionHecha()
            alFotoGuardada(it)
        }
    }

    val galeria = rememberLauncherForActivityResult(ActivityResultContracts.PickVisualMedia()) { uri ->
        vm.fotoElegida(uri?.toString())
    }
    val camara = rememberLauncherForActivityResult(ActivityResultContracts.TakePicture()) { ok ->
        if (ok) vm.fotoElegida(uriCamara?.toString())
    }

    ContenidoNuevaFoto(
        guardando = estado.guardando,
        error = estado.error,
        puedeUsarCamara = puedeUsarCamara,
        alElegirGaleria = {
            galeria.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly))
        },
        alTomarFoto = {
            val uri = vm.nuevoArchivoCamara()
            uriCamara = uri
            camara.launch(uri)
        },
        alVolver = alVolver
    )
}

@Composable
fun ContenidoNuevaFoto(
    guardando: Boolean,
    error: String?,
    puedeUsarCamara: Boolean,
    alElegirGaleria: () -> Unit,
    alTomarFoto: () -> Unit,
    alVolver: () -> Unit
) {
    Scaffold(topBar = { BarraSuperior("Nueva foto", alVolver) }) { relleno ->
        Column(
            Modifier.fillMaxSize().padding(relleno).padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp, Alignment.CenterVertically)
        ) {
            if (guardando) {
                CircularProgressIndicator(Modifier.size(48.dp))
                Text("Preparando tu foto…")
            } else {
                Text("¿De dónde sacamos la foto?", style = MaterialTheme.typography.titleLarge)
                Text(
                    "Se guarda una copia reducida (máximo 1080×1080 px) para jugar sin conexión.",
                    textAlign = TextAlign.Center,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                val forma = RoundedCornerShape(14.dp)
                Button(onClick = alElegirGaleria, shape = forma, modifier = Modifier.fillMaxWidth().height(52.dp)) {
                    Text("Elegir de la galería")
                }
                OutlinedButton(
                    onClick = alTomarFoto,
                    enabled = puedeUsarCamara,
                    shape = forma,
                    modifier = Modifier.fillMaxWidth().height(52.dp)
                ) { Text(if (puedeUsarCamara) "Tomar foto" else "Tomar foto (sin permiso de cámara)") }
                error?.let { Text(it, color = MaterialTheme.colorScheme.error) }
            }
        }
    }
}

@Preview(showBackground = true, widthDp = 360, heightDp = 760)
@Composable
private fun VistaNuevaFoto() = PuzzleTheme { ContenidoNuevaFoto(false, null, true, {}, {}, {}) }
