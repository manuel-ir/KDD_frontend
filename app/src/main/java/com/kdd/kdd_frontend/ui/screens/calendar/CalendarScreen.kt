package com.kdd.kdd_frontend.ui.screens.calendar

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.kdd.kdd_frontend.ui.components.*
import com.kdd.kdd_frontend.ui.theme.*
import com.kdd.kdd_frontend.viewmodel.PlanViewModel
import com.kdd.kdd_frontend.viewmodel.PlanesState
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime
import java.time.format.DateTimeFormatter

/**
 * Pantalla de calendario del usuario.
 *
 * Muestra los planes del usuario divididos en tres pestanas:
 * - Proximos: planes vigentes en los que participa (incluye los propios).
 * - Historial: planes ya caducados en los que participo, con opcion de eliminarlos.
 * - Tu: planes creados por el propio usuario que aun no han caducado.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CalendarScreen(
    onNavigateToMain: () -> Unit,
    onNavigateToExplore: () -> Unit,
    onNavigateToCommunities: () -> Unit,
    onNavigateToCreatePlan: () -> Unit,
    onNavigateToCreateCommunity: () -> Unit,
    onNavigateToPlan: (Long) -> Unit
) {
    var selectedTab by rememberSaveable { mutableIntStateOf(0) }
    val tabs = listOf("Próximos", "Historial", "Tú")
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    var showBottomSheet by remember { mutableStateOf(false) }

    val viewModel: PlanViewModel = viewModel()
    val misPlanes by viewModel.misPlanes.collectAsState()
    val historial by viewModel.historial.collectAsState()
    val misPlanesCreados by viewModel.misPlanesCreados.collectAsState()

    LaunchedEffect(Unit) {
        viewModel.cargarMisPlanes()
        viewModel.cargarHistorial()
        viewModel.cargarMisPlanesCreados()
    }

    val formatter = remember { DateTimeFormatter.ofPattern("yyyy-MM-dd") }
    val timeFormatter = remember { DateTimeFormatter.ofPattern("HH:mm") }

    /**
     * Replica exacta de la logica del backend (planHaCaducado):
     * - Sin fecha: nunca caduca.
     * - Con horaHasta: caduca en fechaEvento + horaHasta.
     * - Sin horaHasta: caduca 24 horas despues de fechaEvento + horaEvento
     *   (si no hay horaEvento se usa medianoche como hora de inicio).
     */
    fun planHaTerminado(plan: com.kdd.kdd_frontend.ui.components.PlanCardData): Boolean {
        val fecha = runCatching { LocalDate.parse(plan.dia, formatter) }.getOrNull()
            ?: return false
        val ahora = LocalDateTime.now()
        val horaFin = plan.horaHasta?.let { runCatching { LocalTime.parse(it.take(5), timeFormatter) }.getOrNull() }
        val horaIni = plan.hora.takeIf { it.isNotBlank() }
            ?.let { runCatching { LocalTime.parse(it.take(5), timeFormatter) }.getOrNull() }

        return if (horaFin != null) {
            ahora.isAfter(LocalDateTime.of(fecha, horaFin))
        } else {
            val inicio = LocalDateTime.of(fecha, horaIni ?: LocalTime.MIDNIGHT)
            ahora.isAfter(inicio.plusHours(24))
        }
    }

    val planesProximos = remember(misPlanes) {
        (misPlanes as? PlanesState.Success)?.planes
            ?.filter { !planHaTerminado(it) }
            ?.sortedBy { it.dia } ?: emptyList()
    }

    val planesHistorial = remember(historial) {
        (historial as? PlanesState.Success)?.planes
            ?.sortedByDescending { it.dia } ?: emptyList()
    }

    val planesTu = remember(misPlanesCreados) {
        (misPlanesCreados as? PlanesState.Success)?.planes
            ?.filter { !planHaTerminado(it) }
            ?.sortedBy { it.dia } ?: emptyList()
    }

    Scaffold(
        bottomBar = {
            BottomNavBar(
                currentItem = BottomNavItem.CALENDAR,
                onHomeClick = onNavigateToMain,
                onExploreClick = onNavigateToExplore,
                onCreateClick = { showBottomSheet = true },
                onCommunitiesClick = onNavigateToCommunities,
                onCalendarClick = { }
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            Text(
                text = "Tus actividades",
                style = MaterialTheme.typography.headlineLarge,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(start = 20.dp, top = 20.dp, bottom = 8.dp)
            )

            HorizontalDivider()

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

            when (selectedTab) {
                // Pestaña Proximos
                0 -> when (misPlanes) {
                    is PlanesState.Loading -> Box(Modifier.fillMaxSize(), Alignment.Center) {
                        CircularProgressIndicator(color = KddPurple)
                    }
                    is PlanesState.Error -> Box(Modifier.fillMaxSize(), Alignment.Center) {
                        Text((misPlanes as PlanesState.Error).mensaje, color = KddTextSecondary)
                    }
                    is PlanesState.Success -> {
                        if (planesProximos.isEmpty()) {
                            Box(Modifier.weight(1f).fillMaxWidth(), Alignment.Center) {
                                Column(
                                    horizontalAlignment = Alignment.CenterHorizontally,
                                    verticalArrangement = Arrangement.spacedBy(12.dp)
                                ) {
                                    Text(
                                        text = "No tienes planes próximos.\nCrea tu actividad",
                                        style = MaterialTheme.typography.bodyMedium,
                                        color = KddTextHint,
                                        textAlign = TextAlign.Center
                                    )
                                    Button(
                                        onClick = onNavigateToCreatePlan,
                                        colors = ButtonDefaults.buttonColors(containerColor = KddPurple)
                                    ) {
                                        Text("Crear actividad", color = Color.White)
                                    }
                                }
                            }
                        } else {
                            LazyColumn {
                                items(planesProximos) { plan ->
                                    PlanCard(data = plan, onClick = { onNavigateToPlan(plan.id) })
                                }
                            }
                        }
                    }
                }

                // Pestaña Historial
                1 -> when (historial) {
                    is PlanesState.Loading -> Box(Modifier.fillMaxSize(), Alignment.Center) {
                        CircularProgressIndicator(color = KddPurple)
                    }
                    is PlanesState.Error -> Box(Modifier.fillMaxSize(), Alignment.Center) {
                        Text((historial as PlanesState.Error).mensaje, color = KddTextSecondary)
                    }
                    is PlanesState.Success -> {
                        if (planesHistorial.isEmpty()) {
                            Box(Modifier.weight(1f).fillMaxWidth(), Alignment.Center) {
                                Text(
                                    text = "No tienes actividades pasadas todavía",
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = KddTextHint,
                                    textAlign = TextAlign.Center
                                )
                            }
                        } else {
                            LazyColumn {
                                items(planesHistorial) { plan ->
                                    PlanCard(
                                        data = plan,
                                        onClick = { onNavigateToPlan(plan.id) },
                                        imagenOverlay = {
                                            // Boton eliminar dentro de la imagen, esquina superior derecha
                                            IconButton(
                                                onClick = {
                                                    viewModel.abandonarPlan(
                                                        planId = plan.id,
                                                        onSuccess = { viewModel.cargarHistorial() },
                                                        onError = {}
                                                    )
                                                },
                                                modifier = Modifier
                                                    .align(Alignment.TopEnd)
                                                    .padding(10.dp)
                                                    .size(36.dp)
                                                    .clip(RoundedCornerShape(8.dp))
                                                    .background(Color.Black.copy(alpha = 0.40f))
                                            ) {
                                                Icon(
                                                    imageVector = Icons.Filled.Delete,
                                                    contentDescription = "Eliminar del historial",
                                                    tint = Color.White,
                                                    modifier = Modifier.size(18.dp)
                                                )
                                            }
                                        }
                                    )
                                }
                            }
                        }
                    }
                }

                // Pestaña Tu
                else -> when (misPlanesCreados) {
                    is PlanesState.Loading -> Box(Modifier.fillMaxSize(), Alignment.Center) {
                        CircularProgressIndicator(color = KddPurple)
                    }
                    is PlanesState.Error -> Box(Modifier.fillMaxSize(), Alignment.Center) {
                        Text((misPlanesCreados as PlanesState.Error).mensaje, color = KddTextSecondary)
                    }
                    is PlanesState.Success -> {
                        if (planesTu.isEmpty()) {
                            Box(Modifier.weight(1f).fillMaxWidth(), Alignment.Center) {
                                Column(
                                    horizontalAlignment = Alignment.CenterHorizontally,
                                    verticalArrangement = Arrangement.spacedBy(12.dp)
                                ) {
                                    Text(
                                        text = "Aún no has creado ningún plan.\n¡Organiza tu primera actividad!",
                                        style = MaterialTheme.typography.bodyMedium,
                                        color = KddTextHint,
                                        textAlign = TextAlign.Center
                                    )
                                    Button(
                                        onClick = onNavigateToCreatePlan,
                                        colors = ButtonDefaults.buttonColors(containerColor = KddPurple)
                                    ) {
                                        Text("Crear actividad", color = Color.White)
                                    }
                                }
                            }
                        } else {
                            LazyColumn {
                                items(planesTu) { plan ->
                                    PlanCard(data = plan, onClick = { onNavigateToPlan(plan.id) })
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    if (showBottomSheet) {
        CreateBottomSheet(
            sheetState = sheetState,
            onDismiss = { showBottomSheet = false },
            onNavigateToCreatePlan = onNavigateToCreatePlan,
            onNavigateToCreateCommunity = onNavigateToCreateCommunity
        )
    }
}
