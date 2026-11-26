package co.edu.univalle.puzzle.ui.navegacion

import android.net.Uri
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import co.edu.univalle.puzzle.datos.cuenta.SelectorGoogle
import co.edu.univalle.puzzle.ui.acceso.CompletarPerfilViewModel
import co.edu.univalle.puzzle.ui.acceso.PantallaCompletarPerfil
import kotlinx.coroutines.launch
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.viewmodel.compose.viewModel
import co.edu.univalle.puzzle.datos.cuenta.ServicioCuentaFirebase
import co.edu.univalle.puzzle.datos.local.SesionLocal
import co.edu.univalle.puzzle.ui.acceso.LoginViewModel
import co.edu.univalle.puzzle.ui.acceso.PantallaLogin
import co.edu.univalle.puzzle.ui.acceso.PantallaRecuperar
import co.edu.univalle.puzzle.ui.acceso.RecuperarViewModel
import co.edu.univalle.puzzle.ui.acceso.PantallaRegistro
import co.edu.univalle.puzzle.ui.acceso.RegistroViewModel
import co.edu.univalle.puzzle.ui.privacidad.PantallaAvisoPrivacidad
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import co.edu.univalle.puzzle.ui.pantallas.PantallaEnConstruccion
import co.edu.univalle.puzzle.ui.pantallas.PantallaInicio
import co.edu.univalle.puzzle.ui.pantallas.amigos.PantallaAgregarAmigo
import co.edu.univalle.puzzle.ui.pantallas.amigos.PantallaMisAmigos
import co.edu.univalle.puzzle.ui.pantallas.fotos.PantallaNuevaFoto
import co.edu.univalle.puzzle.ui.pantallas.fotos.PantallaPermisoFotos
import co.edu.univalle.puzzle.ui.pantallas.fotos.PantallaRecortar
import co.edu.univalle.puzzle.ui.pantallas.misfotos.PantallaMisFotos
import co.edu.univalle.puzzle.ui.pantallas.modos.PantallaModos
import co.edu.univalle.puzzle.ui.pantallas.partida.PantallaPartida

/** Lleva a Inicio y borra las pantallas de acceso del historial. */
fun NavHostController.irAInicio() = navigate(Rutas.INICIO) {
    popUpTo(Rutas.LOGIN) { inclusive = true }
}

