package com.kdd.kdd_frontend.network.dto

/**
 * Respuesta del backend tras una autenticacion exitosa.
 * Contiene el token JWT propio de la app, el id del usuario y su nombre.
 */
data class AuthResponse(
    val token: String,
    val userId: Long,
    val displayName: String,
    val email: String
)
