package co.edu.univalle.puzzle.ui.acceso

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import co.edu.univalle.puzzle.dominio.cuenta.ResultadoCuenta
import co.edu.univalle.puzzle.dominio.cuenta.ValidacionCuenta
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

/** Guarda el perfil de quien entró con Google. En la app es `ServicioCuentaFirebase.completarPerfil`. */
fun interface GuardarPerfil {
    suspend fun guardar(nombre: String, avatar: Int, mayorDeEdad: Boolean, aceptoPrivacidad: Boolean): ResultadoCuenta
}

/** Estado de la pantalla Completar perfil. */
data class EstadoCompletarPerfil(
    val nombre: String = "",
    val avatar: Int = 0,
    val mayorDeEdad: Boolean = false,
    val aceptoPrivacidad: Boolean = false,
    val error: String? = null,
    val cargando: Boolean = false,
    val listo: Boolean = false
) {
    /** Igual que en el registro: solo con ambas casillas marcadas (RF-01, RNF-17). */
    val puedeContinuar: Boolean get() = mayorDeEdad && aceptoPrivacidad && !cargando
}

/** Primera vez con Google (T-16): nombre de jugador, avatar y aceptación del aviso, como en RF-01. */
class CompletarPerfilViewModel(private val guardarPerfil: GuardarPerfil) : ViewModel() {

    private val _estado = MutableStateFlow(EstadoCompletarPerfil())
    val estado: StateFlow<EstadoCompletarPerfil> = _estado.asStateFlow()

    fun cambiarNombre(v: String) = _estado.update { it.copy(nombre = v, error = null) }
    fun elegirAvatar(indice: Int) = _estado.update { it.copy(avatar = indice) }
    fun cambiarMayorDeEdad(v: Boolean) = _estado.update { it.copy(mayorDeEdad = v) }
    fun cambiarAceptoPrivacidad(v: Boolean) = _estado.update { it.copy(aceptoPrivacidad = v) }

    fun continuar() {
        val e = _estado.value
        if (!e.puedeContinuar) return
        ValidacionCuenta.nombre(e.nombre)?.let { problema ->
            _estado.update { it.copy(error = problema) }
            return
        }
        _estado.update { it.copy(cargando = true, error = null) }
        viewModelScope.launch {
            val r = guardarPerfil.guardar(e.nombre.trim(), e.avatar, e.mayorDeEdad, e.aceptoPrivacidad)
            _estado.update {
                when (r) {
                    ResultadoCuenta.Exito -> it.copy(cargando = false, listo = true)
                    is ResultadoCuenta.Error -> it.copy(cargando = false, error = r.tipo.mensaje)
                }
            }
        }
    }

    companion object {
        fun fabrica(guardarPerfil: GuardarPerfil): ViewModelProvider.Factory = viewModelFactory {
            initializer { CompletarPerfilViewModel(guardarPerfil) }
        }
    }
}
