package com.kdd.kdd_frontend.network.dto

/**
 * Datos de un participante en un plan.
 * Incluye si ha confirmado su presencia (presente) y cuantos acompanantes lleva.
 * El campo acompanantes es solo informativo y no afecta al aforo.
 */
data class ParticipanteDto(
    val id: Long,
    val nombre: String,
    val nombreUsuario: String? = null,
    val edad: Int?,
    val descripcion: String?,
    val fotoPerfil: String?,
    val presente: Boolean = false,
    val puntuacion: Double? = null,
    val acompanantes: Int? = null
)
