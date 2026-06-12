package com.kdd.kdd_frontend.ui.screens.communities

import androidx.compose.foundation.background
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
import com.kdd.kdd_frontend.ui.components.*
import com.kdd.kdd_frontend.ui.theme.*
import com.kdd.kdd_frontend.viewmodel.ComunidadDetalleState
import com.kdd.kdd_frontend.viewmodel.ComunidadViewModel

@Composable
fun CommunityDetailScreen(
    communityId: Long,
    onNavigateBack: () -> Unit,
    onNavigateToPlan: (Long) -> Unit
) {
    val viewModel: ComunidadViewModel = viewModel()
    val detalleState by viewModel.detalleState.collectAsState()
    val miembros by viewModel.miembros.collectAsState()

    var selectedTab by remember { mutableIntStateOf(0) }
    val tabs = listOf("Información", "Actividades", "Miembros")
    val snackbarHostState = remember { SnackbarHostState() }
    var errorMsg by remember { mutableStateOf<String?>(null) }

    LaunchedEffect(errorMsg) {
        errorMsg?.let { snackbarHostState.showSnackbar(it); errorMsg = null }
    }

    LaunchedEffect(communityId) {
        viewModel.cargarDetalle(communityId)
    }

    LaunchedEffect(selectedTab) {
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
                            comunidad.admin -> {
                                // Eres el admin — sin botón de unirse/abandonar
                                Surface(
                                    modifier = Modifier.weight(1f).height(48.dp),
                                    shape = RoundedCornerShape(24.dp),
                                    color = KddSurface
                                ) {
                                    Box(contentAlignment = Alignment.Center) {
                                        Text("Eres el admin", color = KddTextSecondary, fontWeight = FontWeight.SemiBold)
                                    }
                                }
                            }
                            comunidad.miembro -> {
                                Button(
                                    onClick = {
                                        viewModel.abandonarComunidad(
                                            id = communityId,
                                            onSuccess = { viewModel.cargarDetalle(communityId) },
                                            onError = { msg -> errorMsg = msg }
                                        )
                                    },
                                    modifier = Modifier.weight(1f).height(48.dp),
                                    shape = RoundedCornerShape(24.dp),
                                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFE53935))
                                ) {
                                    Text("Abandonar", color = Color.White, fontWeight = FontWeight.SemiBold)
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
                                    text = "Comunidad",
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
                            item {
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(32.dp),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text("Sin actividades todavía", style = MaterialTheme.typography.bodyMedium, color = KddTextSecondary)
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
            IconButton(
                onClick = {
                    if (!solicitudEnviada) {
                        solicitudEnviada = true
                        onAnadirAmigo(miembro.id)
                    }
                },
                modifier = Modifier.size(36.dp)
            ) {
                Icon(
                    imageVector = if (solicitudEnviada) Icons.Filled.Check else Icons.Filled.PersonAdd,
                    contentDescription = if (solicitudEnviada) "Solicitud enviada" else "Añadir amigo",
                    tint = if (solicitudEnviada) KddTextHint else KddPurple,
                    modifier = Modifier.size(20.dp)
                )
            }
        }
    }
    HorizontalDivider(modifier = Modifier.padding(start = 76.dp), color = KddDivider)
}
