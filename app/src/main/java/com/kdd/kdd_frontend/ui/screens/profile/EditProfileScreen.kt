package com.kdd.kdd_frontend.ui.screens.profile

import androidx.compose.foundation.background
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.lifecycle.viewmodel.compose.viewModel
import com.kdd.kdd_frontend.ui.theme.*
import com.kdd.kdd_frontend.viewmodel.PerfilState
import com.kdd.kdd_frontend.viewmodel.PerfilViewModel

@Composable
fun EditProfileScreen(
    onNavigateBack: () -> Unit
) {
    val viewModel: PerfilViewModel = viewModel()
    val perfilState by viewModel.perfilState.collectAsState()

    var nombre by remember { mutableStateOf("") }
    var edad by remember { mutableStateOf("") }
    var descripcion by remember { mutableStateOf("") }
    var showNombreDialog by remember { mutableStateOf(false) }
    var showDescripcionDialog by remember { mutableStateOf(false) }

    LaunchedEffect(perfilState) {
        if (perfilState is PerfilState.Success) {
            val usuario = (perfilState as PerfilState.Success).usuario
            if (nombre.isBlank()) nombre = usuario.nombre ?: ""
            if (descripcion.isBlank()) descripcion = usuario.descripcion ?: ""
            if (edad.isBlank()) edad = usuario.edad?.toString() ?: ""
        }
    }

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

        HorizontalDivider()
        Spacer(modifier = Modifier.height(24.dp))

        Box(modifier = Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
            Box {
                Box(
                    modifier = Modifier.size(80.dp).clip(CircleShape).background(KddSurfaceVariant),
                    contentAlignment = Alignment.Center
                ) {
                    Text(nombre.firstOrNull()?.uppercaseChar()?.toString() ?: "?", fontSize = 32.sp, fontWeight = FontWeight.Bold, color = KddTextSecondary)
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

        Column(modifier = Modifier.fillMaxWidth().padding(horizontal = 20.dp), horizontalAlignment = Alignment.CenterHorizontally) {
            Text(nombre.ifBlank { "..." }, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = KddTextPrimary)
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                if (descripcion.isNotBlank()) descripcion else "Sin descripción",
                style = MaterialTheme.typography.bodyMedium,
                color = if (descripcion.isNotBlank()) KddTextSecondary else KddTextHint
            )
        }

        Spacer(modifier = Modifier.height(28.dp))

        Column(modifier = Modifier.padding(horizontal = 16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
            EditOption(label = "Editar nombre y edad", onClick = { showNombreDialog = true })
            EditOption(label = "Cambiar descripción del perfil", onClick = { showDescripcionDialog = true })
        }
    }

    if (showNombreDialog) {
        var tempNombre by remember { mutableStateOf(nombre) }
        var tempEdad by remember { mutableStateOf(edad) }
        Dialog(onDismissRequest = { showNombreDialog = false }) {
            Card(shape = RoundedCornerShape(16.dp), colors = CardDefaults.cardColors(containerColor = Color.White)) {
                Column(modifier = Modifier.padding(24.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {
                    Text("Editar nombre y edad", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                    OutlinedTextField(value = tempNombre, onValueChange = { tempNombre = it }, label = { Text("Nombre") }, singleLine = true, modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(10.dp), colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = KddPurple, unfocusedBorderColor = KddDivider))
                    OutlinedTextField(value = tempEdad, onValueChange = { tempEdad = it.filter { c -> c.isDigit() }.take(3) }, label = { Text("Edad") }, singleLine = true, modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(10.dp), colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = KddPurple, unfocusedBorderColor = KddDivider))
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
                        TextButton(onClick = { showNombreDialog = false }) { Text("Cancelar", color = KddTextSecondary) }
                        Spacer(modifier = Modifier.width(8.dp))
                        Button(
                            onClick = {
                                viewModel.editarPerfil(
                                    nombre = tempNombre.trim(),
                                    descripcion = descripcion,
                                    edad = tempEdad.toIntOrNull(),
                                    onSuccess = {
                                        nombre = tempNombre.trim()
                                        edad = tempEdad
                                        showNombreDialog = false
                                    },
                                    onError = { showNombreDialog = false }
                                )
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = KddPurple),
                            shape = RoundedCornerShape(10.dp)
                        ) { Text("Guardar") }
                    }
                }
            }
        }
    }

    if (showDescripcionDialog) {
        var tempDesc by remember { mutableStateOf(descripcion) }
        Dialog(onDismissRequest = { showDescripcionDialog = false }) {
            Card(shape = RoundedCornerShape(16.dp), colors = CardDefaults.cardColors(containerColor = Color.White)) {
                Column(modifier = Modifier.padding(24.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {
                    Text("Descripción del perfil", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                    OutlinedTextField(value = tempDesc, onValueChange = { if (it.length <= 200) tempDesc = it }, placeholder = { Text("Cuéntanos algo sobre ti...", color = KddTextHint) }, modifier = Modifier.fillMaxWidth().height(120.dp), shape = RoundedCornerShape(10.dp), colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = KddPurple, unfocusedBorderColor = KddDivider))
                    Text("${tempDesc.length}/200", style = MaterialTheme.typography.labelSmall, color = KddTextHint, modifier = Modifier.align(Alignment.End))
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
                        TextButton(onClick = { showDescripcionDialog = false }) { Text("Cancelar", color = KddTextSecondary) }
                        Spacer(modifier = Modifier.width(8.dp))
                        Button(
                            onClick = {
                                viewModel.editarPerfil(
                                    nombre = nombre,
                                    descripcion = tempDesc,
                                    edad = edad.toIntOrNull(),
                                    onSuccess = {
                                        descripcion = tempDesc
                                        showDescripcionDialog = false
                                    },
                                    onError = { showDescripcionDialog = false }
                                )
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = KddPurple),
                            shape = RoundedCornerShape(10.dp)
                        ) { Text("Guardar") }
                    }
                }
            }
        }
    }
}

@Composable
private fun EditOption(label: String, onClick: () -> Unit) {
    Card(onClick = onClick, modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(12.dp), colors = CardDefaults.cardColors(containerColor = KddSurface)) {
        Row(modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 16.dp), verticalAlignment = Alignment.CenterVertically) {
            Text(label, style = MaterialTheme.typography.bodyMedium, color = KddTextPrimary, modifier = Modifier.weight(1f))
            Icon(Icons.AutoMirrored.Filled.ArrowForwardIos, contentDescription = null, tint = KddTextHint, modifier = Modifier.size(14.dp))
        }
    }
}
