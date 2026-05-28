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
import com.kdd.kdd_frontend.ui.theme.*

sealed class ChatItem {
    data class Message(
        val id: Long,
        val contenido: String,
        val hora: String,
        val esMio: Boolean
    ) : ChatItem()

    data class DateSeparator(val fecha: String) : ChatItem()
}

@Composable
fun ChatDetailScreen(
    userId: Long,
    onNavigateBack: () -> Unit
) {
    var mensaje by remember { mutableStateOf("") }
    val listState = rememberLazyListState()

    val mensajes = listOf(
        ChatItem.Message(1L, "Hola! ¿Cómo estás?", "15:30", false),
        ChatItem.Message(2L, "¡Muy bien! Acabo de apuntarme a un plan de senderismo.", "15:31", true),
        ChatItem.Message(3L, "Vas a organizar una actividad. ¡Genial! 🚀 Algunos consejos:\n\n- Comparte el lugar, la hora y el tema.\n- Invita a muchos amigas y amigos.\n- Conversa en el chat grupal.\n\n¡Diviértete! Y avísame si necesitas ayuda o más consejos ✌", "15:35", false),
        ChatItem.DateSeparator("14 abr 2026"),
        ChatItem.Message(4L, "Vas a organizar una actividad. ¡Genial! 🚀 Algunos consejos:\n\n- Comparte el lugar, la hora y el tema.\n- Invita a muchos amigas y amigos.\n- Conversa en el chat grupal.\n\n¡Diviértete! Y avísame si necesitas ayuda o más consejos ✌", "20:22", false),
    )

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

                // Avatar
                Box(
                    modifier = Modifier
                        .size(36.dp)
                        .clip(CircleShape)
                        .background(KddSurfaceVariant),
                    contentAlignment = Alignment.Center
                ) {
                    Text("E", fontWeight = FontWeight.Bold, color = KddTextSecondary)
                }

                Spacer(modifier = Modifier.width(8.dp))

                Text(
                    text = "Emma",
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

        // Lista de mensajes
        LazyColumn(
            state = listState,
            modifier = Modifier
                .weight(1f)
                .padding(horizontal = 12.dp),
            verticalArrangement = Arrangement.spacedBy(4.dp),
            contentPadding = PaddingValues(vertical = 12.dp)
        ) {
            items(mensajes) { item ->
                when (item) {
                    is ChatItem.DateSeparator -> DateSeparatorItem(fecha = item.fecha)
                    is ChatItem.Message -> MessageBubble(message = item)
                }
            }
        }

        // Input de mensaje
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
                            // TODO: enviar mensaje
                            mensaje = ""
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
private fun DateSeparatorItem(fecha: String) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        contentAlignment = Alignment.Center
    ) {
        Surface(
            shape = RoundedCornerShape(12.dp),
            color = KddSurfaceVariant
        ) {
            Text(
                text = fecha,
                modifier = Modifier.padding(horizontal = 12.dp, vertical = 4.dp),
                style = MaterialTheme.typography.labelSmall,
                color = KddTextSecondary
            )
        }
    }
}

@Composable
private fun MessageBubble(message: ChatItem.Message) {
    Column(
        modifier = Modifier.fillMaxWidth(),
        horizontalAlignment = if (message.esMio) Alignment.End else Alignment.Start
    ) {
        Surface(
            shape = RoundedCornerShape(
                topStart = 16.dp,
                topEnd = 16.dp,
                bottomStart = if (message.esMio) 16.dp else 4.dp,
                bottomEnd = if (message.esMio) 4.dp else 16.dp
            ),
            color