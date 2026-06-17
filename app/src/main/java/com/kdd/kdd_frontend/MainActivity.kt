package com.kdd.kdd_frontend

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.navigation.compose.rememberNavController
import com.kdd.kdd_frontend.navigation.NavGraph
import com.kdd.kdd_frontend.ui.theme.KDDTheme

/**
 * Actividad principal y unica de la aplicacion (arquitectura de actividad unica).
 *
 * En lugar de una actividad por pantalla, toda la navegacion se gestiona
 * con Jetpack Navigation dentro de esta actividad. Carga el tema visual
 * y lanza el grafo de navegacion (NavGraph).
 *
 * Al arrancar, comprueba si el usuario ya tiene sesion iniciada (token guardado)
 * para decidir si va a la pantalla de login o directamente a la app.
 */
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            KDDTheme {
                val navController = rememberNavController()
                NavGraph(navController = navController)
            }
        }
    }
}
