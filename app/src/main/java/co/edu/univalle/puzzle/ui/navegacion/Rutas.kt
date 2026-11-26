package co.edu.univalle.puzzle.ui.navegacion

import android.net.Uri

object Rutas {
    const val LOGIN = "login"
    const val REGISTRO = "registro"
    const val RECUPERAR = "recuperar"
    const val PRIVACIDAD = "privacidad"

    const val INICIO = "inicio"
    const val MODOS = "modos"
    const val PARTIDA = "partida/{modo}/{dificultad}/{imagenId}"

    const val MIS_FOTOS = "mis_fotos"
    const val PERMISO_FOTOS = "permiso_fotos"
    const val NUEVA_FOTO = "nueva_foto"
    const val RECORTAR = "recortar/{fotoId}"

    const val AMIGOS = "amigos"
    const val MIS_AMIGOS = "mis_amigos"
    const val DUELO = "duelo"
    const val RANKING = "ranking"
    const val PERFIL = "perfil"
    const val CONFIGURACION = "configuracion"

    fun partida(modo: String, dificultad: Int, imagenId: String) =
        "partida/$modo/$dificultad/${Uri.encode(imagenId)}"

    fun recortar(fotoId: Long) = "recortar/$fotoId"
}
