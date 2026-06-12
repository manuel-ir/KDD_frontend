package com.kdd.kdd_frontend.ui.screens.main

import android.Manifest
import android.annotation.SuppressLint
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.MyLocation
import androidx.compose.material.icons.automirrored.outlined.Chat
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.google.android.gms.location.LocationServices
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.model.CameraPosition
import com.google.android.gms.maps.model.LatLng
import com.google.maps.android.compose.*
import com.kdd.kdd_frontend.ui.components.BottomNavBar
import com.kdd.kdd_frontend.ui.components.BottomNavItem
import com.kdd.kdd_frontend.ui.components.CreateBottomSheet
import com.kdd.kdd_frontend.ui.theme.*
import com.kdd.kdd_frontend.viewmodel.PlanViewModel
import com.kdd.kdd_frontend.viewmodel.PlanesState
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainScreen(
    onNavigateToExplore: () -> Unit,
    onNavigateToCommunities: () -> Unit,
    onNavigateToCalendar: () -> Unit,
    onNavigateToCreatePlan: () -> Unit,
    onNavigateToCreateCommunity: () -> Unit,
    onNavigateToChats: () -> Unit,
    onNavigateToAccount: () -> Unit,
    onNavigateToPlan: (Long) -> Unit = {}
) {
    val context = LocalContext.current
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    var showBottomSheet by remember { mutableStateOf(false) }
    val planViewModel: PlanViewModel = viewModel()
    val planesState by planViewModel.planesState.collectAsState()
    val coroutineScope = rememberCoroutineScope()

    var locationPermissionGranted by remember { mutableStateOf(false) }
    val permissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { granted -> locationPermissionGranted = granted }

    val espana = LatLng(40.4168, -3.7038)
    val cameraPositionState = rememberCameraPositionState {
        position = CameraPosition.fromLatLngZoom(espana, 6f)
    }

    LaunchedEffect(Unit) {
        planViewModel.cargarPlanes()
    }

    Box(modifier = Modifier.fillMaxSize()) {
        Column(modifier = Modifier.fillMaxSize()) {
            KddTopAppBar(
                onChatsClick = onNavigateToChats,
                onAccountClick = onNavigateToAccount
            )

            Box(modifier = Modifier.weight(1f).fillMaxWidth()) {
                GoogleMap(
                    modifier = Modifier.fillMaxSize(),
                    cameraPositionState = cameraPositionState,
                    properties = MapProperties(isMyLocationEnabled = locationPermissionGranted),
                    uiSettings = MapUiSettings(
                        zoomControlsEnabled = false,
                        myLocationButtonEnabled = false
                    )
                ) {
                    if (planesState is PlanesState.Success) {
                        (planesState as PlanesState.Success).planes.forEach { plan ->
                            val lat = plan.latitud
                            val lng = plan.longitud
                            if (lat != null && lng != null) {
                                Marker(
                                    state = rememberMarkerState(position = LatLng(lat, lng)),
                                    title = plan.titulo,
                                    snippet = plan.categoria,
                                    onClick = {
                                        onNavigateToPlan(plan.id)
                                        true
                                    }
                                )
                            }
                        }
                    }
                }

                FloatingActionButton(
                    onClick = {
                        if (locationPermissionGranted) {
                            centrarEnUbicacion(context, cameraPositionState, coroutineScope)
                        } else {
                            permissionLauncher.launch(Manifest.permission.ACCESS_FINE_LOCATION)
                        }
                    },
                    modifier = Modifier
                        .align(Alignment.BottomEnd)
                        .padding(end = 16.dp, bottom = 16.dp)
                        .size(40.dp),
                    containerColor = Color.White,
                    contentColor = KddPurple,
                    elevation = FloatingActionButtonDefaults.elevation(4.dp)
                ) {
                    Icon(
                        imageVector = Icons.Filled.MyLocation,
                        contentDescription = "Mi ubicación",
                        modifier = Modifier.size(20.dp)
                    )
                }
            }

            BottomNavBar(
                currentItem = BottomNavItem.HOME,
                onHomeClick = { },
                onExploreClick = onNavigateToExplore,
                onCreateClick = { showBottomSheet = true },
                onCommunitiesClick = onNavigateToCommunities,
                onCalendarClick = onNavigateToCalendar
            )
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
}

@SuppressLint("MissingPermission")
private fun centrarEnUbicacion(
    context: android.content.Context,
    cameraPositionState: CameraPositionState,
    coroutineScope: kotlinx.coroutines.CoroutineScope
) {
    val fusedClient = LocationServices.getFusedLocationProviderClient(context)
    fusedClient.lastLocation.addOnSuccessListener { location ->
        if (location != null) {
            coroutineScope.launch {
                cameraPositionState.animate(
                    CameraUpdateFactory.newLatLngZoom(
                        LatLng(location.latitude, location.longitude), 14f
                    )
                )
            }
        }
    }
}

@Composable
private fun KddTopAppBar(
    onChatsClick: () -> Unit,
    onAccountClick: () -> Unit
) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        color = Color.White,
        shadowElevation = 2.dp
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .statusBarsPadding()
                .height(56.dp)
                .padding(horizontal = 16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Surface(
                modifier = Modifier.size(32.dp),
                shape = RoundedCornerShape(8.dp),
                color = KddYellow
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Text(
                        text = "K",
                        fontWeight = FontWeight.ExtraBold,
                        color = Color.Black,
                        fontSize = 16.sp
                    )
                }
            }
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = "KDD",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = KddTextPrimary
            )

            Spacer(modifier = Modifier.weight(1f))

            IconButton(onClick = onChatsClick) {
                Icon(
                    imageVector = Icons.AutoMirrored.Outlined.Chat,
                    contentDescription = "Chats",
                    tint = KddTextPrimary
                )
            }

            IconButton(onClick = onAccountClick) {
                Box(
                    modifier = Modifier
                        .size(32.dp)
                        .clip(CircleShape)
                        .background(KddPurple),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "P",
                        color = Color.White,
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.sp
                    )
                }
            }
        }
    }
}
