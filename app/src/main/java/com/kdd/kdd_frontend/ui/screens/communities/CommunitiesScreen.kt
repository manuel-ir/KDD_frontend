package com.kdd.kdd_frontend.ui.screens.communities

import androidx.compose.foundation.background
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
import com.kdd.kdd_frontend.viewmodel.ComunidadViewModel
import com.kdd.kdd_frontend.viewmodel.ComunidadesState

/**
 * Pantalla con la lista de comunidades disponibles.
 *
 * Muestra todas las comunidades y permite buscar por nombre.
 * Al pulsar una comunidad navega a su detalle.
 * Incluye un boton para crear una nueva comunidad.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CommunitiesScreen(
    onNavigateToMain: () -> Unit,
    onNavigateToCommunityDetail: (Long) -> Unit,
    onNavigateToFilters: () -> Unit,
    onNavigateToExplore: () -> Unit,
    onNavigateToCalendar: () -> Unit,
    onNavigateToCreatePlan: () -> Unit,
    onNavigateToCreateCommunity: () -> Unit
) {
    val viewModel: ComunidadViewModel = viewModel(LocalContext.current as ComponentActivity)
    val comunidadesState by viewModel.comunidadesState.collectAsState()
    val misComunidadesState by viewModel.misComunidades.collectAsState()

    // Recargar al entrar en la pantalla para garantizar datos actualizados
    LaunchedEffect(Unit) {
        viewModel.cargarComunidades()
    }

    var selectedTab by remember { mutableIntStateOf(0) }
    val filtroComCiudad by viewModel.filtroComCiudad.collectAsState()
    val filtroComCategoria by viewModel.filtroComCategoria.collectAsState()
    val filtroActivo: String? = when {
        filtroComCategoria.isNotBlank() -> filtroComCategoria
        filtroComCiudad.isNotBlank() -> filtroComCiudad
        else -> null
    }
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    var showBottomSheet by remember { mutableStateOf(false) }
    val tabs = listOf("Descubrir", "Tú")

    Scaffold(
        bottomBar = {
            BottomNavBar(
                currentItem = BottomNavItem.COMMUNITIES,
                onHomeClick = onNavigateToMain,
                onExploreClick = onNavigateToExplore,
                onCreateClick = { showBottomSheet = true },
                onCommunitiesClick = { },
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
                text = "Comunidades",
                style = MaterialTheme.typography.headlineLarge,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(start = 20.dp, top = 20.dp, bottom = 8.dp)
            )

            // Tabs
            TabRow(
                selectedTabIndex = selectedTab,
                containerColor = Color.White,
                contentColor = KddTextPrimary,
                indicator = { tabPositions ->
                    val pos = tabPositions[selectedTab]
                    Box(
                        Modifier
                            .fillMaxWidth()
                            .wrapContentSize(Alignment.BottomStart)
                            .offset(x = pos.left)
                            .width(pos.width)
                            .height(3.dp)
                            .background(KddYellow)
                    )
                }
            ) {
                tabs.forEachIndexed { index, title ->
                    Tab(
                        selected = selectedTab == index,
                        onClick = { selectedTab = index },
                        text = {
                            Text(
                                text = title,
                                fontWeight = if (selectedTab == index) FontWeight.Bold else FontWeight.Normal,
                                color = KddTextPrimary
                            )
                        }
                    )
                }
            }

            // Fila de filtros
            Row(
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp),
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
                        onClick = { viewModel.limpiarFiltrosComunidades() },
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

            LaunchedEffect(selectedTab) {
                if (selectedTab == 1) viewModel.cargarMisComunidades()
            }

            if (selectedTab == 1) {
                when (val estado = misComunidadesState) {
                    is ComunidadesState.Loading -> {
                        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                            CircularProgressIndicator(color = KddPurple)
                        }
                    }
                    is ComunidadesState.Error -> {
                        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                            Text(estado.mensaje, style = MaterialTheme.typography.bodyMedium, color = KddTextSecondary)
                        }
                    }
                    is ComunidadesState.Success -> {
                        if (estado.comunidades.isEmpty()) {
                            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                                Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(8.dp)) {
                                    Text("Aún no perteneces a ninguna comunidad", style = MaterialTheme.typography.bodyMedium, color = KddTextSecondary)
                                    Text("Explora y únete a una", style = MaterialTheme.typography.bodySmall, color = KddTextHint)
                                }
                            }
                        } else {
                            LazyColumn {
                                items(estado.comunidades) { comunidad ->
                                    CommunityCard(
                                        data = comunidad,
                                        onClick = { onNavigateToCommunityDetail(comunidad.id) }
                                    )
                                }
                            }
                        }
                    }
                }
            } else {
                when (val estado = comunidadesState) {
                    is ComunidadesState.Loading -> {
                        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                            CircularProgressIndicator(color = KddPurple)
                        }
                    }
                    is ComunidadesState.Error -> {
                        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(8.dp)) {
                                Text(estado.mensaje, style = MaterialTheme.typography.bodyMedium, color = KddTextSecondary)
                                Button(onClick = { viewModel.cargarComunidades() }, colors = ButtonDefaults.buttonColors(containerColor = KddPurple)) {
                                    Text("Reintentar")
                                }
                            }
                        }
                    }
                    is ComunidadesState.Success -> {
                        LazyColumn {
                            items(estado.comunidades) { comunidad ->
                                CommunityCard(
                                    data = comunidad,
                                    onClick = { onNavigateToCommunityDetail(comunidad.id) }
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
