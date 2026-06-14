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
import androidx.compose.material.icons.filled.*
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
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.AsyncImage
import com.kdd.kdd_frontend.ui.theme.*

@Composable
fun EditProfileScreen(
    onNavigateBack: () -> Unit
) {
    val viewModel: PerfilViewModel = viewModel()
    val perfilState by viewModel.perfilState.collectAsState()
    val subiendoFoto by viewModel.subiendoFoto.collectAsState()

    var nombre by remember { mutableStateOf("") }
    var nombreUsuario by remember { mutableStateOf("") }
    var fechaNacimiento by remember { mutableStateOf("") }
    var descripcion by remember { mutableStateOf("") }
    var fotoPerfil by remember { mutableStateOf<String?>(null) }

    var showNombreDialog by remember { mutableStateOf(false) }
    var showFechaDialog by remember { mutableStateOf(false) }
    var showFechaWarning by remember { mutableStateOf(false) }
    var errorMsg by remember { mutableStateOf("") }
    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(errorMsg) {
        if (errorMsg.isNotBlank()) {
            snackbarHostState.showSnackbar(errorMsg)
            errorMsg = ""
        }
    }

    LaunchedEffect(perfilState) {
        if (perfilState is PerfilState.Success) {
            val u = (perfilState as PerfilState.Success).usuario
            if (nombre.isBlank()) nombre = u.nombre
            if (nombreUsuario.isBlank()) nombreUsuario = u.nombreUsuario ?: ""
            if (descripcion.isBlank()) descripcion = u.descripcion ?: ""
            if (fechaNacimiento.isBlank()) fechaNacimiento = u.fechaNacimiento ?: ""
            if (fotoPerfil == null && u.fotoPerfil != null) fotoPerfil = u.fotoPerfil
        }
    }

    val imagePicker = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri ->
        uri?.let {
            viewModel.subirFotoPerfil(
                imageUri = it,
                onSuccess = { },
                onError = { msg -> errorMsg = msg }
            )
        }
    }

    val yaHayFecha = fechaNacimiento.isNotBlank()

    Scaffold(snackbarHost = { SnackbarHost(snackbarHostState) }) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.White)
                .padding(padding)
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

            HorizontalDivider()
            Spacer(modifier = Modifier.height(24.dp))

            // Avatar clickable
            Box(modifier = Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
                Box(
                    modifier = Modifier
                        .size(88.dp)
                        .clip(CircleShape)
                        .clickable { imagePicker.launch("image/*") },
                    contentAlignment = Alignment.Center
                ) {
                    if (!fotoPerfil.isNullOrBlank()) {
                        AsyncImage(
                            model = fotoPerfil,
                            contentDescription = "Foto de perfil",
                            contentScale = ContentScale.Crop,
                            modifier = Modifier.fillMaxSize()
                        )
                    } else {
                        Box(
                            modifier = Modifier.fillMaxSize().background(KddSurfaceVariant),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                nombre.firstOrNull()?.uppercaseChar()?.toString() ?: "?",
                                fontSize = 36.sp,
                                fontWeight = FontWeight.Bold,
                                color = KddTextSecondary
                            )
                        }
                    }
                    Box(
                        modifier = Modifier.fillMaxSize().background(Color.Black.copy(alpha = 0.35f)),
                        contentAlignment = Alignment.Center
                    ) {
                        if (subiendoFoto) {
                            CircularProgressIndicator(modifier = Modifier.size(28.dp), color = Color.White, strokeWidth = 2.dp)
                        } else {
                            Icon(Icons.Filled.CameraAlt, contentDescription = "Cambiar foto", tint = Color.White, modifier = Modifier.size(28.dp))
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(6.dp))
            Box(modifier = Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
                Text("Toca para cambiar foto", style = MaterialTheme.typography.labelSmall, color = KddTextHint)
            }

            Spacer(modifier = Modifier.height(8.dp))
            Column(modifier = Modifier.fillMaxWidth().padding(horizontal = 20.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                Text(
                    nombreUsuario.ifBlank { nombre.ifBlank { "..." } },
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = KddTextPrimary
                )
                if (nombreUsuario.isNotBlank()) {
                    Text(nombre, style = MaterialTheme.typography.bodySmall, color = KddTextHint)
                }
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    if (descripcion.isNotBlank()) descripcion else "Sin descripción",
                    style = MaterialTheme.typography.bodyMedium,
                    color = if (descripcion.isNotBlank()) KddTextSecondary else KddTextHint
                )
            }

            Spacer(modifier = Modifier.height(28.dp))

            Column(modifier = Modifier.padding(horizontal = 16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                EditOption(
                    label = "Nombre de usuario y descripción",
                    sublabel = if (nombreUsuario.isNotBlank()) "@$nombreUsuario" else "No establecido",
                    onClick = { showNombreDialog = true }
                )
                EditOption(
                    label = "Fecha de nacimiento",
                    sublabel = if (yaHayFecha) runCatching {
                        LocalDate.parse(fechaNacimiento).format(DateTimeFormatter.ofPattern("dd/MM/yyyy"))
                    }.getOrDefault(fechaNacimiento) else "No establecida",
                    locked = yaHayFecha,
                    onClick = { if (yaHayFecha) showFechaWarning = true else showFechaDialog = true }
                )
            }
        }
    }

    if (showFechaWarning) {
        AlertDialog(
            onDismissRequest = { showFechaWarning = false },
            title = { Text("No se puede modificar") },
            text = { Text("La fecha de nacimiento no puede cambiarse una vez establecida. Si hay un error, contacta con soporte.") },
            confirmButton = {
                TextButton(onClick = { showFechaWarning = false }) { Text("Entendido", color = KddPurple) }
            }
        )
    }

    if (showNombreDialog) {
        var tempNombre by remember { mutableStateOf(nombre) }
        var tempNombreUsuario by remember { mutableStateOf(nombreUsuario) }
        var tempDesc by remember { mutableStateOf(descripcion) }

        Dialog(onDismissRequest = { showNombreDialog = false }) {
            Card(shape = RoundedCornerShape(16.dp), colors = CardDefaults.cardColors(containerColor = Color.White)) {
                Column(modifier = Modifier.padding(24.dp), verticalArrangement = Arrangement.spacedBy(14.dp)) {
                    Text("Nombre y descripción", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)

                    OutlinedTextField(
                        value = tempNombre,
                        onValueChange = { tempNombre = it },
                        label = { Text("Nombre real") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(10.dp),
                        colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = KddPurple, unfocusedBorderColor = KddDivider)
                    )

                    OutlinedTextField(
                        value = tempNombreUsuario,
                        onValueChange = { if (it.length <= 20) tempNombreUsuario = it },
                        label = { Text("Nombre de usuario (alias)") },
                        placeholder = { Text("Ej: pepegrillo92", color = KddTextHint) },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(10.dp),
                        colors = OutlinedTextFieldDefaults.color