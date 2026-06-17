package com.kdd.kdd_frontend.network.dto

/**
 * Datos para iniciar sesion con email y contrasena.
 * Se envia a POST /api/auth/login-email.
 */
data class LoginEmailDto(
    val email: String,
    val password: String
)
