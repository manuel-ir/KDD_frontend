package com.kdd.kdd_frontend.network.dto

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
    val admin: Boolean = false
)