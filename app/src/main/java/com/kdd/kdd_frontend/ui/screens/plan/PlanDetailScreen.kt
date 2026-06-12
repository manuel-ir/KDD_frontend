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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.lifecycle.viewmodel.compose.viewModel
import com.kdd.kdd_frontend.ui.theme.*
import com.kdd.kdd_frontend.viewmodel.PlanDetalleState
import com.kdd.kdd_frontend.viewmodel.PlanViewModel

data class ParticipanteInfo(
    val id: Long,
    val nombre: String,
    val edad: Int,
    val descripcion: String = "",
    val fotoPerfil: String? = null,
    val esAmigo: Boolean = false,
    val valoracionMedia: Float = 0f,
    val numValoraciones: Int = 0
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PlanDetailScreen(
    planId: Long,
    onNavigateBack: () -> Unit
) {
    val viewModel: PlanViewModel = viewModel()
    val detalleState by viewModel.detalleState.collectAsState()
    val participando by viewModel.participando.collectAsState()

    var selectedTab by remember { mutableIntStateOf(0) }
    var participanteSeleccionado by remember { mutableStateOf<ParticipanteInfo?>(null) }

    LaunchedEffect(planId) {
        viewModel.cargarDetalle(planId)
    }

    when (val estado = detalleState) {
        is PlanDetalleState.Loading -> {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator(color = KddPurple)
            }
        }
        is PlanDetalleState.Error -> {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Text(estado.mensaje, color = KddTextSecondary)
            }
        }
        is PlanDetalleState.Success -> {
            val plan = estado.plan
            val numParticipantes = plan.numParticipantes
            val tabs = listOf("Información", "Presente ($numParticipantes)")

            val participantes = remember(numParticipantes) {
                (1..maxOf(numParticipantes, 1)).map { i ->
                    ParticipanteInfo(
                        id = i.toLong(),
                        nombre = "Participante $i",
                        edad = 20 + (i % 15),
                        esAmigo = i % 3 == 0,
                        valoracionMedia = (3..5).random().toFloat(),
                        numValoraciones = (0..15).random()
                    )
                }
            }

            Scaffold(
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
                            val esCreador = (detalleState as? PlanDetalleState.Success)?.plan?.creador == true
                            when {
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
                                    Button(
                                        onClick = {
                                            viewModel.abandonarPlan(
                                                planId = planId,
                                                onSuccess = { viewModel.cargarDetalle(planId) },
                                                onError = { }
                                            )
                                        },
                                        modifier = Modifier.weight(1f).height(48.dp),
                                        shape = RoundedCornerShape(24.dp),
                                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFE53935))
                                    ) {
                                        Text("Abandonar plan", color = Color.White, fontWeight = FontWeight.SemiBold)
                                    }
                                }
                                else -> {
                                    Button(
                                        onClick = {
                                            viewModel.unirseAPlan(
                                                planId = planId,
                                                onSuccess = { viewModel.cargarDetalle(planId) },
                                                onError = { }
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
                            Box(modifier = Modifier.fillMaxSize().background(KddSurfaceVariant))
                            IconButton(
                                onClick = onNavigateBack,
                                modifier = Modifier
                                    .align(Alignment.TopStart).padding(8.dp).statusBarsPadding()
                                    .size(36.dp).clip(CircleShape).background(Color.White.copy(alpha = 0.8f))
                            ) {
                                Icon(Icons.Filled.Close, contentDescription = "Cerrar", tint = KddTextPrimary)
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
                                        label = "Presente",
                                        value = "Ver los $numParticipantes participantes",
                                        isClickable = true,
                                        onClick = { selectedTab = 1 }
                                    )
                                    if (!plan.fechaEvento.isNullOrBlank()) {
                                        val fechaHora = "${plan.fechaEvento}${if (!plan.horaEvento.isNullOrBlank()) " · ${plan.horaEvento.take(5)}" else ""}"
                                        PlanInfoRow(icon = Icons.Filled.CalendarMonth, label = "Fecha", value = fechaHora)
                                    }
                                    if (!plan.ubicacionTexto.isNullOrBlank()) {
                                        PlanInfoRow(icon = Icons.Filled.LocationOn, label = "Ubicación", value = plan.ubicacionTexto)
                                    }
                                    if (!plan.idioma.isNullOrBlank()) {
                                        PlanInfoRow(icon = Icons.Filled.Translate, label = "Idioma", value = plan.idioma)
                                    }
                                    Box(
                                        modifier = Modifier.fillMaxWidth().height(160.dp).clip(RoundedCornerShape(12.dp)).background(Color(0xFFE8EAF0)),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                            Icon(Icons.Filled.Map, contentDescription = null, tint = KddPurple.copy(alpha = 0.4f), modifier = Modifier.size(40.dp))
                                            Text("Google Maps", color = KddTextHint, style = MaterialTheme.typography.bodySmall)
                                        }
                                    }
                                }
                            }
                        }
                        1 -> {
                            item {
                                LazyVerticalGrid(
                                    columns = GridCells.Fixed(3),
                                    modifier = Modifier.fillMaxWidth().height(400.dp).padding(8.dp),
                                    userScrollEnabled = false
                                ) {
                                    items(participantes.size) { index ->
                                        val p = participantes[index]
                                        PresenteCard(
                                            nombre = p.nombre,
                                            edad = p.edad,
                                            onClick = { participanteSeleccionado = p }
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }

            participanteSeleccionado?.let { p ->
                PerfilUsuarioDialog(
                    participante = p,
                    planId = planId,
                    onDismiss = { participanteSeleccionado = null }
                )
            }
        }
    }
}

@Composable
private fun PerfilUsuarioDialog(
    participante: ParticipanteInfo,
    planId: Long,
    onDismiss: () -> Unit
) {
    var tabSeleccionado by remember { mutableIntStateOf(0) }
    var estrellas by remember { mutableIntStateOf(0) }

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
                        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                            Text(participante.nombre, style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold, color = KddTextPrimary)
                            Text("${participante.edad}", style = MaterialTheme.typography.headlineSmall, color = KddTextSecondary)
                        }
                        if (participante.numValoraciones > 0) {
                            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                                Icon(Icons.Filled.Star, contentDescription = null, tint = KddYellow, modifier = Modifier.size(18.dp))
                                Text("%.1f".format(participante.valoracionMedia), style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.SemiBold, color = KddTextPrimary)
                                Text("(${participante.numValoraciones})", style = MaterialTheme.typography.bodySmall, color = KddTextHint)
                            }
                        } else {
                            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                                Icon(Icons.Filled.StarBorder, contentDescription = null, tint = KddTextHint, modifier = Modifier.size(18.dp))
                                Text("Sin valoraciones", style = MaterialTheme.typography.bodySmall, color = KddTextHint)
                            }
                        }
                    }
                }

                TabRow(selectedTabIndex = tabSeleccionado, containerColor = Color.White, contentColor = KddPurple) {
                    Tab(selected = tabSeleccionado == 0, onClick = { tabSeleccionado = 0 }, text = { Text("Información", fontWeight = if (tabSeleccionado == 0) FontWeight.Bold else FontWeight.Normal) })
                    Tab(selected = tabSeleccionado == 1, onClick = { tabSeleccionado = 1 }, text = { Text("Planes", fontWeight = if (tabSeleccionado == 1) FontWeight.Bold else FontWeight.Normal) })
                }

                LazyColumn(
                    modifier = Modifier.weight(1f).fillMaxWidth().padding(horizontal = 20.dp, vertical = 16.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    when (tabSeleccionado) {
                        0 -> {
                            item {
                                Text(
                                    text = if (participante.descripcion.isNotBlank()) participante.descripcion else "Este usuario no ha añadido una descripción.",
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = if (participante.descripcion.isNotBlank()) KddTextSecondary else KddTextHint
                                )
                            }
                            item {
                                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                                    HorizontalDivider(color = KddDivider)
                                    Text("Valora a ${participante.nombre}", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.SemiBold, color = KddTextPrimary)
                                    Row(horizontalArrangement = Arrangement.Center, modifier = Modifier.fillMaxWidth()) {
                                        (1..5).forEach { i ->
                                            IconButton(onClick = { estrellas = i }) {
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
                            }
                        }
                        1 -> {
                            item {
                                Text("Los planes compartidos se mostrarán aquí.", style = MaterialTheme.typography.bodyMedium, color = KddTextHint)
                            }
                        }
                    }
                }

                Surface(shadowElevation = 4.dp) {
                    Box(modifier = Modifier.fillMaxWidth().navigationBarsPadding().padding(horizontal = 20.dp, vertical = 12.dp)) {
                        if (estrellas > 0) {
                            val planViewModel: PlanViewModel = viewModel()
                            Button(
                                onClick = {
                                    // POST /api/valoraciones — próximo paso
                                },
                                modifier = Modifier.fillMaxWidth().height(52.dp),
                                shape = RoundedCornerShape(26.dp),
                                colors = ButtonDefaults.buttonColors(containerColor = KddPurple)
                            ) {
                                Icon(Icons.Filled.Star, contentDescription = null, tint = Color.White, modifier = Modifier.size(18.dp))
                                Spacer(modifier = Modifier.width(8.dp))
                                Text("Enviar valoración", color = Color.White, fontWeight = FontWeight.SemiBold)
                            }
                        } else if (participante.esAmigo) {
                            OutlinedButton(
                                onClick = { },
                                modifier = Modifier.fillMaxWidth().height(52.dp),
                                shape = RoundedCornerShape(26.dp),
                                colors = ButtonDefaults.outlinedButtonColors(contentColor = KddPurple)
                            ) {
                                Icon(Icons.Filled.PersonRemove, contentDescription = null, modifier = Modifier.size(18.dp))
                                Spacer(modifier = Modifier.width(8.dp))
                                Text("Eliminar amigo", fontWeight = FontWeight.SemiBold)
                            }
                        } else {
                            Button(
                                onClick = { },
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
private fun PresenteCard(nombre: String, edad: Int, onClick: () -> Unit) {
    Column(modifier = Modifier.padding(6.dp).clickable { onClick() }, horizontalAlignment = Alignment.CenterHorizontally) {
        Box(
            modifier = Modifier.fillMaxWidth().aspectRatio(1f).clip(RoundedCornerShape(8.dp)).background(KddSurfaceVariant),
            contentAlignment = Alignment.Center
        ) {
            Text(nombre.first().toString(), fontWeight = FontWeight.Bold, fontSize = 24.sp, color = KddTextSecondary)
        }
        Spacer(modifier = Modifier.height(4.dp))
        Text(text = nombre, style = MaterialTheme.typography.labelSmall, color = KddTextPrimary)
        Text(text = "$edad", style = MaterialTheme.typography.labelSmall, color = KddTextHint)
    }
}
