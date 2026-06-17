package com.kdd.kdd_frontend.ui.screens.plan

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.grid.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import java.time.LocalDate
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import coil.compose.AsyncImage
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.lifecycle.viewmodel.compose.viewModel
import com.kdd.kdd_frontend.network.dto.ParticipanteDto
import com.google.android.gms.maps.model.CameraPosition
import com.google.android.gms.maps.model.LatLng
import com.google.maps.android.compose.*
import com.kdd.kdd_frontend.ui.theme.*
import com.kdd.kdd_frontend.viewmodel.PlanDetalleState
import com.kdd.kdd_frontend.viewmodel.PlanViewModel

/**
 * Pantalla de detalle de un plan.
 *
 * Muestra toda la informacion del plan: imagen, titulo, descripcion,
 * fecha, horario, ubicacion en mapa, participantes y valoraciones.
 *
 * El anfitrion puede:
 * - Marcar presencia de los participantes.
 * - Editar o eliminar el plan.
 *
 * El resto de usuarios pueden:
 * - Unirse o abandonar el plan.
 * - Confirmar su propia asistencia cuando el plan ya ha comenzado.
 * - Valorar a otros participantes (solo si ambos tienen presente=true).
 */
data class ParticipanteInfo(
    val id: Long,
    val nombre: String,
    val edad: Int?,
    val descripcion: String = "",
    val fotoPerfil: String? = null,
    val presente: Boolean = false,
    val puntuacion: Double? = null,
    val acompanantes: Int? = null
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PlanDetailScreen(
    planId: Long,
    onNavigateBack: () -> Unit,
    onNavigateToEditPlan: (Long) -> Unit = {}
) {
    val viewModel: PlanViewModel = viewModel()
    val detalleState by viewModel.detalleState.collectAsState()
    val participando by viewModel.participando.collectAsState()
    val participantesDto by viewModel.participantes.collectAsState()


    var selectedTab by remember { mutableIntStateOf(0) }
    var participanteSeleccionado by remember { mutableStateOf<ParticipanteInfo?>(null) }
    var mostrarMenuCreador by remember { mutableStateOf(false) }
    var mostrarMenuParticipante by remember { mutableStateOf(false) }
    var mostrarConfirmacionEliminar by remember { mutableStateOf(false) }
    var mostrarConfirmacionAbandonar by remember { mutableStateOf(false) }
    val snackbarHostState = remember { SnackbarHostState() }
    var errorMsg by remember { mutableStateOf("") }

    LaunchedEffect(errorMsg) {
        if (errorMsg.isNotBlank()) {
            snackbarHostState.showSnackbar(errorMsg)
            errorMsg = ""
        }
    }

    LaunchedEffect(planId) {
        viewModel.cargarDetalle(planId)
        viewModel.cargarParticipantes(planId)
    }

    LaunchedEffect(selectedTab) {
        if (selectedTab == 1) viewModel.cargarParticipantes(planId)
    }

    when (val estado = detalleState) {
        is PlanDetalleState.Loading -> {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator(color = KddPurple)
            }
        }
        is PlanDetalleState.Error -> {
            LaunchedEffect(estado) {
                if (estado.codigo == 404) {
                    onNavigateBack()
                }
            }
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Text(if (estado.codigo == 404) "Este plan ya no existe" else estado.mensaje, color = KddTextSecondary)
                    Button(onClick = onNavigateBack, colors = ButtonDefaults.buttonColors(containerColor = KddPurple)) {
                        Text("Volver", color = Color.White)
                    }
                }
            }
        }
        is PlanDetalleState.Success -> {
            val plan = estado.plan
            val numApuntados = plan.numApuntados
            val tabs = listOf("Información", "Apuntados ($numApuntados)")

            val miId = viewModel.miUserId

            val participantes = participantesDto.map { dto ->
                ParticipanteInfo(
                    id = dto.id,
                    nombre = dto.nombreUsuario?.takeIf { it.isNotBlank() } ?: dto.nombre,
                    edad = dto.edad,
                    descripcion = dto.descripcion ?: "",
                    fotoPerfil = dto.fotoPerfil,
                    presente = dto.presente,
                    puntuacion = dto.puntuacion,
                    acompanantes = dto.acompanantes
                )
            }

            // planHasStarted: true cuando el plan ya ha comenzado (considerando horaEvento)
            val planHasStarted = runCatching {
                val fecha = plan.fechaEvento?.let { LocalDate.parse(it) }
                when {
                    fecha == null -> true
                    fecha.isBefore(LocalDate.now()) -> true
                    fecha.isAfter(LocalDate.now()) -> false
                    else -> { // hoy
                        val horaInicio = plan.horaEvento?.let {
                            runCatching { java.time.LocalTime.parse(it.take(5)) }.getOrNull()
                        }
                        horaInicio == null || !java.time.LocalTime.now().isBefore(horaInicio)
                    }
                }
            }.getOrDefault(false)
            val yoEstoyPresente = participantes.any { it.id == miId && it.presente }

            Scaffold(
                snackbarHost = { SnackbarHost(snackbarHostState) },
                bottomBar = {
                    Surface(shadowElevation = 8.dp) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .navigationBarsPadding()
                                .padding(horizontal = 16.dp, vertical = 12.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            IconButton(
                                onClick = { },
                                modifier = Modifier.size(44.dp).clip(CircleShape).background(KddSurface)
                            ) {
                                Icon(Icons.Filled.Share, contentDescription = "Compartir", tint = KddTextPrimary)
                            }
                            IconButton(
                                onClick = { },
                                modifier = Modifier.size(44.dp).clip(CircleShape).background(KddSurface)
                            ) {
                                Icon(Icons.Filled.StarBorder, contentDescription = "Favorito", tint = KddTextPrimary)
                            }
                            val planActual = (detalleState as? PlanDetalleState.Success)?.plan
                            val esCreador = planActual?.creador == true
                            when {
                                // Si el plan ya empezó y el usuario (sea creador o participante) no ha confirmado
                                planHasStarted && (participando || esCreador) && !yoEstoyPresente -> {
                                    Button(
                                        onClick = {
                                            viewModel.marcarPresente(
                                                planId = planId,
                                                usuarioId = miId,
                                                onSuccess = { viewModel.cargarParticipantes(planId) }
                                            )
                                        },
                                        modifier = Modifier.weight(1f).height(48.dp),
                                        shape = RoundedCornerShape(24.dp),
                                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF4CAF50))
                                    ) {
                                        Icon(Icons.Filled.Check, contentDescription = null, tint = Color.White, modifier = Modifier.size(16.dp))
                                        Spacer(modifier = Modifier.width(6.dp))
                                        Text("Confirmar asistencia", color = Color.White, fontWeight = FontWeight.SemiBold)
                                    }
                                }
                                esCreador -> {
                                    Surface(
                                        modifier = Modifier.weight(1f).height(48.dp),
                                        shape = RoundedCornerShape(24.dp),
                                        color = KddSurface
                                    ) {
                                        Box(contentAlignment = Alignment.Center) {
                                            Text("Tu plan", color = KddTextSecondary, fontWeight = FontWeight.SemiBold)
                                        }
                                    }
                                }
                                participando -> {
                                    // Apuntado y plan no iniciado — Abandonar está en el menú MoreVert
                                    Surface(
                                        modifier = Modifier.weight(1f).height(48.dp),
                                        shape = RoundedCornerShape(24.dp),
                                        color = KddSurface
                                    ) {
                                        Box(contentAlignment = Alignment.Center) {
                                            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                                Icon(Icons.Filled.Check, contentDescription = null, tint = Color(0xFF4CAF50), modifier = Modifier.size(16.dp))
                                                Text("Apuntado", color = KddTextSecondary, fontWeight = FontWeight.SemiBold)
                                            }
                                        }
                                    }
                                }
                                else -> {
                                    Button(
                                        onClick = {
                                            viewModel.unirseAPlan(
                                                planId = planId,
                                                onSuccess = { viewModel.cargarDetalle(planId) },
                                                onError = { msg -> errorMsg = msg }
                                            )
                                        },
                                        modifier = Modifier.weight(1f).height(48.dp),
                                        shape = RoundedCornerShape(24.dp),
                                        colors = ButtonDefaults.buttonColors(containerColor = KddPurple)
                                    ) {
                                        Text("Unirse", color = Color.White, fontWeight = FontWeight.SemiBold)
                                    }
                                }
                            }
                        }
                    }
                }
            ) { paddingValues ->
                LazyColumn(modifier = Modifier.fillMaxSize().padding(paddingValues)) {
                    item {
                        Box(modifier = Modifier.fillMaxWidth().height(240.dp)) {
                            if (!plan.fotoPlanUrl.isNullOrBlank()) {
                                AsyncImage(
                                    model = plan.fotoPlanUrl,
                                    contentDescription = "Foto del plan",
                                    modifier = Modifier.fillMaxSize(),
                                    contentScale = ContentScale.Crop
                                )
                                Box(modifier = Modifier.fillMaxSize().background(Color.Black.copy(alpha = 0.35f)))
                            } else {
                                Box(modifier = Modifier.fillMaxSize().background(KddSurfaceVariant))
                            }
                            IconButton(
                                onClick = onNavigateBack,
                                modifier = Modifier
                                    .align(Alignment.TopStart).padding(8.dp).statusBarsPadding()
                                    .size(36.dp).clip(CircleShape).background(Color.White.copy(alpha = 0.8f))
                            ) {
                                Icon(Icons.Filled.Close, contentDescription = "Cerrar", tint = KddTextPrimary)
                            }
                            Box(modifier = Modifier.align(Alignment.TopEnd).padding(8.dp).statusBarsPadding()) {
                                if (plan.creador) {
                                    IconButton(
                                        onClick = { mostrarMenuCreador = true },
                                        modifier = Modifier.size(36.dp).clip(CircleShape).background(Color.White.copy(alpha = 0.8f))
                                    ) {
                                        Icon(Icons.Filled.MoreVert, contentDescription = "Opciones", tint = KddTextPrimary)
                                    }
                                    DropdownMenu(
                                        expanded = mostrarMenuCreador,
                                        onDismissRequest = { mostrarMenuCreador = false }
                                    ) {
                                        DropdownMenuItem(
                                            text = { Text("Editar plan") },
                                            leadingIcon = { Icon(Icons.Filled.Edit, contentDescription = null) },
                                            onClick = {
                                                mostrarMenuCreador = false
                                                onNavigateToEditPlan(planId)
                                            }
                                        )
                                        DropdownMenuItem(
                                            text = { Text("Abandonar plan", color = Color(0xFFE53935)) },
                                            leadingIcon = { Icon(Icons.Filled.ExitToApp, contentDescription = null, tint = Color(0xFFE53935)) },
                                            onClick = {
                                                mostrarMenuCreador = false
                                                mostrarConfirmacionAbandonar = true
                                            }
                                        )
                                        DropdownMenuItem(
                                            text = { Text("Eliminar plan", color = Color(0xFFE53935)) },
                                            leadingIcon = { Icon(Icons.Filled.Delete, contentDescription = null, tint = Color(0xFFE53935)) },
                                            onClick = {
                                                mostrarMenuCreador = false
                                                mostrarConfirmacionEliminar = true
                                            }
                                        )
                                    }
                                } else if (participando) {
                                    IconButton(
                                        onClick = { mostrarMenuParticipante = true },
                                        modifier = Modifier.size(36.dp).clip(CircleShape).background(Color.White.copy(alpha = 0.8f))
                                    ) {
                                        Icon(Icons.Filled.MoreVert, contentDescription = "Opciones", tint = KddTextPrimary)
                                    }
                                    DropdownMenu(
                                        expanded = mostrarMenuParticipante,
                                        onDismissRequest = { mostrarMenuParticipante = false }
                                    ) {
                                        DropdownMenuItem(
                                            text = { Text("Abandonar plan", color = Color(0xFFE53935)) },
                                            leadingIcon = { Icon(Icons.Filled.ExitToApp, contentDescription = null, tint = Color(0xFFE53935)) },
                                            onClick = {
                                                mostrarMenuParticipante = false
                                                mostrarConfirmacionAbandonar = true
                                            }
                                        )
                                        DropdownMenuItem(
                                            text = { Text("Reportar") },
                                            leadingIcon = { Icon(Icons.Filled.Flag, contentDescription = null) },
                                            onClick = {
                                                mostrarMenuParticipante = false
                                                errorMsg = "Función de reporte disponible próximamente"
                                            }
                                        )
                                    }
                                }
                            }
                            Column(modifier = Modifier.align(Alignment.BottomStart).padding(16.dp)) {
                                Text(plan.titulo, style = MaterialTheme.typography.headlineMedium, color = Color.White, fontWeight = FontWeight.Bold)
                                if (!plan.categoria.isNullOrBlank()) {
                                    Text(plan.categoria, style = MaterialTheme.typography.bodyMedium, color = Color.White.copy(alpha = 0.85f))
                                }
                            }
                        }

                        TabRow(selectedTabIndex = selectedTab, containerColor = Color.White, contentColor = KddPurple) {
                            tabs.forEachIndexed { index, title ->
                                Tab(
                                    selected = selectedTab == index,
                                    onClick = { selectedTab = index },
                                    text = {
                                        Text(title, fontWeight = if (selectedTab == index) FontWeight.Bold else FontWeight.Normal)
                                    }
                                )
                            }
                        }
                    }

                    when (selectedTab) {
                        0 -> {
                            item {
                                Column(
                                    modifier = Modifier.padding(16.dp),
                                    verticalArrangement = Arrangement.spacedBy(16.dp)
                                ) {
                                    if (plan.edadMin != null && plan.edadMax != null) {
                                        Surface(shape = RoundedCornerShape(8.dp), color = KddSurface) {
                                            Text(
                                                text = "Edad ${plan.edadMin} - ${plan.edadMax}",
                                                modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                                                style = MaterialTheme.typography.bodySmall,
                                                color = KddTextSecondary
                                            )
                                        }
                                    }
                                    if (!plan.descripcion.isNullOrBlank()) {
                                        Text(
                                            text = plan.descripcion,
                                            style = MaterialTheme.typography.bodyMedium,
                                            color = KddTextSecondary
                                        )
                                    }
                                    Text("Sobre esta actividad", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = KddTextPrimary)
                                    if (!plan.anfitrionNombre.isNullOrBlank()) {
                                        PlanInfoRow(icon = Icons.Filled.Person, label = "Anfitrión", value = plan.anfitrionNombre)
                                    }
                                    PlanInfoRow(
                                        icon = Icons.Filled.Group,
                                        label = "Apuntados",
                                        value = "${plan.numApuntados} persona${if ((plan.numApuntados ?: 0) != 1) "s" else ""}",
                                        isClickable = true,
                                        onClick = { selectedTab = 1 }
                                    )
                                    if (!plan.fechaEvento.isNullOrBlank()) {
                                        val fechaFormateada = runCatching {
                                            val d = java.time.LocalDate.parse(plan.fechaEvento)
                                            "${d.dayOfMonth.toString().padStart(2,'0')}/${d.monthValue.toString().padStart(2,'0')}/${d.year}"
                                        }.getOrDefault(plan.fechaEvento)
                                        val horaInicio = plan.horaEvento?.take(5)
                                        val horaFin = plan.horaHasta?.take(5)
                                        val horario = when {
                                            horaInicio != null && horaFin != null -> "$horaInicio – $horaFin"
                                            horaInicio != null -> horaInicio
                                            else -> null
                                        }
                                        val fechaHora = if (horario != null) "$fechaFormateada · $horario" else fechaFormateada
                                        PlanInfoRow(icon = Icons.Filled.CalendarMonth, label = "Fecha", value = fechaHora)
                                    }
                                    if (!plan.ubicacionTexto.isNullOrBlank()) {
                                        PlanInfoRow(icon = Icons.Filled.LocationOn, label = "Ubicación", value = plan.ubicacionTexto)
                                    }
                                    if (!plan.idioma.isNullOrBlank()) {
                                        PlanInfoRow(icon = Icons.Filled.Translate, label = "Idioma", value = plan.idioma)
                                    }
                                    if (plan.latitud != null && plan.longitud != null) {
                                        val planLatLng = LatLng(plan.latitud, plan.longitud)
                                        val cameraState = rememberCameraPositionState {
                                            position = CameraPosition.fromLatLngZoom(planLatLng, 14f)
                                        }
                                        Box(
                                            modifier = Modifier.fillMaxWidth().height(160.dp).clip(RoundedCornerShape(12.dp))
                                        ) {
                                            GoogleMap(
                                                modifier = Modifier.fillMaxSize(),
                                                cameraPositionState = cameraState,
                                                uiSettings = MapUiSettings(
                                                    zoomControlsEnabled = false,
                                                    scrollGesturesEnabled = false,
                                                    zoomGesturesEnabled = false,
                                                    myLocationButtonEnabled = false
                                                )
                                            ) {
                                                Marker(
                                                    state = rememberMarkerState(position = planLatLng),
                                                    title = plan.titulo
                                                )
                                            }
                                        }
                                    } else {
                                        Box(
                                            modifier = Modifier.fillMaxWidth().height(160.dp).clip(RoundedCornerShape(12.dp)).background(Color(0xFFE8EAF0)),
                                            contentAlignment = Alignment.Center
                                        ) {
                                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                                Icon(Icons.Filled.Map, contentDescription = null, tint = KddPurple.copy(alpha = 0.4f), modifier = Modifier.size(40.dp))
                                                Text("Sin ubicación", color = KddTextHint, style = MaterialTheme.typography.bodySmall)
                                            }
                                        }
                                    }
                                }
                            }
                        }
                        1 -> {
                            if (participantes.isEmpty()) {
                                item {
                                    Box(
                                        modifier = Modifier.fillMaxWidth().padding(32.dp),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Text("Nadie se ha apuntado todavía", style = MaterialTheme.typography.bodyMedium, color = KddTextHint)
                                    }
                                }
                            } else {
                                item {
                                    LazyVerticalGrid(
                                        columns = GridCells.Fixed(3),
                                        modifier = Modifier.fillMaxWidth().height((((participantes.size + 2) / 3) * 160).dp).padding(8.dp),
                                        userScrollEnabled = false
                                    ) {
                                        items(participantes.size) { index ->
                                            val p = participantes[index]
                                            PresenteCard(
                                                nombre = p.nombre,
                                                edad = p.edad,
                                                presente = p.presente,
                                                puntuacion = p.puntuacion,
                                                acompanantes = p.acompanantes,
                                                esAdmin = plan.creador,
                                                onMarcarPresente = if (plan.creador) {
                                                    { viewModel.marcarPresente(planId, p.id, onSuccess = {}) }
                                                } else null,
                                                onClick = { participanteSeleccionado = p }
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }

            // Puedo valorar si estoy marcado como presente
            val estaConfirmado = yoEstoyPresente

            participanteSeleccionado?.let { p ->
                PerfilUsuarioDialog(
                    participante = p,
                    planId = planId,
                    viewModel = viewModel,
                    estaConfirmado = estaConfirmado,
                    onDismiss = { participanteSeleccionado = null }
                )
            }

            if (mostrarConfirmacionAbandonar) {
                AlertDialog(
                    onDismissRequest = { mostrarConfirmacionAbandonar = false },
                    title = { Text("Abandonar plan") },
                    text = { Text("¿Seguro que quieres abandonar este plan?") },
                    confirmButton = {
                        TextButton(
                            onClick = {
                                mostrarConfirmacionAbandonar = false
                                viewModel.abandonarPlan(
                                    planId = planId,
                                    onSuccess = { viewModel.cargarDetalle(planId) },
                                    onError = { msg -> errorMsg = msg }
                                )
                            }
                        ) {
                            Text("Abandonar", color = Color(0xFFE53935))
                        }
                    },
                    dismissButton = {
                        TextButton(onClick = { mostrarConfirmacionAbandonar = false }) {
                            Text("Cancelar")
                        }
                    }
                )
            }

            if (mostrarConfirmacionEliminar) {
                AlertDialog(
                    onDismissRequest = { mostrarConfirmacionEliminar = false },
                    title = { Text("Eliminar plan") },
                    text = { Text("¿Seguro que quieres eliminar este plan? Esta acción no se puede deshacer.") },
                    confirmButton = {
                        TextButton(
                            onClick = {
                                mostrarConfirmacionEliminar = false
                                viewModel.eliminarPlan(
                                    planId = planId,
                                    onSuccess = { onNavigateBack() },
                                    onError = { msg -> errorMsg = msg }
                                )
                            }
                        ) {
                            Text("Eliminar", color = Color(0xFFE53935))
                        }
                    },
                    dismissButton = {
                        TextButton(onClick = { mostrarConfirmacionEliminar = false }) {
                            Text("Cancelar")
                        }
                    }
                )
            }
        }
    }
}

@Composable
private fun PerfilUsuarioDialog(
    participante: ParticipanteInfo,
    planId: Long,
    viewModel: PlanViewModel,
    estaConfirmado: Boolean,
    onDismiss: () -> Unit
) {
    var estrellas by remember { mutableIntStateOf(0) }
    var valoracionEnviada by remember { mutableStateOf(false) }
    var errorValorar by remember { mutableStateOf<String?>(null) }
    val solicitudesEnviadas by viewModel.solicitudesEnviadas.collectAsState()
    val amigos by viewModel.amigos.collectAsState()
    val solicitudEnviada = solicitudesEnviadas.contains(participante.id)
    val esAmigo = amigos.contains(participante.id)
    val esSiMismo = participante.id == viewModel.miUserId

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Box(modifier = Modifier.fillMaxSize().background(Color.White)) {
            Column(modifier = Modifier.fillMaxSize()) {

                Box(
                    modifier = Modifier.fillMaxWidth().statusBarsPadding()
                        .padding(horizontal = 8.dp, vertical = 8.dp)
                ) {
                    IconButton(
                        onClick = onDismiss,
                        modifier = Modifier.align(Alignment.TopStart).size(40.dp).clip(CircleShape).background(KddSurface)
                    ) {
                        Icon(Icons.Filled.Close, contentDescription = "Cerrar", tint = KddTextPrimary)
                    }
                }

                Row(
                    modifier = Modifier.fillMaxWidth().padding(horizontal = 20.dp, vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    Box(
                        modifier = Modifier.size(90.dp).clip(CircleShape).background(KddPurple),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(participante.nombre.first().toString(), color = Color.White, fontWeight = FontWeight.Bold, fontSize = 36.sp)
                    }
                    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                        Text(participante.nombre, style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold, color = KddTextPrimary)
                        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                            if (participante.edad != null) {
                                Text("${participante.edad} años", style = MaterialTheme.typography.bodySmall, color = KddTextHint)
                            }
                            Icon(Icons.Filled.Star, contentDescription = null, tint = Color(0xFF2196F3), modifier = Modifier.size(14.dp))
                            Text(
                                text = if (participante.puntuacion != null) String.format("%.1f", participante.puntuacion) else "Sin val.",
                                style = MaterialTheme.typography.bodySmall,
                                color = KddTextHint
                            )
                        }
                    }
                }

                LazyColumn(
                    modifier = Modifier.weight(1f).fillMaxWidth().padding(horizontal = 20.dp, vertical = 16.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    item {
                        Text(
                            text = if (participante.descripcion.isNotBlank()) participante.descripcion else "Este usuario no ha añadido una descripción.",
                            style = MaterialTheme.typography.bodyMedium,
                            color = if (participante.descripcion.isNotBlank()) KddTextSecondary else KddTextHint
                        )
                    }
                    if (!esSiMismo) {
                        item {
                            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                                HorizontalDivider(color = KddDivider)
                                Text(
                                    text = if (valoracionEnviada) "¡Valoración enviada!" else "Valora a ${participante.nombre}",
                                    style = MaterialTheme.typography.titleSmall,
                                    fontWeight = FontWeight.SemiBold,
                                    color = if (valoracionEnviada) KddPurple else KddTextPrimary
                                )
                                if (!valoracionEnviada) {
                                    Row(horizontalArrangement = Arrangement.Center, modifier = Modifier.fillMaxWidth()) {
                                        (1..5).forEach { i ->
                                            IconButton(onClick = { estrellas = i; errorValorar = null }) {
                                                Icon(
                                                    imageVector = if (i <= estrellas) Icons.Filled.Star else Icons.Filled.StarBorder,
                                                    contentDescription = null,
                                                    tint = KddYellow,
                                                    modifier = Modifier.size(36.dp)
                                                )
                                            }
                                        }
                                    }
                                }
                                if (errorValorar != null) {
                                    Text(
                                        text = errorValorar!!,
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.error
                                    )
                                }
                            }
                        }
                    }
                }

                Surface(shadowElevation = 4.dp) {
                    Box(modifier = Modifier.fillMaxWidth().navigationBarsPadding().padding(horizontal = 20.dp, vertical = 12.dp)) {
                        when {
                            estrellas > 0 && !valoracionEnviada && !estaConfirmado -> {
                                Surface(
                                    shape = RoundedCornerShape(12.dp),
                                    color = Color(0xFFFFF3E0),
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Text(
                                        text = "Confirma tu asistencia al plan para poder valorar a los demás",
                                        modifier = Modifier.padding(14.dp),
                                        style = MaterialTheme.typography.bodySmall,
                                        color = Color(0xFF795548)
                                    )
                                }
                            }
                            estrellas > 0 && !valoracionEnviada -> {
                                Button(
                                    onClick = {
                                        viewModel.valorar(
                                            valoradoId = participante.id,
                                            planId = planId,
                                            puntuacion = estrellas,
                                            onSuccess = {
                                                valoracionEnviada = true
                                                errorValorar = null
                                                viewModel.cargarParticipantes(planId)
                                            },
                                            onError = { msg -> errorValorar = msg }
                                        )
                                    },
                                    modifier = Modifier.fillMaxWidth().height(52.dp),
                                    shape = RoundedCornerShape(26.dp),
                                    colors = ButtonDefaults.buttonColors(containerColor = KddPurple)
                                ) {
                                    Icon(Icons.Filled.Star, contentDescription = null, tint = Color.White, modifier = Modifier.size(18.dp))
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text("Enviar valoración", color = Color.White, fontWeight = FontWeight.SemiBold)
                                }
                            }
                            !esSiMismo && esAmigo -> {
                                Box(
                                    modifier = Modifier.fillMaxWidth().height(52.dp),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                        Icon(Icons.Filled.People, contentDescription = null, tint = KddSuccess, modifier = Modifier.size(18.dp))
                                        Text("Ya sois amigos", color = KddSuccess, style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.SemiBold)
                                    }
                                }
                            }
                            !esSiMismo && solicitudEnviada -> {
                                Box(
                                    modifier = Modifier.fillMaxWidth().height(52.dp),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text("Solicitud enviada", color = KddTextHint, style = MaterialTheme.typography.bodyMedium)
                                }
                            }
                            !esSiMismo -> {
                                Button(
                                    onClick = {
                                        viewModel.enviarSolicitud(participante.id, onSuccess = {}, onError = {})
                                    },
                                    modifier = Modifier.fillMaxWidth().height(52.dp),
                                    shape = RoundedCornerShape(26.dp),
                                    colors = ButtonDefaults.buttonColors(containerColor = Color.Transparent, contentColor = KddPurple),
                                    elevation = ButtonDefaults.buttonElevation(0.dp)
                                ) {
                                    Icon(Icons.Filled.PersonAdd, contentDescription = null, tint = KddPurple, modifier = Modifier.size(20.dp))
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text("Añadir amigo", fontWeight = FontWeight.SemiBold, color = KddPurple)
                                }
                            }
                            else -> {
                                // esSiMismo sin estrellas seleccionadas — no mostrar nada
                                Box(modifier = Modifier.fillMaxWidth().height(52.dp))
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun PlanInfoRow(
    icon: ImageVector,
    label: String,
    value: String,
    isClickable: Boolean = false,
    onClick: (() -> Unit)? = null
) {
    val mod = if (isClickable && onClick != null) Modifier.fillMaxWidth().clickable { onClick() } else Modifier.fillMaxWidth()
    Row(verticalAlignment = Alignment.CenterVertically, modifier = mod) {
        Box(modifier = Modifier.size(44.dp).clip(CircleShape).background(KddSurface), contentAlignment = Alignment.Center) {
            Icon(icon, contentDescription = null, tint = KddTextSecondary, modifier = Modifier.size(22.dp))
        }
        Spacer(modifier = Modifier.width(12.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(text = label, style = MaterialTheme.typography.bodyMedium, color = KddTextPrimary, fontWeight = FontWeight.Medium)
            Text(text = value, style = MaterialTheme.typography.bodySmall, color = KddTextSecondary)
        }
        if (isClickable) {
            Icon(Icons.Filled.ChevronRight, contentDescription = null, tint = KddTextHint)
        }
    }
}

@Composable
private fun SolicitudParticipanteRow(
    participante: ParticipanteDto,
    onConfirmar: () -> Unit,
    onRechazar: () -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Box(
            modifier = Modifier.size(44.dp).clip(CircleShape).background(KddSurfaceVariant),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = participante.nombre.firstOrNull()?.uppercaseChar()?.toString() ?: "?",
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Bold,
                color = KddTextSecondary
            )
        }
        Column(modifier = Modifier.weight(1f)) {
            Text(participante.nombre, style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.SemiBold, color = KddTextPrimary)
            if (participante.edad != null) {
                Text("${participante.edad} años", style = MaterialTheme.typography.bodySmall, color = KddTextHint)
            }
        }
        IconButton(
            onClick = onConfirmar,
            modifier = Modifier.size(36.dp).clip(CircleShape).background(Color(0xFF4CAF50))
        ) {
            Icon(Icons.Filled.Check, contentDescription = "Confirmar", tint = Color.White, modifier = Modifier.size(18.dp))
        }
        IconButton(
            onClick = onRechazar,
            modifier = Modifier.size(36.dp).clip(CircleShape).background(KddSurfaceVariant)
        ) {
            Icon(Icons.Filled.Close, contentDescription = "Rechazar", tint = KddTextSecondary, modifier = Modifier.size(18.dp))
        }
    }
}

@Composable
private fun PresenteCard(
    nombre: String,
    edad: Int?,
    presente: Boolean = false,
    puntuacion: Double? = null,
    acompanantes: Int? = null,
    esAdmin: Boolean = false,
    onMarcarPresente: (() -> Unit)? = null,
    onClick: () -> Unit
) {
    Column(
        modifier = Modifier.padding(6.dp).clickable { onClick() },
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Box(modifier = Modifier.fillMaxWidth().aspectRatio(1f)) {
            Box(
                modifier = Modifier.fillMaxSize().clip(RoundedCornerShape(8.dp)).background(KddSurfaceVariant),
                contentAlignment = Alignment.Center
            ) {
                Text(nombre.first().toString(), fontWeight = FontWeight.Bold, fontSize = 24.sp, color = KddTextSecondary)
            }
            if (presente) {
                Box(
                    modifier = Modifier
                        .align(Alignment.BottomEnd)
                        .padding(4.dp)
                        .size(22.dp)
                        .clip(CircleShape)
                        .background(Color(0xFF4CAF50))
                        .then(if (esAdmin && onMarcarPresente != null) Modifier.clickable { onMarcarPresente() } else Modifier),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(Icons.Filled.Check, contentDescription = "Presente (toca para desmarcar)", tint = Color.White, modifier = Modifier.size(13.dp))
                }
            } else if (esAdmin && onMarcarPresente != null) {
                Box(
                    modifier = Modifier
                        .align(Alignment.BottomEnd)
                        .padding(4.dp)
                        .size(22.dp)
                        .clip(CircleShape)
                        .background(Color.White)
                        .clickable { onMarcarPresente() },
                    contentAlignment = Alignment.Center
                ) {
                    Icon(Icons.Filled.CheckCircleOutline, contentDescription = "Marcar como presente", tint = KddPurple, modifier = Modifier.size(20.dp))
                }
            }
            // Badge de acompañantes (esquina inferior izquierda)
            if (acompanantes != null && acompanantes > 1) {
                Box(
                    modifier = Modifier
                        .align(Alignment.BottomStart)
                        .padding(4.dp)
                        .size(22.dp)
                        .clip(CircleShape)
                        .background(KddPurple),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "+${acompanantes - 1}",
                        color = Color.White,
                        fontSize = 8.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
        Spacer(modifier = Modifier.height(4.dp))
        // Nombre + edad en la misma línea
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text(
                text = nombre.split(" ").first(),
                style = MaterialTheme.typography.labelSmall,
                color = KddTextPrimary,
                maxLines = 1
            )
            if (edad != null) {
                Text(
                    text = " · $edad",
                    style = MaterialTheme.typography.labelSmall,
                    color = KddTextHint,
                    fontSize = 9.sp
                )
            }
        }
        // Puntuación
        if (puntuacion != null) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center
            ) {
                Icon(Icons.Filled.Star, contentDescription = null, tint = KddYellow, modifier = Modifier.size(10.dp))
                Text(
                    text = " ${"%.1f".format(puntuacion)}",
                    style = MaterialTheme.typography.labelSmall,
                    color = KddTextHint,
                    fontSize = 9.sp
                )
            }
        }
    }
}
