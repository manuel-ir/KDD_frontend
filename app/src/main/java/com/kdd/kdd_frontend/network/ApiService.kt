package com.kdd.kdd_frontend.network

import com.kdd.kdd_frontend.network.dto.AmistadDto
import com.kdd.kdd_frontend.network.dto.AuthResponse
import com.kdd.kdd_frontend.network.dto.ComunidadDto
import com.kdd.kdd_frontend.network.dto.CrearComunidadDto
import com.kdd.kdd_frontend.network.dto.MiembroComunidadDto
import com.kdd.kdd_frontend.network.dto.CrearPlanDto
import com.kdd.kdd_frontend.network.dto.GoogleAuthRequest
import com.kdd.kdd_frontend.network.dto.MensajeDto
import com.kdd.kdd_frontend.network.dto.PlanDto
import com.kdd.kdd_frontend.network.dto.UsuarioDto
import retrofit2.Response
import retrofit2.http.*

interface ApiService {

    // Auth
    @POST("api/auth/google")
    suspend fun loginConGoogle(@Body request: GoogleAuthRequest): Response<AuthResponse>

    @GET("api/health")
    suspend fun health(): Response<Map<String, String>>

    // Usuarios
    @GET("api/usuarios/me")
    suspend fun getMiPerfil(): Response<UsuarioDto>

    @PUT("api/usuarios/me")
    suspend fun editarPerfil(@Body body: Map<String, @JvmSuppressWildcards Any>): Response<UsuarioDto>

    // Planes
    @GET("api/planes")
    suspend fun getPlanes(): Response<List<PlanDto>>

    @GET("api/planes/mis-planes")
    suspend fun getMisPlanes(): Response<List<PlanDto>>

    @GET("api/planes/{id}")
    suspend fun getPlan(@Path("id") id: Long): Response<PlanDto>

    @POST("api/planes")
    suspend fun crearPlan(@Body body: CrearPlanDto): Response<PlanDto>

    @POST("api/planes/{id}/unirse")
    suspend fun unirseAPlan(@Path("id") id: Long): Response<Void>

    @DELETE("api/planes/{id}/abandonar")
    suspend fun abandonarPlan(@Path("id") id: Long): Response<Void>

    // Comunidades
    @GET("api/comunidades")
    suspend fun getComunidades(): Response<List<ComunidadDto>>

    @GET("api/comunidades/{id}")
    suspend fun getComunidad(@Path("id") id: Long): Response<ComunidadDto>

    @POST("api/comunidades")
    suspend fun crearComunidad(@Body body: CrearComunidadDto): Response<ComunidadDto>

    @POST("api/comunidades/{id}/unirse")
    suspend fun unirseAComunidad(@Path("id") id: Long): Response<Void>

    @DELETE("api/comunidades/{id}/abandonar")
    suspend fun abandonarComunidad(@Path("id") id: Long): Response<Void>

    @GET("api/comunidades/{id}/miembros")
    suspend fun getMiembrosComunidad(@Path("id") id: Long): Response<List<MiembroComunidadDto>>

    // Amistades
    @GET("api/amistades")
    suspend fun getAmigos(): Response<List<AmistadDto>>

    @POST("api/amistades/{id}")
    suspend fun enviarSolicitud(@Path("id") id: Long): Response<Void>

    @PUT("api/amistades/{id}/aceptar")
    suspend fun aceptarAmistad(@Path("id") id: Long): Response<Void>

    @DELETE("api/amistades/{id}")
    suspend fun eliminarAmistad(@Path("id") id: Long): Response<Void>

    // Mensajes
    @GET("api/mensajes/{id}")
    suspend fun getConversacion(@Path("id") id: Long): Response<List<MensajeDto>>

    @POST("api/mensajes/{id}")
    suspend fun enviarMensaje(@Path("id") id: Long, @Body body: Map<String, String>): Response<MensajeDto>

    // Valoraciones
    @POST("api/valoraciones")
    suspend fun valorar(@Body body: Map<String, @JvmSuppressWildcards Any>): Response<Void>
}
