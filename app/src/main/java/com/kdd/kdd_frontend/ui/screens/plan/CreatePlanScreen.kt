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
import com.kdd.kdd_frontend.ui.theme.*
import java.time.LocalDate
import java.time.LocalTime
import java.time.format.DateTimeFormatter

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
    onPlanCreated: () -> Unit
) {
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

    val dateFormatter = DateTimeFormatter.ofPattern("dd/MM/yy")
    val timeFormatter = DateTimeFormatter.ofPattern("HH:mm")

    Column(modifier = Modifier.fillMaxSize().background(Color.White)) {
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
                onClick = { /* TODO: seleccionar ubicación */ },
                colors = ButtonDefaults.buttonColors(containerColor = KddPurple),
                shape = RoundedCornerShape(20.dp),
                modifier = Modifier.height(42.dp)
            ) {
                Text("Elige una ubicación", color = Color.White)
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
            Button(
                onClick = onPlanCreated,
                modifier = Modifier.fillMaxWidth().height(52.dp),
                shape = RoundedCornerShape(26.dp),
                colors = ButtonDefaults.buttonColors(containerColor = KddPurple),
                enabled = titulo.isNotBlank() && categoria.isNotBlank() &&
                        (categoria != "Personalizada" || categoriaPersonalizada.isNotBlank())
            ) {
                Text("Terminar", color = Color.White, fontWeight = FontWeight.SemiBold, fontSize = 16.sp)
            }
        }
    }

    // ─── Date Picker: Desde ───
    if (showDateDesdeDialog) {
        val dateState = rememberDatePickerState()
        DatePickerDialog(
            onDismissRequest = { showDateDesdeDialog = false },
            confirmButton = {
                TextButton(onClick = {
                    dateState.selectedDateMillis?.let {
                        fechaDesde = LocalDate.ofEpochDay(it / 86400000)
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
                        fechaHasta = LocalDate.ofEpochDay(it / 86400000)
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
                horaHasta = LocalTime.of(timeState.hour, timeState.minute)
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
        Card(shape = RoundedCornerShape(16.dp), colors = CardDefaults.cardColors(containerColor = Color.White)) {
            Column(modifier = Modifier.padding(20.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                Text("Selecciona la hora", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                Spacer(modifier = Modifier.height(16.dp))
                content()
                Spacer(modifier = Modifier.height(16.dp))
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
                    TextButton(onClick = onDismiss) { Text("Cancelar") }
                    Spacer(modifier = Modifier.width(8.dp))
                    Button(onClick = onConfirm, colors = ButtonDefaults.buttonColors(containerColor = KddPurple), shape = RoundedCornerShape(10.dp)) { Text("Aceptar") }
                }
            }
        }
    }
}

@Composable
private fun FormField(
    label: String, maxChars: Int, value: String, onValueChange: (String) -> Unit,
    placeholder: String, singleLine: Boolean = true, minLines: Int = 1
) {
    Column {
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
            Text(text = label, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
            Text(text = "${value.length}/$maxChars", style = MaterialTheme.typography.bodySmall, color = KddTextHint)
        }
        Spacer(modifier = Modifier.height(6.dp))
        OutlinedTextField(
            value = value, onValueChange = onValueChange,
            placeholder = { Text(placeholder, color = KddTextHint) },
            modifier = Modifier.fillMaxWidth(), singleLine = singleLine, minLines = minLines,
            shape = RoundedCornerShape(10.dp),
            colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = KddPurple, unfocusedBorderColor = KddDivider)
        )
    }
}

@Composable
private fun SliderField(
    label: String, sublabel: String, value: Float, onValueChange: (Float) -> Unit,
    valueRange: ClosedFloatingPointRange<Float>, steps: Int = 0
) {
    Card(colors = CardDefaults.cardColors(containerColor = KddSurface), shape = RoundedCornerShape(12.dp)) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(text = label, style = MaterialTheme.typography.titleMedium, color = KddTextSecondary)
            Text(text = sublabel, style = MaterialTheme.typography.bodySmall, color = KddTextHint)
            Slider(
                value = value, onValueChange = onValueChange, valueRange = valueRange, steps = steps,
                colors = SliderDefaults.colors(thumbColor = KddPurple, activeTrackColor = KddPurple)
            )
        }
    }
}
