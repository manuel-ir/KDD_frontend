package com.kdd.kdd_frontend.ui.screens.main

import android.Manifest
import android.annotation.SuppressLint
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Paint
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.Chat
import androidx.compose.material.icons.filled.MyLocation
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import com.kdd.kdd_frontend.R
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.DialogProperties
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.AsyncImage
import com.google.android.gms.location.LocationServices
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.model.BitmapDescriptor
import com.google.android.gms.maps.model.BitmapDescriptorFactory
import com.google.android.gms.maps.model.CameraPosition
import com.google.android.gms.maps.model.LatLng
import com.google.maps.android.compose.*
import com.kdd.kdd_frontend.ui.components.BottomNavBar
import com.kdd.kdd_frontend.ui.components.BottomNavItem
import com.kdd.kdd_frontend.ui.components.CreateBottomSheet
import com.kdd.kdd_frontend.ui.theme.*
import com.kdd.kdd_frontend.viewmodel.PerfilState
import com.kdd.kdd_frontend.viewmodel.PerfilViewModel
import com.kdd.kdd_frontend.viewmodel.PlanViewModel
import com.kdd.kdd_frontend.viewmodel.PlanesState
import kotlinx.coroutines.launch
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneOffset
import java.time.format.DateTimeFormatter

private val EMOJI_CATEGORIA = mapOf(
    "Deportes" to "🏃",
    "Naturaleza" to "🌿",
    "Fiesta" to "🎉",
    "Música" to "🎵",
    "Arte y Cultura" to "🎨",
    "Gastronomía" to "🍴",
    "Viajes" to "✈️",
    "Tecnología" to "💻",
    "Cine y Series" to "🍿",
    "Fotografía" to "📷",
    "Juegos" to "🎮",
    "Lectura" to "📚",
    "Idiomas" to "🗣️",
    "Voluntariado" to "🤝"
)

/**
 * Pantalla principal con el mapa de Google Maps.
 *
 * Muestra los planes disponibles como marcadores en el mapa.
 * Al pulsar un marcador se puede ver el detalle del plan.
 * Incluye un banner de error si el servidor no esta disponible.
 *
 * Usa Google Maps SDK para Android con Jetpack Compose.
 * Las coordenadas de cada plan se obtienen del backend.
 */
