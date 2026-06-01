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
import com.kdd.kdd_frontend.ui.theme.*

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
    var selectedTab by remember { mutableIntStateOf(0) }
    val presenteCount = 9
    val tabs = listOf("Información", "Presente ($presenteCount)")

    var participanteSeleccionado by remember { mutableStateOf<ParticipanteInfo?>(null) }

    val participantes = remember {
        (1..9).map { i ->
            ParticipanteInfo(
                id = i.toLong(),
                nombre = "Usuario $i",
                edad = 20 + i,
                descripcion = if (i % 2 == 0) "Me encanta conocer gente y hacer planes por la ciudad." else "",
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
                        onClick = { /* TODO */ },
                        modifier = Modifier.size(44.dp).clip(CircleShape).background(KddSurface)
                    ) {
                        Icon(Icons.Filled.StarBorder, contentDescription = "Favorito", tint = KddTextPrimary)
                    }
                    IconButton(
                        onClick = { /* TODO */ },
                        modifier = Modifier.size(44.dp).clip(CircleShape).background(KddSurface)
                    ) {
                        Icon(Icons.Filled.Share, contentDescription = "Compartir", tint = KddTextPrimary)
                    }
                    Button(
                        onClick = { /* TODO */ },
                        modifier = Modifier.weight(1f).height(48.dp),
                        shape = RoundedCornerShape(24.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = KddPurple)
                    ) {
                        Text("Unirse", color = Color.White, fontWeight = FontWeight.SemiBold)
                    }
                    OutlinedButton(
                        onClick = { /* TODO */ },
                        modifier = Modifier.height(48.dp),
                        shape = RoundedCornerShape(24.dp),
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = KddPurple)
                    ) {
                        Text("+1", fontWeight = FontWeight.SemiBold)
                    }
                }
            }
        }
    ) { paddingValues ->
        LazyColumn(
            modifier = Modifier.fillMaxSize().padding(paddingValues)
        ) {
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
                    IconButton(
                        onClick = { /* TODO: reportar */ },
                        modifier = Modifier
                            .align(Alignment.TopEnd).padding(8.dp).statusBarsPadding()
                            .size(36.dp).clip(CircleShape).background(Color.White.copy(alpha = 0.8f))
                    ) {
                        Icon(Icons.Filled.MoreVert, contentDescription = "Opciones", tint = KddTextPrimary)
                    }
                    Column(modifier = Modifier.align(Alignment.BottomStart).padding(16.dp)) {
                        Text("Grupo Whutpps", style = MaterialTheme.typography.headlineMedium, color = Color.White, fontWeight = FontWeight.Bold)
                        Text("Actividad", style = MaterialTheme.typography.bodyMedium, color = Color.White.copy(alpha = 0.85f))
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
                            Surface(shape = RoundedCornerShape(8.dp), color = KddSuccess.copy(alpha = 0.15f)) {
                                Text(
                                    text = "Ya hay 27 amigos interesados",
                                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                                    style = MaterialTheme.typography.bodySmall,
                                    color = KddSuccess
                                )
                            }
                            Surface(shape = RoundedCornerShape(8.dp), color = KddSurface) {
                                Text(
                                    text = "Edad 20-30",
                                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                                    style = MaterialTheme.typography.bodySmall,
                                    color = KddTextSecondary
                                )
                            }
                            Text(
                                text = "Grupo para ampliar el círculo de amistades por Sevilla, hacer planes tipo tomar algo, senderismo, juegos de mesa… de todo un poco.",
                                style = MaterialTheme.typography.bodyMedium,
                                color = KddTextSecondary
                            )
                            Text("Sobre esta actividad", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = KddTextPrimary)
                            PlanInfoRow(icon = Icons.Filled.Person, label = "Anfitrión", value = "Jesús  25")
                            PlanInfoRow(icon = Icons.Filled.Group, label = "Presente", value = "Ver los $presenteCount participantes", isClickable = true, onClick = { selectedTab = 1 })
                            PlanInfoRow(icon = Icons.Filled.CalendarMonth, label = "Ahora", value = "16:15 - 16:15")
                            PlanInfoRow(icon = Icons.Filled.LocationOn, label = "Av. Eduardo Dato, SE", value = "25km")
                            PlanInfoRow(icon = Icons.Filled.Translate, label = "Idiomas hablados", value = "Todos los idiomas")
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

    // Pantalla de perfil de participante
    participanteSeleccionado?.let { p ->
        PerfilUsuarioDialog(
            participante = p,
            onDismiss = { participanteSeleccionado = null }
        )
    }
}

@Composable
private fun PerfilUsuarioDialog(
    participante: ParticipanteInfo,
    onDismiss: () -> Unit
) {
    var tabSeleccionado by remember { mutableIntStateOf(0) }
    val planTerminado = false // TODO: plan.fechaEvento.isBefore(LocalDate.now())
    var estrellas by remember { mutableIntStateOf(0) }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.White)
        ) {
            Column(modifier = Modifier.fillMaxSize()) {

                // ── Cabecera ──────────────────────────────────────────────
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .statusBarsPadding()
                        .padding(horizontal = 8.dp, vertical = 8.dp)
                ) {
                    IconButton(
                        onClick = onDismiss,
                        modifier = Modifier
                            .align(Alignment.TopStart)
                            .size(40.dp)
                            .clip(CircleShape)
                            .background(KddSurface)
                    ) {
                        Icon(Icons.Filled.Close, contentDescription = "Cerrar", tint = KddTextPrimary)
                    }
                    IconButton(
                        onClick = { /* TODO: reportar */ },
                        modifier = Modifier
                            .align(Alignment.TopEnd)
                            .size(40.dp)
                            .clip(CircleShape)
                            .background(KddSurface)
                    ) {
                        Icon(Icons.Filled.MoreVert, contentDescription = "Opciones", tint = KddTextPrimary)
                    }
                }

                // ── Foto + nombre + valoración ────────────────────────────
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 20.dp, vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    // Foto de perfil
                    Box(
                        modifier = Modifier
                            .size(90.dp)
                            .clip(CircleShape)
                            .background(KddPurple),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = participante.nombre.first().toString(),
                            color = Color.White,
                            fontWeight = FontWeight.Bold,
                            fontSize = 36.sp
                        )
                    }

                    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                        // Nombre y edad
                        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                            Text(
                                text = participante.nombre,
                                style = MaterialTheme.typography.headlineSmall,
                                fontWeight = FontWeight.Bold,
                                color = KddTextPrimary
                            )
                            Text(
                                text = "${participante.edad}",
                                style = MaterialTheme.typography.headlineSmall,
                                color = KddTextSecondary
                            )
                        }

                        // Valoración media
                        if (participante.numValoraciones > 0) {
                            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                                Icon(Icons.Filled.Star, contentDescription = null, tint = KddYellow, modifier = Modifier.size(18.dp))
                                Text(
                                    text = "%.1f".format(participante.valoracionMedia),
                                    style = MaterialTheme.typography.bodyMedium,
                                    fontWeight = FontWeight.SemiBold,
                                    color = KddTextPrimary
                                )
                                Text(
                                    text = "(${participante.numValoraciones})",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = KddTextHint
                                )
                            }
                        } else {
                            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                                Icon(Icons.Filled.StarBorder, contentDescription = null, tint = KddTextHint, modifier = Modifier.size(18.dp))
                                Text("Sin valoraciones", style = MaterialTheme.typography.bodySmall, color = KddTextHint)
                            }
                        }
                    }
                }

                // ── Tabs ──────────────────────────────────────────────────
                TabRow(
                    selectedTabIndex = tabSeleccionado,
                    containerColor = Color.White,
                    contentColor = KddPurple
                ) {
                    Tab(
                        selected = tabSeleccionado == 0,
                        onClick = { tabSeleccionado = 0 },
                        text = { Text("Información", fontWeight = if (tabSeleccionado == 0) FontWeight.Bold else FontWeight.Normal) }
                    )
                    Tab(
                        selected = tabSeleccionado == 1,
                        onClick = { tabSeleccionado = 1 },
                        text = { Text("Planes", fontWeight = if (tabSeleccionado == 1) FontWeight.Bold else FontWeight.Normal) }
                    )
                }

                // ── Contenido del tab ─────────────────────────────────────
                LazyColumn(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth()
                        .padding(horizontal = 20.dp, vertical = 16.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    when (tabSeleccionado) {
                        0 -> {
                            item {
                                if (participante.descripcion.isNotBlank()) {
                                    Text(
                                        text = participante.descripcion,
                                        style = MaterialTheme.typography.bodyMedium,
                                        color = KddTextSecondary
                                    )
                                } else {
                                    Text(
                                        text = "Este usuario no ha añadido una descripción.",
                                        style = MaterialTheme.typography.bodyMedium,
                                        color = KddTextHint
                                    )
                                }
                            }

                            // Valorar (solo si el plan ya terminó)
                            if (planTerminado) {
                                item {
                                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                                        HorizontalDivider(color = KddDivider)
                                        Text(
                                            "Valora a ${participante.nombre}",
                                            style = MaterialTheme.typography.titleSmall,
                                            fontWeight = FontWeight.SemiBold,
                                            color = KddTextPrimary
                                        )
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
                        }
                        1 -> {
                            item {
                                Text(
                                    text = "Los planes compartidos se mostrarán aquí.",
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = KddTextHint
                                )
                            }
                        }
                    }
                }

                // ── Botón inferior ────────────────────────────────────────
                Surface(shadowElevation = 4.dp) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .navigationBarsPadding()
                            .padding(horizontal = 20.dp, vertical = 12.dp)
                    ) {
                        if (planTerminado && estrellas > 0) {
                            Button(
                                onClick = { /* TODO: API valoración */ },
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
                                onClick = { /* TODO: eliminar amigo */ },
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
                                onClick = { /* TODO: solicitud amistad */ },
                                modifier = Modifier.fillMaxWidth().height(52.dp),
                                shape = RoundedCornerShape(26.dp),
                                colors = ButtonDefaults.buttonColors(containerColor = Color.Transparent, contentColor = KddPurple),
                                elevation = ButtonDefaults.buttonElevation(0.dp)
                            ) {
                                Icon(Icons.Filled.PersonAdd, contentDescription = null, tint = KddPurple, modifier = Modifier.size(20.dp))
                                Spacer(modifier = Modifier.width(8.dp))
                                Text("Amigo", fontWeight = FontWeight.SemiBold, color = KddPurple)
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
    val clickModifier = if (isClickable && onClick != null)
        Modifier.fillMaxWidth().clickable { onClick() }
    else
        Modifier.fillMaxWidth()

    Row(verticalAlignment = Alignment.CenterVertically, modifier = clickModifier) {
        Box(
            modifier = Modifier.size(44.dp).clip(CircleShape).background(KddSurface),
            contentAlignment = Alignment.Center
        ) {
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
    Column(
        modifier = Modifier.padding(6.dp).clickable { onClick() },
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
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