@Composable
fun NavegacionPuzzle() {
    val nav = rememberNavController()
    val context = LocalContext.current
    val cuenta = remember { ServicioCuentaFirebase(context.applicationContext) }
    val volver: () -> Unit = { nav.popBackStack() }
    val crearCuenta: () -> Unit = { nav.navigate(Rutas.REGISTRO) }
    // Quien entra con Google por primera vez completa su perfil antes de ir a Inicio (T-16).
    val entrarOCompletar: () -> Unit = {
        if (cuenta.perfilPendiente) {
            nav.navigate(Rutas.COMPLETAR_PERFIL) { popUpTo(Rutas.LOGIN) { inclusive = true } }
        } else {
            nav.irAInicio()
        }
    }

    NavHost(navController = nav, startDestination = Rutas.LOGIN) {

        // ---------- Acceso: pantallas de T-19 y T-65 con Firebase (T-15) ----------
        composable(Rutas.LOGIN) {
            val vm: LoginViewModel = viewModel(factory = LoginViewModel.fabrica(cuenta))
            PantallaLogin(
                vm = vm,
                alEntrar = { entrarOCompletar() },
                // T-16: el selector de Google entrega el token y Firebase registra o inicia sesión.
                alContinuarConGoogle = {
                    vm.continuarConGoogle { SelectorGoogle.pedirToken(context) }
                },
                alIrARegistro = { nav.navigate(Rutas.REGISTRO) },
                alIrARecuperar = { nav.navigate(Rutas.RECUPERAR) },
                alJugarComoInvitado = {
                    SesionLocal.usarInvitado()
                    nav.irAInicio()
                }
            )
        }
        composable(Rutas.REGISTRO) {
            val vm: RegistroViewModel = viewModel(factory = RegistroViewModel.fabrica(cuenta))
            PantallaRegistro(
                vm = vm,
                // Con correo el perfil queda completo y va a Inicio; con Google puede faltar completarlo.
                alRegistrado = { entrarOCompletar() },
                alVolver = volver,
                alVerPrivacidad = { nav.navigate(Rutas.PRIVACIDAD) },
                alContinuarConGoogle = {
                    vm.continuarConGoogle { SelectorGoogle.pedirToken(context) }
                }
            )
        }
        composable(Rutas.COMPLETAR_PERFIL) {
            PantallaCompletarPerfil(
                vm = viewModel(factory = CompletarPerfilViewModel.fabrica(cuenta::completarPerfil)),
                alListo = {
                    nav.navigate(Rutas.INICIO) { popUpTo(Rutas.COMPLETAR_PERFIL) { inclusive = true } }
                },
                alVerPrivacidad = { nav.navigate(Rutas.PRIVACIDAD) }
            )
        }
        composable(Rutas.RECUPERAR) {
            PantallaRecuperar(vm = viewModel(factory = RecuperarViewModel.fabrica(cuenta)), alVolver = volver)
        }
        composable(Rutas.PRIVACIDAD) { PantallaAvisoPrivacidad(alEntendido = volver, alVolver = volver) }

        // ---------- Juego ----------
        composable(Rutas.INICIO) {
            PantallaInicio(alNavegar = { ruta -> nav.navigate(ruta) })
        }
        composable(Rutas.MODOS) {
            PantallaModos(
                alVolver = volver,
                alJugar = { modo, dificultad, imagenId ->
                    nav.navigate(Rutas.partida(modo, dificultad, imagenId))
                },
                alDesafiar = { nav.navigate(Rutas.DUELO) },
                alMisFotos = { nav.navigate(Rutas.MIS_FOTOS) }
            )
        }
        composable(
            Rutas.PARTIDA,
            arguments = listOf(
                navArgument("modo") { type = NavType.StringType },
                navArgument("dificultad") { type = NavType.IntType },
                navArgument("imagenId") { type = NavType.StringType }
            )
        ) { entrada ->
            val args = entrada.arguments
            PantallaPartida(
                modo = args?.getString("modo").orEmpty(),
                dificultad = args?.getInt("dificultad") ?: 3,
                imagenId = Uri.decode(args?.getString("imagenId").orEmpty()),
                alVolver = volver
            )
        }

        // ---------- Fotos ----------
        composable(Rutas.MIS_FOTOS) {
            PantallaMisFotos(
                alAgregarFoto = { nav.navigate(Rutas.PERMISO_FOTOS) },
                alVolver = volver
            )
        }
        composable(Rutas.PERMISO_FOTOS) {
            PantallaPermisoFotos(
                alConcedido = {
                    nav.navigate(Rutas.NUEVA_FOTO) {
                        popUpTo(Rutas.PERMISO_FOTOS) { inclusive = true }
                    }
                },
                alVolver = volver
            )
        }
        composable(Rutas.NUEVA_FOTO) {
            PantallaNuevaFoto(
                alFotoGuardada = { fotoId ->
                    nav.navigate(Rutas.recortar(fotoId)) {
                        popUpTo(Rutas.NUEVA_FOTO) { inclusive = true }
                    }
                },
                alVolver = volver
            )
        }
        composable(
            Rutas.RECORTAR,
            arguments = listOf(navArgument("fotoId") { type = NavType.LongType })
        ) { entrada ->
            PantallaRecortar(
                fotoId = entrada.arguments?.getLong("fotoId") ?: 0L,
                alListo = volver,
                alVolver = volver
            )
        }

        // ---------- Secciones que requieren cuenta ----------
        composable(Rutas.AMIGOS) {
            SeccionProtegida("Amigos", crearCuenta, volver) {
                PantallaAgregarAmigo(
                    alVerAmigos = { nav.navigate(Rutas.MIS_AMIGOS) },
                    alVolver = volver
                )
            }
        }
        composable(Rutas.MIS_AMIGOS) {
            SeccionProtegida("Amigos", crearCuenta, volver) {
                PantallaMisAmigos(alAgregar = { nav.navigate(Rutas.AMIGOS) }, alVolver = volver)
            }
        }
        composable(Rutas.DUELO) {
            SeccionProtegida("Duelo VS", crearCuenta, volver) {
                PantallaEnConstruccion("Duelo VS", volver)
            }
        }
        composable(Rutas.RANKING) {
            SeccionProtegida("Ranking", crearCuenta, volver) {
                PantallaEnConstruccion("Ranking", volver)
            }
        }
        composable(Rutas.PERFIL) { PantallaEnConstruccion("Perfil", volver) }
        composable(Rutas.CONFIGURACION) { PantallaEnConstruccion("Configuración", volver) }
    }
}
