package com.kdd.kdd_frontend.network.dto

/**
 * Datos de una comunidad recibidos desde el backend.
 * Incluye si el usuario actual es miembro o administrador de la comunidad.
 */
data class ComunidadDto(
    val id: Long,
    val nombre: String,
    val descripcion: String?,
    val ubicacion: String?,
    val edadMin: Int?,
    val edadMax: Int?,
    val adminNombre: String?,
    val adminId: Long?,
    val numMiembros: Int,
    val miembro: Boolean = false,
    val admin: Boolean = false,
    val fotoComunidadUrl: String? = null,
    val categoria: String? = null
)