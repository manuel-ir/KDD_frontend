package com.kdd.kdd_frontend.network.dto

/**
 * Datos para registrarse con email y contrasena.
 * Se envia a POST /api/auth/registro.
 */
data class RegistroEmailDto(
    val nombre: String,
    val email: String,
    val password: String
)
