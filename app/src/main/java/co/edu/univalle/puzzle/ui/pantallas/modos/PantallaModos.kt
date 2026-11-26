package co.edu.univalle.puzzle.ui.pantallas.modos

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.systemBarsPadding
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import co.edu.univalle.puzzle.motor.Dificultad
import co.edu.univalle.puzzle.ui.acceso.BotonPrincipal
import co.edu.univalle.puzzle.ui.acceso.EncabezadoAcceso

private val Naranja = Color(0xFFF2A93B)
private val Verde = Color(0xFF3CC29A)
private val Rosa = Color(0xFFE8637A)

/**
 * Pantalla 04 · Modos de juego y 04c · Elegir imagen (T-73). En la 04 se toca Clásico o
 * Contrarreloj y se pasa a la 04c, donde se cambia la dificultad y se elige una imagen del
 * catálogo (T-71). La pantalla solo dibuja; elegir, cargar y descargar lo hace [ModosViewModel].
 */
@Composable
fun PantallaModos(
    alVolver: () -> Unit,
    alJugar: (modo: String, dificultad: Int, imagenId: String) -> Unit,
    alDesafiar: () -> Unit,
    alMisFotos: () -> Unit,
    vm: ModosViewModel = viewModel(factory = ModosViewModel.fabrica(LocalContext.current))
) {
    val estado by vm.estado.collectAsStateWithLifecycle()
    LaunchedEffect(estado.jugada) {
        estado.jugada?.let {
            vm.navegacionHecha()
            alJugar(it.modo, it.dificultad, it.imagenId)
        }
    }
    BackHandler(enabled = estado.viendoImagenes, onBack = vm::volverAModos)

    if (estado.viendoImagenes) {
        ElegirImagen(estado, vm::elegirImagen, vm::elegirDificultad, vm::jugar, vm::volverAModos)
    } else {
        ListaModos(alVolver, vm::elegirModo, alDesafiar, alMisFotos)
    }
}

