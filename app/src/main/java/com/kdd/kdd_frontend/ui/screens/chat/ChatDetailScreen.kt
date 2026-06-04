package com.kdd.kdd_frontend.ui.screens.chat

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.kdd.kdd_frontend.network.dto.MensajeDto
import com.kdd.kdd_frontend.ui.theme.*
import com.kdd.kdd_frontend.viewmodel.ChatViewModel
import com.kdd.kdd_frontend.viewmodel.MensajesState

@Composable
fun ChatDetailScreen(
    userId: Long,
    nombre: String,
    onNavigateBack: () -> Unit
) {
    val viewModel: ChatViewModel = viewModel()
    val mensajesState by viewModel.mensajesState.collectAsState()

    var mensaje by remember { mutableStateOf("") }
    val listState = rememberLazyListState()

    LaunchedEffect(userId) {
        viewModel.cargarConversacion(userId)
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.White)
    ) {
        Surface(shadowElevation = 2.dp) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .statusBarsPadding()
                    .padding(horizontal = 8.dp, vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = onNavigateBack) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = "Volver",
                        tint = KddPurple
                    )
                }

                Box(
                    modifier = Modifier
                        .size(36.dp)
                        .clip(CircleShape)
                        .background(KddSurfaceVariant),
                    contentAlignment = Alignment.Center
                ) {
                    Text(nombre.firstOrNull()?.toString() ?: "?", fontWeight = FontWeight.Bold, color = KddTextSecondary)
                }

                Spacer(modifier = Modifier.width(8.dp))

                Text(
                    text = nombre,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold,
                    color = KddTextPrimary,
                    modifier = Modifier.weight(1f)
                )

                Box {
                    var menuExpanded by remember { mutableStateOf(false) }
                    IconButton(onClick = { menuExpanded = true }) {
                        Icon(Icons.Filled.MoreVert, contentDescription = "Opciones", tint = KddTextPrimary)
                    }
                    DropdownMenu(
                        expanded = menuExpanded,
                        onDismissRequest = { menuExpanded = false }
                    ) {
                        DropdownMenuItem(
                            text = { Text("Borrar amigo") },
                            onClick = { menuExpanded = false }
                        )
                        DropdownMenuItem(
                            text = { Text("Reportar usuario") },
                            onClick = { menuExpanded = false }
                        )
                    }
                }
            }
        }

        HorizontalDivider()

        when (val estado = mensajesState) {
            is MensajesState.Loading -> {
                Box(modifier = Modifier.weight(1f).fillMaxWidth(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator(color = KddPurple)
                }
            }
            is MensajesState.Error -> {
                Box(modifier = Modifier.weight(1f).fillMaxWidth(), contentAlignment = Alignment.Center) {
                    Text(estado.mensaje, style = MaterialTheme.typography.bodyMedium, color = KddTextSecondary)
                }
            }
            is MensajesState.Success -> {
                val miId = viewModel.miUserId
                LazyColumn(
                    state = listState,
                    modifier = Modifier
                        .weight(1f)
                        .padding(horizontal = 12.dp),
                    verticalArrangement = Arrangement.spacedBy(4.dp),
                    contentPadding = PaddingValues(vertical = 12.dp)
                ) {
                    items(estado.mensajes) { msg ->
                        MessageBubble(msg = msg, esMio = msg.emisorId == miId)
                    }
                }

                LaunchedEffect(estado.mensajes.size) {
                    if (estado.mensajes.isNotEmpty()) {
                        listState.animateScrollToItem(estado.mensajes.size - 1)
                    }
                }
            }
        }

        Surface(shadowElevation = 4.dp) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .navigationBarsPadding()
                    .padding(horizontal = 12.dp, vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                OutlinedTextField(
                    value = mensaje,
                    onValueChange = { mensaje = it },
                    placeholder = { Text("Escribe tu mensaje", color = KddTextHint) },
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(24.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = KddPurple,
                        unfocusedBorderColor = KddDivider
                    ),
                    maxLines = 4
                )

                IconButton(
                    onClick = {
                        if (mensaje.isNotBlank()) {
                            val texto = mensaje
                            mensaje = ""
                            viewModel.enviarMensaje(userId, texto, onSuccess = {})
                        }
                    },
                    modifier = Modifier
                        .size(44.dp)
                        .clip(CircleShape)
                        .background(if (mensaje.isNotBlank()) KddPurple else KddSurfaceVariant)
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.Send,
                        contentDescription = "Enviar",
                        tint = if (mensaje.isNotBlank()) Color.White else KddTextHint,
                        modifier = Modifier.size(20.dp)
                    )
                }
            }
        }
    }
}

@Composable
private fun MessageBubble(msg: MensajeDto, esMio: Boolean) {
    Column(
        modifier = Modifier.fillMaxWidth(),
        horizontalAlignment = if (esMio) Alignment.End else Alignment.Start
    ) {
        Surface(
            shape = RoundedCornerShape(
                topStart = 16.dp,
                topEnd = 16.dp,
                bottomStart = if (esMio) 16.dp else 4.dp,
                bottomEnd = if (esMio) 4.dp else 16.dp
            ),
            color = if (esMio) KddPurple else KddSurface,
            modifier = Modifier.widthIn(max = 280.dp)
        ) {
            Text(
                text = msg.contenido,
                modifier = Modifier.padding(horizontal = 14.dp, vertical = 10.dp),
                style = MaterialTheme.typography.bodyMedium,
                color = if (esMio) Color.White else KddTextPrimary
            )
        }
        Spacer(modifier = Modifier.height(2.dp))
        Text(
            text = msg.fechaEnvio?.take(16)?.replace("T", " ") ?: "",
            style = MaterialTheme.typography.labelSmall,
            color = KddTextHint,
            fontSize = 11.sp,
            modifier = Modifier.padding(horizontal = 4.dp)
        )
    }
}
