package com.kdd.kdd_frontend.ui.screens.calendar

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.kdd.kdd_frontend.ui.components.*
import com.kdd.kdd_frontend.ui.theme.*

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
    // Tabs: Próximos y Favoritos
    var selectedTab by remember { mutableIntStateOf(0) }
    val tabs = listOf("Próximos", "Favoritos")
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    var showBottomSheet by remember { mutableStateOf(false) }

    // Datos de ejemplo
    val misPlanes = listOf<PlanCardData>() // vacío para mostrar estado empty

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

            if (misPlanes.isEmpty()) {
                // Estado vacío
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth(),
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Text(
                            text = "Aquí no hay planes por el momento.\nCrea tu actividad",
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
                    items(misPlanes) { plan ->
                        PlanCard(
                            data = plan,
                            onClick = { onNavigateToPlan(plan.id) }
                        )
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
