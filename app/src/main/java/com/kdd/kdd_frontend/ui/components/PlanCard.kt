package com.kdd.kdd_frontend.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForwardIos
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.kdd.kdd_frontend.ui.theme.*

/**
 * Tarjeta visual que representa un plan en las listas de la app.
 *
 * Muestra la informacion resumida del plan: imagen, titulo, categoria,
 * descripcion, fecha, horario y ubicacion. Se usa en la pantalla Explora,
 * el Calendario y el detalle de comunidad.
 *
 * PlanCardData es el modelo de datos que recibe este componente.
 */
data class PlanCardData(
    val id: Long,
    val titulo: String,
    val categoria: String,
    val descripcion: String,
    val dia: String,           // raw yyyy-MM-dd para parsing en CalendarScreen
    val hora: String,          // raw HH:mm o HH:mm:ss
    val horaHasta: String? = null, // raw HH:mm o HH:mm:ss
    val distanciaKm: String,
    val ubicacion: String? = null,
    val fotoUrl: String? = null,
    val anfitrionNombre: String,
    val anfitrionFotoUrl: String? = null,
    val latitud: Double? = null,
    val longitud: Double? = null
)

@Composable
fun PlanCard(
    data: PlanCardData,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    imagenOverlay: (@Composable BoxScope.() -> Unit)? = null
) {
    Card(
        onClick = onClick,
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp),
        shape = RoundedCornerShape(16.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White)
    ) {
        Column {
            // Imagen (2/3 del card)
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(180.dp)
            ) {
                if (data.fotoUrl != null) {
                    AsyncImage(
                        model = data.fotoUrl,
                        contentDescription = data.titulo,
                        contentScale = ContentScale.Crop,
                        modifier = Modifier.fillMaxSize()
                    )
                } else {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(KddSurfaceVariant)
                    )
                }

                // Contenido opcional superpuesto en la imagen (ej: boton eliminar historial)
                imagenOverlay?.invoke(this)

                // Título y categoría sobre la imagen
                Column(
                    modifier = Modifier
                        .align(Alignment.BottomStart)
                        .padding(12.dp)
                ) {
                    Text(
                        text = data.titulo,
                        style = MaterialTheme.typography.titleMedium,
                        color = Color.White,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = data.categoria,
                        style = MaterialTheme.typography.bodySmall,
                        color = Color.White.copy(alpha = 0.9f)
                    )
                }
            }

            // Barra de información (1/3)
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 12.dp, vertical = 10.dp)
            ) {
                // Chips de día, hora, distancia y ubicación
                val diaFormateado = runCatching {
                    val d = java.time.LocalDate.parse(data.dia)
                    "${d.dayOfMonth.toString().padStart(2,'0')}/${d.monthValue.toString().padStart(2,'0')}/${d.year}"
                }.getOrDefault(data.dia)
                val horaInicio = data.hora.takeIf { it.isNotBlank() }?.take(5)
                val horaFin = data.horaHasta?.take(5)
                val horarioChip = when {
                    horaInicio != null && horaFin != null -> "$horaInicio – $horaFin"
                    horaInicio != null -> horaInicio
                    else -> null
                }
                Row(
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    if (diaFormateado.isNotBlank()) InfoChip(text = diaFormateado)
                    if (horarioChip != null) InfoChip(text = horarioChip)
                    if (data.distanciaKm.isNotBlank()) InfoChip(text = data.distanciaKm)
                    if (data.ubicacion != null) InfoChip(text = data.ubicacion)
                }

                Spacer(modifier = Modifier.height(8.dp))

                // Descripción y anfitrión + botón
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.Bottom
                ) {
                    Text(
                        text = data.descripcion,
                        style = MaterialTheme.typography.bodySmall,
                        color = KddTextSecondary,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.weight(1f)
                    )

                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        modifier = Modifier.padding(start = 8.dp)
                    ) {
                        // Foto anfitrión (más grande)
                        Box(
                            modifier = Modifier
                                .size(36.dp)
                                .clip(CircleShape)
                                .background(KddSurfaceVariant),
                            contentAlignment = Alignment.Center
                        ) {
                            if (data.anfitrionFotoUrl != null) {
                                AsyncImage(
                                    model = data.anfitrionFotoUrl,
                                    contentDescription = data.anfitrionNombre,
                                    contentScale = ContentScale.Crop,
                                    modifier = Modifier.fillMaxSize()
                                )
                            } else {
                                Text(
                                    text = data.anfitrionNombre.firstOrNull()?.uppercaseChar()?.toString() ?: "?",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = KddTextSecondary,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                        // Etiqueta + nombre alineados
                        Column(verticalArrangement = Arrangement.Center) {
                            Text(
                                text = "Anfitrión",
                                style = MaterialTheme.typography.labelSmall,
                                color = KddTextHint,
                                fontSize = 10.sp
                            )
                            Text(
                                text = data.anfitrionNombre,
                                style = MaterialTheme.typography.labelSmall,
                                color = KddTextSecondary,
                                fontWeight = FontWeight.SemiBold
                            )
                        }
                        // Flecha decorativa (el click lo gestiona el Card)
                        Box(
                            modifier = Modifier
                                .size(32.dp)
                                .clip(androidx.compose.foundation.shape.CircleShape)
                                .background(KddPurple),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.ArrowForwardIos,
                                contentDescription = null,
                                modifier = Modifier.size(14.dp),
                                tint = Color.White
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun InfoChip(text: String) {
    Surface(
        shape = RoundedCornerShape(6.dp),
        color = KddSurfaceVariant
    ) {
        Text(
            text = text,
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
            style = MaterialTheme.typography.labelSmall,
            color = KddTextSecondary
        )
    }
}
