package com.kdd.kdd_frontend.network.dto

data class CrearPlanDto(
    val titulo: String,
    val descripcion: String? = null,
    val categoria: String,
    val fechaEvento: String? = null,
    val horaEvento: String? = null,
    val ubicacionTexto: String? = null,
    val edadMin: Int = 18,
    val edadMax: Int = 55,
    val numMaxPersonas: Int = 10,
    val idioma: String? = null,
    val latitud: Double? = null,
    val longitud: Double? = null,
    val comunidadId: Long? = null
)
