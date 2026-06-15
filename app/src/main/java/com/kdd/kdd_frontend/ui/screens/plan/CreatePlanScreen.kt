package com.kdd.kdd_frontend.ui.screens.plan

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.lifecycle.viewmodel.compose.viewModel
import com.kdd.kdd_frontend.network.dto.CrearPlanDto
import com.kdd.kdd_frontend.ui.theme.*
import com.kdd.kdd_frontend.viewmodel.PlanViewModel
import java.time.LocalDate
import java.time.LocalTime
import java.time.format.DateTimeFormatter
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.window.DialogProperties
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.model.CameraPosition
import com.google.android.gms.maps.model.LatLng
import com.google.maps.android.compose.*
import android.location.Geocoder
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.util.Locale
import kotlinx.coroutines.launch

val CATEGORIAS_PREDEFINIDAS = listOf(
    "Deportes", "Naturaleza", "Fiesta", "Música", "Arte y Cultura",
    "Gastronomía", "Viajes", "Tecnología", "Cine y Series", "Fotografía",
    "Juegos", "Lectura", "Idiomas", "Voluntariado", "Personalizada"
)

val EU_LANGUAGES = listOf(
    "Español", "Inglés", "Francés", "Alemán", "Italiano",
    "Portugués", "Neerlandés", "Polaco", "Rumano", "Sueco",
    "Checo", "Húngaro", "Griego", "Búlgaro", "Danés",
    "Finlandés", "Eslovaco", "Croata", "Lituano", "Letón"
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CreatePlanScreen(
    onNavigateBack: () -> Unit,
    onPlanCreated: () -> Unit,
    communityId: Long = -1L
) {
    val viewModel: PlanViewModel = viewModel()
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    var cargando by remember { mutableStateOf(false) }
    var errorMsg by remember { mutableStateOf("") }
    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(errorMsg) {
        if (errorMsg.isNotBlank()) {
            snackbarHostState.showSnackbar(errorMsg)
            errorMsg = ""
        }
    }

    var titulo by remember { mutableStateOf("") }
    var categoria by remember { mutableStateOf("") }
    var categoriaPersonalizada by remember { mutableStateOf("") }
    var descripcion by remember { mutableStateOf("") }
    var vasAcompanado by remember { mutableFloatStateOf(1f) }
    var maxAcompanantes by remember { mutableFloatStateOf(5f) }
    var idiomasSeleccionados by remember { mutableStateOf(setOf<String>()) }
    var edadMin by remember { mutableFloatStateOf(18f) }
    var edadMax by remember { mutableFloatStateOf(55f) }

    // Fechas y horas
    var fechaDesde by remember { mutableStateOf<LocalDate?>(null) }
    var horaDesde by remember { mutableStateOf<LocalTime?>(null) }
    var hastaHabilitado by remember { mutableStateOf(false) }
    var fechaHasta by remember { mutableStateOf<LocalDate?>(null) }
    var horaHasta by remember { mutableStateOf<LocalTime?>(null) }

    // Dialogs de fecha/hora
    var showDateDesdeDialog by remember { mutableStateOf(false) }
    var showTimeDesdeDialog by remember { mutableStateOf(false) }
    var showDateHastaDialog by remember { mutableStateOf(false) }
    var showTimeHastaDialog by remember { mutableStateOf(false) }
    var showIdiomasDialog by remember { mutableStateOf(false) }
    var showCategoriasDialog by remember { mutableStateOf(false) }
    var showLocationPicker by remember { mutableStateOf(false) }
    var selectedLatLng by remember { mutableStateOf<LatLng?>(null) }
    var ubicacionNombre by remember { mutableStateOf("") }

    val dateFormatter = DateTimeFormatter.ofPattern("dd/MM/yy")
    val timeFormatter = DateTimeFormatter.ofPattern("HH:mm")

    Scaffold(snackbarHost = { SnackbarHost(snackbarHostState) }) { innerPadding ->
    Column(modifier = Modifier.fillMaxSize().background(Color.White).padding(innerPadding)) {
        // Barra superior
        Row(
            modifier = Modifier.fillMaxWidth().statusBarsPadding().padding(horizontal = 8.dp, vertical = 4.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = onNavigateBack) {
                Icon(Icons.Filled.Close, contentDescription = "Cerrar", tint = KddTextPrimary)
            }
            Text("Crear actividad", style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Bold, modifier = Modifier.padding(start = 8.dp))
        }

        HorizontalDivider()

        Column(
            modifier = Modifier.weight(1f).verticalScroll(rememberScrollState()).padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(20.dp)
        ) {
            FormField(label = "Título", maxChars = 25, value = titulo, onValueChange = { if (it.length <= 25) titulo = it }, placeholder = "¿Cómo se llama tu actividad?")

            // Selector de categoría
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text("Categoría", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold, color = KddTextPrimary)
                OutlinedButton(
                    onClick = { showCategoriasDialog = true },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(10.dp),
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = if (categoria.isNotBlank()) KddPurple else KddTextHint),
                    border = androidx.compose.foundation.BorderStroke(1.dp, if (categoria.isNotBlank()) KddPurple else KddDivider)
                ) {
                    Icon(Icons.Filled.Category, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        if (categoria.isBlank()) "Selecciona una categoría" else categoria,
                        modifier = Modifier.weight(1f)
                    )
                    Icon(Icons.Filled.KeyboardArrowDown, contentDescription = null, modifier = Modifier.size(16.dp))
                }
                if (categoria == "Personalizada") {
                    OutlinedTextField(
                        value = categoriaPersonalizada,
                        onValueChange = { if (it.length <= 20) categoriaPersonalizada = it },
                        placeholder = { Text("Escribe tu categoría...", color = KddTextHint) },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true,
                        shape = RoundedCornerShape(10.dp),
                        colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = KddPurple, unfocusedBorderColor = KddDivider),
                        supportingText = { Text("${categoriaPersonalizada.length}/20", color = KddTextHint) }
                    )
                }
            }

            FormField(label = "Descripción", maxChars = 300, value = descripcion, onValueChange = { if (it.length <= 300) descripcion = it }, placeholder = "Describe a qué invitas a los amigos", singleLine = false, minLines = 3)

            // ¿Cuándo?
            Text("¿Cuándo?", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = KddTextPrimary)

            // Desde
            Column {
                Text("Desde", style = MaterialTheme.typography.bodySmall, color = KddTextSecondary)
                Spacer(modifier = Modifier.height(6.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Button(
                        onClick = { showDateDesdeDialog = true },
                        modifier = Modifier.weight(1f),
                        colors = ButtonDefaults.buttonColors(containerColor = KddPurple),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Icon(Icons.Filled.CalendarToday, contentDescription = null, modifier = Modifier.size(14.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(fechaDesde?.format(dateFormatter) ?: "DD/MM/AA", color = Color.White, fontSize = 13.sp)
                    }
                    Button(
                        onClick = { showTimeDesdeDialog = true },
                        modifier = Modifier.weight(1f),
                        colors = ButtonDefaults.buttonColors(containerColor = KddPurple),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Icon(Icons.Filled.Schedule, contentDescription = null, modifier = Modifier.size(14.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(horaDesde?.format(timeFormatter) ?: "--:--", color = Color.White, fontSize = 13.sp)
                    }
                }
            }

            // Hasta (con checkbox)
            Column {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Checkbox(
                        checked = hastaHabilitado,
                        onCheckedChange = { hastaHabilitado = it },
                        colors = CheckboxDefaults.colors(checkedColor = KddPurple)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Hasta (opcional)", style = MaterialTheme.typography.bodyMedium, color = KddTextSecondary)
                }
                Spacer(modifier = Modifier.height(6.dp))
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
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(fechaHasta?.format(dateFormatter) ?: "DD/MM/AA", color = Color.White, fontSize = 13.sp)
                    }
                    Button(
                        onClick = { if (hastaHabilitado) showTimeHastaDialog = true },
                        modifier = Modifier.weight(1f),
                        colors = ButtonDefaults.buttonColors(containerColor = KddPurpleLight),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Icon(Icons.Filled.Schedule, contentDescription = null, modifier = Modifier.size(14.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(horaHasta?.format(timeFormatter) ?: "--:--", color = Color.White, fontSize = 13.sp)
                    }
                }
            }

            // ¿Dónde?
            Text("¿Dónde?", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = KddTextPrimary)
            Button(
                onClick = { showLocationPicker = true },
                colors = ButtonDefaults.buttonColors(containerColor = KddPurple),
                shape = RoundedCornerShape(20.dp),
                modifier = Modifier.height(42.dp)
            ) {
                Text(
                    if (selectedLatLng != null && ubicacionNombre.isNotBlank()) ubicacionNombre
                    else if (selectedLatLng != null) "Ubicación seleccionada ✓"
                    else "Elige una ubicación",
                    color = Color.White
                )
            }

            // Foto
            Text("Personalizar", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = KddTextPrimary)
            Box(
                modifier = Modifier.fillMaxWidth().height(100.dp).clip(RoundedCornerShape(12.dp)).border(1.dp, KddPurple.copy(alpha = 0.4f), RoundedCornerShape(12.dp)).background(Color.White),
                contentAlignment = Alignment.Center
            ) {
                TextButton(onClick = { /* TODO: seleccionar foto */ }) {
                    Text("+ Añade tu propia foto", color = KddPurple)
                }
            }

            HorizontalDivider()

            // Detalles
            Text("Detalles", style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Bold, color = KddTextPrimary)

            SliderField(
                label = "¿Vas acompañado?",
                sublabel = "${vasAcompanado.toInt()} persona${if (vasAcompanado > 1) "s" else ""} (incluyéndote)",
                value = vasAcompanado, onValueChange = { vasAcompanado = it },
                valueRange = 1f..10f, steps = 8
            )

            SliderField(
                label = "Máximo de acompañantes",
                sublabel = "${maxAcompanantes.toInt()} personas (incluyéndote)",
                value = maxAcompanantes, onValueChange = { maxAcompanantes = it },
                valueRange = 1f..50f
            )

            // Selector de idiomas
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text("Idiomas", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold, color = KddTextPrimary)
                OutlinedButton(
                    onClick = { showIdiomasDialog = true },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(10.dp),
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = KddPurple)
                ) {
                    Icon(Icons.Filled.Language, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        if (idiomasSeleccionados.isEmpty()) "Selecciona idiomas"
                        else idiomasSeleccionados.joinToString(", "),
                        modifier = Modifier.weight(1f)
                    )
                    Icon(Icons.Filled.KeyboardArrowDown, contentDescription = null, modifier = Modifier.size(16.dp))
                }
            }

            // Rango de edad
            Card(colors = CardDefaults.cardColors(containerColor = KddSurface), shape = RoundedCornerShape(12.dp)) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text("Rango de edad", style = MaterialTheme.typography.titleMedium, color = KddTextSecondary)
                    Spacer(modifier = Modifier.height(8.dp))
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        Text(edadMin.toInt().toString(), color = KddTextPrimary, fontWeight = FontWeight.Medium)
                        Text(edadMax.toInt().toString(), color = KddTextPrimary, fontWeight = FontWeight.Medium)
                    }
                    RangeSlider(
                        value = edadMin..edadMax,
                        onValueChange = { range -> edadMin = range.start; edadMax = range.endInclusive },
                        valueRange = 18f..80f,
                        colors = SliderDefaults.colors(thumbColor = KddPurple, activeTrackColor = KddPurple)
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))
        }

        // Botón terminar
        Box(modifier = Modifier.padding(16.dp)) {
            val categoriaFinal = if (categoria == "Personalizada") categoriaPersonalizada.trim() else categoria
            val formValido = titulo.isNotBlank() && categoria.isNotBlank() &&
                    (categoria != "Personalizada" || categoriaPersonalizada.isNotBlank())
            Button(
                onClick = {
                    if (formValido && !cargando) {
                        if (fechaDesde != null && fechaDesde!!.isBefore(LocalDate.now())) {
                            errorMsg = "La fecha del evento debe ser posterior a hoy"
                        } else {
                            cargando = true
                            val apiFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd")
                            viewModel.crearPlan(
                                dto = CrearPlanDto(
                                    titulo = titulo.trim(),
                                    descripcion = descripcion.trim(),
                                    categoria = categoriaFinal,
                                    fechaEvento = fechaDesde?.format(apiFormatter),
                                    horaEvento = horaDesde?.format(DateTimeFormatter.ofPattern("HH:mm:ss")),
                                    ubicacionTexto = ubicacionNombre.ifBlank { null },
                                    latitud = selectedLatLng?.latitude,
                                    longitud = selectedLatLng?.longitude,
                                    edadMin = edadMin.toInt(),
                                    edadMax = edadMax.toInt(),
                                    numMaxPersonas = (vasAcompanado + maxAcompanantes).toInt(),
                                    idioma = idiomasSeleccionados.joinToString(", ").ifBlank { null }
                                ),
                                onSuccess = { onPlanCreated() },
                                onError = {
                                    cargando = false
                                    errorMsg = "Error al crear el plan. Comprueba los datos."
                                }
                            )
                        }
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
                    CircularProgressIndicator(modifier = Modifier.size(20.dp), color = Color.White, strokeWidth = 2.dp)
                } else {
                    Text("Terminar", color = Color.White, fontWeight = FontWeight.SemiBold, fontSize = 16.sp)
                }
            }
        }
    }
    } // cierre Scaffold

    // ─── Date Picker: Desde ───
    if (showDateDesdeDialog) {
        val dateState = rememberDatePickerState()
        DatePickerDialog(
            onDismissRequest = { showDateDesdeDialog = false },
            confirmButton = {
                TextButton(onClick = {
                    dateState.selectedDateMillis?.let {
                        val seleccionada = LocalDate.ofEpochDay(it / 86400000)
                        if (seleccionada.isBefore(LocalDate.now())) {
                            errorMsg = "La fecha del evento debe ser posterior a hoy"
                        } else {
                            fechaDesde = seleccionada
                            if (fechaHasta != null && fechaHasta!!.isBefore(seleccionada)) {
                                fechaHasta = null
                            }
                        }
                    }
                    showDateDesdeDialog = false
                }) { Text("Aceptar", color = KddPurple) }
            },
            dismissButton = { TextButton(onClick = { showDateDesdeDialog = false }) { Text("Cancelar") } }
        ) { DatePicker(state = dateState) }
    }

    // ─── Time Picker: Desde ───
    if (showTimeDesdeDialog) {
        val timeState = rememberTimePickerState(initialHour = 12, initialMinute = 0, is24Hour = true)
        TimePickerDialogKdd(
            onDismiss = { showTimeDesdeDialog = false },
            onConfirm = {
                horaDesde = LocalTime.of(timeState.hour, timeState.minute)
                showTimeDesdeDialog = false
            }
        ) { TimePicker(state = timeState) }
    }

    // ─── Date Picker: Hasta ───
    if (showDateHastaDialog) {
        val dateState = rememberDatePickerState()
        DatePickerDialog(
            onDismissRequest = { showDateHastaDialog = false },
            confirmButton = {
                TextButton(onClick = {
                    dateState.selectedDateMillis?.let {
                        val seleccionada = LocalDate.ofEpochDay(it / 86400000)
                        when {
                            seleccionada.isBefore(LocalDate.now()) ->
                                errorMsg = "La fecha de fin no puede ser anterior a hoy"
                            fechaDesde != null && seleccionada.isBefore(fechaDesde) ->
                                errorMsg = "La fecha de fin debe ser posterior a la fecha de inicio"
                            else -> fechaHasta = seleccionada
                        }
                    }
                    showDateHastaDialog = false
                }) { Text("Aceptar", color = KddPurple) }
            },
            dismissButton = { TextButton(onClick = { showDateHastaDialog = false }) { Text("Cancelar") } }
        ) { DatePicker(state = dateState) }
    }

    // ─── Time Picker: Hasta ───
    if (showTimeHastaDialog) {
        val timeState = rememberTimePickerState(initialHour = 12, initialMinute = 0, is24Hour = true)
        TimePickerDialogKdd(
            onDismiss = { showTimeHastaDialog = false },
            onConfirm = {
                val candidata = LocalTime.of(timeState.hour, timeState.minute)
                if (fechaHasta != null && fechaDesde != null && fechaHasta == fechaDesde
                    && horaDesde != null && !candidata.isAfter(horaDesde)) {
                    errorMsg = "La hora de fin debe ser posterior a la hora de inicio"
                } else {
                    horaHasta = candidata
                }
                showTimeHastaDialog = false
            }
        ) { TimePicker(state = timeState) }
    }

    // ─── Dialog: Categorías ───
    if (showCategoriasDialog) {
        Dialog(onDismissRequest = { showCategoriasDialog = false }) {
            Card(shape = RoundedCornerShape(16.dp), colors = CardDefaults.cardColors(containerColor = Color.White)) {
                Column(modifier = Modifier.padding(20.dp)) {
                    Text("Elige una categoría", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = KddTextPrimary)
                    Spacer(modifier = Modifier.height(12.dp))
                    LazyColumn(modifier = Modifier.heightIn(max = 360.dp)) {
                        items(CATEGORIAS_PREDEFINIDAS) { cat ->
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable {
                                        categoria = cat
                                        if (cat != "Personalizada") categoriaPersonalizada = ""
                                        showCategoriasDialog = false
                                    }
                                    .padding(vertical = 10.dp, horizontal = 4.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text(cat, style = MaterialTheme.typography.bodyMedium, color = KddTextPrimary)
                                if (categoria == cat) {
                                    Icon(Icons.Filled.Check, contentDescription = null, tint = KddPurple, modifier = Modifier.size(18.dp))
                                }
                            }
                            if (cat != CATEGORIAS_PREDEFINIDAS.last()) {
                                HorizontalDivider(color = KddDivider)
                            }
                        }
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                    TextButton(onClick = { showCategoriasDialog = false }, modifier = Modifier.align(Alignment.End)) {
                        Text("Cancelar", color = KddTextSecondary)
                    }
                }
            }
        }
    }

    // ─── Location Picker ───
    if (showLocationPicker) {
        val pickerCamera = rememberCameraPositionState {
            position = CameraPosition.fromLatLngZoom(
                selectedLatLng ?: LatLng(40.4168, -3.7038), 10f
            )
        }
        var tempNombre by remember { mutableStateOf(ubicacionNombre) }

        LaunchedEffect(pickerCamera.isMoving) {
            if (!pickerCamera.isMoving) {
                val target = pickerCamera.position.target
                val nombre = withContext(Dispatchers.IO) {
                    try {
                        @Suppress("DEPRECATION")
                        val addresses = Geocoder(context, Locale.getDefault())
                            .getFromLocation(target.latitude, target.longitude, 1)
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
                    uiSettings = MapUiSettings(zoomControlsEnabled = true, myLocationButtonEnabled = false),
                    onMapClick = { latLng ->
                        coroutineScope.launch {
                            pickerCamera.animate(CameraUpdateFactory.newLatLng(latLng))
                        }
                    }
                ) {
                    val target = pickerCamera.position.target
                    Marker(
                        state = rememberMarkerState(position = target),
                        title = "Ubicación seleccionada"
                    )
                }

                // Crosshair central
                Icon(
                    imageVector = Icons.Filled.LocationOn,
                    contentDescription = null,
                    tint = KddPurple,
                    modifier = Modifier.size(40.dp).align(Alignment.Center).offset(y = (-20).dp)
                )

                // Panel inferior
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

    // ─── Dialog: Idiomas ───
    if (showIdiomasDialog) {
        var tempSeleccion by remember { mutableStateOf(idiomasSeleccionados) }
        Dialog(onDismissRequest = { showIdiomasDialog = false }) {
            Card(shape = RoundedCornerShape(16.dp), colors = CardDefaults.cardColors(containerColor = Color.White)) {
                Column(modifier = Modifier.padding(20.dp)) {
                    Text("Selecciona idiomas", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                    Spacer(modifier = Modifier.height(12.dp))
                    LazyColumn(modifier = Modifier.heightIn(max = 300.dp)) {
                        items(EU_LANGUAGES) { idioma ->
                            Row(
                                modifier = Modifier.fillMaxWidth().clickable {
                                    tempSeleccion = if (tempSeleccion.contains(idioma))
                                        tempSeleccion - idioma else tempSeleccion + idioma
                                }.padding(vertical = 4.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Checkbox(
                                    checked = tempSeleccion.contains(idioma),
                                    onCheckedChange = {
                                        tempSeleccion = if (it) tempSeleccion + idioma else tempSeleccion - idioma
                                    },
                                    colors = CheckboxDefaults.colors(checkedColor = KddPurple)
                                )
                                Text(idioma, style = MaterialTheme.typography.bodyMedium, color = KddTextPrimary)
                            }
                        }
                    }
                    Spacer(modifier = Modifier.height(12.dp))
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
                        TextButton(onClick = { showIdiomasDialog = false }) { Text("Cancelar", color = KddTextSecondary) }
                        Spacer(modifier = Modifier.width(8.dp))
                        Button(
                            onClick = { idiomasSeleccionados = tempSeleccion; showIdiomasDialog = false },
                            colors = ButtonDefaults.buttonColors(containerColor = KddPurple),
                            shape = RoundedCornerShape(10.dp)
                        ) { Text("Aceptar") }
                    }
                }
            }
        }
    }
}

@Composable
private fun TimePickerDialogKdd(
    onDismiss: () -> Unit,
    onConfirm: () -> Unit,
    content: @Composable () -> Unit
) {
    Dialog(onDismissRequest = onDismiss) {
        Card(
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White)
        ) {
            Column(modifier = Modifier.padding(24.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                content()
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End
                ) {
                    TextButton(onClick = onDismiss) { Text("Cancelar") }
                    TextButton(onClick = onConfirm) { Text("Aceptar", color = KddPurple) }
                }
            }
        }
    }
}

@Composable
private fun FormField(
    label: String,
    value: String,
    onValueChange: (String) -> Unit,
    placeholder: String = "",
    maxChars: Int = Int.MAX_VALUE,
    singleLine: Boolean = true,
    minLines: Int = 1
) {
    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
        Text(label, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold, color = KddTextPrimary)
        OutlinedTextField(
            value = value,
            onValueChange = onValueChange,
            placeholder = { Text(placeholder, color = KddTextHint) },
            singleLine = singleLine,
            minLines = if (singleLine) 1 else minLines,
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(10.dp),
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = KddPurple,
                unfocusedBorderColor = KddDivider
            ),
            supportingText = if (maxChars < Int.MAX_VALUE) {
                { Text("${value.length}/$maxChars", style = MaterialTheme.typography.labelSmall, color = KddTextHint) }
            } else null
        )
    }
}

@Composable
private fun SliderField(
    label: String,
    sublabel: String,
    value: Float,
    onValueChange: (Float) -> Unit,
    valueRange: ClosedFloatingPointRange<Float> = 0f..100f,
    steps: Int = 0
) {
    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
        Text(label, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold, color = KddTextPrimary)
        Text(sublabel, style = MaterialTheme.typography.bodySmall, color = KddTextSecondary)
        Slider(
            value = value,
            onValueChange = onValueChange,
            valueRange = valueRange,
            steps = steps,
            colors = SliderDefaults.colors(thumbColor = KddPurple, activeTrackColor = KddPurple)
        )
    }
}
