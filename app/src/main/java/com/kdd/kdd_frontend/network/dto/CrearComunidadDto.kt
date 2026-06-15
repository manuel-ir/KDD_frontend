package com.kdd.kdd_frontend.network.dto

data class CrearComunidadDto(
    val nombre: String,
    val descripcion: String,
    val ubicacion: String,
    val edadMin: Int,
    val edadMax: Int
)
