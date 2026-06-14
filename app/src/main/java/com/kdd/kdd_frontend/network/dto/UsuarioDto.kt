package com.kdd.kdd_frontend.network.dto

data class UsuarioDto(
    val id: Long,
    val nombre: String,
    val nombreUsuario: String? = null,
    val email: String,
    val fotoPerfil: String? = null,
    val descripcion: String? = null,
    val fechaNacimiento: String? = null,
    val esInvitado: Boolean = false
) {
    val nombreMostrado: String get() = nombreUsuario?.takeIf { it.isNotBlank() } ?: nombre
}
