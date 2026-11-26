package co.edu.univalle.puzzle.datos.cuenta

import android.content.Context
import co.edu.univalle.puzzle.datos.local.SesionLocal
import co.edu.univalle.puzzle.datos.nube.Nube
import co.edu.univalle.puzzle.datos.nube.Nube.Colecciones
import co.edu.univalle.puzzle.dominio.cuenta.DatosRegistro
import co.edu.univalle.puzzle.dominio.cuenta.ErrorCuenta
import co.edu.univalle.puzzle.dominio.cuenta.ResultadoCuenta
import co.edu.univalle.puzzle.dominio.cuenta.ServicioCuenta
import co.edu.univalle.puzzle.dominio.privacidad.AvisoPrivacidad
import com.google.firebase.FirebaseNetworkException
import com.google.firebase.auth.FirebaseAuthException
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestoreException
import kotlinx.coroutines.tasks.await

/**
 * Cuenta con Firebase Authentication (T-15). La contraseña solo la recibe Firebase:
 * la app no la guarda ni la escribe en Firestore ni en el registro (RNF-14).
 *
 * Al registrarse se reserva el nombre en «nombres/{nombre}» y se crea el perfil en
 * «usuarios/{uid}» en la misma operación (reglas de T-14). El correo y el consentimiento del
 * aviso de privacidad (T-65) van en «usuarios/{uid}/privado/datos», que solo ve su dueño.
 */
open class ServicioCuentaFirebase(private val context: Context) : ServicioCuenta {

    private val hayNube: Boolean get() = Nube.disponible(context)

    override fun haySesion(): Boolean {
        if (!hayNube) return false
        val uid = Nube.uid ?: return false
        SesionLocal.usarCuenta(uid)
        return true
    }

    override suspend fun registrar(datos: DatosRegistro): ResultadoCuenta = conErrores(enLogin = false) {
        val nombreClave = datos.nombre.trim().lowercase()
        val db = Nube.firestore
        // Se revisa antes de crear la cuenta para no dejar cuentas sin perfil.
        if (db.collection(Colecciones.NOMBRES).document(nombreClave).get().await().exists()) {
            return@conErrores ResultadoCuenta.Error(ErrorCuenta.NOMBRE_EN_USO)
        }
        val consentimiento = AvisoPrivacidad.consentir(datos.aceptoPrivacidad, datos.mayorDeEdad, System.currentTimeMillis())
        val usuario = Nube.auth.createUserWithEmailAndPassword(datos.correo, datos.contrasena).await().user
            ?: return@conErrores ResultadoCuenta.Error(ErrorCuenta.DESCONOCIDO)
        val uid = usuario.uid
        try {
            db.batch().apply {
                set(db.collection(Colecciones.NOMBRES).document(nombreClave), mapOf("uid" to uid))
                set(db.collection(Colecciones.USUARIOS).document(uid), mapOf(
                    "nombre" to datos.nombre.trim(),
                    "nombreMinusculas" to nombreClave,
                    "avatar" to datos.avatar,
                    "creadoEn" to FieldValue.serverTimestamp()
                ))
                set(db.collection(Colecciones.USUARIOS).document(uid).collection("privado").document("datos"), mapOf(
                    "correo" to datos.correo.trim(),
                    "avisoVersion" to consentimiento.version,
                    "avisoAceptadoEn" to consentimiento.aceptadoEnMillis,
                    "mayorDeEdad" to consentimiento.mayorDeEdad
                ))
            }.commit().await()
        } catch (e: Exception) {
            // Si el perfil no se pudo crear, se borra la cuenta para no dejarla a medias.
            usuario.delete().await()
            val nombreTomado = e is FirebaseFirestoreException &&
                e.code == FirebaseFirestoreException.Code.PERMISSION_DENIED
            if (nombreTomado) return@conErrores ResultadoCuenta.Error(ErrorCuenta.NOMBRE_EN_USO)
            throw e
        }
        SesionLocal.usarCuenta(uid)
        ResultadoCuenta.Exito
    }

    override suspend fun iniciarSesion(correo: String, contrasena: String): ResultadoCuenta = conErrores(enLogin = true) {
        val uid = Nube.auth.signInWithEmailAndPassword(correo, contrasena).await().user?.uid
            ?: return@conErrores ResultadoCuenta.Error(ErrorCuenta.CREDENCIALES_INCORRECTAS)
        SesionLocal.usarCuenta(uid)
        ResultadoCuenta.Exito
    }

    /**
     * RF-04 (T-18): envía el enlace para crear una contraseña nueva. Si el correo no está
     * registrado responde igual que si lo estuviera, para no revelar qué cuentas existen.
     */
    override suspend fun enviarRecuperacion(correo: String): ResultadoCuenta = conErrores(enLogin = false) {
        Nube.auth.useAppLanguage()
        try {
            Nube.auth.sendPasswordResetEmail(correo.trim()).await()
        } catch (e: FirebaseAuthException) {
            if (e.errorCode != "ERROR_USER_NOT_FOUND") throw e
        }
        ResultadoCuenta.Exito
    }

    /** La implementa T-16. */
    override suspend fun iniciarConGoogle(idToken: String): ResultadoCuenta =
        ResultadoCuenta.Error(ErrorCuenta.DESCONOCIDO)

    /** Cierra la sesión; el teléfono vuelve a los datos del invitado. */
    fun cerrarSesion() {
        if (hayNube) Nube.auth.signOut()
        SesionLocal.usarInvitado()
    }

    /** Ejecuta [accion] y convierte cualquier falla de Firebase en un [ResultadoCuenta.Error]. */
    protected suspend fun conErrores(enLogin: Boolean, accion: suspend () -> ResultadoCuenta): ResultadoCuenta {
        if (!hayNube) return ResultadoCuenta.Error(ErrorCuenta.SIN_CONEXION)
        return try {
            accion()
        } catch (e: FirebaseAuthException) {
            ResultadoCuenta.Error(errorDeCodigo(e.errorCode, enLogin))
        } catch (e: FirebaseNetworkException) {
            ResultadoCuenta.Error(ErrorCuenta.SIN_CONEXION)
        } catch (e: IllegalArgumentException) {
            ResultadoCuenta.Error(if (enLogin) ErrorCuenta.CREDENCIALES_INCORRECTAS else ErrorCuenta.DESCONOCIDO)
        } catch (e: Exception) {
            ResultadoCuenta.Error(ErrorCuenta.DESCONOCIDO)
        }
    }
}
