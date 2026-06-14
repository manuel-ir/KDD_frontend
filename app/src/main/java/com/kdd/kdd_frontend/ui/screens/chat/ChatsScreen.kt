package com.kdd.kdd_frontend.ui.screens.chat

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.kdd.kdd_frontend.network.dto.AmistadDto
import com.kdd.kdd_frontend.ui.theme.*
import com.kdd.kdd_frontend.viewmodel.AmigosState
import com.kdd.kdd_frontend.viewmodel.ChatViewModel
import com.kdd.kdd_frontend.viewmodel.SolicitudesState

@Composable
fun ChatsScreen(
    onNavigateBack: () -> Unit,
    onNavigateToChatDetail: (Long, String) -> Unit
) {
    val viewModel: ChatViewModel = viewModel()
    val amigosState by viewModel.amigosState.collectAsState()
    val solicitudesState by viewModel.solicitudesState.collectAsState()

    Column(modifier = Modifier.fillMaxSize().background(Color.White)) {
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
                    tint = KddPurple
                )
            }
            Text(
                text = "Chats",
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(start = 8.dp)
            )
        }

        HorizontalDivider()

        LazyColumn(modifier = Modifier.fillMaxSize()) {

            // --- Sección solicitudes pendientes ---
            val solicitudes = (solicitudesState as? SolicitudesState.Success)?.solicitudes ?: emptyList()
            if (solicitudes.isNotEmpty()) {
                item {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(KddSurface)
                            .padding(horizontal = 16.dp, vertical = 10.dp)
                    ) {
                        Text(
                            text = "Solicitudes de amistad (${solicitudes.size})",
                            style = MaterialTheme.typography.labelMedium,
                            fontWeight = FontWeight.SemiBold,
                            color = KddTextSecondary
                        )
                    }
                }
                items(solicitudes) { solicitud ->
                    SolicitudRow(
                        solicitud = solicitud,
                        onAceptar = { viewModel.aceptarSolicitud(solicitud.idAmigo) },
                        onRechazar = { viewModel.rechazarSolicitud(solicitud.idAmigo) }
                    )
                    HorizontalDivider(modifier = Modifier.padding(start = 72.dp))
                }
                item { HorizontalDivider(thickness = 4.dp, color = KddSurface) }
            }

            // --- Lista de amigos confirmados ---
            when (val estado = amigosState) {
                is AmigosState.Loading -> {
                    item {
                        Box(
                            modifier = Modifier.fillMaxWidth().padding(32.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            CircularProgressIndicator(color = KddPurple)
                        }
                    }
                }
                is AmigosState.Error -> {
                    item {
                        Box(
                            modifier = Modifier.fillMaxWidth().padding(32.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(8.dp)) {
                                Text(estado.mensaje, style = MaterialTheme.typography.bodyMedium, color = KddTextSecondary)
                                Button(onClick = { viewModel.cargarAmigos() }, colors = ButtonDefaults.buttonColors(containerColor = KddPurple)) {
                                    Text("Reintentar")
                                }
                            }
                        }
                    }
                }
                is AmigosState.Success -> {
                    if (estado.amigos.isEmpty()) {
                        item {
                            Box(
                                modifier = Modifier.fillMaxWidth().padding(32.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(8.dp)) {
                                    Text("Aún no tienes amigos", style = MaterialTheme.typography.bodyMedium, color = KddTextSecondary)
                                    Text("Únete a comunidades y añade a otros miembros", style = MaterialTheme.typography.bodySmall, color = KddTextHint)
                                }
                            }
                        }
                    } else {
                        items(estado.amigos) { amigo ->
                            AmigoRow(
                                nombre = amigo.nombre,
                                onClick = { onNavigateToChatDetail(amigo.idAmigo, amigo.nombre) }
                            )
                            HorizontalDivider(modifier = Modifier.padding(start = 72.dp))
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun SolicitudRow(
    solicitud: AmistadDto,
    onAceptar: () -> Unit,
    onRechazar: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Box(
            modifier = Modifier.size(44.dp).clip(CircleShape).background(KddSurfaceVariant),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = solicitud.nombre.firstOrNull()?.uppercaseChar()?.toString() ?: "?",
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Bold,
                color = KddTextSecondary
            )
        }
        Text(
            text = solicitud.nombre,
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.SemiBold,
            color = KddTextPrimary,
            modifier = Modifier.weight(1f)
        )
        IconButton(
            onClick = onAceptar,
            modifier = Modifier.size(36.dp).clip(CircleShape).background(Color(0xFF4CAF50))
        ) {
            Icon(Icons.Filled.Check, contentDescription = "Aceptar", tint = Color.White, modifier = Modifier.size(18.dp))
        }
        IconButton(
            onClick = onRechazar,
            modifier = Modifier.size(36.dp).clip(CircleShape).background(KddSurfaceVariant)
        ) {
            Icon(Icons.Filled.Close, contentDescription = "Rechazar", tint = KddTextSecondary, modifier = Modifier.size(18.dp))
        }
    }
}

@Composable
private fun AmigoRow(nombre: String, onClick: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(horizontal = 16.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Box(
            modifier = Modifier.size(48.dp).clip(CircleShape).background(KddSurfaceVariant),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = nombre.first().toString(),
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = KddTextSecondary
            )
        }
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = nombre,
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.SemiBold,
                color = KddTextPrimary
            )
            Text(
                text = "Toca para abrir la conversación",
                style = MaterialTheme.typography.bodySmall,
                color = KddTextHint,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}
