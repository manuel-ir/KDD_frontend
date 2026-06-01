package com.kdd.kdd_frontend.ui.screens.profile

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForwardIos
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.kdd.kdd_frontend.ui.theme.*

@Composable
fun AccountScreen(
    onNavigateBack: () -> Unit,
    onNavigateToEditProfile: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(KddSurface)
    ) {
        // Barra superior
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(Color.White)
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
                text = "Cuenta",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(start = 8.dp)
            )
        }

        HorizontalDivider()

        Spacer(modifier = Modifier.height(16.dp))

        // Card de perfil
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White)
        ) {
            Row(
                modifier = Modifier.padding(16.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Avatar
                Box(
                    modifier = Modifier
                        .size(56.dp)
                        .clip(CircleShape)
                        .background(KddPurple),
                    contentAlignment = Alignment.Center
                ) {
                    Text("M", color = Color.White, fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleLarge)
                }

                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "Manuel",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = KddTextPrimary
                    )
                    Text(
                        text = "minfarodriguez@gmail.com",
                        style = MaterialTheme.typography.bodySmall,
                        color = KddTextSecondary
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    // Valoración media — TODO: obtener del backend
                    val valoracionMedia = 4.2f // ejemplo
                    val numValoraciones = 7
                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(2.dp)) {
                        (1..5).forEach { i ->
                            Icon(
                                imageVector = when {
                                    i <= valoracionMedia.toInt() -> Icons.Filled.Star
                                    i - valoracionMedia < 1f -> Icons.Filled.StarHalf
                                    else -> Icons.Filled.StarBorder
                            },
                                contentDescription = null,
                                tint = KddYellow,
                                modifier = Modifier.size(16.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = "%.1f".format(valoracionMedia),
                            style = MaterialTheme.typography.labelMedium,
                            fontWeight = FontWeight.SemiBold,
                            color = KddTextPrimary
                        )
                        Text(
                            text = "($numValoraciones)",
                            style = MaterialTheme.typography.labelSmall,
                            color = KddTextHint
                        )
                    }
                    Spacer(modifier = Modifier.height(4.dp))
                    Surface(
                        shape = RoundedCornerShape(6.dp),
                        color = KddSuccess.copy(alpha = 0.12f)
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            Icon(Icons.Filled.CheckCircle, contentDescription = null, tint = KddSuccess, modifier = Modifier.size(12.dp))
                            Text(
                                text = "Vinculado con Google",
                                style = MaterialTheme.typography.labelSmall,
                                color = KddSuccess
                            )
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Opciones
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White)
        ) {
            AccountOption(
                icon = Icons.Filled.Edit,
                label = "Editar perfil",
                onClick = onNavigateToEditProfile
            )
            HorizontalDivider(modifier = Modifier.padding(horizontal = 16.dp))
            AccountOption(
                icon = Icons.Filled.Settings,
                label = "Ajustes",
                onClick = { /* TODO */ }
            )
        }
    }
}

@Composable
private fun AccountOption(icon: ImageVector, label: String, onClick: () -> Unit) {
    Card(
        onClick = onClick,
        colors = CardDefaults.cardColors(containerColor = Color.Transparent),
        shape = RoundedCornerShape(0.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Icon(icon, contentDescription = null, tint = KddTextSecondary, modifier = Modifier.size(20.dp))
            Text(
                text = label,
                style = MaterialTheme.typography.bodyMedium,
                color = KddTextPrimary,
                modifier = Modifier.weight(1f)
            )
            Icon(Icons.AutoMirrored.Filled.ArrowForwardIos, contentDescription = null, tint = KddTextHint, modifier = Modifier.size(14.dp))
        }
    }
}
