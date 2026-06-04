package com.kdd.kdd_frontend.ui.screens.profile

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForwardIos
import androidx.compose.material.icons.automirrored.filled.Logout
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.kdd.kdd_frontend.ui.theme.*
import com.kdd.kdd_frontend.viewmodel.PerfilState
import com.kdd.kdd_frontend.viewmodel.PerfilViewModel

@Composable
fun AccountScreen(
    onNavigateBack: () -> Unit,
    onNavigateToEditProfile: () -> Unit,
    onLogout: () -> Unit
) {
    val context = LocalContext.current
    val viewModel: PerfilViewModel = viewModel()
    val perfilState by viewModel.perfilState.collectAsState()
    var showLogoutDialog by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(KddSurface)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(Color.White)
                .statusBarsPadding()
                .padding(horizontal = 8.dp, vertical = 4.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = onNavigateBack) {
                Icon(imageVector = Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Volver", tint = KddTextPrimary)
            }
            Text(text = "Cuenta", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold, modifier = Modifier.padding(start = 8.dp))
        }

        HorizontalDivider()
        Spacer(modifier = Modifier.height(16.dp))

        when (val state = perfilState) {
            is PerfilState.Loading -> {
                Box(modifier = Modifier.fillMaxWidth().padding(32.dp), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator(color = KddPurple)
                }
            }
            is PerfilState.Error -> {
                Box(modifier = Modifier.fillMaxWidth().padding(32.dp), contentAlignment = Alignment.Center) {
                    Text(state.mensaje, color = KddTextSecondary)
                }
            }
            is PerfilState.Success -> {
                val usuario = state.usuario

                Card(
                    modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White)
                ) {
                    Row(
                        modifier = Modifier.padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        Box(
                            modifier = Modifier.size(56.dp).clip(CircleShape).background(KddPurple),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = usuario.nombre.first().uppercaseChar().toString(),
                                color = Color.White,
                                fontWeight = FontWeight.Bold,
                                style = MaterialTheme.typography.titleLarge
                            )
                        }
                        Column(modifier = Modifier.weight(1f)) {
                            Text(text = usuario.nombre, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = KddTextPrimary)
                            Text(text = usuario.email, style = MaterialTheme.typography.bodySmall, color = KddTextSecondary)
                            Spacer(modifier = Modifier.height(6.dp))
                            Surface(shape = RoundedCornerShape(6.dp), color = KddSuccess.copy(alpha = 0.12f)) {
                                Row(
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                                ) {
                                    Icon(Icons.Filled.CheckCircle, contentDescription = null, tint = KddSuccess, modifier = Modifier.size(12.dp))
                                    Text(text = "Vinculado con Google", style = MaterialTheme.typography.labelSmall, color = KddSuccess)
                                }
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                Card(
                    modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White)
                ) {
                    AccountOption(icon = Icons.Filled.Edit, label = "Editar perfil", onClick = onNavigateToEditProfile)
                    HorizontalDivider(modifier = Modifier.padding(horizontal = 16.dp))
                    AccountOption(icon = Icons.Filled.Settings, label = "Ajustes", onClick = { })
                    HorizontalDivider(modifier = Modifier.padding(horizontal = 16.dp))
                    AccountOption(icon = Icons.AutoMirrored.Filled.Logout, label = "Cerrar sesión", tint = KddError, onClick = { showLogoutDialog = true })
                }
            }
        }
    }

    if (showLogoutDialog) {
        AlertDialog(
            onDismissRequest = { showLogoutDialog = false },
            title = { Text("Cerrar sesión") },
            text = { Text("¿Seguro que quieres cerrar sesión?") },
            confirmButton = {
                TextButton(onClick = {
                    showLogoutDialog = false
                    viewModel.cerrarSesion(context, onLogout)
                }) { Text("Cerrar sesión", color = KddError) }
            },
            dismissButton = {
                TextButton(onClick = { showLogoutDialog = false }) { Text("Cancelar") }
            }
        )
    }
}

@Composable
private fun AccountOption(
    icon: ImageVector,
    label: String,
    tint: Color = KddTextSecondary,
    onClick: () -> Unit
) {
    Card(
        onClick = onClick,
        colors = CardDefaults.cardColors(containerColor = Color.Transparent),
        shape = RoundedCornerShape(0.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Icon(icon, contentDescription = null, tint = tint, modifier = Modifier.size(20.dp))
            Text(
                text = label,
                style = MaterialTheme.typography.bodyMedium,
                color = if (tint == KddTextSecondary) KddTextPrimary else tint,
                modifier = Modifier.weight(1f)
            )
            Icon(Icons.AutoMirrored.Filled.ArrowForwardIos, contentDescription = null, tint = KddTextHint, modifier = Modifier.size(14.dp))
        }
    }
}
