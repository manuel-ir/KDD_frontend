package com.kdd.kdd_frontend.network.dto

/**
 * Datos de un plan recibidos desde el backend.
 *
 * Incluye toda la informacion del plan: titulo, descripcion, fecha, hora,
 * ubicacion, aforo, categoria, coordenadas y si el usuario actual
 * es participante o creador del plan.
 */
data class PlanDto(
    val id: Long,
    val titulo: String,
    val descripcion: String?,
    val categoria: String?,
    val fechaEvento: String?,
    val horaEvento: String?,
    val horaHasta: String? = null,
    val ubicacionTexto: String?,
    val edadMin: Int?,
    val edadMax: Int?,
    val numMaxPersonas: Int?,
    val idioma: String?,
    val anfitrionNombre: String?,
    val anfitrionId: Long?,
    val latitud: Double? = null,
    val longitud: Double? = null,
    val fotoPlanUrl: String? = null,
    val numParticipantes: Int,
    val numApuntados: Int = 0,
    val miembro: Boolean = false,
    val creador: Boolean = false,
    val pendiente: Boolean = false
)