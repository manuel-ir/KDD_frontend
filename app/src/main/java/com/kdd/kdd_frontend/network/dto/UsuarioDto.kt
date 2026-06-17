package com.kdd.kdd_frontend.network.dto

/**
 * Datos del perfil de un usuario recibidos desde el backend.
 * Incluye nombre, alias, descripcion, foto, puntuacion media y
 * si el usuario inicio sesion con Google.
 */
data class UsuarioDto(
    val id: Long,
    val nombre: String,
    val nombreUsuario: String? = null,
    val email: String,
    val fotoPerfil: String? = null,
    val descripcion: String? = null,
    val fechaNacimiento: String? = null,
    val esInvitado: Boolean = false,
    val puntuacionMedia: Double? = null,
    val esGoogleUser: Boolean = false,
    val contadorCambiosAlias: Int = 0
) {
    val nombreMostrado: String get() = nombreUsuario?.takeIf { it.isNotBlank() } ?: nombre
}