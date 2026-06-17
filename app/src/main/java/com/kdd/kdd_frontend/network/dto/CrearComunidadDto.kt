package com.kdd.kdd_frontend.network.dto

/**
 * Datos que se envian al backend para crear una nueva comunidad.
 */
data class CrearComunidadDto(
    val nombre: String,
    val descripcion: String,
    val ubicacion: String,
    val edadMin: Int,
    val edadMax: Int,
    val fotoComunidadUrl: String? = null,
    val categoria: String? = null
)
