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
import androidx.lifecycle.viewmodel.compose.viewModel
import com.kdd.kdd_frontend.ui.theme.*
import com.kdd.kdd_frontend.viewmodel.AmigosState
import com.kdd.kdd_frontend.viewmodel.ChatViewModel

@Composable
fun ChatsScreen(
    onNavigateBack: () -> Unit,
    onNavigateToChatDetail: (Long, String) -> Unit
) {
    val viewModel: ChatViewModel = viewModel()
    val amigosState by viewModel.amigosState.collectAsState()

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

        when (val estado = amigosState) {
            is AmigosState.Loading -> {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator(color = KddPurple)
                }
            }
            is AmigosState.Error -> {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Text(estado.mensaje, style = MaterialTheme.typography.bodyMedium, color = KddTextSecondary)
                        Button(onClick = { viewModel.cargarAmigos() }, colors = ButtonDefaults.buttonColors(containerColor = KddPurple)) {
                            Text("Reintentar")
                        }
                    }
                }
            }
            is AmigosState.Success -> {
                if (estado.amigos.isEmpty()) {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            Text("Aún no tienes amigos", style = MaterialTheme.typography.bodyMedium, color = KddTextSecondary)
                            Text("Únete a planes para conocer gente", style = MaterialTheme.typography.bodySmall, color = KddTextHint)
                        }
                    }
                } else {
                    LazyColumn {
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
            modifier = Modifier
                .size(48.dp)
                .clip(CircleShape)
                .background(KddSurfaceVariant),
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
