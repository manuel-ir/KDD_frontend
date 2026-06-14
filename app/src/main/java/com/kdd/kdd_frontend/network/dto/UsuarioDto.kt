package com.kdd.kdd_frontend.network.dto

data class UsuarioDto(
    val id: Long,
    val nombre: String,
    val nombreUsuario: String? = null,
    val email: String,
