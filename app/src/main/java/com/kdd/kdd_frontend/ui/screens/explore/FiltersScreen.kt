package com.kdd.kdd_frontend.ui.screens.explore

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.kdd.kdd_frontend.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FiltersScreen(
    onNavigateBack: () -> Unit,
    isCommunityFilter: Boolean = false
) {
    var ordenarPor by remember { mutableStateOf("fecha") } // "fecha", "distancia", "popularidad"
    var edadMin by remember { mutableFloatStateOf(18f) }
    var edadMax by remember { mutableFloatStateOf(55f) }
    var distanciaMax by remember { mutableFloatStateOf(55f) }
    var sedeTexto by remember { mutableStateOf("") }

    Column(modifier = Modifier.fillMaxSize()) {
        // Barra superior con flecha
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
            // Ordenar por
            Card(
                colors = CardDefaults.cardColors(containerColor = KddSurface),
                shape = RoundedCornerShape(12.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "Ordenar por:",
                        style = MaterialTheme.typography.titleMedium,
                        color = KddTextSecondary
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                        OutlinedButton(
                            onClick = { ordenarPor = "fecha" },
                            shape = RoundedCornerShape(20.dp),
                            colors = ButtonDefaults.outlinedButtonColors(
                                containerColor = if (ordenarPor == "fecha") KddPurple.copy(alpha = 0.1f) else Color.Transparent,
                                contentColor = if (ordenarPor == "fecha") KddPurple else KddTextSecondary
                            ),
                            border = ButtonDefaults.outlinedButtonBorder.copy(
                            )
                        ) {
                            Text("Fecha de inicio")
                        }

                        if (!isCommunityFilter) {
                            OutlinedButton(
                                onClick = { ordenarPor = "distancia" },
                                shape = RoundedCornerShape(20.dp),
                                colors = ButtonDefaults.outlinedButtonColors(
                                    containerColor = if (ordenarPor == "distancia") KddPurple.copy(alpha = 0.1f) else Color.Transparent,
                                    contentColor = if (ordenarPor == "distancia") KddPurple else KddTextSecondary
                                )
                            ) {
                                Text("Distancia")
                            }
                        } else {
                            OutlinedButton(
                                onClick = { ordenarPor = "popularidad" },
                                shape = RoundedCornerShape(20.dp),
                                colors = ButtonDefaults.outlinedButtonColors(
                                    containerColor = if (ordenarPor == "popularidad") KddPurple.copy(alpha = 0.1f) else Color.Transparent,
                                    contentColor = if (ordenarPor == "popularidad") KddPurple else KddTextSecondary
                                )
                            ) {
                                Text("Popularidad")
                            }
                        }
                    }
                }
            }

            // Edad del anfitrión (solo en actividades)
            if (!isCommunityFilter) {
                Card(
                    colors = CardDefaults.cardColors(containerColor = KddSurface),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text(
                            text = "Edad del anfitrión",
                            style = MaterialTheme.typography.titleMedium,
                            color = KddTextSecondary
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(text = edadMin.toInt().toString(), color = KddTextPrimary, fontWeight = FontWeight.Medium)
                            Text(text = edadMax.toInt().toString(), color = KddTextPrimary, fontWeight = FontWeight.Medium)
                        }
                        RangeSlider(
                            value = edadMin..edadMax,
                            onValueChange = { range ->
                                edadMin = range.start
                                edadMax = range.endInclusive
                            },
                            valueRange = 18f..80f,
                            colors = SliderDefaults.colors(
                                thumbColor = KddPurple,
                                activeTrackColor = KddPurple
                            )
                        )
                    }
                }

                // Distancia
                Card(
                    colors = CardDefaults.cardColors(containerColor = KddSurface),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(
                                text = "Distancia (en km)",
                                style = MaterialTheme.typography.titleMedium,
                                color = KddTextSecondary
                            )
                            Text(
                                text = "${distanciaMax.toInt()} km",
                                color = KddTextPrimary,
                                fontWeight = FontWeight.Medium
                            )
                        }
                        Spacer(modifier = Modifier.height(8.dp))
                        Slider(
                            value = distanciaMax,
                            onValueChange = { distanciaMax = it },
                            valueRange = 1f..200f,
                            colors = SliderDefaults.colors(
                                thumbColor = KddPurple,
                                activeTrackColor = KddPurple
                            )
                        )
                    }
                }
            }

            // Categoría
            Card(
                colors = CardDefaults.cardColors(containerColor = KddSurface),
                shape = RoundedCornerShape(12.dp)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = "Categoría",
                            style = MaterialTheme.typography.titleMedium,
                            color = KddTextSecondary
                        )
                        Text(
                            text = "Todos los intereses",
                            style = MaterialTheme.typography.bodyMedium,
                            color = KddTextHint
                        )
                    }
                    Text(text = ">", color = KddTextHint)
                }
            }

            // Sede (comunidades)
            if (isCommunityFilter) {
                Card(
                    colors = CardDefaults.cardColors(containerColor = KddSurface),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                        Text(
                            text = "Sede de la comunidad",
                            style = MaterialTheme.typography.titleMedium,
                            color = KddTextSecondary
                        )
                        OutlinedTextField(
                            value = sedeTexto,
                            onValueChange = { sedeTexto = it },
                            placeholder = { Text("Ciudad o localidad", color = KddTextHint) },
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
            }
        }

        // Botón guardar
        Box(modifier = Modifier.padding(16.dp)) {
            Button(
                onClick = onNavigateBack,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp),
                shape = RoundedCornerShape(26.dp),
                colors = ButtonDefaults.buttonColors(containerColor = KddPurple)
            ) {
                Text("Guardar", color = Color.White, fontWeight = FontWeight.SemiBold)
            }
        }
    }
}
