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
import androidx.lifecycle.viewmodel.compose.viewModel
import com.kdd.kdd_frontend.ui.components.*
import com.kdd.kdd_frontend.ui.theme.*
import com.kdd.kdd_frontend.viewmodel.PlanViewModel
import com.kdd.kdd_frontend.viewmodel.PlanesState

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
    var filtroActivo by remember { mutableStateOf<String?>(null) }
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    var showBottomSheet by remember { mutableStateOf(false) }
    val planViewModel: PlanViewModel = viewModel()
    val planesState by planViewModel.planesState.collectAsState()

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
            // Título
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

            // Fila de filtros
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

                // Chip de filtro activo
                if (filtroActivo != null) {
                    InputChip(
                        selected = true,
                        onClick = { filtroActivo = null },
                        label = { Text(filtroActivo!!) },
                        trailingIcon = {
                            Icon(
                                Icons.Filled.Close,
                                contentDescription = "Quitar filtro",
                                modifier = Modifier.size(14.dp)
                            )
                        },
                        colors = InputChipDefaults.inputChipColors(
                            selectedContainerColor = KddPurple.copy(alpha = 0.1f),
                            selectedLabelColor = KddPurple
                        )
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Lista de planes
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
