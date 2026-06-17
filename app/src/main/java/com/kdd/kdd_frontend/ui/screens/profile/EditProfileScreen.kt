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
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import coil.compose.AsyncImage
import com.kdd.kdd_frontend.ui.theme.*
import com.kdd.kdd_frontend.viewmodel.PerfilViewModel
import com.kdd.kdd_frontend.viewmodel.PerfilState

/**
 * Pantalla para editar el perfil del usuario.
 *
 * Permite cambiar el nombre de usuario (alias), la descripcion personal
 * y la foto de perfil. El alias tiene un limite de 3 cambios totales.
 * Los cambios se guardan en el backend al pulsar Guardar.
 */
@OptIn(ExperimentalMaterial3Api::class)
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
    var showFechaWarning by remember { mutableStateOf(false) }       // cuando YA está puesta
    var showFechaConfirmacion by remember { mutableStateOf(false) }  // antes de poner por primera vez
    var fechaPendienteConfirmar by remember { mutableStateOf("") }
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
                if (perfilState is PerfilState.Success) {
                    val pm = (perfilState as PerfilState.Success).usuario.puntuacionMedia
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp),
                        modifier = Modifier.padding(bottom = 4.dp)
                    ) {
                        Icon(Icons.Filled.Star, contentDescription = null, tint = Color(0xFF2196F3), modifier = Modifier.size(14.dp))
                        Text(
                            text = if (pm != null) String.format("%.1f", pm) else "Sin valoraciones",
                            style = MaterialTheme.typography.bodySmall,
                            color = KddTextHint
                        )
                    }
                }
                Text(
                    if (descripcion.isNotBlank()) descripcion else "Sin descripción",
                    style = MaterialTheme.typography.bodyMedium,
                    color = if (descripcion.isNotBlank()) KddTextSecondary else KddTextHint
                )
            }

            Spacer(modifier = Modifier.height(28.dp))

            Column(modifier = Modifier.padding(horizontal = 16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                val cambiosAlias = (perfilState as? PerfilState.Success)?.usuario?.contadorCambiosAlias ?: 0
                val aliasLabel = when {
                    cambiosAlias >= 3 -> "Alias y descripción · Sin cambios disponibles"
                    nombreUsuario.isNotBlank() -> "Alias y descripción · ${3 - cambiosAlias} cambio${if (3 - cambiosAlias == 1) "" else "s"} restante${if (3 - cambiosAlias == 1) "" else "s"}"
                    else -> "Alias y descripción"
                }
                EditOption(
                    label = aliasLabel,
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

    if (showFechaConfirmacion && fechaPendienteConfirmar.isNotBlank()) {
        val fechaFormateada = runCatching {
            LocalDate.parse(fechaPendienteConfirmar).format(DateTimeFormatter.ofPattern("dd/MM/yyyy"))
        }.getOrDefault(fechaPendienteConfirmar)
        AlertDialog(
            onDismissRequest = { showFechaConfirmacion = false },
            title = { Text("Confirmar fecha de nacimiento") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text("Has seleccionado: $fechaFormateada")
                    Text(
                        "⚠️ Una vez guardada, la fecha de nacimiento no podrá cambiarse. Asegúrate de que es correcta.",
                        style = MaterialTheme.typography.bodySmall,
                        color = Color(0xFF795548)
                    )
                }
            },
            confirmButton = {
                TextButton(onClick = {
                    fechaNacimiento = fechaPendienteConfirmar
                    showFechaConfirmacion = false
                    viewModel.editarPerfil(
                        nombre = nombre,
                        nombreUsuario = nombreUsuario.ifBlank { null },
                        descripcion = descripcion,
                        fechaNacimiento = fechaNacimiento,
                        onSuccess = {},
                        onError = { errorMsg = it }
                    )
                }) { Text("Confirmar", color = KddPurple) }
            },
            dismissButton = {
                TextButton(onClick = { showFechaConfirmacion = false }) { Text("Cancelar") }
            }
        )
    }

    if (showNombreDialog) {
        var tempNombreUsuario by remember { mutableStateOf(nombreUsuario) }
        var tempDesc by remember { mutableStateOf(descripcion) }
        val contadorCambios = (perfilState as? PerfilState.Success)?.usuario?.contadorCambiosAlias ?: 0
        val aliasAgotado = contadorCambios >= 3
        val cambiosRestantes = (3 - contadorCambios).coerceAtLeast(0)

        Dialog(onDismissRequest = { showNombreDialog = false }) {
            Card(shape = RoundedCornerShape(16.dp), colors = CardDefaults.cardColors(containerColor = Color.White)) {
                Column(modifier = Modifier.padding(24.dp), verticalArrangement = Arrangement.spacedBy(14.dp)) {
                    Text("Alias y descripción", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                    OutlinedTextField(
                        value = tempNombreUsuario,
                        onValueChange = { if (it.length <= 20 && !aliasAgotado) tempNombreUsuario = it },
                        label = { Text("Alias (nombre de usuario)") },
                        placeholder = { Text("Ej: pepegrillo92", color = KddTextHint) },
                        singleLine = true,
                        enabled = !aliasAgotado,
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(10.dp),
                        colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = KddPurple, unfocusedBorderColor = KddDivider),
                        supportingText = {
                            if (aliasAgotado) {
                                Text("Has alcanzado el límite de 3 cambios de alias", color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.labelSmall)
                            } else if (nombreUsuario.isNotBlank()) {
                                Text("Cambios restantes: $cambiosRestantes", color = KddTextHint, style = MaterialTheme.typography.labelSmall)
                            }
                        }
                    )
                    OutlinedTextField(
                        value = tempDesc,
                        onValueChange = { if (it.length <= 150) tempDesc = it },
                        label = { Text("Descripción") },
                        placeholder = { Text("Cuéntanos algo sobre ti", color = KddTextHint) },
                        singleLine = false,
                        maxLines = 3,
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(10.dp),
                        colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = KddPurple, unfocusedBorderColor = KddDivider)
                    )
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
                        TextButton(onClick = { showNombreDialog = false }) { Text("Cancelar") }
                        TextButton(onClick = {
                            viewModel.editarPerfil(
                                nombre = nombre,
                                nombreUsuario = tempNombreUsuario.ifBlank { null },
                                descripcion = tempDesc,
                                fechaNacimiento = null,
                                onSuccess = {
                                    nombreUsuario = tempNombreUsuario
                                    descripcion = tempDesc
                                    showNombreDialog = false
                                },
                                onError = { errorMsg = it }
                            )
                        }) { Text("Guardar", color = KddPurple) }
                    }
                }
            }
        }
    }

    if (showFechaDialog) {
        val datePickerState = rememberDatePickerState()
        DatePickerDialog(
            onDismissRequest = { showFechaDialog = false },
            confirmButton = {
                TextButton(onClick = {
                    val millis = datePickerState.selectedDateMillis
                    if (millis != null) {
                        val cal = java.util.Calendar.getInstance().apply { timeInMillis = millis }
                        val y = cal.get(java.util.Calendar.YEAR)
                        val m = (cal.get(java.util.Calendar.MONTH) + 1).toString().padStart(2, '0')
                        val d = cal.get(java.util.Calendar.DAY_OF_MONTH).toString().padStart(2, '0')
                        fechaPendienteConfirmar = "$y-$m-$d"
                        showFechaDialog = false
                        showFechaConfirmacion = true
                    } else {
                        showFechaDialog = false
                    }
                }) { Text("Aceptar", color = KddPurple) }
            },
            dismissButton = {
                TextButton(onClick = { showFechaDialog = false }) { Text("Cancelar") }
            }
        ) {
            DatePicker(state = datePickerState)
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun EditOption(
    label: String,
    sublabel: String,
    locked: Boolean = false,
    onClick: () -> Unit
) {
    Card(
        onClick = onClick,
        colors = CardDefaults.cardColors(containerColor = Color.White),
        shape = RoundedCornerShape(12.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(label, style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.SemiBold, color = KddTextPrimary)
                Text(sublabel, style = MaterialTheme.typography.bodySmall, color = KddTextHint)
            }
            if (locked) {
                Icon(Icons.Filled.Lock, contentDescription = null, tint = KddTextHint, modifier = Modifier.size(18.dp))
            } else {
                Icon(Icons.AutoMirrored.Filled.ArrowForwardIos, contentDescription = null, tint = KddTextHint, modifier = Modifier.size(16.dp))
            }
        }
    }
}