package com.kdd.kdd_frontend.network.dto

data class UsuarioDto(
    val id: Long,
    val nombre: String,
    val email: String,
    val fotoPerfil: String?,
    val descripcion: String?,
    val edad: Int?,
    val fechaNacimiento: String? = null // ISO YYYY-MM-DD
)
