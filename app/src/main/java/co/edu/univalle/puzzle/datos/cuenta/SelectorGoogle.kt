package co.edu.univalle.puzzle.datos.cuenta

import android.content.Context
import androidx.credentials.CredentialManager
import androidx.credentials.CustomCredential
import androidx.credentials.GetCredentialRequest
import androidx.credentials.exceptions.GetCredentialCancellationException
import androidx.credentials.exceptions.GetCredentialException
import androidx.credentials.exceptions.NoCredentialException
import com.google.android.libraries.identity.googleid.GetSignInWithGoogleOption
import com.google.android.libraries.identity.googleid.GoogleIdTokenCredential

/** Lo que devuelve el selector de cuentas de Google. */
sealed interface RespuestaGoogle {
    data class Token(val idToken: String) : RespuestaGoogle
    /** El jugador cerró el selector: se queda en la pantalla de acceso, sin error (T-16). */
    data object Cancelado : RespuestaGoogle
    data class Falla(val mensaje: String) : RespuestaGoogle
}

/**
 * Abre el selector de cuentas de Google con Credential Manager (T-16) y devuelve el token
 * que luego se entrega a Firebase. [context] debe ser la actividad.
 */
object SelectorGoogle {

    /**
     * Id del cliente web de Firebase. Lo genera el plugin de google-services a partir de
     * google-services.json; si no existe, el inicio con Google no está configurado.
     */
    fun idClienteWeb(context: Context): String? {
        val id = context.resources.getIdentifier("default_web_client_id", "string", context.packageName)
        return if (id == 0) null else context.getString(id)
    }

    suspend fun pedirToken(context: Context): RespuestaGoogle {
        val cliente = idClienteWeb(context)
            ?: return RespuestaGoogle.Falla("El inicio con Google no está configurado en esta copia de la app")
        val solicitud = GetCredentialRequest.Builder()
            .addCredentialOption(GetSignInWithGoogleOption.Builder(cliente).build())
            .build()
        return try {
            val credencial = CredentialManager.create(context).getCredential(context, solicitud).credential
            if (credencial is CustomCredential && credencial.type == GoogleIdTokenCredential.TYPE_GOOGLE_ID_TOKEN_CREDENTIAL) {
                RespuestaGoogle.Token(GoogleIdTokenCredential.createFrom(credencial.data).idToken)
            } else {
                RespuestaGoogle.Falla("No se recibió una cuenta de Google")
            }
        } catch (e: GetCredentialCancellationException) {
            RespuestaGoogle.Cancelado
        } catch (e: NoCredentialException) {
            RespuestaGoogle.Falla("No hay cuentas de Google en este teléfono")
        } catch (e: GetCredentialException) {
            RespuestaGoogle.Falla("No se pudo iniciar sesión con Google")
        }
    }
}
