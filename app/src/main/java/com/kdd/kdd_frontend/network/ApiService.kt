package com.kdd.kdd_frontend.network

import com.kdd.kdd_frontend.network.dto.AmistadDto
import com.kdd.kdd_frontend.network.dto.AuthResponse
import com.kdd.kdd_frontend.network.dto.ComunidadDto
import com.kdd.kdd_frontend.network.dto.CrearComunidadDto
import com.kdd.kdd_frontend.network.dto.MiembroComunidadDto
import com.kdd.kdd_frontend.network.dto.CrearPlanDto
import com.kdd.kdd_frontend.network.dto.GoogleAuthRequest
import com.kdd.kdd_frontend.network.dto.LoginEmailDto
import com.kdd.kdd_frontend.network.dto.RegistroEmailDto
import com.kdd.kdd_frontend.network.dto.ConversacionDto
import com.kdd.kdd_frontend.network.dto.MensajeDto
import com.kdd.kdd_frontend.network.dto.ParticipanteDto
import com.kdd.kdd_frontend.network.dto.PlanDto
import com.kdd.kdd_frontend.network.dto.UsuarioDto
import retrofit2.Response
import retrofit2.http.*

/**
 * Interfaz con todos los endpoints de la API REST del backend.
 *
 * Retrofit2 genera automaticamente la implementacion de esta interfaz.
 * Cada funcion corresponde a un endpoint del backend:
 * metodo HTTP, ruta, parametros y tipo de respuesta.
 *
 * Las funciones son suspending (suspend fun) para usarse con corrutinas.
 */
interface ApiService {
    @POST("api/auth/google")
    suspend fun loginConGoogle(@Body request: GoogleAuthRequest): Response<AuthResponse>
    @POST("api/auth/registro")
    suspend fun registroConEmail(@Body body: RegistroEmailDto): Response<AuthResponse>
    @POST("api/auth/login-email")
    suspend fun loginConEmail(@Body body: LoginEmailDto): Response<AuthResponse>
    @GET("api/health")
    suspend fun health(): Response<Map<String, String>>

    @GET("api/usuarios/me")
    suspend fun getMiPerfil(): Response<UsuarioDto>
    @PUT("api/usuarios/me")
    suspend fun editarPerfil(@Body body: Map<String, @JvmSuppressWildcards Any>): Response<UsuarioDto>

    @GET("api/planes")
    suspend fun getPlanes(): Response<List<PlanDto>>
    @GET("api/planes/mis-planes")
    suspend fun getMisPlanes(): Response<List<PlanDto>>
    @GET("api/planes/mis-planes-creados")
    suspend fun getMisPlanesCreados(): Response<List<PlanDto>>
    @GET("api/planes/historial")
    suspend fun getHistorial(): Response<List<PlanDto>>
    @GET("api/planes/{id}")
    suspend fun getPlan(@Path("id") id: Long): Response<PlanDto>
    @POST("api/planes")
    suspend fun crearPlan(@Body body: CrearPlanDto): Response<PlanDto>
    @PUT("api/planes/{id}")
    suspend fun editarPlan(@Path("id") id: Long, @Body body: CrearPlanDto): Response<PlanDto>
    @DELETE("api/planes/{id}")
    suspend fun eliminarPlan(@Path("id") id: Long): Response<Void>
    @GET("api/planes/{id}/participantes")
    suspend fun getParticipantes(@Path("id") id: Long): Response<List<ParticipanteDto>>
    @GET("api/planes/{id}/solicitudes")
    suspend fun getSolicitudesPlan(@Path("id") id: Long): Response<List<ParticipanteDto>>
    @PUT("api/planes/{id}/participantes/{usuarioId}/confirmar")
    suspend fun confirmarParticipante(@Path("id") id: Long, @Path("usuarioId") usuarioId: Long): Response<Void>
    @DELETE("api/planes/{id}/participantes/{usuarioId}")
    suspend fun rechazarParticipante(@Path("id") id: Long, @Path("usuarioId") usuarioId: Long): Response<Void>
    @POST("api/planes/{id}/unirse")
    suspend fun unirseAPlan(@Path("id") id: Long): Response<Void>
    @DELETE("api/planes/{id}/abandonar")
    suspend fun abandonarPlan(@Path("id") id: Long): Response<Void>
    @PATCH("api/planes/{id}/participantes/{usuarioId}/presente")
    suspend fun marcarPresente(@Path("id") id: Long, @Path("usuarioId") usuarioId: Long): Response<Void>

    @GET("api/comunidades")
    suspend fun getComunidades(): Response<List<ComunidadDto>>
    @GET("api/comunidades/mis-comunidades")
    suspend fun getMisComunidades(): Response<List<ComunidadDto>>
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
    @GET("api/comunidades/{id}/planes")
    suspend fun getPlanesComunidad(@Path("id") id: Long): Response<List<PlanDto>>

    @GET("api/amistades")
    suspend fun getAmigos(): Response<List<AmistadDto>>
    @GET("api/amistades/solicitudes")
    suspend fun getSolicitudes(): Response<List<AmistadDto>>
    @GET("api/amistades/enviadas")
    suspend fun getSolicitudesEnviadas(): Response<List<AmistadDto>>
    @POST("api/amistades/{id}")
    suspend fun enviarSolicitud(@Path("id") id: Long): Response<Void>
    @PUT("api/amistades/{id}/aceptar")
    suspend fun aceptarAmistad(@Path("id") id: Long): Response<Void>
    @DELETE("api/amistades/{id}")
    suspend fun eliminarAmistad(@Path("id") id: Long): Response<Void>

    @GET("api/mensajes/conversaciones")
    suspend fun getConversaciones(): Response<List<ConversacionDto>>
    @GET("api/mensajes/{id}")
    suspend fun getConversacion(@Path("id") id: Long): Response<List<MensajeDto>>
    @POST("api/mensajes/{id}")
    suspend fun enviarMensaje(@Path("id") id: Long, @Body body: Map<String, String>): Response<MensajeDto>
    @DELETE("api/mensajes/conversacion/{id}")
    suspend fun borrarConversacion(@Path("id") id: Long): Response<Void>

    @GET("api/planes/categorias")
    suspend fun getCategorias(): Response<List<String>>

    @POST("api/valoraciones")
    suspend fun valorar(@Body body: Map<String, @JvmSuppressWildcards Any>): Response<Void>
}