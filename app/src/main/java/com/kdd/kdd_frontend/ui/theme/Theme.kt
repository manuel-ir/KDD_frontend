package com.kdd.kdd_frontend.ui.theme

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier

private val KddColorScheme = lightColorScheme(
    primary = KddPurple,
    onPrimary = KddOnPrimary,
    primaryContainer = KddPurpleLight,
    secondary = KddYellow,
    onSecondary = KddTextPrimary,
    background = KddBackground,
    surface = KddSurface,
    onBackground = KddTextPrimary,
    onSurface = KddOnSurface,
    error = KddError
)

@Composable
fun KDDTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = KddColorScheme,
        typography = Typography
    ) {
        Surface(
            modifier = Modifier.fillMaxSize(),
            color = MaterialTheme.colorScheme.background
        ) {
            content()
        }
    }
}
