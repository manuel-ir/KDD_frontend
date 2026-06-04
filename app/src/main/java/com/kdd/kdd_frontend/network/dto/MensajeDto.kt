package com.kdd.kdd_frontend.network.dto

data class MensajeDto(
    val id: Long,
    val emisorId: Long,
    val receptorId: Long,
    val contenido: String,
    val fechaEnvio: String?
)
