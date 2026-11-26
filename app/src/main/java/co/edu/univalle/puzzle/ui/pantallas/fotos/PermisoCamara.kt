package co.edu.univalle.puzzle.ui.pantallas.fotos

// La galería usa el selector del sistema, que no necesita permiso: solo se pide el de la cámara.
enum class PasoPermiso {
    EXPLICAR,
    NEGADO,
    IR_A_AJUSTES,
    CONCEDIDO
}

// debeExplicar es shouldShowRequestPermissionRationale: verdadero tras una primera negativa.
fun pasoPermiso(concedido: Boolean, yaSePidio: Boolean, debeExplicar: Boolean): PasoPermiso = when {
    concedido -> PasoPermiso.CONCEDIDO
    !yaSePidio -> PasoPermiso.EXPLICAR
    debeExplicar -> PasoPermiso.NEGADO
    else -> PasoPermiso.IR_A_AJUSTES
}
