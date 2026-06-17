package com.kdd.kdd_frontend.ui.screens.chat

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
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
import com.kdd.kdd_frontend.network.dto.ConversacionDto
import com.kdd.kdd_frontend.ui.theme.*
import com.kdd.kdd_frontend.viewmodel.ChatViewModel
import com.kdd.kdd_frontend.viewmodel.ConversacionesState
import com.kdd.kdd_frontend.viewmodel.SolicitudesState

/**
 * Pantalla con la lista de conversaciones activas del usuario.
 *
 * Muestra todas las conversaciones con otros usuarios, con el nombre
 * del contacto y el ultimo mensaje enviado. Al pulsar una conversacion
 * navega al chat detallado con ese usuario.
 *
 * La lista de conversaciones es independiente de la amistad: si se elimina
 * a un amigo, el historial de chat sigue visible hasta que se borre manualmente.
 */
@Composable
fun ChatsScreen(
    onNavigateBack: () -> Unit,
    onNavigateToChatDetail: (Long, String) -> Unit
) {
    val viewModel: ChatViewModel = viewModel()
    val conversacionesState by viewModel.conversacionesState.collectAsState()
    val solicitudesState by viewModel.solicitudesState.collectAsState()

    // Refrescar cada vez que la pantalla es visible
    LaunchedEffect(Unit) {
        viewModel.cargarConversaciones()
        viewModel.cargarSolicitudes()
    }

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
            when (val estado = conversacionesState) {
                is ConversacionesState.Loading -> {
                    item {
                        Box(
                            modifier = Modifier.fillMaxWidth().padding(32.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            CircularProgressIndicator(color = KddPurple)
                        }
                    }
                }
                is ConversacionesState.Error -> {
                    item {
                        Box(
                            modifier = Modifier.fillMaxWidth().padding(32.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(8.dp)) {
                                Text(estado.mensaje, style = MaterialTheme.typography.bodyMedium, color = KddTextSecondary)
                                Button(onClick = { viewModel.cargarConversaciones() }, colors = ButtonDefaults.buttonColors(containerColor = KddPurple)) {
                                    Text("Reintentar")
                                }
                            }
                        }
                    }
                }
                is ConversacionesState.Success -> {
                    if (estado.conversaciones.isEmpty()) {
                        item {
                            Box(
                                modifier = Modifier.fillMaxWidth().padding(32.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(8.dp)) {
                                    Text("Aun no tienes conversaciones", style = MaterialTheme.typography.bodyMedium, color = KddTextSecondary)
                                    Text("Unete a comunidades y conecta con otros miembros", style = MaterialTheme.typography.bodySmall, color = KddTextHint)
                                }
                            }
                        }
                    } else {
                        items(estado.conversaciones) { conv ->
                            ConversacionRow(
                                conversacion = conv,
                                onClick = { onNavigateToChatDetail(conv.usuarioId, conv.nombre) }
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
private fun ConversacionRow(conversacion: ConversacionDto, onClick: () -> Unit) {
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
                text = conversacion.nombre.firstOrNull()?.uppercaseChar()?.toString() ?: "?",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = KddTextSecondary
            )
        }
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = conversacion.nombre,
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.SemiBold,
                color = KddTextPrimary
            )
            if (conversacion.ultimoMensaje.isNotBlank()) {
                Text(
                    text = conversacion.ultimoMensaje,
                    style = MaterialTheme.typography.bodySmall,
                    color = KddTextHint,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }
    }
}
