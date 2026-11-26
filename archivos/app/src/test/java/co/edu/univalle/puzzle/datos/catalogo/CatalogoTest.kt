package co.edu.univalle.puzzle.datos.catalogo

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import java.io.File
import javax.imageio.ImageIO

class CatalogoTest {

    private val carpeta = File("src/main/assets/catalogo")

    private fun imagen(id: String, ubicacion: Ubicacion) =
        ImagenCatalogo(id, id, "a", "o", "l", ubicacion, "r")

    @Test
    fun `la app incluye al menos 5 imagenes cuadradas de 1080 y maximo 500 KB (RNF-06)`() {
        val archivos = carpeta.listFiles { f -> f.extension == "jpg" }!!.toList()
        assertTrue(archivos.size >= 5)
        for (f in archivos) {
            val im = ImageIO.read(f)
            assertEquals("${f.name} ancho", 1080, im.width)
            assertEquals("${f.name} alto", 1080, im.height)
            assertTrue("${f.name} pesa ${f.length()}", f.length() <= 500 * 1024)
        }
    }

    @Test
    fun `cada imagen incluida tiene autor, origen y licencia registrados`() {
        val json = File(carpeta, "incluidas.json").readText()
        val lista = json.split("\"id\"").size - 1
        assertTrue(lista >= 5)
        assertEquals(lista, json.split("\"autor\"").size - 1)
        assertEquals(lista, json.split("\"licencia\"").size - 1)
        // Cada archivo del listado existe.
        Regex("\"archivo\": \"([^\"]+)\"").findAll(json).forEach {
            assertTrue(it.groupValues[1], File(carpeta, it.groupValues[1]).exists())
        }
    }

    @Test
    fun `al combinar no se repiten imagenes y gana la copia local`() {
        val r = ImagenCatalogo.combinar(
            listOf(imagen("a", Ubicacion.INCLUIDA)),
            listOf(imagen("b", Ubicacion.DESCARGADA)),
            listOf(imagen("b", Ubicacion.EN_LA_NUBE), imagen("c", Ubicacion.EN_LA_NUBE))
        )
        assertEquals(listOf("a", "b", "c"), r.map { it.id })
        assertEquals(Ubicacion.DESCARGADA, r[1].ubicacion)
    }

    @Test
    fun `sin conexion solo sirven las incluidas y descargadas (RNF-13)`() {
        assertTrue(imagen("a", Ubicacion.INCLUIDA).disponibleSinConexion)
        assertTrue(imagen("a", Ubicacion.DESCARGADA).disponibleSinConexion)
        assertEquals(false, imagen("a", Ubicacion.EN_LA_NUBE).disponibleSinConexion)
    }
}
