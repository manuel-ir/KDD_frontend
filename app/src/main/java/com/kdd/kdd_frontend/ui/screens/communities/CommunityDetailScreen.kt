package com.kdd.kdd_frontend.ui.screens.communities

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
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
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.kdd.kdd_frontend.ui.components.*
import com.kdd.kdd_frontend.ui.theme.*

@Composable
fun CommunityDetailScreen(
    communityId: Long,
    onNavigateBack: () -> Unit,
    onNavigateToPlan: (Long) -> Unit
) {
    var selectedTab by remember { mutableIntStateOf(0) }
    val tabs = listOf("Información", "Actividades", "Miembros")

    // Datos de ejemplo
    val comunidad = CommunityCardData(
        id = communityId,
        nombre = "Escalada SEV",
        edadMin = 18,
        edadMax = 55,
        ubicacion = "Sevilla",
        numMiembros = 50,
        adminNombre = "Juan"
    )

    val actividades = listOf(
        PlanCardData(
            id = 1L,
            titulo = "Rock&Wall",
            categoria = "Escalada",
            descripcion = "Sesión de escalada en rocódromo",
            dia = "sábado",
            hora = "10:00",
            distanciaKm = "5km",
            anfitrionNombre = "Juan"
        )
    )

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
                        onClick = { /* TODO: compartir */ },
                        modifier = Modifier
                            .size(44.dp)
                            .clip(CircleShape)
                            .background(KddSurface)
                    ) {
                        Icon(Icons.Filled.Share, contentDescription = "Compartir", tint = KddTextPrimary)
                    }
                    Button(
                        onClick = { /* TODO: unirse */ },
                        modifier = Modifier
                            .weight(1f)
                            .height(48.dp),
                        shape = RoundedCornerShape(24.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = KddPurple)
                    ) {
                        Text("Unirse", color = Color.White, fontWeight = FontWeight.SemiBold)
                    }
                }
            }
            BottomNavBar(
                currentItem = BottomNavItem.COMMUNITIES,
                onHomeClick = { },
                onExploreClick = { },
                onCreateClick = { },
                onCommunitiesClick = { },
                onCalendarClick = { }
            )
        }
    ) { paddingValues ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            item {
                // Foto de la comunidad con overlay
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

                    // X para cerrar
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

                    // Nombre y tipo
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

                // Tabs
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
                    // Información
                    item {
                        Column(
                            modifier = Modifier.padding(16.dp),
                            verticalArrangement = Arrangement.spacedBy(16.dp)
                        ) {
                            Text(
                                text = "Toda persona es bienvenida independientemente de su nivel.\nEstamos aquí para organizar eventos en rocódromos o vías ferrata",
                                style = MaterialTheme.typography.bodyMedium,
                                color = KddTextSecondary
                            )

                            CommunityInfoRow(
                                icon = Icons.Filled.Person,
                                label = "Admin",
                                value = comunidad.adminNombre
                            )
                            CommunityInfoRow(
                                icon = Icons.Filled.Group,
                                label = "Miembros",
                                value = comunidad.numMiembros.toString()
                            )
                            CommunityInfoRow(
                                icon = Icons.Filled.LocationOn,
                                label = "Lugar",
                                value = comunidad.ubicacion
                            )
                            CommunityInfoRow(
                                icon = Icons.Filled.CalendarMonth,
                                label = "Próxima actividad",
                                value = "Rock&Wall"
                            )
                        }
                    }
                }
                1 -> {
                    // Actividades
                    items(actividades) { plan ->
                        PlanCard(data = plan, onClick = { onNavigateToPlan(plan.id) })
                    }
                }
                2 -> {
                    // Miembros
                    items(List(50) { index ->
                        Pair("Miembro ${index + 1}", (3.5f + (index % 5) * 0.3f).coerceAtMost(5f))
                    }) { (nombre, puntuacion) ->
                        MemberRow(nombre = nombre, puntuacion = puntuacion)
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
private fun MemberRow(nombre: String, puntuacion: Float) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(40.dp)
                .clip(CircleShape)
                .background(KddSurface),
            contentAlignment = Alignment.Center
        ) {
            Text(nombre.first().toString(), fontWeight = FontWeight.Bold, color = KddTextSecondary)
        }
        Spacer(modifier = Modifier.width(12.dp))
        Text(
            text = nombre,
            style = MaterialTheme.typography.bodyMedium,
            color = KddTextPrimary,
            modifier = Modifier.weight(1f)
        )
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(Icons.Filled.Star, contentDescription = null, tint = KddYellow, modifier = Modifier.size(16.dp))
            Spacer(modifier = Modifier.width(4.dp))
            Text(
                text = String.format("%.1f", puntuacion),
                style = MaterialTheme.typography.bodySmall,
                color = KddTextSecondary
            )
        }
    }
}
