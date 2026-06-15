package com.kdd.kdd_frontend.network.dto

data class CrearPlanDto(
    val titulo: String,
    val descripcion: String,
    val categoria: String,
    val fechaEvento: String?,
    val horaEvento: String?,
    val ubicacionTexto: String?,
    val edadMin: Int,
    val edadMax: Int,
    val numMaxPersonas: Int,
    val idioma: String?,
    val latitud: Double? = null,
    val longitud: Double? = null
)