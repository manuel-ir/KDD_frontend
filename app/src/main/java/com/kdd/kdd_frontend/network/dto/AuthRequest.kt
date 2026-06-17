package com.kdd.kdd_frontend.network.dto

/**
 * Datos que se envian al backend para iniciar sesion con Google.
 * Contiene el idToken generado por Google Sign-In en el dispositivo.
 */
data class GoogleAuthRequest(
    val idToken: String
)
