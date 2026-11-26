package co.edu.univalle.puzzle

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.lifecycleScope
import co.edu.univalle.puzzle.datos.local.SesionLocal
import co.edu.univalle.puzzle.datos.nube.Nube
import co.edu.univalle.puzzle.notificaciones.AvisosEnVivo
import co.edu.univalle.puzzle.notificaciones.Notificaciones
import co.edu.univalle.puzzle.ui.navegacion.NavegacionPuzzle
import co.edu.univalle.puzzle.ui.tema.PuzzleTheme
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

class MainActivity : ComponentActivity() {

    /** Pantalla que pidió abrir una notificación (T-62). */
    private val rutaPendiente = mutableStateOf<String?>(null)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        Notificaciones.crearCanal(this)
        rutaPendiente.value = Notificaciones.rutaSegura(intent?.getStringExtra(Notificaciones.EXTRA_RUTA))
        escucharAvisos()
        setContent {
            PuzzleTheme {
                NavegacionPuzzle(
                    rutaPendiente = rutaPendiente.value,
                    alAbrirRuta = { rutaPendiente.value = null }
                )
            }
        }
    }

    /** Mientras haya una cuenta con sesión se escuchan sus avisos (T-62); el invitado no recibe. */
    private fun escucharAvisos() {
        if (!Nube.disponible(this)) return
        val avisos = AvisosEnVivo(this)
        lifecycleScope.launch {
            SesionLocal.cuentaId.collectLatest { cuenta ->
                if (cuenta != SesionLocal.INVITADO) avisos.escuchar(cuenta)
            }
        }
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        rutaPendiente.value = Notificaciones.rutaSegura(intent.getStringExtra(Notificaciones.EXTRA_RUTA))
    }
}
