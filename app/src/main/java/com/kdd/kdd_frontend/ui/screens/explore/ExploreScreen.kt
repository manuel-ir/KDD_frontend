package com.kdd.kdd_frontend.ui.screens.explore

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.FilterList
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.activity.ComponentActivity
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.viewmodel.compose.viewModel
import com.kdd.kdd_frontend.ui.components.*
import com.kdd.kdd_frontend.ui.theme.*
import com.kdd.kdd_frontend.viewmodel.PlanViewModel
import com.kdd.kdd_frontend.viewmodel.PlanesState

/**
 * Pantalla de exploracion de planes en formato lista.
 *
 * Muestra todos los planes disponibles ordenados por fecha.
 * Permite aplicar filtros por categoria, idioma, edad, aforo y horario.
 * Al pulsar una tarjeta navega al detalle del plan.
 *
 * Si no hay conexion con el servidor muestra un mensaje de error
 * con un boton para reintentar la carga.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ExploreScreen(
    onNavigateBack: () -> Unit,
    onNavigateToPlan: (Long) -> Unit,
    onNavigateToFilters: () -> Unit,
    onNavigateToMain: () -> Unit,
    onNavigateToCommunities: () -> Unit,
    onNavigateToCalendar: () -> Unit,
    onNavigateToCreatePlan: () -> Unit,
    onNavigateToCreateCommunity: () -> Unit
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    var showBottomSheet by remember { mutableStateOf(false) }
    val planViewModel: PlanViewModel = viewModel(LocalContext.current as ComponentActivity)
    val planesState by planViewModel.planesState.collectAsState()
    val filtroCategoria by planViewModel.filtroCategoria.collectAsState()
    val filtroFecha by planViewModel.filtroFecha.collectAsState()

    // Recargar al entrar en la pantalla para garantizar datos actualizados
    LaunchedEffect(Unit) {
        planViewModel.cargarPlanes()
    }

    val filtroActivo: String? = when {
        filtroCategoria.isNotBlank() && filtroFecha != null -> "$filtroCategoria · $filtroFecha"
        filtroCategoria.isNotBlank() -> filtroCategoria
        filtroFecha != null -> filtroFecha
        else -> null
    }

    Scaffold(
        bottomBar = {
            BottomNavBar(
                currentItem = BottomNavItem.EXPLORE,
                onHomeClick = onNavigateToMain,
                onExploreClick = { },
                onCreateClick = { showBottomSheet = true },
                onCommunitiesClick = onNavigateToCommunities,
                onCalendarClick = onNavigateToCalendar
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            Text(
                text = "Explora",
                style = MaterialTheme.typography.headlineLarge,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(start = 20.dp, top = 20.dp, bottom = 4.dp)
            )
            Text(
                text = "Actividades",
                style = MaterialTheme.typography.titleMedium,
                color = KddTextSecondary,
                modifier = Modifier.padding(start = 20.dp, bottom = 12.dp)
            )

            Row(
                modifier = Modifier.padding(horizontal = 16.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                OutlinedButton(
                    onClick = onNavigateToFilters,
                    shape = RoundedCornerShape(20.dp),
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = KddTextPrimary)
                ) {
                    Icon(Icons.Filled.FilterList, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Filtros")
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("▼", style = MaterialTheme.typography.bodySmall)
                }
                if (filtroActivo != null) {
                    InputChip(
                        selected = true,
                        onClick = { planViewModel.limpiarFiltrosExplora() },
                        label = { Text(filtroActivo!!) },
                        trailingIcon = {
                            Icon(Icons.Filled.Close, contentDescription = "Quitar filtro", modifier = Modifier.size(14.dp))
                        },
                        colors = InputChipDefaults.inputChipColors(
                            selectedContainerColor = KddPurple.copy(alpha = 0.1f),
                            selectedLabelColor = KddPurple
                        )
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            when (val state = planesState) {
                is PlanesState.Loading -> {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        CircularProgressIndicator(color = KddPurple)
                    }
                }
                is PlanesState.Error -> {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(12.dp)) {
                            Text(state.mensaje, color = KddTextSecondary)
                            Button(onClick = { planViewModel.cargarPlanes() }, colors = ButtonDefaults.buttonColors(containerColor = KddPurple)) {
                                Text("Reintentar", color = Color.White)
                            }
                        }
                    }
                }
                is PlanesState.Success -> {
                    LazyColumn {
                        items(state.planes) { plan ->
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

    if (showBottomSheet) {
        CreateBottomSheet(
            sheetState = sheetState,
            onDismiss = { showBottomSheet = false },
            onNavigateToCreatePlan = onNavigateToCreatePlan,
            onNavigateToCreateCommunity = onNavigateToCreateCommunity
        )
    }
}
