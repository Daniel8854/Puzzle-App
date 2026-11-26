package co.edu.univalle.puzzle.ui.pantallas.fotos

import android.Manifest
import android.app.Activity
import android.content.Context
import android.content.ContextWrapper
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.provider.Settings
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.repeatOnLifecycle
import co.edu.univalle.puzzle.ui.pantallas.BarraSuperior
import co.edu.univalle.puzzle.ui.tema.PuzzleTheme

@Composable
fun PantallaPermisoFotos(alConcedido: () -> Unit, alVolver: () -> Unit) {
    val context = LocalContext.current
    var yaSePidio by rememberSaveable { mutableStateOf(false) }
    var concedido by rememberSaveable { mutableStateOf(tienePermisoCamara(context)) }

    val pedir = rememberLauncherForActivityResult(ActivityResultContracts.RequestPermission()) { ok ->
        yaSePidio = true
        concedido = ok
    }

    // Al volver de los ajustes se revisa otra vez.
    val ciclo = LocalLifecycleOwner.current.lifecycle
    LaunchedEffect(ciclo) {
        ciclo.repeatOnLifecycle(Lifecycle.State.RESUMED) { concedido = tienePermisoCamara(context) }
    }

    val paso = pasoPermiso(
        concedido = concedido,
        yaSePidio = yaSePidio,
        debeExplicar = context.buscarActividad()?.let {
            ActivityCompat.shouldShowRequestPermissionRationale(it, Manifest.permission.CAMERA)
        } ?: false
    )
    LaunchedEffect(paso) { if (paso == PasoPermiso.CONCEDIDO) alConcedido() }

    ContenidoPermiso(
        paso = paso,
        alPedir = { pedir.launch(Manifest.permission.CAMERA) },
        alAbrirAjustes = { context.startActivity(intentAjustes(context)) },
        alSoloGaleria = alConcedido,
        alVolver = alVolver
    )
}

@Composable
fun ContenidoPermiso(
    paso: PasoPermiso,
    alPedir: () -> Unit,
    alAbrirAjustes: () -> Unit,
    alSoloGaleria: () -> Unit,
    alVolver: () -> Unit
) {
    Scaffold(topBar = { BarraSuperior("Agregar foto", alVolver) }) { relleno ->
        Column(
            Modifier.fillMaxSize().padding(relleno).padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp, Alignment.CenterVertically)
        ) {
            Box(
                Modifier.size(96.dp).clip(CircleShape).background(MaterialTheme.colorScheme.primaryContainer),
                contentAlignment = Alignment.Center
            ) { Text("📷", fontSize = 40.sp) }

            Text(
                if (paso == PasoPermiso.EXPLICAR) "Usa tus propias fotos" else "Sin acceso a la cámara",
                style = MaterialTheme.typography.titleLarge
            )
            Text(
                when (paso) {
                    PasoPermiso.EXPLICAR ->
                        "Para crear un puzzle con tu foto necesitamos la cámara (para tomarla) o tu galería " +
                            "(para elegirla). Tus fotos son privadas: solo tú las ves."
                    PasoPermiso.NEGADO ->
                        "No diste permiso para la cámara. Puedes volver a intentarlo o elegir una foto de tu galería."
                    else ->
                        "El permiso de la cámara está desactivado. Actívalo en los ajustes del teléfono " +
                            "o elige una foto de tu galería."
                },
                textAlign = TextAlign.Center,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(Modifier.height(8.dp))

            val forma = RoundedCornerShape(14.dp)
            when (paso) {
                PasoPermiso.IR_A_AJUSTES ->
                    Button(onClick = alAbrirAjustes, shape = forma, modifier = Modifier.fillMaxWidth().height(52.dp)) {
                        Text("Abrir ajustes")
                    }
                else ->
                    Button(onClick = alPedir, shape = forma, modifier = Modifier.fillMaxWidth().height(52.dp)) {
                        Text(if (paso == PasoPermiso.EXPLICAR) "Continuar" else "Intentar de nuevo")
                    }
            }
            if (paso != PasoPermiso.EXPLICAR) {
                TextButton(onClick = alSoloGaleria, modifier = Modifier.height(48.dp)) { Text("Elegir de la galería") }
            }
        }
    }
}

private fun tienePermisoCamara(context: Context) =
    ContextCompat.checkSelfPermission(context, Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED

private fun intentAjustes(context: Context) =
    Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS, Uri.fromParts("package", context.packageName, null))
        .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)

private tailrec fun Context.buscarActividad(): Activity? = when (this) {
    is Activity -> this
    is ContextWrapper -> baseContext.buscarActividad()
    else -> null
}

@Preview(showBackground = true, widthDp = 360, heightDp = 760, name = "Permiso · explicación")
@Composable
private fun VistaPermiso() = PuzzleTheme { ContenidoPermiso(PasoPermiso.EXPLICAR, {}, {}, {}, {}) }

@Preview(showBackground = true, widthDp = 360, heightDp = 760, name = "Permiso · ajustes")
@Composable
private fun VistaPermisoAjustes() = PuzzleTheme { ContenidoPermiso(PasoPermiso.IR_A_AJUSTES, {}, {}, {}, {}) }
