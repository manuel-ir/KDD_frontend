package com.kdd.kdd_frontend.network.dto

/**
 * Datos de una conversacion (chat) con otro usuario.
 * Incluye el nombre e id del otro usuario y el ultimo mensaje enviado.
 */
data class ConversacionDto(
    val usuarioId: Long,
    val nombre: String,
    val fotoPerfil: String? = null,
    val ultimoMensaje: String,
    val fechaUltimoMensaje: String
)
