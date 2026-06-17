package com.kdd.kdd_frontend.ui.screens.explore

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.activity.ComponentActivity
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.viewmodel.compose.viewModel
import com.kdd.kdd_frontend.ui.theme.*
import com.kdd.kdd_frontend.viewmodel.ComunidadViewModel
import com.kdd.kdd_frontend.viewmodel.PlanViewModel
import java.time.LocalDate
import java.time.format.DateTimeFormatter

/**
 * Pantalla de filtros para Explora y Comunidades.
 *
 * Los filtros seleccionados se guardan en el ViewModel correspondiente al pulsar Aplicar,
 * de modo que persisten al volver a la pantalla anterior.
 *
 * Explora: categoria, fecha, rango de edad.
 * Comunidades: ciudad, rango de edad.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FiltersScreen(
    onNavigateBack: () -> Unit,
    isCommunityFilter: Boolean = false
) {
    val activity = LocalContext.current as ComponentActivity
    val planViewModel: PlanViewModel = viewModel(activity)
    val comunidadViewModel: ComunidadViewModel = viewModel(activity)
    val categoriasDisponibles by planViewModel.categorias.collectAsState()

    // Inicializar estado local desde el ViewModel (para que los filtros activos se vean al abrir)
    var categoriaSeleccionada by remember {
        mutableStateOf(
            if (!isCommunityFilter) planViewModel.filtroCategoria.value
            else comunidadViewModel.filtroComCategoria.value
        )
    }
    var categoriaPersonalizada by remember { mutableStateOf("") }
    var mostrarCategoriasDialog by remember { mutableStateOf(false) }

    var fechaSeleccionada by remember {
        mutableStateOf(
            if (!isCommunityFilter) planViewModel.filtroFecha.value?.let {
                runCatching { LocalDate.parse(it) }.getOrNull()
            } else null
        )
    }
    var mostrarDatePicker by remember { mutableStateOf(false) }

    var edadMin by remember {
        mutableFloatStateOf(
            if (isCommunityFilter) comunidadViewModel.filtroComEdadMin.value.toFloat()
            else planViewModel.filtroEdadMin.value.toFloat()
        )
    }
    var edadMax by remember {
        mutableFloatStateOf(
            if (isCommunityFilter) comunidadViewModel.filtroComEdadMax.value.toFloat()
            else planViewModel.filtroEdadMax.value.toFloat()
        )
    }

    var ciudadTexto by remember {
        mutableStateOf(if (isCommunityFilter) comunidadViewModel.filtroComCiudad.value else "")
    }

    val dateFormatter = DateTimeFormatter.ofPattern("dd/MM/yyyy")
    val apiDateFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd")

    Column(modifier = Modifier.fillMaxSize()) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .statusBarsPadding()
                .padding(horizontal = 8.dp, vertical = 4.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = onNavigateBack) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                    contentDescription = "Volver",
                    tint = KddTextPrimary
                )
            }
            Text(
                text = "Filtros",
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(start = 8.dp)
            )
        }

        HorizontalDivider()

        Column(
            modifier = Modifier
                .weight(1f)
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Ciudad (solo comunidades)
            if (isCommunityFilter) {
                Card(colors = CardDefaults.cardColors(containerColor = KddSurface), shape = RoundedCornerShape(12.dp)) {
                    Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                        Text("Ciudad", style = MaterialTheme.typography.titleMedium, color = KddTextSecondary)
                        OutlinedTextField(
                            value = ciudadTexto,
                            onValueChange = { ciudadTexto = it },
                            placeholder = { Text("Ciudad o localidad", color = KddTextHint) },
                            modifier = Modifier.fillMaxWidth(),
                            singleLine = true,
                            shape = RoundedCornerShape(10.dp),
                            colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = KddPurple, unfocusedBorderColor = KddDivider)
                        )
                    }
                }
            }

            // Categoria (Explora y Comunidades)
            Card(colors = CardDefaults.cardColors(containerColor = KddSurface), shape = RoundedCornerShape(12.dp)) {
                Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text("Categoria", style = MaterialTheme.typography.titleMedium, color = KddTextSecondary)
                    OutlinedButton(
                        onClick = { mostrarCategoriasDialog = true },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(10.dp),
                        colors = ButtonDefaults.outlinedButtonColors(
                            contentColor = if (categoriaSeleccionada.isNotBlank()) KddPurple else KddTextHint
                        ),
                        border = androidx.compose.foundation.BorderStroke(
                            1.dp,
                            if (categoriaSeleccionada.isNotBlank()) KddPurple else KddDivider
                        )
                    ) {
                        Icon(Icons.Filled.Category, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = if (categoriaSeleccionada.isBlank()) "Todas las categorias"
                                   else if (categoriaSeleccionada == "Personalizada") categoriaPersonalizada.ifBlank { "Personalizada" }
                                   else categoriaSeleccionada,
                            modifier = Modifier.weight(1f)
                        )
                        Icon(Icons.Filled.KeyboardArrowDown, contentDescription = null, modifier = Modifier.size(16.dp))
                    }
                    if (categoriaSeleccionada == "Personalizada") {
                        OutlinedTextField(
                            value = categoriaPersonalizada,
                            onValueChange = { if (it.length <= 30) categoriaPersonalizada = it },
                            placeholder = { Text("Escribe la categoria...", color = KddTextHint) },
                            modifier = Modifier.fillMaxWidth(),
                            singleLine = true,
                            shape = RoundedCornerShape(10.dp),
                            colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = KddPurple, unfocusedBorderColor = KddDivider)
                        )
                    }
                    if (categoriaSeleccionada.isNotBlank()) {
                        TextButton(
                            onClick = { categoriaSeleccionada = ""; categoriaPersonalizada = "" },
                            contentPadding = PaddingValues(0.dp)
                        ) {
                            Text("Limpiar categoria", color = KddTextHint, style = MaterialTheme.typography.bodySmall)
                        }
                    }
                }
            }

            // Fecha (solo Explora)
            if (!isCommunityFilter) {
                Card(colors = CardDefaults.cardColors(containerColor = KddSurface), shape = RoundedCornerShape(12.dp)) {
                    Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                        Text("Fecha del evento", style = MaterialTheme.typography.titleMedium, color = KddTextSecondary)
                        OutlinedButton(
                            onClick = { mostrarDatePicker = true },
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(10.dp),
                            colors = ButtonDefaults.outlinedButtonColors(
                                contentColor = if (fechaSeleccionada != null) KddPurple else KddTextHint
                            ),
                            border = androidx.compose.foundation.BorderStroke(
                                1.dp,
                                if (fechaSeleccionada != null) KddPurple else KddDivider
                            )
                        ) {
                            Icon(Icons.Filled.CalendarToday, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = fechaSeleccionada?.format(dateFormatter) ?: "Cualquier fecha",
                                modifier = Modifier.weight(1f)
                            )
                            if (fechaSeleccionada != null) {
                                Icon(Icons.Filled.Close, contentDescription = "Quitar fecha",
                                    modifier = Modifier.size(16.dp), tint = KddTextHint)
                            } else {
                                Icon(Icons.Filled.KeyboardArrowDown, contentDescription = null, modifier = Modifier.size(16.dp))
                            }
                        }
                        if (fechaSeleccionada != null) {
                            TextButton(
                                onClick = { fechaSeleccionada = null },
                                contentPadding = PaddingValues(0.dp)
                            ) {
                                Text("Ver todos los dias", color = KddTextHint, style = MaterialTheme.typography.bodySmall)
                            }
                        }
                    }
                }
            }

            // Rango de edad
            Card(colors = CardDefaults.cardColors(containerColor = KddSurface), shape = RoundedCornerShape(12.dp)) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = if (isCommunityFilter) "Edad de la comunidad" else "Rango de edad del plan",
                        style = MaterialTheme.typography.titleMedium,
                        color = KddTextSecondary
                    )
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
        }

        // Boton Aplicar: guarda los filtros en el ViewModel
        Row(
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            OutlinedButton(
                onClick = {
                    if (isCommunityFilter) comunidadViewModel.limpiarFiltrosComunidades()
                    else planViewModel.limpiarFiltrosExplora()
                    onNavigateBack()
                },
                modifier = Modifier.height(52.dp),
                shape = RoundedCornerShape(26.dp),
                colors = ButtonDefaults.outlinedButtonColors(contentColor = KddTextSecondary),
                border = androidx.compose.foundation.BorderStroke(1.dp, KddDivider)
            ) {
                Text("Limpiar")
            }
            Button(
                onClick = {
                    if (isCommunityFilter) {
                        comunidadViewModel.aplicarFiltrosComunidades(
                            ciudad = ciudadTexto.trim(),
                            edadMin = edadMin.toInt(),
                            edadMax = edadMax.toInt(),
                            categoria = categoriaSeleccionada
                        )
                    } else {
                        val categoriaFinal = if (categoriaSeleccionada == "Personalizada") categoriaPersonalizada.trim()
                                            else categoriaSeleccionada
                        planViewModel.aplicarFiltrosExplora(
                            categoria = categoriaFinal,
                            fecha = fechaSeleccionada?.format(apiDateFormatter),
                            edadMin = edadMin.toInt(),
                            edadMax = edadMax.toInt()
                        )
                    }
                    onNavigateBack()
                },
                modifier = Modifier
                    .weight(1f)
                    .height(52.dp),
                shape = RoundedCornerShape(26.dp),
                colors = ButtonDefaults.buttonColors(containerColor = KddPurple)
            ) {
                Text("Aplicar", color = Color.White, fontWeight = FontWeight.SemiBold)
            }
        }
    }

    // Dialog de categorias
    if (mostrarCategoriasDialog) {
        Dialog(onDismissRequest = { mostrarCategoriasDialog = false }) {
            Card(shape = RoundedCornerShape(16.dp), colors = CardDefaults.cardColors(containerColor = Color.White)) {
                Column(modifier = Modifier.padding(20.dp)) {
                    Text("Elige una categoria", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = KddTextPrimary)
                    Spacer(modifier = Modifier.height(12.dp))
                    LazyColumn(modifier = Modifier.heightIn(max = 360.dp)) {
                        item {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable { categoriaSeleccionada = ""; categoriaPersonalizada = ""; mostrarCategoriasDialog = false }
                                    .padding(vertical = 10.dp, horizontal = 4.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text("Todas las categorias", style = MaterialTheme.typography.bodyMedium, color = KddTextSecondary)
                                if (categoriaSeleccionada.isBlank()) {
                                    Icon(Icons.Filled.Check, contentDescription = null, tint = KddPurple, modifier = Modifier.size(18.dp))
                                }
                            }
                            HorizontalDivider(color = KddDivider)
                        }
                        items(categoriasDisponibles) { cat ->
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable { categoriaSeleccionada = cat; categoriaPersonalizada = ""; mostrarCategoriasDialog = false }
                                    .padding(vertical = 10.dp, horizontal = 4.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text(cat, style = MaterialTheme.typography.bodyMedium, color = KddTextPrimary)
                                if (categoriaSeleccionada == cat) {
                                    Icon(Icons.Filled.Check, contentDescription = null, tint = KddPurple, modifier = Modifier.size(18.dp))
                                }
                            }
                            HorizontalDivider(color = KddDivider)
                        }
                        item {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable { categoriaSeleccionada = "Personalizada"; mostrarCategoriasDialog = false }
                                    .padding(vertical = 10.dp, horizontal = 4.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text("Personalizada...", style = MaterialTheme.typography.bodyMedium, color = KddTextSecondary)
                                if (categoriaSeleccionada == "Personalizada") {
                                    Icon(Icons.Filled.Check, contentDescription = null, tint = KddPurple, modifier = Modifier.size(18.dp))
                                }
                            }
                        }
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                    TextButton(onClick = { mostrarCategoriasDialog = false }, modifier = Modifier.align(Alignment.End)) {
                        Text("Cancelar", color = KddTextSecondary)
                    }
                }
            }
        }
    }

    // Date Picker
    if (mostrarDatePicker) {
        val dateState = rememberDatePickerState()
        DatePickerDialog(
            onDismissRequest = { mostrarDatePicker = false },
            confirmButton = {
                TextButton(onClick = {
                    dateState.selectedDateMillis?.let {
                        fechaSeleccionada = LocalDate.ofEpochDay(it / 86400000)
                    }
                    mostrarDatePicker = false
                }) { Text("Aceptar", color = KddPurple) }
            },
            dismissButton = {
                TextButton(onClick = { mostrarDatePicker = false }) { Text("Cancelar") }
            }
        ) { DatePicker(state = dateState) }
    }
}
