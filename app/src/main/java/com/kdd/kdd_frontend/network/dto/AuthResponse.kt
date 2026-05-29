package com.kdd.kdd_frontend.network.dto

data class AuthResponse(
    val token: String,
    val userId: Long,
    val displayName: String,
    val email: String
)
