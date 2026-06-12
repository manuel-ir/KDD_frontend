package com.kdd.kdd_frontend.network.dto

data class PlanDto(
    val id: Long,
    val titulo: String,
    val descripcion: String?,
    val categoria: String?,
    val fechaEvento: String?,
    val horaEvento: String?,
    val ubicacionTexto: String?,
    val edadMin: Int?,
    val edadMax: Int?,
    val numMaxPersonas: Int?,
    val idioma: String?,
    val anfitrionNombre: String?,
    val anfitrionId: Long?,
    val numParticipantes: Int,
    val numApuntados: Int = 0,
    val miembro: Boolean = false,
    val creador: Boolean = false,
    val pendiente: Boolean = false
)
