package com.kdd.kdd_frontend.ui.screens.chat

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.kdd.kdd_frontend.ui.theme.*

data class ChatPreview(
    val userId: Long,
    val nombre: String,
    val ultimoMensaje: String,
    val fotoUrl: String? = null
)

@Composable
fun ChatsScreen(
    onNavigateBack: () -> Unit,
    onNavigateToChatDetail: (Long) -> Unit
) {
    val chats = listOf(
        ChatPreview(1L, "Javier", "No te vas a creer a quien me encontré en la ruta del otr..."),
        ChatPreview(2L, "Emma", "¡Diviértete! Y avísame si necesitas ayuda o más consejos ✌"),
        ChatPreview(3L, "Lucía", "¿Quedamos el sábado para el senderismo?")
    )

    Column(modifier = Modifier.fillMaxSize().background(Color.White)) {
        // Barra superior
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

        LazyColumn {
            items(chats) { chat ->
                ChatPreviewRow(
                    chat = chat,
                    onClick = { onNavigateToChatDetail(chat.userId) }
                )
                HorizontalDivider(modifier = Modifier.padding(start = 72.dp))
            }
        }
    }
}

@Composable
private fun ChatPreviewRow(chat: ChatPreview, onClick: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(horizontal = 16.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        // Avatar
        Box(
            modifier = Modifier
                .size(48.dp)
                .clip(CircleShape)
                .background(KddSurfaceVariant),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = chat.nombre.first().toString(),
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = KddTextSecondary
            )
        }

        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = chat.nombre,
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.SemiBold,
                color = KddTextPrimary
            )
            Text(
                text = chat.ultimoMensaje,
                style = MaterialTheme.typography.bodySmall,
                color = KddTextSecondary,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}
