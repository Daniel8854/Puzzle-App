package co.edu.univalle.puzzle.dominio.cuenta

interface ServicioCuenta {
    fun haySesion(): Boolean

    suspend fun registrar(datos: DatosRegistro): ResultadoCuenta

    suspend fun iniciarSesion(correo: String, contrasena: String): ResultadoCuenta

    // Responde Exito aunque el correo no esté registrado, para no revelar si la cuenta existe.
    suspend fun enviarRecuperacion(correo: String): ResultadoCuenta

    suspend fun iniciarConGoogle(idToken: String): ResultadoCuenta
}

data class DatosRegistro(
    val nombre: String,
    val correo: String,
    val contrasena: String,
    val avatar: Int,
    val mayorDeEdad: Boolean,
    val aceptoPrivacidad: Boolean
)

sealed interface ResultadoCuenta {
    data object Exito : ResultadoCuenta
    data class Error(val tipo: ErrorCuenta) : ResultadoCuenta
}

enum class ErrorCuenta(val mensaje: String) {
    CREDENCIALES_INCORRECTAS("Correo o contraseña incorrectos"),
    CORREO_EN_USO("Este correo ya está registrado"),
    NOMBRE_EN_USO("Ese nombre de jugador ya está en uso"),
    CONTRASENA_DEBIL("La contraseña debe tener al menos 8 caracteres"),
    CORREO_INVALIDO("Escribe un correo válido"),
    SIN_CONEXION("Sin conexión. Revisa tu internet e inténtalo de nuevo"),
    CANCELADO("Se canceló el inicio de sesión"),
    DESCONOCIDO("Ocurrió un error. Inténtalo de nuevo")
}
