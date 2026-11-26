package co.edu.univalle.puzzle.datos.local

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

/**
 * Cuenta con la que se está jugando en este teléfono. Todos los datos locales se guardan con
 * este identificador, así dos cuentas en el mismo teléfono no ven los datos de la otra (T-12).
 *
 * El inicio de sesión (T-15) llama a [usarCuenta] con el uid de Firebase; el modo invitado (T-17)
 * llama a [usarInvitado].
 */
object SesionLocal {

    const val INVITADO = "invitado"

    private val _cuentaId = MutableStateFlow(INVITADO)
    val cuentaId: StateFlow<String> = _cuentaId.asStateFlow()

    val esInvitado: Boolean get() = _cuentaId.value == INVITADO

    fun usarCuenta(uid: String) {
        require(uid.isNotBlank() && uid != INVITADO) { "uid inválido" }
        _cuentaId.value = uid
    }

    fun usarInvitado() {
        _cuentaId.value = INVITADO
    }
}
