package com.kdd.kdd_frontend.ui.screens.plan

import android.Manifest
import android.content.pm.PackageManager
import android.location.Geocoder
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.core.content.ContextCompat
import androidx.lifecycle.viewmodel.compose.viewModel
import com.google.android.gms.location.LocationServices
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.model.CameraPosition
import com.google.android.gms.maps.model.LatLng
import com.google.maps.android.compose.*
import com.kdd.kdd_frontend.network.dto.CrearPlanDto
import com.kdd.kdd_frontend.ui.theme.*
import com.kdd.kdd_frontend.viewmodel.PlanDetalleState
import com.kdd.kdd_frontend.viewmodel.PlanViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext
import java.time.LocalDate
import java.time.LocalTime
import java.time.format.DateTimeFormatter
import java.util.Locale

/**
 * Pantalla para editar un plan existente.
 *
 * Carga los datos actuales del plan y permite modificarlos.
 * Solo el creador del plan puede acceder a esta pantalla.
 * Funciona igual que CreatePlanScreen pero en modo edicion.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditPlanScreen(
    planId: Long,
    onNavigateBack: () -> Unit,
    onPlanEditado: () -> Unit
) {
    val viewModel: PlanViewModel = viewModel()
    val detalleState by viewModel.detalleState.collectAsState()
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()

    var cargado by remember { mutableStateOf(false) }
    var cargando by remember { mutableStateOf(false) }
    var errorMsg by remember { mutableStateOf("") }
    val snackbarHostState = remember { SnackbarHostState() }

    // Campos del formulario
    var titulo by remember { mutableStateOf("") }
    var descripcion by remember { mutableStateOf("") }
    var categoria by remember { mutableStateOf("") }
    var categoriaPersonalizada by remember { mutableStateOf("") }
    var idiomasSeleccionados by remember { mutableStateOf(setOf<String>()) }
    var showIdiomasDialog by remember { mutableStateOf(false) }
    var edadMin by remember { mutableFloatStateOf(18f) }
    var edadMax by remember { mutableFloatStateOf(55f) }
    var maxPersonas by remember { mutableFloatStateOf(10f) }

    // Fechas y horas
    var fechaDesde by remember { mutableStateOf<LocalDate?>(null) }
    var horaDesde by remember { mutableStateOf<LocalTime?>(null) }
    var hastaHabilitado by remember { mutableStateOf(false) }
    var fechaHasta by remember { mutableStateOf<LocalDate?>(null) }
    var horaHasta by remember { mutableStateOf<LocalTime?>(null) }

    // Ubicación
    var selectedLatLng by remember { mutableStateOf<LatLng?>(null) }
    var ubicacionNombre by remember { mutableStateOf("") }
    var showLocationPicker by remember { mutableStateOf(false) }

    // Diálogos
    var showCategoriasDialog by remember { mutableStateOf(false) }
    var showDateDesdeDialog by remember { mutableStateOf(false) }
    var showTimeDesdeDialog by remember { mutableStateOf(false) }
    var showDateHastaDialog by remember { mutableStateOf(false) }
    var showTimeHastaDialog by remember { mutableStateOf(false) }

    val dateFormatter = DateTimeFormatter.ofPattern("dd/MM/yy")
    val timeFormatter = DateTimeFormatter.ofPattern("HH:mm")
    val apiDateFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd")

    LaunchedEffect(planId) {
        viewModel.cargarDetalle(planId)
    }

    LaunchedEffect(detalleState) {
        if (!cargado && detalleState is PlanDetalleState.Success) {
            val plan = (detalleState as PlanDetalleState.Success).plan
            titulo = plan.titulo
            descripcion = plan.descripcion ?: ""
            categoria = plan.categoria ?: ""
            ubicacionNombre = plan.ubicacionTexto ?: ""
            idiomasSeleccionados = plan.idioma
                ?.split(",")
                ?.map { it.trim() }
                ?.filter { it.isNotBlank() }
                ?.toSet() ?: emptySet()
            edadMin = (plan.edadMin ?: 18).toFloat()
            edadMax = (plan.edadMax ?: 55).toFloat()
            maxPersonas = (plan.numMaxPersonas ?: 10).toFloat()
            if (plan.latitud != null && plan.longitud != null) {
                selectedLatLng = LatLng(plan.latitud, plan.longitud)
            }
            fechaDesde = plan.fechaEvento?.let { runCatching { LocalDate.parse(it) }.getOrNull() }
            horaDesde = plan.horaEvento?.let { runCatching { LocalTime.parse(it.take(5), timeFormatter) }.getOrNull() }
            horaHasta = plan.horaHasta?.let { runCatching { LocalTime.parse(it.take(5), timeFormatter) }.getOrNull() }
            cargado = true
        }
    }

    LaunchedEffect(errorMsg) {
        if (errorMsg.isNotBlank()) {
            snackbarHostState.showSnackbar(errorMsg)
            errorMsg = ""
        }
    }

    Scaffold(snackbarHost = { SnackbarHost(snackbarHostState) }) { innerPadding ->
        when (detalleState) {
            is PlanDetalleState.Loading -> {
                Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator(color = KddPurple)
                }
            }
            else -> {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(Color.White)
                        .padding(innerPadding)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .statusBarsPadding()
                            .padding(horizontal = 8.dp, vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        IconButton(onClick = onNavigateBack) {
                            Icon(Icons.Filled.Close, contentDescription = "Cerrar", tint = KddTextPrimary)
                        }
                        Text(
                            "Editar actividad",
                            style = MaterialTheme.typography.headlineMedium,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(start = 8.dp)
                        )
                    }

                    Column(
                        modifier = Modifier
                            .weight(1f)
                            .verticalScroll(rememberScrollState())
                            .padding(horizontal = 16.dp),
                        verticalArrangement = Arrangement.spacedBy(14.dp)
                    ) {
                        Spacer(Modifier.height(4.dp))

                        // Título
                        OutlinedTextField(
                            value = titulo,
                            onValueChange = { if (it.length <= 60) titulo = it },
                            label = { Text("Título del plan") },
                            modifier = Modifier.fillMaxWidth(),
                            singleLine = true,
                            colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = KddPurple, focusedLabelColor = KddPurple)
                        )

                        // Descripción
                        OutlinedTextField(
                            value = descripcion,
                            onValueChange = { if (it.length <= 300) descripcion = it },
                            label = { Text("Descripción") },
                            modifier = Modifier.fillMaxWidth().height(100.dp),
                            maxLines = 5,
                            colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = KddPurple, focusedLabelColor = KddPurple)
                        )

                        // Categoría
                        Text("Categoría", style = MaterialTheme.typography.labelMedium, color = KddTextSecondary)
                        OutlinedButton(
                            onClick = { showCategoriasDialog = true },
                            modifier = Modifier.fillMaxWidth(),
                            colors = ButtonDefaults.outlinedButtonColors(
                                contentColor = if (categoria.isNotBlank()) KddPurple else KddTextHint
                            )
                        ) {
                            Text(if (categoria.isBlank()) "Selecciona una categoría" else categoria, modifier = Modifier.weight(1f))
                            Icon(Icons.Filled.KeyboardArrowDown, contentDescription = null, Modifier.size(16.dp))
                        }
                        if (categoria == "Personalizada") {
                            OutlinedTextField(
                                value = categoriaPersonalizada,
                                onValueChange = { if (it.length <= 20) categoriaPersonalizada = it },
                                label = { Text("Nombre de la categoría") },
                                modifier = Modifier.fillMaxWidth(),
                                singleLine = true,
                                colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = KddPurple, focusedLabelColor = KddPurple)
                            )
                        }

                        // Cuándo — Desde
                        Text("¿Cuándo?", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = KddTextPrimary)
                        Column {
                            Text("Desde", style = MaterialTheme.typography.bodySmall, color = KddTextSecondary)
                            Spacer(Modifier.height(6.dp))
                            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                Button(
                                    onClick = { showDateDesdeDialog = true },
                                    modifier = Modifier.weight(1f),
                                    colors = ButtonDefaults.buttonColors(containerColor = KddPurple),
                                    shape = RoundedCornerShape(8.dp)
                                ) {
                                    Icon(Icons.Filled.CalendarToday, contentDescription = null, modifier = Modifier.size(14.dp))
                                    Spacer(Modifier.width(4.dp))
                                    Text(fechaDesde?.format(dateFormatter) ?: "DD/MM/AA", color = Color.White, fontSize = 13.sp)
                                }
                                Button(
                                    onClick = { showTimeDesdeDialog = true },
                                    modifier = Modifier.weight(1f),
                                    colors = ButtonDefaults.buttonColors(containerColor = KddPurple),
                                    shape = RoundedCornerShape(8.dp)
                                ) {
                                    Icon(Icons.Filled.Schedule, contentDescription = null, modifier = Modifier.size(14.dp))
                                    Spacer(Modifier.width(4.dp))
                                    Text(horaDesde?.format(timeFormatter) ?: "--:--", color = Color.White, fontSize = 13.sp)
                                }
                            }
                        }

                        // Cuándo — Hasta
                        Column {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Checkbox(
                                    checked = hastaHabilitado,
                                    onCheckedChange = { hastaHabilitado = it },
                                    colors = CheckboxDefaults.colors(checkedColor = KddPurple)
                                )
                                Spacer(Modifier.width(4.dp))
                                Text("Hasta (opcional)", style = MaterialTheme.typography.bodyMedium, color = KddTextSecondary)
                            }
                            Spacer(Modifier.height(4.dp))
                            Row(
                                horizontalArrangement = Arrangement.spacedBy(8.dp),
                                modifier = Modifier.alpha(if (hastaHabilitado) 1f else 0.35f)
                            ) {
                                Button(
                                    onClick = { if (hastaHabilitado) showDateHastaDialog = true },
                                    modifier = Modifier.weight(1f),
                                    colors = ButtonDefaults.buttonColors(containerColor = KddPurpleLight),
                                    shape = RoundedCornerShape(8.dp)
                                ) {
                                    Icon(Icons.Filled.CalendarToday, contentDescription = null, modifier = Modifier.size(14.dp))
                                    Spacer(Modifier.width(4.dp))
                                    Text(fechaHasta?.format(dateFormatter) ?: "DD/MM/AA", color = Color.White, fontSize = 13.sp)
                                }
                                Button(
                                    onClick = { if (hastaHabilitado) showTimeHastaDialog = true },
                                    modifier = Modifier.weight(1f),
                                    colors = ButtonDefaults.buttonColors(containerColor = KddPurpleLight),
                                    shape = RoundedCornerShape(8.dp)
                                ) {
                                    Icon(Icons.Filled.Schedule, contentDescription = null, modifier = Modifier.size(14.dp))
                                    Spacer(Modifier.width(4.dp))
                                    Text(horaHasta?.format(timeFormatter) ?: "--:--", color = Color.White, fontSize = 13.sp)
                                }
                            }
                        }

                        // Dónde
                        Text("¿Dónde?", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = KddTextPrimary)
                        Button(
                            onClick = { showLocationPicker = true },
                            colors = ButtonDefaults.buttonColors(containerColor = KddPurple),
                            shape = RoundedCornerShape(20.dp),
                            modifier = Modifier.height(42.dp)
                        ) {
                            Icon(Icons.Filled.LocationOn, contentDescription = null, tint = Color.White, modifier = Modifier.size(16.dp))
                            Spacer(Modifier.width(6.dp))
                            Text(
                                if (selectedLatLng != null && ubicacionNombre.isNotBlank()) ubicacionNombre
                                else if (selectedLatLng != null) "Ubicación seleccionada ✓"
                                else if (ubicacionNombre.isNotBlank()) ubicacionNombre
                                else "Elige una ubicación",
                                color = Color.White
                            )
                        }

                        // Idioma
                        Text("Idioma", style = MaterialTheme.typography.labelMedium, color = KddTextSecondary)
                        OutlinedButton(
                            onClick = { showIdiomasDialog = true },
                            modifier = Modifier.fillMaxWidth(),
                            colors = ButtonDefaults.outlinedButtonColors(
                                contentColor = if (idiomasSeleccionados.isNotEmpty()) KddPurple else KddTextHint
                            )
                        ) {
                            Text(
                                if (idiomasSeleccionados.isEmpty()) "Selecciona idiomas"
                                else idiomasSeleccionados.joinToString(", "),
                                modifier = Modifier.weight(1f)
                            )
                            Icon(Icons.Filled.KeyboardArrowDown, contentDescription = null, Modifier.size(16.dp))
                        }

                        // Rango de edad
                        Card(colors = CardDefaults.cardColors(containerColor = KddSurface), shape = RoundedCornerShape(12.dp)) {
                            Column(Modifier.padding(16.dp)) {
                                Text("Rango de edad", style = MaterialTheme.typography.titleMedium, color = KddTextSecondary)
                                Spacer(Modifier.height(8.dp))
                                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                    Text("${edadMin.toInt()} años", color = KddTextPrimary, fontWeight = FontWeight.Medium)
                                    Text("${edadMax.toInt()} años", color = KddTextPrimary, fontWeight = FontWeight.Medium)
                                }
                                RangeSlider(
                                    value = edadMin..edadMax,
                                    onValueChange = { r -> edadMin = r.start; edadMax = r.endInclusive },
                                    valueRange = 18f..80f,
                                    colors = SliderDefaults.colors(thumbColor = KddPurple, activeTrackColor = KddPurple)
                                )
                            }
                        }

                        // Máximo de personas
                        Card(colors = CardDefaults.cardColors(containerColor = KddSurface), shape = RoundedCornerShape(12.dp)) {
                            Column(Modifier.padding(16.dp)) {
                                Text("Máximo de personas: ${maxPersonas.toInt()}", style = MaterialTheme.typography.titleMedium, color = KddTextSecondary)
                                Slider(
                                    value = maxPersonas,
                                    onValueChange = { maxPersonas = it },
                                    valueRange = 1f..50f,
                                    colors = SliderDefaults.colors(thumbColor = KddPurple, activeTrackColor = KddPurple)
                                )
                            }
                        }

                        Spacer(Modifier.height(8.dp))
                    }

                    // Botón guardar
                    Box(Modifier.padding(16.dp)) {
                        val categoriaFinal = if (categoria == "Personalizada") categoriaPersonalizada.trim() else categoria
                        val formValido = titulo.isNotBlank() && categoriaFinal.isNotBlank()
                        Button(
                            onClick = {
                                if (formValido && !cargando) {
                                    cargando = true
                                    viewModel.editarPlan(
                                        planId = planId,
                                        dto = CrearPlanDto(
                                            titulo = titulo.trim(),
                                            descripcion = descripcion.trim().ifBlank { "" },
                                            categoria = categoriaFinal,
                                            fechaEvento = fechaDesde?.format(apiDateFormatter),
                                            horaEvento = horaDesde?.format(DateTimeFormatter.ofPattern("HH:mm:ss")),
                                            horaHasta = horaHasta?.format(DateTimeFormatter.ofPattern("HH:mm:ss")),
                                            ubicacionTexto = ubicacionNombre.trim().ifBlank { null },
                                            latitud = selectedLatLng?.latitude,
                                            longitud = selectedLatLng?.longitude,
                                            edadMin = edadMin.toInt(),
                                            edadMax = edadMax.toInt(),
                                            numMaxPersonas = maxPersonas.toInt(),
                                            idioma = idiomasSeleccionados.joinToString(", ").ifBlank { null },
                                        ),
                                        onSuccess = { onPlanEditado() },
                                        onError = {
                                            cargando = false
                                            errorMsg = "Error al guardar los cambios"
                                        }
                                    )
                                }
                            },
                            modifier = Modifier.fillMaxWidth().height(52.dp),
                            shape = RoundedCornerShape(26.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = if (formValido && !cargando) KddPurple else KddDivider
                            ),
                            enabled = formValido && !cargando
                        ) {
                            if (cargando) {
                                CircularProgressIndicator(Modifier.size(20.dp), color = Color.White, strokeWidth = 2.dp)
                            } else {
                                Text("Guardar cambios", color = Color.White, fontWeight = FontWeight.SemiBold, fontSize = 16.sp)
                            }
                        }
                    }
                }
            }
        }
    }

    // Categorías
    if (showCategoriasDialog) {
        Dialog(onDismissRequest = { showCategoriasDialog = false }) {
            Card(shape = RoundedCornerShape(16.dp)) {
                Column(Modifier.padding(16.dp)) {
                    Text("Categoría", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                    Spacer(Modifier.height(8.dp))
                    CATEGORIAS_PREDEFINIDAS.forEach { cat ->
                        TextButton(
                            onClick = {
                                categoria = cat
                                if (cat != "Personalizada") categoriaPersonalizada = ""
                                showCategoriasDialog = false
                            },
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text(
                                cat,
                                color = if (categoria == cat) KddPurple else KddTextPrimary,
                                fontWeight = if (categoria == cat) FontWeight.Bold else FontWeight.Normal
                            )
                        }
                    }
                    TextButton(onClick = { showCategoriasDialog = false }, Modifier.align(Alignment.End)) {
                        Text("Cancelar", color = KddTextHint)
                    }
                }
            }
        }
    }

    // Idiomas dialog
    if (showIdiomasDialog) {
        androidx.compose.ui.window.Dialog(onDismissRequest = { showIdiomasDialog = false }) {
            androidx.compose.material3.Card(shape = RoundedCornerShape(16.dp)) {
                Column(Modifier.padding(16.dp)) {
                    Text("Idiomas", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                    Spacer(Modifier.height(8.dp))
                    androidx.compose.foundation.lazy.LazyColumn(Modifier.heightIn(max = 320.dp)) {
                        items(EU_LANGUAGES.size) { i ->
                            val lang = EU_LANGUAGES[i]
                            val sel = lang in idiomasSeleccionados
                            Row(
                                Modifier
                                    .fillMaxWidth()
                                    .clickable {
                                        idiomasSeleccionados = if (sel) idiomasSeleccionados - lang
                                        else idiomasSeleccionados + lang
                                    }
                                    .padding(vertical = 10.dp, horizontal = 4.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text(lang, style = MaterialTheme.typography.bodyMedium, color = KddTextPrimary)
                                if (sel) Icon(Icons.Filled.Check, contentDescription = null, tint = KddPurple, modifier = Modifier.size(18.dp))
                            }
                            if (i < EU_LANGUAGES.size - 1) HorizontalDivider(color = KddDivider)
                        }
                    }
                    Spacer(Modifier.height(8.dp))
                    TextButton(onClick = { showIdiomasDialog = false }, Modifier.align(Alignment.End)) {
                        Text("Listo", color = KddPurple)
                    }
                }
            }
        }
    }

    // Date Picker Desde
    if (showDateDesdeDialog) {
        val datePickerState = rememberDatePickerState(
            initialSelectedDateMillis = fechaDesde?.atStartOfDay(java.time.ZoneId.systemDefault())?.toInstant()?.toEpochMilli()
        )
        DatePickerDialog(
            onDismissRequest = { showDateDesdeDialog = false },
            confirmButton = {
                TextButton(onClick = {
                    datePickerState.selectedDateMillis?.let { millis ->
                        fechaDesde = java.time.Instant.ofEpochMilli(millis).atZone(java.time.ZoneId.systemDefault()).toLocalDate()
                    }
                    showDateDesdeDialog = false
                }) { Text("OK", color = KddPurple) }
            },
            dismissButton = { TextButton(onClick = { showDateDesdeDialog = false }) { Text("Cancelar") } }
        ) { DatePicker(state = datePickerState) }
    }

    // Time Picker Desde
    if (showTimeDesdeDialog) {
        val timePickerState = rememberTimePickerState(
            initialHour = horaDesde?.hour ?: 12,
            initialMinute = horaDesde?.minute ?: 0,
            is24Hour = true
        )
        TimePickerDialogKdd(
            onDismiss = { showTimeDesdeDialog = false },
            onConfirm = {
                horaDesde = LocalTime.of(timePickerState.hour, timePickerState.minute)
                showTimeDesdeDialog = false
            }
        ) { TimePicker(state = timePickerState) }
    }

    // Date Picker Hasta
    if (showDateHastaDialog) {
        val datePickerState = rememberDatePickerState(
            initialSelectedDateMillis = fechaHasta?.atStartOfDay(java.time.ZoneId.systemDefault())?.toInstant()?.toEpochMilli()
        )
        DatePickerDialog(
            onDismissRequest = { showDateHastaDialog = false },
            confirmButton = {
                TextButton(onClick = {
                    datePickerState.selectedDateMillis?.let { millis ->
                        val sel = java.time.Instant.ofEpochMilli(millis).atZone(java.time.ZoneId.systemDefault()).toLocalDate()
                        if (fechaDesde != null && sel.isBefore(fechaDesde)) {
                            errorMsg = "La fecha de fin debe ser posterior a la fecha de inicio"
                        } else {
                            fechaHasta = sel
                        }
                    }
                    showDateHastaDialog = false
                }) { Text("OK", color = KddPurple) }
            },
            dismissButton = { TextButton(onClick = { showDateHastaDialog = false }) { Text("Cancelar") } }
        ) { DatePicker(state = datePickerState) }
    }

    // Time Picker Hasta
    if (showTimeHastaDialog) {
        val timePickerState = rememberTimePickerState(
            initialHour = horaHasta?.hour ?: 12,
            initialMinute = horaHasta?.minute ?: 0,
            is24Hour = true
        )
        TimePickerDialogKdd(
            onDismiss = { showTimeHastaDialog = false },
            onConfirm = {
                val candidata = LocalTime.of(timePickerState.hour, timePickerState.minute)
                if (fechaHasta != null && fechaDesde != null && fechaHasta == fechaDesde
                    && horaDesde != null && !candidata.isAfter(horaDesde)) {
                    errorMsg = "La hora de fin debe ser posterior a la hora de inicio"
                } else {
                    horaHasta = candidata
                }
                showTimeHastaDialog = false
            }
        ) { TimePicker(state = timePickerState) }
    }

    // Location Picker
    if (showLocationPicker) {
        val locationPermissionGranted = remember {
            ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED
        }
        val permissionLauncher = rememberLauncherForActivityResult(
            ActivityResultContracts.RequestPermission()
        ) { /* el mapa se actualizará solo */ }

        LaunchedEffect(Unit) {
            if (!locationPermissionGranted) {
                permissionLauncher.launch(Manifest.permission.ACCESS_FINE_LOCATION)
            }
        }

        // Determinar punto de inicio del mapa
        val initialPosition = remember { selectedLatLng ?: LatLng(40.4168, -3.7038) }
        val pickerCamera = rememberCameraPositionState {
            position = CameraPosition.fromLatLngZoom(initialPosition, 14f)
        }
        var tempNombre by remember { mutableStateOf(ubicacionNombre) }

        LaunchedEffect(Unit) {
            if (selectedLatLng == null && locationPermissionGranted) {
                try {
                    val fusedLocation = LocationServices.getFusedLocationProviderClient(context)
                    val location = fusedLocation.lastLocation.await()
                    if (location != null) {
                        pickerCamera.animate(CameraUpdateFactory.newLatLngZoom(LatLng(location.latitude, location.longitude), 14f))
                    }
                } catch (_: Exception) { }
            }
        }

        LaunchedEffect(pickerCamera.isMoving) {
            if (!pickerCamera.isMoving) {
                val target = pickerCamera.position.target
                val nombre = withContext(Dispatchers.IO) {
                    try {
                        @Suppress("DEPRECATION")
                        val addresses = Geocoder(context, Locale.getDefault()).getFromLocation(target.latitude, target.longitude, 1)
                        if (!addresses.isNullOrEmpty()) {
                            val addr = addresses[0]
                            when {
                                addr.featureName != null && !addr.featureName.matches(Regex("\\d+")) ->
                                    "${addr.featureName}${if (addr.locality != null) ", ${addr.locality}" else ""}"
                                addr.thoroughfare != null ->
                                    "${addr.thoroughfare}${if (addr.locality != null) ", ${addr.locality}" else ""}"
                                addr.locality != null -> addr.locality
                                else -> null
                            }
                        } else null
                    } catch (_: Exception) { null }
                }
                if (nombre != null) tempNombre = nombre
            }
        }

        Dialog(
            onDismissRequest = { showLocationPicker = false },
            properties = DialogProperties(usePlatformDefaultWidth = false, decorFitsSystemWindows = false)
        ) {
            Box(modifier = Modifier.fillMaxSize()) {
                GoogleMap(
                    modifier = Modifier.fillMaxSize(),
                    cameraPositionState = pickerCamera,
                    properties = MapProperties(isMyLocationEnabled = locationPermissionGranted),
                    uiSettings = MapUiSettings(
                        zoomControlsEnabled = false,
                        myLocationButtonEnabled = locationPermissionGranted
                    ),
                    onMapClick = { latLng ->
                        coroutineScope.launch {
                            pickerCamera.animate(CameraUpdateFactory.newLatLng(latLng))
                        }
                    }
                ) {
                    Marker(
                        state = rememberMarkerState(position = pickerCamera.position.target),
                        title = "Ubicación seleccionada"
                    )
                }

                Icon(
                    imageVector = Icons.Filled.LocationOn,
                    contentDescription = null,
                    tint = KddPurple,
                    modifier = Modifier.size(40.dp).align(Alignment.Center).offset(y = (-20).dp)
                )

                Card(
                    modifier = Modifier.fillMaxWidth().align(Alignment.BottomCenter).padding(12.dp),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    elevation = CardDefaults.cardElevation(8.dp)
                ) {
                    Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                        Text("Arrastra el mapa para posicionar el marcador", style = MaterialTheme.typography.bodySmall, color = KddTextSecondary)
                        OutlinedTextField(
                            value = tempNombre,
                            onValueChange = { tempNombre = it },
                            placeholder = { Text("Nombre del lugar (opcional)", color = KddTextHint) },
                            modifier = Modifier.fillMaxWidth(),
                            singleLine = true,
                            shape = RoundedCornerShape(10.dp),
                            colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = KddPurple, unfocusedBorderColor = KddDivider)
                        )
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            OutlinedButton(
                                onClick = { showLocationPicker = false },
                                modifier = Modifier.weight(1f),
                                shape = RoundedCornerShape(10.dp)
                            ) { Text("Cancelar") }
                            Button(
                                onClick = {
                                    selectedLatLng = pickerCamera.position.target
                                    ubicacionNombre = tempNombre
                                    showLocationPicker = false
                                },
                                modifier = Modifier.weight(1f),
                                shape = RoundedCornerShape(10.dp),
                                colors = ButtonDefaults.buttonColors(containerColor = KddPurple)
                            ) { Text("Confirmar", color = Color.White) }
                        }
                    }
                }
            }
        }
    }
}
