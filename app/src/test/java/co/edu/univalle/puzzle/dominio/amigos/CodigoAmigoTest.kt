package co.edu.univalle.puzzle.dominio.amigos

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import kotlin.random.Random

class CodigoAmigoTest {

    @Test
    fun `el codigo generado tiene 8 caracteres validos`() {
        val aleatorio = Random(42)
        repeat(1000) {
            val codigo = CodigoAmigo.generar(aleatorio)
            assertEquals(8, codigo.length)
            assertTrue("Código inválido: $codigo", CodigoAmigo.esValido(codigo))
        }
    }

    @Test
    fun `el alfabeto no tiene caracteres confusos`() {
        listOf('0', 'O', '1', 'I').forEach { prohibido ->
            assertFalse(prohibido in CodigoAmigo.ALFABETO)
        }
    }

    @Test
    fun `codigos con 0, O, 1 o I no son validos`() {
        assertFalse(CodigoAmigo.esValido("ABCD2340"))
        assertFalse(CodigoAmigo.esValido("ABCD234O"))
        assertFalse(CodigoAmigo.esValido("ABCD2341"))
        assertFalse(CodigoAmigo.esValido("ABCD234I"))
    }

    @Test
    fun `codigos de otra longitud o en minusculas no son validos`() {
        assertFalse(CodigoAmigo.esValido("ABCD234"))
        assertFalse(CodigoAmigo.esValido("ABCD23456"))
        assertFalse(CodigoAmigo.esValido(""))
        assertFalse(CodigoAmigo.esValido("abcd2345"))
        assertTrue(CodigoAmigo.esValido("ABCD2345"))
    }

    @Test
    fun `normalizar acepta lo que escribe el usuario`() {
        assertEquals("ABCD2345", CodigoAmigo.normalizar(" abcd-2345 "))
        assertTrue(CodigoAmigo.esValido(CodigoAmigo.normalizar("abcd 2345")))
    }

    @Test
    fun `generar con la misma semilla da el mismo codigo`() {
        assertEquals(
            CodigoAmigo.generar(Random(7)),
            CodigoAmigo.generar(Random(7))
        )
    }
}
