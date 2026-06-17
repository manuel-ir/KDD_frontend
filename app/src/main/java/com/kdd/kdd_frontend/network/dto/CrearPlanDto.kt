package com.kdd.kdd_frontend.network.dto

/**
 * Datos que se envian al backend para crear o editar un plan.
 * Los campos opcionales pueden ser nulos si el usuario no los rellena.
 */
data class CrearPlanDto(
    val titulo: String,
    val descripcion: String,
    val categoria: String,
    val fechaEvento: String?,
    val horaEvento: String?,
    val horaHasta: String? = null,
    val ubicacionTexto: String?,
    val edadMin: Int,
    val edadMax: Int,
    val numMaxPersonas: Int,
    val idioma: String?,
    val latitud: Double? = null,
    val longitud: Double? = null,
    val fotoPlanUrl: String? = null,
    val comunidadId: Long? = null,
    val acompanantes: Int = 1
)