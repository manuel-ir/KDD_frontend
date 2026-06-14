package com.kdd.kdd_frontend.network.dto

data class ParticipanteDto(
    val id: Long,
    val nombre: String,
    val edad: Int?,
    val descripcion: String?,
    val fotoPerfil: String?
)
