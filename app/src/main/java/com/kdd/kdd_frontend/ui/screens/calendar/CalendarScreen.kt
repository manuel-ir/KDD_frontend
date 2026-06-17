package com.kdd.kdd_frontend.ui.screens.calendar

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
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
import java.time.LocalTime
import java.time.format.DateTimeFormatter

/**
 * Pantalla de calendario del usuario.
 *
 * Muestra los planes en los que el usuario participa, divididos en dos secciones:
 * - Proximos: planes que aun no han terminado.
 * - Historial: planes que ya han pasado.
 *
 * La division se hace comparando la fecha y hora del plan con el momento actual.
 * Si hay hora de fin (horaHasta), se usa esa; si no, se usa la hora de inicio.
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
    val tabs = listOf("Próximos", "Historial", "Favoritos")
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    var showBottomSheet by remember { mutableStateOf(false) }

    val viewModel: PlanViewModel = viewModel()
    val misPlanes by viewModel.misPlanes.collectAsState()

    LaunchedEffect(Unit) {
        viewModel.cargarMisPlanes()
    }

    val hoy = remember { LocalDate.now() }
    val formatter = remember { DateTimeFormatter.ofPattern("yyyy-MM-dd") }
    val timeFormatter = remember { DateTimeFormatter.ofPattern("HH:mm") }

    // Un plan "ha terminado" si: tiene horaHasta y ya paso, o lleva mas de 24h desde el inicio
    fun planHaTerminado(plan: com.kdd.kdd_frontend.ui.components.PlanCardData): Boolean {
        val fecha = runCatching { LocalDate.parse(plan.dia, formatter) }.getOrNull()
            ?: return false // sin fecha → nunca en historial
        val hoyFecha = LocalDate.now()
        val ahora = LocalTime.now()
        if (fecha.isAfter(hoyFecha)) return false

        val horaFin = plan.horaHasta?.let { runCatching { LocalTime.parse(it.take(5), timeFormatter) }.getOrNull() }
        val horaIni = plan.hora.takeIf { it.isNotBlank() }?.let { runCatching { LocalTime.parse(it.take(5), timeFormatter) }.getOrNull() }

        if (fecha.isBefore(hoyFecha)) {
            // Dia pasado: con horaHasta siempre terminado
            if (horaFin != null) return true
            // Sin horaHasta: termina 24h despues del inicio
            val fechaExpiracion = fecha.plusDays(1)
            return when {
                fechaExpiracion.isBefore(hoyFecha) -> true
                fechaExpiracion.isEqual(hoyFecha) -> horaIni == null || !ahora.isBefore(horaIni)
                else -> false
            }
        }

        // Hoy: comprobar hora de fin
        if (horaFin != null) return !ahora.isBefore(horaFin)
        // Sin horaHasta: expira manana a la misma hora de inicio → hoy aun no terminado
        return false
    }

    val planesProximos = remember(misPlanes) {
        (misPlanes as? PlanesState.Success)?.planes
            ?.filter { !planHaTerminado(it) }
            ?.sortedBy { it.dia } ?: emptyList()
    }

    // Historial pendiente de implementar en version futura
    val planesHistorial = emptyList<com.kdd.kdd_frontend.ui.components.PlanCardData>()

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

            when (misPlanes) {
                is PlanesState.Loading -> {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        CircularProgressIndicator(color = KddPurple)
                    }
                }
                is PlanesState.Error -> {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        Text((misPlanes as PlanesState.Error).mensaje, color = KddTextSecondary)
                    }
                }
                is PlanesState.Success -> {
                    val planesAMostrar = when (selectedTab) {
                        0 -> planesProximos
                        1 -> planesHistorial
                        else -> emptyList()
                    }

                    if (planesAMostrar.isEmpty()) {
                        Box(
                            modifier = Modifier.weight(1f).fillMaxWidth(),
                            contentAlignment = Alignment.Center
                        ) {
                            Column(
                                horizontalAlignment = Alignment.CenterHorizontally,
                                verticalArrangement = Arrangement.spacedBy(12.dp)
                            ) {
                                Text(
                                    text = when (selectedTab) {
                                        0 -> "No tienes planes próximos.\nCrea tu actividad"
                                        1 -> "Historial de actividades\nProximamente"
                                        else -> "No tienes planes favoritos"
                                    },
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = KddTextHint,
                                    textAlign = TextAlign.Center
                                )
                                if (selectedTab == 0) {
                                    Button(
                                        onClick = onNavigateToCreatePlan,
                                        colors = ButtonDefaults.buttonColors(containerColor = KddPurple)
                                    ) {
                                        Text("Crear actividad", color = Color.White)
                                    }
                                }
                            }
                        }
                    } else {
                        LazyColumn {
                            items(planesAMostrar) { plan ->
                                PlanCard(
                                    data = plan,
                                    onClick = { onNavigateToPlan(plan.id) }
                                )
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
