package co.edu.univalle.puzzle.dominio.amigos

import kotlin.random.Random

/**
 * Código de amigo (RF-39): 8 caracteres sin 0, O, 1 ni I, para que no se confundan.
 * La unicidad por cuenta se garantiza al guardarlo en Firestore (T-51 / T-14).
 */
object CodigoAmigo {

    const val LONGITUD = 8

    /** Letras y números permitidos: se excluyen 0, O, 1 e I. */
    const val ALFABETO = "ABCDEFGHJKLMNPQRSTUVWXYZ23456789"

    /** Genera un código aleatorio nuevo. */
    fun generar(aleatorio: Random = Random.Default): String =
        buildString(LONGITUD) {
            repeat(LONGITUD) { append(ALFABETO[aleatorio.nextInt(ALFABETO.length)]) }
        }

    /**
     * Limpia lo que escribe el usuario al buscar un amigo:
     * quita espacios y guiones y lo pasa a mayúsculas.
     */
    fun normalizar(texto: String): String =
        texto.filterNot { it.isWhitespace() || it == '-' }.uppercase()

    /** Verdadero si [codigo] (ya normalizado) tiene el formato correcto. */
    fun esValido(codigo: String): Boolean =
        codigo.length == LONGITUD && codigo.all { it in ALFABETO }
}