/** 04 · Modos de juego. */
@Composable
private fun ListaModos(
    alVolver: () -> Unit,
    alElegir: (ModoJuego) -> Unit,
    alDesafiar: () -> Unit,
    alMisFotos: () -> Unit
) {
    Column(
        Modifier.fillMaxSize().systemBarsPadding().verticalScroll(rememberScrollState()).padding(horizontal = 20.dp, vertical = 20.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        EncabezadoAcceso("Modos de juego", "Elige cómo quieres jugar hoy", alVolver)
        TarjetaDuelo(alDesafiar)
        Text("Todos los modos", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
        TarjetaModo("∞", MaterialTheme.colorScheme.primary, ModoJuego.CLASICO.nombre, ModoJuego.CLASICO.descripcion) {
            alElegir(ModoJuego.CLASICO)
        }
        TarjetaModo("60s", Naranja, ModoJuego.CONTRARRELOJ.nombre, ModoJuego.CONTRARRELOJ.descripcion) {
            alElegir(ModoJuego.CONTRARRELOJ)
        }
        TarjetaModo("+", Verde, "Mis fotos", "Convierte tus fotos en un puzzle.", alMisFotos)
    }
}

@Composable
private fun TarjetaDuelo(alDesafiar: () -> Unit) {
    Box(
        Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(20.dp))
            .background(Brush.horizontalGradient(listOf(MaterialTheme.colorScheme.primary, Rosa)))
            .padding(18.dp)
    ) {
        Column(Modifier.padding(end = 96.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
            Text("DUELO EN VIVO", color = Color(0xFFFFD27A), style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold)
            Text("Reta a un amigo", color = Color.White, fontSize = 22.sp, fontWeight = FontWeight.Bold)
            Text("Mismo tablero para los dos.", color = Color.White.copy(alpha = 0.85f), style = MaterialTheme.typography.bodySmall)
            Spacer(Modifier.height(8.dp))
            Box(
                Modifier
                    .clip(RoundedCornerShape(12.dp))
                    .background(Color.White)
                    .clickable(role = Role.Button, onClick = alDesafiar)
                    .padding(horizontal = 18.dp, vertical = 10.dp)
            ) { Text("Desafiar", color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.Bold) }
        }
        Box(Modifier.align(Alignment.CenterEnd).size(96.dp)) {
            Circulo("J", Naranja, Modifier.align(Alignment.TopStart))
            Box(
                Modifier.align(Alignment.Center).size(26.dp).clip(CircleShape).background(Color(0xFF1E1B2E)),
                contentAlignment = Alignment.Center
            ) { Text("VS", color = Color.White, fontSize = 9.sp, fontWeight = FontWeight.Bold) }
            Circulo("?", Verde, Modifier.align(Alignment.BottomEnd))
        }
    }
}

@Composable
private fun Circulo(texto: String, color: Color, modifier: Modifier) {
    Box(
        modifier.size(46.dp).clip(CircleShape).background(color).border(2.dp, Color.White, CircleShape),
        contentAlignment = Alignment.Center
    ) { Text(texto, color = Color.White, fontWeight = FontWeight.Bold, fontSize = 18.sp) }
}

@Composable
private fun TarjetaModo(icono: String, color: Color, titulo: String, descripcion: String, alTocar: () -> Unit) {
    Row(
        Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(Color.White)
            .clickable(role = Role.Button, onClick = alTocar)
            .padding(14.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        Box(
            Modifier.size(48.dp).clip(RoundedCornerShape(12.dp)).background(color),
            contentAlignment = Alignment.Center
        ) { Text(icono, color = Color.White, fontWeight = FontWeight.Bold) }
        Column(Modifier.weight(1f)) {
            Text(titulo, fontWeight = FontWeight.Bold)
            Text(descripcion, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
        Text("›", fontSize = 22.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
    }
}

/** 04c · Elegir imagen (catálogo). */
@Composable
private fun ElegirImagen(
    estado: EstadoModos,
    alElegirImagen: (String) -> Unit,
    alElegirDificultad: (Dificultad) -> Unit,
    alJugar: () -> Unit,
    alVolver: () -> Unit
) {
    val eleccion = estado.eleccion
    var menu by remember { mutableStateOf(false) }
    Column(Modifier.fillMaxSize().systemBarsPadding().padding(horizontal = 20.dp, vertical = 20.dp)) {
        EncabezadoAcceso("Elegir imagen", "Catálogo · ${estado.imagenes.size} imágenes incluidas", alVolver)
        Spacer(Modifier.height(16.dp))
        Row(verticalAlignment = Alignment.CenterVertically) {
            val d = eleccion.dificultad
            Text(
                "${eleccion.modo.nombre} · ${d.tamano}×${d.tamano}",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.weight(1f)
            )
            Box {
                TextButton(onClick = { menu = true }) {
                    Text("Cambiar ▾", color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
                DropdownMenu(expanded = menu, onDismissRequest = { menu = false }) {
                    Dificultad.entries.forEach { opcion ->
                        DropdownMenuItem(
                            text = { Text("${opcion.tamano}×${opcion.tamano} ${opcion.nombre}") },
                            onClick = { alElegirDificultad(opcion); menu = false }
                        )
                    }
                }
            }
        }
        Spacer(Modifier.height(8.dp))
        LazyVerticalGrid(
            columns = GridCells.Fixed(3),
            modifier = Modifier.weight(1f),
            contentPadding = PaddingValues(vertical = 4.dp),
            horizontalArrangement = Arrangement.spacedBy(10.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            items(estado.imagenes, key = { it.id }) { img ->
                val elegida = eleccion.imagenId == img.id
                Box(
                    Modifier
                        .aspectRatio(1f)
                        .clip(RoundedCornerShape(14.dp))
                        .background(MaterialTheme.colorScheme.surfaceVariant)
                        .border(if (elegida) 3.dp else 0.dp, MaterialTheme.colorScheme.primary, RoundedCornerShape(14.dp))
                        .clickable { alElegirImagen(img.id) },
                    contentAlignment = Alignment.Center
                ) {
                    val mini = estado.miniaturas[img.id]
                    if (mini != null) {
                        Image(mini, img.titulo, contentScale = ContentScale.Crop, modifier = Modifier.fillMaxSize())
                    } else {
                        Text("☁ ${img.titulo}", style = MaterialTheme.typography.labelSmall, modifier = Modifier.padding(4.dp))
                    }
                }
            }
        }
        estado.error?.let {
            Text(it, color = MaterialTheme.colorScheme.error, modifier = Modifier.padding(vertical = 6.dp))
        }
        Spacer(Modifier.height(12.dp))
        BotonPrincipal("Jugar", habilitado = eleccion.lista, cargando = estado.descargando, alPulsar = alJugar)
    }
}