private fun emojiABitmapDescriptor(emoji: String, sizePx: Int = 96): BitmapDescriptor {
    val bitmap = Bitmap.createBitmap(sizePx, sizePx, Bitmap.Config.ARGB_8888)
    val canvas = Canvas(bitmap)
    val paint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        textSize = sizePx * 0.75f
        textAlign = Paint.Align.CENTER
    }
    val textY = sizePx / 2f - (paint.descent() + paint.ascent()) / 2f
    canvas.drawText(emoji, sizePx / 2f, textY, paint)
    return BitmapDescriptorFactory.fromBitmap(bitmap)
}

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
    val perfilViewModel: PerfilViewModel = viewModel()
    val planesState by planViewModel.planesState.collectAsState()
    val perfilState by perfilViewModel.perfilState.collectAsState()
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

    val fotoPerfil = (perfilState as? PerfilState.Success)?.usuario?.fotoPerfil
    val inicialesNombre = (perfilState as? PerfilState.Success)?.usuario?.nombreMostrado?.firstOrNull()?.uppercaseChar()?.toString() ?: "P"

    // Popups obligatorios: alias → edad (en ese orden)
    val usuario = (perfilState as? PerfilState.Success)?.usuario
    var mostrarDialogoAlias by remember { mutableStateOf(false) }
    var guardandoAlias by remember { mutableStateOf(false) }
    var errorAlias by remember { mutableStateOf<String?>(null) }
    var mostrarDialogoEdad by remember { mutableStateOf(false) }
    var guardandoEdad by remember { mutableStateOf(false) }
    var errorEdad by remember { mutableStateOf<String?>(null) }

    LaunchedEffect(usuario) {
        if (usuario != null) {
            if (usuario.nombreUsuario.isNullOrBlank()) {
                mostrarDialogoAlias = true
            } else if (usuario.fechaNacimiento.isNullOrBlank()) {
                mostrarDialogoEdad = true
            }
        }
    }

    if (mostrarDialogoAlias) {
        AliasObligatorioDialog(
            guardando = guardandoAlias,
            error = errorAlias,
            onConfirmar = { alias ->
                guardandoAlias = true
                errorAlias = null
                perfilViewModel.editarPerfil(
                    nombre = usuario?.nombre ?: "",
                    nombreUsuario = alias,
                    descripcion = usuario?.descripcion ?: "",
                    fechaNacimiento = null,
                    onSuccess = {
                        guardandoAlias = false
                        mostrarDialogoAlias = false
                    },
                    onError = { msg ->
                        guardandoAlias = false
                        errorAlias = msg
                    }
                )
            }
        )
    }

    if (mostrarDialogoEdad) {
        EdadObligatoriaDialog(
            guardando = guardandoEdad,
            error = errorEdad,
            onConfirmar = { fechaIso ->
                guardandoEdad = true
                errorEdad = null
                perfilViewModel.editarPerfil(
                    nombre = usuario?.nombre ?: "",
                    nombreUsuario = usuario?.nombreUsuario,
                    descripcion = usuario?.descripcion ?: "",
                    fechaNacimiento = fechaIso,
                    onSuccess = {
                        guardandoEdad = false
                        mostrarDialogoEdad = false
                    },
                    onError = { msg ->
                        guardandoEdad = false
                        errorEdad = msg
                    }
                )
            }
        )
    }

    Box(modifier = Modifier.fillMaxSize()) {
        Column(modifier = Modifier.fillMaxSize()) {
            KddTopAppBar(
                fotoPerfil = fotoPerfil,
                iniciales = inicialesNombre,
                onChatsClick = onNavigateToChats,
                onAccountClick = onNavigateToAccount
            )

            if (planesState is PlanesState.Error) {
                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    color = MaterialTheme.colorScheme.errorContainer
                ) {
                    Text(
                        text = "Sin conexion con el servidor. Comprueba tu red.",
                        modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onErrorContainer
                    )
                }
            }

            Box(modifier = Modifier.weight(1f).fillMaxWidth()) {
                GoogleMap(
                    modifier = Modifier.fillMaxSize(),
                    cameraPositionState = cameraPositionState,
                    properties = MapProperties(isMyLocationEnabled = locationPermissionGranted),
                    uiSettings = MapUiSettings(
                        zoomControlsEnabled = true,
                        myLocationButtonEnabled = false
                    )
                ) {
                    if (planesState is PlanesState.Success) {
                        (planesState as PlanesState.Success).planes.forEach { plan ->
                            val lat = plan.latitud
                            val lng = plan.longitud
                            if (lat != null && lng != null) {
                                val emoji = EMOJI_CATEGORIA[plan.categoria]
                                val icon = if (emoji != null) {
                                    remember(plan.categoria) { emojiABitmapDescriptor(emoji) }
                                } else null
                                Marker(
                                    state = rememberMarkerState(position = LatLng(lat, lng)),
                                    title = plan.titulo,
                                    snippet = plan.categoria,
                                    icon = icon,
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
    fusedClient.getCurrentLocation(
        com.google.android.gms.location.Priority.PRIORITY_HIGH_ACCURACY,
        null
    ).addOnSuccessListener { location ->
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
private fun AliasObligatorioDialog(
    guardando: Boolean,
    error: String?,
    onConfirmar: (String) -> Unit
) {
    var alias by remember { mutableStateOf("") }
    androidx.compose.ui.window.Dialog(
        onDismissRequest = { /* no se puede cerrar */ },
        properties = androidx.compose.ui.window.DialogProperties(
            dismissOnBackPress = false,
            dismissOnClickOutside = false
        )
    ) {
        androidx.compose.material3.Card(
            shape = androidx.compose.foundation.shape.RoundedCornerShape(20.dp),
            colors = androidx.compose.material3.CardDefaults.cardColors(containerColor = Color.White)
        ) {
            Column(
                modifier = Modifier.padding(24.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Text(
                    "¿Cómo quieres que te llamen?",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = KddTextPrimary
                )
                Text(
                    "Elige un alias para identificarte en los planes. Podrás cambiarlo hasta 3 veces.",
                    style = MaterialTheme.typography.bodySmall,
                    color = KddTextSecondary
                )
                OutlinedTextField(
                    value = alias,
                    onValueChange = { if (it.length <= 20) alias = it },
                    placeholder = { Text("Ej: pepegrillo92", color = KddTextHint) },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                    shape = androidx.compose.foundation.shape.RoundedCornerShape(10.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = KddPurple,
                        unfocusedBorderColor = KddDivider
                    )
                )
                if (error != null) {
                    Text(
                        text = error,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.error
                    )
                }
                Button(
                    onClick = { if (alias.isNotBlank()) onConfirmar(alias.trim()) },
                    modifier = Modifier.fillMaxWidth(),
                    enabled = alias.isNotBlank() && !guardando,
                    colors = ButtonDefaults.buttonColors(containerColor = KddPurple)
                ) {
                    if (guardando) {
                        CircularProgressIndicator(color = Color.White, modifier = Modifier.size(20.dp), strokeWidth = 2.dp)
                    } else {
                        Text("Confirmar alias", color = Color.White, fontWeight = FontWeight.SemiBold)
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun EdadObligatoriaDialog(
    guardando: Boolean,
    error: String?,
    onConfirmar: (String) -> Unit
) {
    val haceAños18 = LocalDate.now().minusYears(18)
    val millis18 = haceAños18.atStartOfDay(ZoneOffset.UTC).toInstant().toEpochMilli()
    val datePickerState = rememberDatePickerState(
        initialSelectedDateMillis = millis18,
        yearRange = 1920..LocalDate.now().minusYears(13).year
    )
    var mostrarConfirmacion by remember { mutableStateOf(false) }
    var fechaPendiente by remember { mutableStateOf<String?>(null) }
    val formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd")
    val fechaSeleccionada: String? = datePickerState.selectedDateMillis?.let { millis ->
        Instant.ofEpochMilli(millis).atZone(ZoneOffset.UTC).toLocalDate().format(formatter)
    }
    val fechaLegible: String? = datePickerState.selectedDateMillis?.let { millis ->
        Instant.ofEpochMilli(millis).atZone(ZoneOffset.UTC).toLocalDate()
            .format(DateTimeFormatter.ofPattern("dd/MM/yyyy"))
    }
    if (mostrarConfirmacion && fechaPendiente != null) {
        AlertDialog(
            onDismissRequest = { mostrarConfirmacion = false },
            title = { Text("¿Seguro que es correcta?") },
            text = { Text("Has seleccionado $fechaLegible.\n\nEsta fecha no podrá cambiarse después.") },
            confirmButton = {
                Button(
                    onClick = { mostrarConfirmacion = false; onConfirmar(fechaPendiente!!) },
                    colors = ButtonDefaults.buttonColors(containerColor = KddPurple),
                    enabled = !guardando
                ) {
                    if (guardando) {
                        CircularProgressIndicator(color = Color.White, modifier = Modifier.size(16.dp), strokeWidth = 2.dp)
                    } else {
                        Text("Sí, es correcta")
                    }
                }
            },
            dismissButton = {
                TextButton(onClick = { mostrarConfirmacion = false }) { Text("Volver") }
            }
        )
    }
    DatePickerDialog(
        onDismissRequest = { /* No se puede cerrar */ },
        properties = DialogProperties(dismissOnBackPress = false, dismissOnClickOutside = false),
        confirmButton = {
            Button(
                onClick = {
                    if (fechaSeleccionada != null) {
                        fechaPendiente = fechaSeleccionada
                        mostrarConfirmacion = true
                    }
                },
                enabled = fechaSeleccionada != null && !guardando,
                colors = ButtonDefaults.buttonColors(containerColor = KddPurple)
            ) {
                Text(if (fechaLegible != null) "Confirmar ($fechaLegible)" else "Confirmar")
            }
        }
    ) {
        DatePicker(
            state = datePickerState,
            showModeToggle = true,
            title = {
                Column(modifier = Modifier.padding(start = 24.dp, end = 24.dp, top = 16.dp)) {
                    Text("¿Cuándo naciste?", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                    Text("Necesario para los límites de edad en los planes.", style = MaterialTheme.typography.bodySmall, color = KddTextSecondary)
                    if (error != null) {
                        Spacer(Modifier.height(4.dp))
                        Text(text = error, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.error)
                    }
                }
            }
        )
    }
}

@Composable
private fun KddTopAppBar(
    fotoPerfil: String?,
    iniciales: String,
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
            Image(
                painter = painterResource(id = R.drawable.logo_kdd),
                contentDescription = "Logo KDD",
                modifier = Modifier.size(32.dp)
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = "KDD",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = KddTextPrimary
            )
            Spacer(modifier = Modifier.weight(1f))
            IconButton(onClick = onChatsClick) {
                Icon(imageVector = Icons.AutoMirrored.Outlined.Chat, contentDescription = "Chats", tint = KddTextPrimary)
            }
            IconButton(onClick = onAccountClick) {
                Box(modifier = Modifier.size(32.dp).clip(CircleShape), contentAlignment = Alignment.Center) {
                    if (!fotoPerfil.isNullOrBlank()) {
                        AsyncImage(
                            model = fotoPerfil,
                            contentDescription = "Perfil",
                            contentScale = ContentScale.Crop,
                            modifier = Modifier.fillMaxSize().clip(CircleShape)
                        )
                    } else {
                        Box(
                            modifier = Modifier.fillMaxSize().background(KddPurple),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(iniciales, color = Color.White, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                        }
                    }
                }
            }
        }
    }
}