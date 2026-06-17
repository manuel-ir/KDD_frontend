package com.kdd.kdd_frontend.network.dto

/**
 * Datos de un mensaje recibido o enviado en el chat.
 * Contiene el contenido del texto, los ids de emisor y receptor, y la fecha.
 */
data class MensajeDto(
    val id: Long,
    val emisorId: Long,
    val receptorId: Long,
    val contenido: String,
    val fechaEnvio: String?
)
