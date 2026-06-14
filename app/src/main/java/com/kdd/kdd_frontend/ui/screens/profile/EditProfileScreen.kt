package com.kdd.kdd_frontend.ui.screens.profile

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForwardIos
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.kdd.kdd_frontend.ui.theme.*

@Composable
fun EditProfileScreen(
    onNavigateBack: () -> Unit
) {
    var nombre by remember { mutableStateOf("Manuel") }
    var edad by remember { mutableStateOf("22") }
    var descripcion by remember { mutableStateOf("") }
    var fotoPerfil by remember { mutableStateOf<String?>(null) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.White)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .statusBarsPadding()
                .padding(horizontal = 8.dp, vertical = 4.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = onNavigateBack) {
                Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Volver", tint = KddTextPrimary)
            }
            Text("Editar perfil", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold, modifier = Modifier.padding(start = 8.dp))
        }
    }

        HorizontalDivider()
        Spacer(modifier = Modifier.height(24.dp))

        // Avatar
        Box(modifier = Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
            Box {
                Box(
                    modifier = Modifier.size(80.dp).clip(CircleShape).background(KddSurfaceVariant),
                    contentAlignment = Alignment.Center
                ) {
                    Text(nombre.firstOrNull()?.uppercaseChar()?.toString() ?: "M", fontSize = 32.sp, fontWeight = FontWeight.Bold, color = KddTextSecondary)
                }
                Box(
                    modifier = Modifier.size(26.dp).clip(CircleShape).background(KddPurple).align(Alignment.TopEnd),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(Icons.Filled.Add, contentDescription = "Cambiar foto", tint = Color.White, modifier = Modifier.size(16.dp))
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Nombre y descripción visibles
        Column(modifier = Modifier.fillMaxWidth().padding(horizontal = 20.dp), horizontalAlignment = Alignment.CenterHorizontally) {
            Text(nombre, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = KddTextPrimary)
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                if (descripcion.isNotBlank()) descripcion else "Sin descripción",
                style = MaterialTheme.typography.bodyMedium,
                color = if (descripcion.isNotBlank()) KddTextSecondary else KddTextHint
            )
        }
    }

    val yaHayFecha = fechaNacimiento.isNotBlank()

        Column(modifier = Modifier.padding(horizontal = 16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
            EditOption(label = "Editar nombre y edad", onClick = { showNombreDialog = true })
            EditOption(label = "Cambiar descripción del perfil", onClick = { showDescripcionDialog = true })
