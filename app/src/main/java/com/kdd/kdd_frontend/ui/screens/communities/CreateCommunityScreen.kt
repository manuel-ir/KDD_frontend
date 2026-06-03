package com.kdd.kdd_frontend.ui.screens.communities

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import com.kdd.kdd_frontend.ui.screens.plan.CATEGORIAS_PREDEFINIDAS
import com.kdd.kdd_frontend.ui.theme.*

@Composable
fun CreateCommunityScreen(
    onNavigateBack: () -> Unit,
    onCommunityCreated: () -> Unit
) {
    var titulo by remember { mutableStateOf("") }
    var categoria by remember { mutableStateOf("") }
    var categoriaPersonalizada by remember { mutableStateOf("") }
    var edadMin by remember { mutableFloatStateOf(18f) }
    var edadMax by remember { mutableFloatStateOf(80f) }
    var ubicacion by remember { mutableStateOf("") }
    var descripcion by remember { mutableStateOf("") }
    var showCategoriasDialog by remember { mutableStateOf(false) }

    val formValido = titulo.isNotBlank() && ubicacion.isNotBlank() &&
            categoria.isNotBlank() && (categoria != "Personalizada" || categoriaPersonalizada.isNotBlank())

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.White)
    ) {
        // Barra superior
        Surface(shadowElevation = 2.dp) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .statusBarsPadding()
                    .height(56.dp)
                    .padding(horizontal = 8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = onNavigateBack) {
                    Icon(Icons.Filled.Close, contentDescription = "Cerrar", tint = KddTextPrimary)
                }
                Text(
                    text = "Crear comunidad",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold,
                    color = KddTextPrimary,
                    modifier = Modifier.weight(1f)
                )
            }
        }

        // Formulario
        Column(
            modifier = Modifier
                .weight(1f)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 20.dp, vertical = 16.dp),
            verticalArrangement = Arrangement.spacedBy(20.dp)
        ) {
            // 1. Título
            CommunityFormField(
                label = "Nombre de la comunidad",

                value = titulo,
                onValueChange = { if (it.length <= 50) titulo = it },
                placeholder = "Ej: Escalada Sevilla",
                maxChars = 50
            )

            // 2. Categoría
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text("Categoría", style = MaterialTheme.typography.labelLarge, fontWeight = FontWeight.SemiBold, color = KddTextPrimary)
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

            // 3. Edad mínima y máxima
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

            // 4. Ubicación
            CommunityFormField(
                label = "Ubicación",
                value = ubicacion,
                onValueChange = { if (it.length <= 60) ubicacion = it },
                placeholder = "Ciudad o pueblo",
                maxChars = 60
            )

            // 5. Descripción
            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = "Descripción",
                        style = MaterialTheme.typography.labelLarge,
                        fontWeight = FontWeight.SemiBold,
                        color = KddTextPrimary
                    )
                    Text(
                        text = "${descripcion.length}/300",
                        style = MaterialTheme.typography.labelSmall,
                        color = KddTextHint
                    )
                }
                OutlinedTextField(
                    value = descripcion,
                    onValueChange = { if (it.length <= 300) descripcion = it },
                    placeholder = { Text("Describe tu comunidad...", color = KddTextHint) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(120.dp),
                    shape = RoundedCornerShape(10.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = KddPurple,
                        unfocusedBorderColor = KddDivider
                    )
                )
            }

            // 6. Añadir foto
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text(
                    text = "Foto de la comunidad",
                    style = MaterialTheme.typography.labelLarge,
                    fontWeight = FontWeight.SemiBold,
                    color = KddTextPrimary
                )
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(140.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(KddSurface)
                        .border(1.dp, KddDivider, RoundedCornerShape(12.dp)),
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        Box(
                            modifier = Modifier
                                .size(48.dp)
                                .clip(CircleShape)
                                .background(KddPurple.copy(alpha = 0.1f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                Icons.Filled.AddAPhoto,
                                contentDescription = null,
                                tint = KddPurple,
                                modifier = Modifier.size(24.dp)
                            )
                        }
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "Añadir foto",
                            style = MaterialTheme.typography.bodyMedium,
                            color = KddPurple,
                            fontWeight = FontWeight.Medium
                        )
                        Text(
                            text = "Toca para seleccionar",
                            style = MaterialTheme.typography.bodySmall,
                            color = KddTextHint
                        )
                    }
                }
            }
        }

        // Botón Terminar
        Surface(shadowElevation = 4.dp) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .navigationBarsPadding()
                    .padding(horizontal = 20.dp, vertical = 12.dp)
            ) {
                Button(
                    onClick = onCommunityCreated,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(52.dp),
                    shape = RoundedCornerShape(26.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (formValido) KddYellow else KddDivider,
                        contentColor = if (formValido) Color.Black else KddTextHint
                    ),
                    enabled = formValido
                ) {
                    Text(
                        text = "Terminar",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold
                    )
                }
            }
        }
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
}

@Composable
private fun CommunityFormField(
    label: String,
    value: String,
    onValueChange: (String) -> Unit,
    placeholder: String,
    maxChars: Int
) {
    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                text = label,
                style = MaterialTheme.typography.labelLarge,
                fontWeight = FontWeight.SemiBold,
                color = KddTextPrimary
            )
            Text(
                text = "${value.length}/$maxChars",
                style = MaterialTheme.typography.labelSmall,
                color = KddTextHint
            )
        }
        OutlinedTextField(
            value = value,
            onValueChange = onValueChange,
            placeholder = { Text(placeholder, color = KddTextHint) },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true,
            shape = RoundedCornerShape(10.dp),
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = KddPurple,
                unfocusedBorderColor = KddDivider
            )
        )
    }
}
