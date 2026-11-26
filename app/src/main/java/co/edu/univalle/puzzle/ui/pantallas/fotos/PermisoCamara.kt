package co.edu.univalle.puzzle.ui.pantallas.fotos

/**
 * Qué mostrar en la pantalla de permiso (RF-33).
 *
 * La galería se abre con el selector de fotos del sistema, que no necesita permiso.
 * El único permiso que se pide es el de la cámara, y solo cuando el jugador va a agregar una foto.
 */
enum class PasoPermiso {
    /** Explicación previa: aún no se ha pedido o se puede volver a pedir. */
    EXPLICAR,
    /** El jugador lo negó una vez: se explica de nuevo y se puede volver a pedir. */
    NEGADO,
    /** Lo negó de forma permanente: solo queda abrir los ajustes. */
    IR_A_AJUSTES,
    /** Concedido: el flujo sigue. */
    CONCEDIDO
}

/**
 * Decide el paso según lo que responde Android.
 * [debeExplicar] es `shouldShowRequestPermissionRationale`: verdadero después de una primera negativa.
 */
fun pasoPermiso(concedido: Boolean, yaSePidio: Boolean, debeExplicar: Boolean): PasoPermiso = when {
    concedido -> PasoPermiso.CONCEDIDO
    !yaSePidio -> PasoPermiso.EXPLICAR
    debeExplicar -> PasoPermiso.NEGADO
    else -> PasoPermiso.IR_A_AJUSTES
}
