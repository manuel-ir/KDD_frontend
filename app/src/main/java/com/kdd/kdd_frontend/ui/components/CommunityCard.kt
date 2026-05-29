package com.kdd.kdd_frontend.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.kdd.kdd_frontend.ui.theme.*

data class CommunityCardData(
    val id: Long,
    val nombre: String,
    val edadMin: Int,
    val edadMax: Int,
    val ubicacion: String,
    val numMiembros: Int,
    val fotoUrl: String? = null,
    val adminNombre: String,
    val adminFotoUrl: String? = null,
    val miembrosFotos: List<String?> = emptyList()
)

@Composable
fun CommunityCard(
    data: CommunityCardData,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
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
            // Imagen
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(160.dp)
            ) {
                if (data.fotoUrl != null) {
                    AsyncImage(
                        model = data.fotoUrl,
                        contentDescription = data.nombre,
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

                // Chips de edad y ubicación arriba-izquierda
                Row(
                    modifier = Modifier
                        .align(Alignment.TopStart)
                        .padding(8.dp),
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Surface(
                        shape = RoundedCornerShape(6.dp),
                        color = Color.White.copy(alpha = 0.85f)
                    ) {
                        Text(
                            text = "Edad ${data.edadMin}-${data.edadMax}",
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                            style = MaterialTheme.typography.labelSmall,
                            color = KddTextPrimary
                        )
                    }
                    Surface(
                        shape = RoundedCornerShape(6.dp),
                        color = Color.White.copy(alpha = 0.85f)
                    ) {
                        Text(
                            text = data.ubicacion,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                            style = MaterialTheme.typography.labelSmall,
                            color = KddTextPrimary
                        )
                    }
                }

                // Nombre de la comunidad abajo-izquierda sobre la imagen
                Text(
                    text = data.nombre,
                    modifier = Modifier
                        .align(Alignment.BottomStart)
                        .padding(12.dp),
                    style = MaterialTheme.typography.titleMedium,
                    color = Color.White,
                    fontWeight = FontWeight.Bold
                )
            }

            // Barra inferior
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 12.dp, vertical = 10.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Fotos de miembros superpuestas + contador
                Box(modifier = Modifier.height(28.dp).width(if (data.miembrosFotos.isEmpty()) 0.dp else (20 + (data.miembrosFotos.size - 1) * 16).dp)) {
                    data.miembrosFotos.take(3).forEachIndexed { index, fotoUrl ->
                        Box(
                            modifier = Modifier
                                .offset(x = (index * 16).dp)
                                .size(28.dp)
                                .clip(CircleShape)
                                .background(KddSurfaceVariant)
                        ) {
                            if (fotoUrl != null) {
                                AsyncImage(
                                    model = fotoUrl,
                                    contentDescription = null,
                                    contentScale = ContentScale.Crop,
                                    modifier = Modifier.fillMaxSize()
                                )
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "${data.numMiembros} miembros",
                    style = MaterialTheme.typography.bodySmall,
                    color = KddTextSecondary
                )

                Spacer(modifier = Modifier.weight(1f))

                // Admin
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(28.dp)
                            .clip(CircleShape)
                            .background(KddSurfaceVariant)
                    ) {
                        if (data.adminFotoUrl != null) {
                            AsyncImage(
                                model = data.adminFotoUrl,
                                contentDescription = data.adminNombre,
                                contentScale = ContentScale.Crop,
                                modifier = Modifier.fillMaxSize()
                            )
                        }
                    }
                    Text(
                        text = data.adminNombre,
                        style = MaterialTheme.typography.bodySmall,
                        color = KddTextSecondary
                    )
                    Surface(
                        shape = RoundedCornerShape(4.dp),
                        color = KddSurfaceVariant
                    ) {
                        Text(
                            text = "admin",
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                            style = MaterialTheme.typography.labelSmall,
                            color = KddTextSecondary,
                            fontSize = 11.sp
                        )
                    }
                }
            }
        }
    }
}
