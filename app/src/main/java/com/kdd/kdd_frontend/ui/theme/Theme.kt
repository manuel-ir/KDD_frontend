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

/**
 * Tema visual de la aplicacion.
 *
 * Aplica los colores, tipografia y formas definidos a todos los
 * componentes de Jetpack Compose de la app. Soporta modo claro
 * y oscuro (aunque la app usa principalmente modo claro).
 */
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
