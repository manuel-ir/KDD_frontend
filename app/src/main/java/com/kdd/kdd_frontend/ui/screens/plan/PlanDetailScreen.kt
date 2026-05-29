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
import com.kdd.kdd_frontend.ui.theme.*

@Composable
fun PlanDetailScreen(
    planId: Long,
    onNavigateBack: () -> Unit
) {
    var selectedTab by remember { mutableIntStateOf(0) }
    val presenteCount = 9 // TODO: obtener del backend
    val tabs = listOf("Información", "Presente ($presenteCount)")

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
                    // Favoritos
                    IconButton(
                        onClick = { /* TODO */ },
                        modifier = Modifier
                            .size(44.dp)
                            .clip(CircleShape)
                            .background(KddSurface)
                    ) {
                        Icon(Icons.Filled.StarBorder, contentDescription = "Favorito", tint = KddTextPrimary)
                    }
                    // Compartir
                    IconButton(
                        onClick = { /* TODO */ },
                        modifier = Modifier
                            .size(44.dp)
                            .clip(CircleShape)
                            .background(KddSurface)
                    ) {
                        Icon(Icons.Filled.Share, contentDescription = "Compartir", tint = KddTextPrimary)
                    }
                    // Unirse
                    Button(
                        onClick = { /* TODO */ },
                        modifier = Modifier
                            .weight(1f)
                            .height(48.dp),
                        shape = RoundedCornerShape(24.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = KddPurple)
                    ) {
                        Text("Unirse", color = Color.White, fontWeight = FontWeight.SemiBold)
                    }
                    // +1
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
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            item {
                // Foto del plan
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

                    // X arriba-izquierda
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

                    // 3 puntos arriba-derecha
                    IconButton(
                        onClick = { /* TODO: reportar */ },
                        modifier = Modifier
                            .align(Alignment.TopEnd)
                            .padding(8.dp)
                            .statusBarsPadding()
                            .size(36.dp)
                            .clip(CircleShape)
                            .background(Color.White.copy(alpha = 0.8f))
                    ) {
                        Icon(Icons.Filled.MoreVert, contentDescription = "Opciones", tint = KddTextPrimary)
                    }

                    // Nombre del plan
                    Column(
                        modifier = Modifier
                            .align(Alignment.BottomStart)
                            .padding(16.dp)
                    ) {
                        Text(
                            text = "Grupo Whutpps",
                            style = MaterialTheme.typography.headlineMedium,
                            color = Color.White,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "Actividad",
                            style = MaterialTheme.typography.bodyMedium,
                            color = Color.White.copy(alpha = 0.85f)
                        )
                    }
                }

                // Tabs
                TabRow(
                    selectedTabIndex = selectedTab,
                    containerColor = Color.White,
                    contentColor = KddPurple
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
                            // Badge de participantes
                            Surface(
                                shape = RoundedCornerShape(8.dp),
                                color = KddSuccess.copy(alpha = 0.15f)
                            ) {
                                Text(
                                    text = "Ya hay 27 amigos interesados",
                                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                                    style = MaterialTheme.typography.bodySmall,
                                    color = KddSuccess
                                )
                            }

                            // Rango de edad
                            Surface(
                                shape = RoundedCornerShape(8.dp),
                                color = KddSurface
                            ) {
                                Text(
                                    text = "Edad 20-30",
                                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                                    style = MaterialTheme.typography.bodySmall,
                                    color = KddTextSecondary
                                )
                            }

                            // Descripción
                            Text(
                                text = "Grupo para ampliar el círculo de amistades por Sevilla, hacer planes tipo tomar algo, senderismo, juegos de mesa… de todo un poco.",
                                style = MaterialTheme.typography.bodyMedium,
                                color = KddTextSecondary
                            )

                            Text(
                                text = "Sobre esta actividad",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = KddTextPrimary
                            )

                            PlanInfoRow(icon = Icons.Filled.Person, label = "Anfitrión", value = "Jesús  25")
                            PlanInfoRow(icon = Icons.Filled.Group, label = "Presente", value = "Ver los $presenteCount participantes", isClickable = true, onClick = { selectedTab = 1 })
                            PlanInfoRow(icon = Icons.Filled.CalendarMonth, label = "Ahora", value = "16:15 - 16:15")
                            PlanInfoRow(icon = Icons.Filled.LocationOn, label = "Av. Eduardo Dato, SE", value = "25km")
                            PlanInfoRow(icon = Icons.Filled.Translate, label = "Idiomas hablados", value = "Todos los idiomas")

                            // Mapa placeholder
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(160.dp)
                                    .clip(RoundedCornerShape(12.dp))
                                    .background(Color(0xFFE8EAF0)),
                                contentAlignment = Alignment.Center
                            ) {
                                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                    Icon(
                                        Icons.Filled.Map,
                                        contentDescription = null,
                                        tint = KddPurple.copy(alpha = 0.4f),
                                        modifier = Modifier.size(40.dp)
                                    )
                                    Text(
                                        text = "Google Maps",
                                        color = KddTextHint,
                                        style = MaterialTheme.typography.bodySmall
                                    )
                                }
                            }
                        }
                    }
                }
                1 -> {
                    // Presente - Grid de participantes
                    item {
                        LazyVerticalGrid(
                            columns = GridCells.Fixed(3),
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(400.dp)
                                .padding(8.dp),
                            userScrollEnabled = false
                        ) {
                            items(9) { index ->
                                PresenteCard(nombre = "Usuario ${index + 1}", edad = 20 + index)
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

    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = clickModifier
    ) {
        Box(
            modifier = Modifier
                .size(44.dp)
                .clip(CircleShape)
                .background(KddSurface),
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
private fun PresenteCard(nombre: String, edad: Int) {
    Column(
        modifier = Modifier.padding(6.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .aspectRatio(1f)
                .clip(RoundedCornerShape(8.dp))
                .background(KddSurfaceVariant),
            contentAlignment = Alignment.Center
        ) {
            Text(nombre.first().toString(), fontWeight = FontWeight.Bold, fontSize = 24.sp, color = KddTextSecondary)
        }
        Spacer(modifier = Modifier.height(4.dp))
        Text(text = nombre, style = MaterialTheme.typography.labelSmall, color = KddTextPrimary)
        Text(text = "$edad", style = MaterialTheme.typography.labelSmall, color = KddTextHint)
    }
}
