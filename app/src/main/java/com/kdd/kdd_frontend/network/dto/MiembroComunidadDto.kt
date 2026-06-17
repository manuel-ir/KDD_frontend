package com.kdd.kdd_frontend.network.dto

/**
 * Datos basicos de un miembro de una comunidad.
 */
data class MiembroComunidadDto(
    val id: Long,
    val nombre: String,
    val fotoPerfil: String?,
    val edad: Int?
)
