package com.kdd.kdd_frontend

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.navigation.compose.rememberNavController
import com.kdd.kdd_frontend.navigation.NavGraph
import com.kdd.kdd_frontend.ui.theme.KDDTheme

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
