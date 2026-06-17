package com.kdd.kdd_frontend.ui.screens.communities

import androidx.compose.foundation.background
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.AsyncImage
import com.kdd.kdd_frontend.network.dto.MiembroComunidadDto
import com.kdd.kdd_frontend.ui.components.PlanCard
import com.kdd.kdd_frontend.ui.components.PlanCardData
import com.kdd.kdd_frontend.network.dto.PlanDto
import com.kdd.kdd_frontend.ui.components.*
import com.kdd.kdd_frontend.ui.theme.*
import com.kdd.kdd_frontend.viewmodel.ComunidadDetalleState
import com.kdd.kdd_frontend.viewmodel.ComunidadViewModel

/**
 * Pantalla de detalle de una comunidad.
 *
 * Muestra la informacion de la comunidad, sus miembros y los planes
 * que tiene asociados. Permite unirse o abandonar la comunidad,
 * y acceder al detalle de cada plan.
 */
@Composable
fun CommunityDetailScreen(
    communityId: Long,
    onNavigateBack: () -> Unit,
    onNavigateToPlan: (Long) -> Unit,
    onNavigateToCreatePlan: () -> Unit = {}
) {
    val viewModel: ComunidadViewModel = viewModel()
    val detalleState by viewModel.detalleState.collectAsState()
    val miembros by viewModel.miembros.collectAsState()
    val planesComunidad by viewModel.planesComunidad.collectAsState()

    var selectedTab by remember { mutableIntStateOf(0) }
    val tabs = listOf("Informacion", "Actividades", "Miembros")
    val snackbarHostState = remember { SnackbarHostState() }
    var errorMsg by remember { mutableStateOf<String?>(null) }
    var mostrarConfirmAbandonar by remember { mutableStateOf(false) }
    var mostrarMenu by remember { mutableStateOf(false) }

    LaunchedEffect(errorMsg) {
        errorMsg?.let { snackbarHostState.showSnackbar(it); errorMsg = null }
    }

    LaunchedEffect(communityId) {
        viewModel.cargarDetalle(communityId)
    }

    LaunchedEffect(selectedTab) {
        if (selectedTab == 1) viewModel.cargarPlanesComunidad(communityId)
        if (selectedTab == 2) viewModel.cargarMiembros(communityId)
    }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        bottomBar = {
            val estado = detalleState
            if (estado is ComunidadDetalleState.Success) {
                val comunidad = estado.comunidad
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
                        when {
                            comunidad.admin || comunidad.miembro -> {
                                Button(
                                    onClick = onNavigateToCreatePlan,
                                    modifier = Modifier.weight(1f).height(48.dp),
                                    shape = RoundedCornerShape(24.dp),
                                    colors = ButtonDefaults.buttonColors(containerColor = KddPurple)
                                ) {
                                    Icon(Icons.Filled.Add, contentDescription = null, tint = Color.White, modifier = Modifier.size(18.dp))
                                    Spacer(Modifier.width(6.dp))
                                    Text(
                                        if (comunidad.admin) "Añadir actividad" else "Crear actividad",
                                        color = Color.White,
                                        fontWeight = FontWeight.SemiBold
                                    )
                                }
                            }
                            else -> {
                                Button(
                                    onClick = {
                                        viewModel.unirseAComunidad(
                                            id = communityId,
                                            onSuccess = { viewModel.cargarDetalle(communityId) },
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
        }
    ) { paddingValues ->
        when (val estado = detalleState) {
            is ComunidadDetalleState.Loading -> {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator(color = KddPurple)
                }
            }
            is ComunidadDetalleState.Error -> {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text(estado.mensaje, style = MaterialTheme.typography.bodyMedium, color = KddTextSecondary)
                }
            }
            is ComunidadDetalleState.Success -> {
                val comunidad = estado.comunidad
                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(paddingValues)
                ) {
                    item {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(240.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .background(KddSurfaceVariant)
                            )

                            IconButton(
                                onClick = onNavigateBack,
                                modifier = Modifier
                                    .align(Alignment.TopStart)
                                    .padding(8.dp)
                                    .statusBarsPadding()
                                    .size(36.dp)
                                    .clip(CircleShape)
                                    .background(Color.White.copy(alpha = 0.8f))
                            ) {
                                Icon(Icons.Filled.Close, contentDescription = "Cerrar", tint = KddTextPrimary)
                            }

                            // MoreVert para admins y miembros
                            if (comunidad.admin || comunidad.miembro) {
                                Box(
                                    modifier = Modifier
                                        .align(Alignment.TopEnd)
                                        .padding(8.dp)
                                        .statusBarsPadding()
                                ) {
                                    IconButton(
                                        onClick = { mostrarMenu = true },
                                        modifier = Modifier
                                            .size(36.dp)
                                            .clip(CircleShape)
                                            .background(Color.White.copy(alpha = 0.8f))
                                    ) {
                                        Icon(Icons.Filled.MoreVert, contentDescription = "Opciones", tint = KddTextPrimary)
                                    }
                                    DropdownMenu(
                                        expanded = mostrarMenu,
                                        onDismissRequest = { mostrarMenu = false }
                                    ) {
                                        if (comunidad.admin) {
                                            DropdownMenuItem(
                                                text = { Text("Editar comunidad") },
                                                leadingIcon = { Icon(Icons.Filled.Edit, contentDescription = null) },
                                                onClick = {
                                                    mostrarMenu = false
                                                    errorMsg = "Editar comunidad estara disponible proximamente"
                                                }
                                            )
                                        }
                                        DropdownMenuItem(
                                            text = { Text("Abandonar comunidad", color = Color(0xFFE53935)) },
                                            leadingIcon = { Icon(Icons.Filled.ExitToApp, contentDescription = null, tint = Color(0xFFE53935)) },
                                            onClick = {
                                                mostrarMenu = false
                                                mostrarConfirmAbandonar = true
                                            }
                                        )
                                    }
                                }
                            }

                            Column(
                                modifier = Modifier
                                    .align(Alignment.BottomStart)
                                    .padding(16.dp)
                            ) {
                                Text(
                                    text = comunidad.nombre,
                                    style = MaterialTheme.typography.headlineMedium,
                                    color = Color.White,
                                    fontWeight = FontWeight.Bold
                                )
                                Text(
                                    text = if (!comunidad.categoria.isNullOrBlank()) "Comunidad · ${comunidad.categoria}" else "Comunidad",
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = Color.White.copy(alpha = 0.85f)
                                )
                            }
                        }

                        TabRow(
                            selectedTabIndex = selectedTab,
                            containerColor = Color.White,
                            contentColor = KddTextPrimary
                        ) {
                            tabs.forEachIndexed { index, title ->
                                Tab(
                                    selected = selectedTab == index,
                                    onClick = { selectedTab = index },
                                    text = {
                                        Text(
                                            text = title,
                                            fontWeight = if (selectedTab == index) FontWeight.Bold else FontWeight.Normal
                                        )
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
                                    if (!comunidad.descripcion.isNullOrBlank()) {
                                        Text(
                                            text = comunidad.descripcion,
                                            style = MaterialTheme.typography.bodyMedium,
                                            color = KddTextSecondary
                                        )
                                    }
                                    if (!comunidad.adminNombre.isNullOrBlank()) {
                                        CommunityInfoRow(
                                            icon = Icons.Filled.Person,
                                            label = "Admin",
                                            value = comunidad.adminNombre
                                        )
                                    }
                                    CommunityInfoRow(
                                        icon = Icons.Filled.Group,
                                        label = "Miembros",
                                        value = comunidad.numMiembros.toString()
                                    )
                                    if (comunidad.edadMin != null && comunidad.edadMax != null) {
                                        CommunityInfoRow(
                                            icon = Icons.Filled.People,
                                            label = "Edad",
                                            value = "${comunidad.edadMin} - ${comunidad.edadMax} años"
                                        )
                                    }
                                }
                            }
                        }
                        1 -> {
                            if (planesComunidad.isEmpty()) {
                                item {
                                    Box(
                                        modifier = Modifier.fillMaxWidth().padding(32.dp),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Text("Sin actividades todavía", style = MaterialTheme.typography.bodyMedium, color = KddTextSecondary)
                                    }
                                }
                            } else {
                                items(planesComunidad) { plan ->
                                    PlanCard(
                                        data = PlanCardData(
                                            id = plan.id,
                                            titulo = plan.titulo,
                                            categoria = plan.categoria ?: "",
                                            descripcion = plan.descripcion ?: "",
                                            dia = plan.fechaEvento ?: "Sin fecha",
                                            hora = plan.horaEvento ?: "",
                                            distanciaKm = "",
                                            ubicacion = plan.ubicacionTexto,
                                            anfitrionNombre = plan.anfitrionNombre ?: ""
                                        ),
                                        onClick = { onNavigateToPlan(plan.id) }
                                    )
                                }
                            }
                        }
                        2 -> {
                            if (miembros.isEmpty()) {
                                item {
                                    Box(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(32.dp),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        CircularProgressIndicator(color = KddPurple, modifier = Modifier.size(24.dp))
                                    }
                                }
                            } else {
                                items(miembros) { miembro ->
                                    MemberCard(
                                        miembro = miembro,
                                        esSoyYo = miembro.id == viewModel.miUserId,
                                        onAnadirAmigo = { id ->
                                            viewModel.enviarSolicitudAmistad(
                                                destinatarioId = id,
                                                onSuccess = {},
                                                onError = {}
                                            )
                                        }
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    if (mostrarConfirmAbandonar) {
        AlertDialog(
            onDismissRequest = { mostrarConfirmAbandonar = false },
            title = { Text("Abandonar comunidad") },
            text = { Text("¿Seguro que quieres abandonar esta comunidad?") },
            confirmButton = {
                TextButton(onClick = {
                    mostrarConfirmAbandonar = false
                    viewModel.abandonarComunidad(
                        id = communityId,
                        onSuccess = { viewModel.cargarDetalle(communityId) },
                        onError = { msg -> errorMsg = msg }
                    )
                }) { Text("Abandonar", color = Color(0xFFE53935)) }
            },
            dismissButton = {
                TextButton(onClick = { mostrarConfirmAbandonar = false }) { Text("Cancelar") }
            }
        )
    }
}

@Composable
private fun CommunityInfoRow(icon: ImageVector, label: String, value: String) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Box(
            modifier = Modifier
                .size(40.dp)
                .clip(CircleShape)
                .background(KddSurface),
            contentAlignment = Alignment.Center
        ) {
            Icon(icon, contentDescription = null, tint = KddTextSecondary, modifier = Modifier.size(20.dp))
        }
        Column {
            Text(text = label, style = MaterialTheme.typography.labelSmall, color = KddTextHint)
            Text(text = value, style = MaterialTheme.typography.bodyMedium, color = KddTextPrimary)
        }
    }
}

@Composable
private fun MemberCard(
    miembro: MiembroComunidadDto,
    esSoyYo: Boolean,
    onAnadirAmigo: (Long) -> Unit
) {
    var solicitudEnviada by remember { mutableStateOf(false) }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        if (!miembro.fotoPerfil.isNullOrBlank()) {
            AsyncImage(
                model = miembro.fotoPerfil,
                contentDescription = miembro.nombre,
                modifier = Modifier.size(48.dp).clip(CircleShape),
                contentScale = ContentScale.Crop
            )
        } else {
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .clip(CircleShape)
                    .background(KddSurfaceVariant),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = miembro.nombre.firstOrNull()?.uppercaseChar()?.toString() ?: "?",
                    fontWeight = FontWeight.Bold,
                    color = KddTextSecondary,
                    fontSize = 18.sp
                )
            }
        }
        Spacer(modifier = Modifier.width(12.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(miembro.nombre, style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.SemiBold, color = KddTextPrimary)
            if (miembro.edad != null) {
                Text("${miembro.edad} años", style = MaterialTheme.typography.bodySmall, color = KddTextHint)
            }
        }
        if (!esSoyYo) {
            var menuExpandido by remember { mutableStateOf(false) }
            Box {
                IconButton(
                    onClick = { menuExpandido = true },
                    modifier = Modifier.size(36.dp)
                ) {
                    Icon(
                        imageVector = Icons.Filled.MoreVert,
                        contentDescription = "Opciones",
                        tint = KddTextSecondary,
                        modifier = Modifier.size(20.dp)
                    )
                }
                DropdownMenu(
                    expanded = menuExpandido,
                    onDismissRequest = { menuExpandido = false }
                ) {
                    DropdownMenuItem(
                        text = {
                            Text(if (solicitudEnviada) "Solicitud enviada" else "Agregar amigo")
                        },
                        onClick = {
                            if (!solicitudEnviada) {
                                solicitudEnviada = true
                                onAnadirAmigo(miembro.id)
                            }
                            menuExpandido = false
                        },
                        leadingIcon = {
                            Icon(
                                if (solicitudEnviada) Icons.Filled.Check else Icons.Filled.PersonAdd,
                                contentDescription = null,
                                tint = if (solicitudEnviada) KddTextHint else KddPurple
                            )
                        },
                        enabled = !solicitudEnviada
                    )
                    DropdownMenuItem(
                        text = { Text("Bloquear") },
                        onClick = { menuExpandido = false },
                        leadingIcon = {
                            Icon(Icons.Filled.Block, contentDescription = null, tint = KddTextSecondary)
                        }
                    )
                }
            }
        }
    }
    HorizontalDivider(modifier = Modifier.padding(start = 76.dp), color = KddDivider)
}
