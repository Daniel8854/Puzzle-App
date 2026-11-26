package co.edu.univalle.puzzle.datos.cuenta

import co.edu.univalle.puzzle.dominio.cuenta.ErrorCuenta

/**
 * Traduce los códigos de error de Firebase Authentication a los mensajes de la app (RF-01, RF-02).
 * En el login nunca se dice si falló el correo o la contraseña.
 */
fun errorDeCodigo(codigo: String?, enLogin: Boolean): ErrorCuenta = when (codigo) {
    "ERROR_EMAIL_ALREADY_IN_USE", "ERROR_CREDENTIAL_ALREADY_IN_USE" -> ErrorCuenta.CORREO_EN_USO
    "ERROR_WEAK_PASSWORD" -> ErrorCuenta.CONTRASENA_DEBIL
    "ERROR_INVALID_EMAIL" -> if (enLogin) ErrorCuenta.CREDENCIALES_INCORRECTAS else ErrorCuenta.CORREO_INVALIDO
    "ERROR_WRONG_PASSWORD", "ERROR_USER_NOT_FOUND", "ERROR_INVALID_CREDENTIAL", "ERROR_USER_DISABLED" ->
        ErrorCuenta.CREDENCIALES_INCORRECTAS
    "ERROR_NETWORK_REQUEST_FAILED" -> ErrorCuenta.SIN_CONEXION
    else -> if (enLogin) ErrorCuenta.CREDENCIALES_INCORRECTAS else ErrorCuenta.DESCONOCIDO
}
