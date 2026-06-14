package com.kdd.kdd_frontend.network.dto

data class ConversacionDto(
    val usuarioId: Long,
    val nombre: String,
    val fotoPerfil: String? = null,
    val ultimoMensaje: String,
    val fechaUltimoMensaje: String
)
