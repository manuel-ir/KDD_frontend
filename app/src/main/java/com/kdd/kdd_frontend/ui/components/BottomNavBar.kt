package com.kdd.kdd_frontend.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.kdd.kdd_frontend.ui.theme.KddPurple
import com.kdd.kdd_frontend.ui.theme.KddTextPrimary

enum class BottomNavItem {
    HOME, EXPLORE, CREATE, COMMUNITIES, CALENDAR
}

@Composable
fun BottomNavBar(
    currentItem: BottomNavItem,
    onHomeClick: () -> Unit,
    onExploreClick: () -> Unit,
    onCreateClick: () -> Unit,
    onCommunitiesClick: () -> Unit,
    onCalendarClick: () -> Unit
) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shadowElevation = 8.dp,
        color = Color.White
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .navigationBarsPadding()
                .height(64.dp)
                .padding(horizontal = 8.dp),
            horizontalArrangement = Arrangement.SpaceAround,
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Inicio
            IconButton(onClick = onHomeClick) {
                Icon(
                    imageVector = Icons.Outlined.Home,
                    contentDescription = "Inicio",
                    tint = if (currentItem == BottomNavItem.HOME) KddPurple else KddTextPrimary,
                    modifier = Modifier.size(26.dp)
                )
            }

            // Explorar
            IconButton(onClick = onExploreClick) {
                Icon(
                    imageVector = Icons.Outlined.Explore,
                    contentDescription = "Explorar",
                    tint = if (currentItem == BottomNavItem.EXPLORE) KddPurple else KddTextPrimary,
                    modifier = Modifier.size(26.dp)
                )
            }

            // Crear (botón central)
            Box(
                modifier = Modifier
                    .size(52.dp)
                    .clip(RoundedCornerShape(14.dp))
                    .background(KddPurple),
                contentAlignment = Alignment.Center
            ) {
                IconButton(onClick = onCreateClick) {
                    Icon(
                        imageVector = Icons.Filled.Add,
                        contentDescription = "Crear",
                        tint = Color.White,
                        modifier = Modifier.size(28.dp)
                    )
                }
            }

            // Comunidades
            IconButton(onClick = onCommunitiesClick) {
                Icon(
                    imageVector = Icons.Outlined.Groups,
                    contentDescription = "Comunidades",
                    tint = if (currentItem == BottomNavItem.COMMUNITIES) KddPurple else KddTextPrimary,
                    modifier = Modifier.size(26.dp)
                )
            }

            // Calendario
            IconButton(onClick = onCalendarClick) {
                Icon(
                    imageVector = Icons.Outlined.CalendarMonth,
                    contentDescription = "Calendario",
                    tint = if (currentItem == BottomNavItem.CALENDAR) KddPurple else KddTextPrimary,
                    modifier = Modifier.size(26.dp)
                )
            }
        }
    }
}
