package com.kdd.kdd_frontend.ui.screens.main

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.MyLocation
import androidx.compose.material.icons.automirrored.outlined.Chat
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.kdd.kdd_frontend.ui.components.BottomNavBar
import com.kdd.kdd_frontend.ui.components.BottomNavItem
import com.kdd.kdd_frontend.ui.components.CreateBottomSheet
import com.kdd.kdd_frontend.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainScreen(
    onNavigateToExplore: () -> Unit,
    onNavigateToCommunities: () -> Unit,
    onNavigateToCalendar: () -> Unit,
    onNavigateToCreatePlan: () -> Unit,
    onNavigateToCreateCommunity: () -> Unit,
    onNavigateToChats: () -> Unit,
    onNavigateToAccount: () -> Unit
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    var showBottomSheet by remember { mutableStateOf(false) }

    Box(modifier = Modifier.fillMaxSize()) {
        Column(modifier = Modifier.fillMaxSize()) {
            // Barra superior
            KddTopAppBar(
                onChatsClick = onNavigateToChats,
                onAccountClick = onNavigateToAccount
            )

            // Mapa (placeholder)
            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
                    .background(Color(0xFFE8EAF0)),
                contentAlignment = Alignment.Center
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    Icon(
                        imageVector = Icons.Filled.MyLocation,
                        contentDescription = null,
                        tint = KddPurple.copy(alpha = 0.4f),
                        modifier = Modifier.size(64.dp)
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    Text(
                        text = "Mapa interactivo",
                        color = KddTextHint,
                        style = MaterialTheme.typography.bodyMedium
                    )
                    Text(
                        text = "Próximamente",
                        color = KddTextHint,
                        style = MaterialTheme.typography.bodySmall
                    )
                }

                FloatingActionButton(
                    onClick = { /* TODO: centrar en ubicación */ },
                    modifier = Modifier
                        .align(Alignment.BottomEnd)
                        .padding(end = 16.dp, bottom = 16.dp)
                        .size(40.dp),
                    containerColor = Color.White,
                    contentColor = KddPurple,
                    elevation = FloatingActionButtonDefaults.elevation(4.dp)
                ) {
                    Icon(
                        imageVector = Icons.Filled.MyLocation,
                        contentDescription = "Mi ubicación",
                        modifier = Modifier.size(20.dp)
                    )
                }
            }

            // Barra inferior
            BottomNavBar(
                currentItem = BottomNavItem.HOME,
                onHomeClick = { },
                onExploreClick = onNavigateToExplore,
                onCreateClick = { showBottomSheet = true },
                onCommunitiesClick = onNavigateToCommunities,
                onCalendarClick = onNavigateToCalendar
            )
        }

        // Bottom sheet para crear
        if (showBottomSheet) {
            CreateBottomSheet(
                sheetState = sheetState,
                onDismiss = { showBottomSheet = false },
                onNavigateToCreatePlan = onNavigateToCreatePlan,
                onNavigateToCreateCommunity = onNavigateToCreateCommunity
            )
        }
    }
}

@Composable
private fun KddTopAppBar(
    onChatsClick: () -> Unit,
    onAccountClick: () -> Unit
) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        color = Color.White,
        shadowElevation = 2.dp
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .statusBarsPadding()
                .height(56.dp)
                .padding(horizontal = 16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Logo KDD
            Surface(
                modifier = Modifier.size(32.dp),
                shape = RoundedCornerShape(8.dp),
                color = KddYellow
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Text(
                        text = "K",
                        fontWeight = FontWeight.ExtraBold,
                        color = Color.Black,
                        fontSize = 16.sp
                    )
                }
            }
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = "KDD",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = KddTextPrimary
            )

            Spacer(modifier = Modifier.weight(1f))

            // Icono bocadillo (chat)
            IconButton(onClick = onChatsClick) {
                Icon(
                    imageVector = Icons.AutoMirrored.Outlined.Chat,
                    contentDescription = "Chats",
                    tint = KddTextPrimary
                )
            }

            // Avatar de perfil
            IconButton(onClick = onAccountClick) {
                Box(
                    modifier = Modifier
                        .size(32.dp)
                        .clip(CircleShape)
                        .background(KddPurple),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "P",
                        color = Color.White,
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.sp
                    )
                }
            }
        }
    }
}

