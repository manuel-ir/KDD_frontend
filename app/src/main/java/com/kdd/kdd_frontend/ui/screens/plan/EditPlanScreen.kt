package com.kdd.kdd_frontend.ui.screens.plan

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.lifecycle.viewmodel.compose.viewModel
import com.kdd.kdd_frontend.network.dto.CrearPlanDto
import com.kdd.kdd_frontend.ui.theme.*
import com.kdd.kdd_frontend.viewmodel.PlanDetalleState
import com.kdd.kdd_frontend.viewmodel.PlanViewModel
import java.time.LocalDate
import java.time.LocalTime
import java.time.format.DateTimeFormatter

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditPlanScreen(
    planId: Long,
    onNavigateBack: () -> Unit,
    onPlanEditado: () -> Unit
) {
    val viewModel: PlanViewModel = viewModel()
    val detalleState by viewModel.detalleState.collectAsState()

    var cargado by remember { mutableStateOf(false) }
    var cargando by remember { mutableStateOf(false) }
    var errorMsg by remember { mutableStateOf("") }
    val snackbarHostState = remember { SnackbarHostState() }

    // Campos del formulario
    var titulo by remember { mutableStateOf("") }
    var descripcion by remember { mutableStateOf("") }
    var categoria by remember { mutableStateOf("") }
    var categoriaPersonalizada by remember { mutableStateOf("") }
    var ubicacionTexto by remember { mutableStateOf("") }
    var idioma by remember { mutableStateOf("") }
    var edadMin by remember { mutableFloatStateOf(18f) }
    var edadMax by remember { mutableFloatStateOf(55f) }
    var maxPersonas by remember { mutableFloatStateOf(10f) }
    var fechaEvento by remember { mutableStateOf<LocalDate?>(null) }
    var horaEvento by remember { mutableStateOf<LocalTime?>(null) }

    var showCategoriasDialog by remember { mutableStateOf(false) }
    var showDateDialog by remember { mutableStateOf(false) }
    var showTimeDialog by remember { mutableStateOf(false) }

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
            ubicacionTexto = plan.ubicacionTexto ?: ""
            idioma = plan.idioma ?: ""
            edadMin = (plan.edadMin ?: 18).toFloat()
            edadMax = (plan.edadMax ?: 55).toFloat()
            maxPersonas = (plan.numMaxPersonas ?: 10).toFloat()
            fechaEvento = plan.fechaEvento?.let {
                runCatching { LocalDate.parse(it) }.getOrNull()
            }
            horaEvento = plan.horaEvento?.let {
                runCatching { LocalTime.parse(it.take(5), timeFormatter) }.getOrNull()
            }
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
                    // Barra superior
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
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Spacer(Modifier.height(4.dp))

                        // Título
                        OutlinedTextField(
                            value = titulo,
                            onValueChange = { if (it.length <= 60) titulo = it },
                            label = { Text("Título del plan") },
                            modifier = Modifier.fillMaxWidth(),
                            singleLine = true,
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = KddPurple,
                                focusedLabelColor = KddPurple
                            )
                        )

                        // Descripción
                        OutlinedTextField(
                            value = descripcion,
                            onValueChange = { if (it.length <= 300) descripcion = it },
                            label = { Text("Descripción") },
                            modifier = Modifier.fillMaxWidth().height(100.dp),
                            maxLines = 5,
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = KddPurple,
                                focusedLabelColor = KddPurple
                            )
                        )

                        // Categoría
                        OutlinedButton(
                            onClick = { showCategoriasDialog = true },
                            modifier = Modifier.fillMaxWidth(),
                            colors = ButtonDefaults.outlinedButtonColors(
                                contentColor = if (categoria.isNotBlank()) KddPurple else KddTextHint
                            )
                        ) {
                            Text(if (categoria.isBlank()) "Selecciona una categoría" else categoria)
                            Spacer(Modifier.weight(1f))
                            Icon(Icons.Filled.KeyboardArrowDown, contentDescription = null, Modifier.size(16.dp))
                        }
                        if (categoria == "Personalizada") {
                            OutlinedTextField(
                                value = categoriaPersonalizada,
                                onValueChange = { if (it.length <= 20) categoriaPersonalizada = it },
                                label = { Text("Nombre de la categoría") },
                                modifier = Modifier.fillMaxWidth(),
                                singleLine = true,
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedBorderColor = KddPurple,
                                    focusedLabelColor = KddPurple
                                )
                            )
                        }

                        // Fecha
                        OutlinedButton(
                            onClick = { showDateDialog = true },
                            modifier = Modifier.fillMaxWidth(),
                            colors = ButtonDefaults.outlinedButtonColors(
                                contentColor = if (fechaEvento != null) KddPurple else KddTextHint
                            )
                        ) {
                            Icon(Icons.Filled.CalendarMonth, contentDescription = null, Modifier.size(16.dp))
                            Spacer(Modifier.width(8.dp))
                            Text(fechaEvento?.format(dateFormatter) ?: "Fecha del evento")
                            Spacer(Modifier.weight(1f))
                        }

                        // Hora
                        OutlinedButton(
                            onClick = { showTimeDialog = true },
                            modifier = Modifier.fillMaxWidth(),
                            colors = ButtonDefaults.outlinedButtonColors(
                                contentColor = if (horaEvento != null) KddPurple else KddTextHint
                            )
                        ) {
                            Icon(Icons.Filled.Schedule, contentDescription = null, Modifier.size(16.dp))
                            Spacer(Modifier.width(8.dp))
                            Text(horaEvento?.format(timeFormatter) ?: "Hora del evento")
                            Spacer(Modifier.weight(1f))
                        }

                        // Ubicación
                        OutlinedTextField(
                            value = ubicacionTexto,
                            onValueChange = { ubicacionTexto = it },
                            label = { Text("Ubicación") },
                            modifier = Modifier.fillMaxWidth(),
                            singleLine = true,
                            leadingIcon = { Icon(Icons.Filled.LocationOn, contentDescription = null) },
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = KddPurple,
                                focusedLabelColor = KddPurple
                            )
                        )

                        // Idioma
                        OutlinedTextField(
                            value = idioma,
                            onValueChange = { idioma = it },
                            label = { Text("Idioma") },
                            modifier = Modifier.fillMaxWidth(),
                            singleLine = true,
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = KddPurple,
                                focusedLabelColor = KddPurple
                            )
                        )

                        // Rango de edad
                        Card(
                            colors = CardDefaults.cardColors(containerColor = KddSurface),
                            shape = RoundedCornerShape(12.dp)
                        ) {
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
                        Card(
                            colors = CardDefaults.cardColors(containerColor = KddSurface),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Column(Modifier.padding(16.dp)) {
                                Text("Máximo de personas: ${maxPersonas.toInt()}", style = MaterialTheme.typography.titleMedium, color = KddTextSecondary)
                                Slider(
                                    value = maxPersonas,
                                    onValueChange = { maxPersonas = it },
                                    valueRange = 2f..50f,
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
                                            descripcion = descripcion.trim().ifBlank { null },
                                            categoria = categoriaFinal,
                                            fechaEvento = fechaEvento?.format(apiDateFormatter),
                                            horaEvento = horaEvento?.format(DateTimeFormatter.ofPattern("HH:mm:ss")),
                                            ubicacionTexto = ubicacionTexto.trim().ifBlank { null },
                                            latitud = null,
                                            longitud = null,
                                            edadMin = edadMin.toInt(),
                                            edadMax = edadMax.toInt(),
                                            numMaxPersonas = maxPersonas.toInt(),
                                            idioma = idioma.trim().ifBlank { null },
                                            comunidadId = null
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

    // Diálogo categorías
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

    // Date Picker
    if (showDateDialog) {
        val datePickerState = rememberDatePickerState(
            initialSelectedDateMillis = fechaEvento
                ?.atStartOfDay(java.time.ZoneId.systemDefault())?.toInstant()?.toEpochMilli()
        )
        DatePickerDialog(
            onDismissRequest = { showDateDialog = false },
            confirmButton = {
                TextButton(onClick = {
                    datePickerState.selectedDateMillis?.let { millis ->
                        fechaEvento = java.time.Instant.ofEpochMilli(millis)
                            .atZone(java.time.ZoneId.systemDefault()).toLocalDate()
                    }
                    showDateDialog = false
                }) { Text("OK", color = KddPurple) }
            },
            dismissButton = {
                TextButton(onClick = { showDateDialog = false }) { Text("Cancelar", color = KddTextHint) }
            }
        ) { DatePicker(state = datePickerState) }
    }

    // Time Picker
    if (showTimeDialog) {
        val timePickerState = rememberTimePickerState(
            initialHour = horaEvento?.hour ?: 12,
            initialMinute = horaEvento?.minute ?: 0
        )
        Dialog(onDismissRequest = { showTimeDialog = false }, properties = DialogProperties()) {
            Card(shape = RoundedCornerShape(16.dp)) {
                Column(Modifier.padding(16.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                    TimePicker(state = timePickerState)
                    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
                        TextButton(onClick = { showTimeDialog = false }) { Text("Cancelar", color = KddTextHint) }
                        TextButton(onClick = {
                            horaEvento = LocalTime.of(timePickerState.hour, timePickerState.minute)
                            showTimeDialog = false
                        }) { Text("OK", color = KddPurple) }
                    }
                }
            }
        }
    }
}
