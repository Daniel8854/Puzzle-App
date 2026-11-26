package co.edu.univalle.puzzle.dominio.privacidad

// Si cambia el texto del aviso hay que subir VERSION para volver a pedir la aceptación.
object AvisoPrivacidad {
    const val VERSION = "2026-11"
    const val LEY = "Ley 1581 de 2012"

    data class Seccion(val titulo: String, val texto: String)

    val secciones = listOf(
        Seccion(
            "Qué datos usamos",
            "Correo, nombre, avatar, código de amigo, partidas, duelos y ranking. " +
                "Tus fotos propias solo las ves tú."
        ),
        Seccion(
            "Para qué los usamos",
            "Para tu cuenta, tus amigos, los duelos y el ranking. No vendemos ni compartimos tus datos."
        ),
        Seccion(
            "Tus derechos",
            "Puedes consultar este aviso en Configuración y eliminar tu cuenta y tus fotos cuando quieras."
        )
    )

    const val AUTORIZACION =
        "Para registrarte debes aceptar este aviso. Sin aceptarlo no se crea la cuenta."

    data class Consentimiento(val version: String, val aceptadoEnMillis: Long, val mayorDeEdad: Boolean)

    fun consentir(aceptoAviso: Boolean, mayorDeEdad: Boolean, ahoraMillis: Long): Consentimiento {
        require(aceptoAviso) { "Debe aceptar el aviso de privacidad" }
        require(mayorDeEdad) { "Debe declarar que es mayor de edad" }
        return Consentimiento(VERSION, ahoraMillis, mayorDeEdad)
    }

    fun estaVigente(consentimiento: Consentimiento?): Boolean = consentimiento?.version == VERSION
}
