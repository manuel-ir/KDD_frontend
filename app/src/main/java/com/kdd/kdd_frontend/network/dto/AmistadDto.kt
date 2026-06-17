package com.kdd.kdd_frontend.network.dto

/**
 * Datos de una relacion de amistad recibidos desde el backend.
 * Incluye el id y nombre del otro usuario y el estado: pendiente o confirmado.
 */
data class AmistadDto(
    val idAmigo: Long,
    val nombre: String,
    val fotoPerfil: String?,
    val estado: String
)
