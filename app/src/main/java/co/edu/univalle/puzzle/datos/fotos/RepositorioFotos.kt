package co.edu.univalle.puzzle.datos.fotos

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Matrix
import android.net.Uri
import androidx.core.content.FileProvider
import androidx.exifinterface.media.ExifInterface
import co.edu.univalle.puzzle.datos.local.BaseDatosPuzzle
import co.edu.univalle.puzzle.datos.local.FotoPropia
import co.edu.univalle.puzzle.datos.local.SesionLocal
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.withContext
import java.io.ByteArrayOutputStream
import java.io.File

/**
 * Lo que necesitan los ViewModels de fotos. Es una interfaz para poder probarlos con datos falsos,
 * sin teléfono ni base de datos.
 */
interface AlmacenFotos {
    /** Archivo temporal y su Uri para que la cámara guarde la foto. */
    fun nuevoArchivoCamara(): Uri

    /** Comprime la foto de [origen] (una Uri en texto) y la guarda. Devuelve el id de la foto. */
    suspend fun guardar(origen: String): Long
}

/**
 * Fotos propias del jugador (RF-34): las comprime (RNF-05) y guarda una copia local para jugar
 * sin conexión (RNF-08). Cada cuenta tiene su carpeta y sus registros.
 */
class RepositorioFotos(context: Context) : AlmacenFotos {

    private val app = context.applicationContext
    private val dao = BaseDatosPuzzle.obtener(app).fotos()

    fun deLaCuenta(cuentaId: String = SesionLocal.cuentaId.value): Flow<List<FotoPropia>> = dao.deCuenta(cuentaId)

    suspend fun porId(id: Long, cuentaId: String = SesionLocal.cuentaId.value): FotoPropia? = dao.porId(id, cuentaId)

    /** Archivo temporal y su Uri para que la cámara guarde la foto (FileProvider de T-11). */
    override fun nuevoArchivoCamara(): Uri {
        val carpeta = File(app.cacheDir, "camara").apply { mkdirs() }
        val archivo = File(carpeta, "foto_${System.currentTimeMillis()}.jpg")
        return FileProvider.getUriForFile(app, "${app.packageName}.archivos", archivo)
    }

    override suspend fun guardar(origen: String): Long = guardar(Uri.parse(origen))

    /** Comprime la foto de [origen] y la guarda. Devuelve el id de la foto guardada. */
    suspend fun guardar(origen: Uri, cuentaId: String = SesionLocal.cuentaId.value): Long =
        withContext(Dispatchers.IO) {
            val bytes = comprimir(origen)
            val carpeta = File(app.filesDir, "fotos/$cuentaId").apply { mkdirs() }
            val archivo = File(carpeta, "foto_${System.currentTimeMillis()}.jpg")
            archivo.writeBytes(bytes)
            dao.insertar(FotoPropia(cuentaId = cuentaId, rutaLocal = archivo.absolutePath, fechaMs = System.currentTimeMillis()))
        }

    suspend fun actualizar(foto: FotoPropia) = dao.actualizar(foto)

    /** Lee, endereza según EXIF, reduce a 1080 px y baja la calidad hasta 500 KB o menos. */
    private fun comprimir(origen: Uri): ByteArray {
        val resolver = app.contentResolver
        val limites = BitmapFactory.Options().apply { inJustDecodeBounds = true }
        resolver.openInputStream(origen)?.use { BitmapFactory.decodeStream(it, null, limites) }
            ?: error("No se pudo abrir la foto")
        val opciones = BitmapFactory.Options().apply {
            inSampleSize = Compresion.submuestreo(limites.outWidth, limites.outHeight)
        }
        val leida = resolver.openInputStream(origen)?.use { BitmapFactory.decodeStream(it, null, opciones) }
            ?: error("La foto no es una imagen válida")

        val grados = resolver.openInputStream(origen)?.use {
            Compresion.gradosPorExif(ExifInterface(it).getAttributeInt(ExifInterface.TAG_ORIENTATION, 1))
        } ?: 0
        val (ancho, alto) = Compresion.tamanoFinal(leida.width, leida.height)
        val matriz = Matrix().apply {
            postScale(ancho.toFloat() / leida.width, alto.toFloat() / leida.height)
            postRotate(grados.toFloat())
        }
        val final = Bitmap.createBitmap(leida, 0, 0, leida.width, leida.height, matriz, true)

        var resultado = ByteArray(0)
        for (calidad in Compresion.calidades()) {
            val salida = ByteArrayOutputStream()
            final.compress(Bitmap.CompressFormat.JPEG, calidad, salida)
            resultado = salida.toByteArray()
            if (resultado.size <= Compresion.BYTES_MAXIMOS) break
        }
        return resultado
    }
}
