package com.kdd.kdd_frontend.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.kdd.kdd_frontend.network.ApiClient
import com.kdd.kdd_frontend.network.dto.CrearPlanDto
import com.kdd.kdd_frontend.network.dto.ParticipanteDto
import com.kdd.kdd_frontend.network.dto.PlanDto
import com.kdd.kdd_frontend.ui.components.PlanCardData
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

sealed class PlanesState {
    object Loading : PlanesState()
    data class Success(val planes: List<PlanCardData>) : PlanesState()
    data class Error(val mensaje: String) : PlanesState()
}

sealed class PlanDetalleState {
    object Loading : PlanDetalleState()
    data class Success(val plan: PlanDto) : PlanDetalleState()
    data class Error(val mensaje: String) : PlanDetalleState()
}

class PlanViewModel : ViewModel() {

    private val _planesState = MutableStateFlow<PlanesState>(PlanesState.Loading)
    val planesState: StateFlow<PlanesState> = _planesState

    private val _detalleState = MutableStateFlow<PlanDetalleState>(PlanDetalleState.Loading)
    val detalleState: StateFlow<PlanDetalleState> = _detalleState

    private val _misPlanes = MutableStateFlow<PlanesState>(PlanesState.Loading)
    val misPlanes: StateFlow<PlanesState> = _misPlanes

    private val _participando = MutableStateFlow(false)
    val participando: StateFlow<Boolean> = _participando

    private val _participantes = MutableStateFlow<List<ParticipanteDto>>(emptyList())
    val participantes: StateFlow<List<ParticipanteDto>> = _participantes

    private val _solicitudesPlan = MutableStateFlow<List<ParticipanteDto>>(emptyList())
    val solicitudesPlan: StateFlow<List<ParticipanteDto>> = _solicitudesPlan

    init {
        cargarPlanes()
    }

    fun cargarMisPlanes() {
        viewModelScope.launch {
            _misPlanes.value = PlanesState.Loading
            try {
                val response = ApiClient.api.getMisPlanes()
                if (response.isSuccessful) {
                    val planes = response.body()?.map { it.toPlanCardData() } ?: emptyList()
                    _misPlanes.value = PlanesState.Success(planes)
                } else {
                    _misPlanes.value = PlanesState.Error("Error ${response.code()}")
                }
            } catch (e: Exception) {
                _misPlanes.value = PlanesState.Error("No se pudo conectar con el servidor")
            }
        }
    }

    fun cargarPlanes() {
        viewModelScope.launch {
            _planesState.value = PlanesState.Loading
            try {
                val response = ApiClient.api.getPlanes()
                if (response.isSuccessful) {
                    val planes = response.body()?.map { it.toPlanCardData() } ?: emptyList()
                    _planesState.value = PlanesState.Success(planes)
                } else {
                    _planesState.value = PlanesState.Error("Error ${response.code()}")
                }
            } catch (e: Exception) {
                _planesState.value = PlanesState.Error("No se pudo conectar con el servidor")
            }
        }
    }

    fun cargarDetalle(planId: Long) {
        viewModelScope.launch {
            _detalleState.value = PlanDetalleState.Loading
            try {
                val response = ApiClient.api.getPlan(planId)
                if (response.isSuccessful) {
                    val plan = response.body()!!
                    _detalleState.value = PlanDetalleState.Success(plan)
                    _participando.value = plan.miembro
                } else {
                    _detalleState.value = PlanDetalleState.Error("Error ${response.code()}")
                }
            } catch (e: Exception) {
                _detalleState.value = PlanDetalleState.Error("No se pudo conectar con el servidor")
            }
        }
    }

    fun crearPlan(dto: CrearPlanDto, onSuccess: () -> Unit, onError: (String) -> Unit) {
        viewModelScope.launch {
            try {
                val response = ApiClient.api.crearPlan(dto)
                if (response.isSuccessful) {
                    cargarPlanes()
                    onSuccess()
                } else {
                    onError("Error al crear el plan (${response.code()})")
                }
            } catch (e: Exception) {
                onError("No se pudo conectar con el servidor")
            }
        }
    }

    fun cargarParticipantes(planId: Long) {
        viewModelScope.launch {
            try {
                val response = ApiClient.api.getParticipantes(planId)
                if (response.isSuccessful) {
                    _participantes.value = response.body() ?: emptyList()
                }
            } catch (e: Exception) {
                // silencioso
            }
        }
    }

    fun valorar(
        valoradoId: Long,
        planId: Long,
        puntuacion: Int,
        onSuccess: () -> Unit,
        onError: (String) -> Unit
    ) {
        viewModelScope.launch {
            try {
                val body = mapOf<String, Any>(
                    "idValorado" to valoradoId,
                    "idPlan" to planId,
                    "puntuacion" to puntuacion
                )
                val response = ApiClient.api.valorar(body)
                if (response.isSuccessful) onSuccess()
                else onError("Error al valorar (${response.code()})")
            } catch (e: Exception) {
                onError("No se pudo conectar con el servidor")
            }
        }
    }

    fun cargarSolicitudesPlan(planId: Long) {
        viewModelScope.launch {
            try {
                val response = ApiClient.api.getSolicitudesPlan(planId)
                if (response.isSuccessful) {
                    _solicitudesPlan.value = response.body() ?: emptyList()
                }
            } catch (e: Exception) { /* silencioso */ }
        }
    }

    fun confirmarParticipante(planId: Long, usuarioId: Long, onSuccess: () -> Unit) {
        viewModelScope.launch {
            try {
                val response = ApiClient.api.confirmarParticipante(planId, usuarioId)
                if (response.isSuccessful) {
                    cargarSolicitudesPlan(planId)
                    cargarParticipantes(planId)
                    onSuccess()
                }
            } catch (e: Exception) { /* silencioso */ }
        }
    }

    fun rechazarParticipante(planId: Long, usuarioId: Long) {
        viewModelScope.launch {
            try {
                ApiClient.api.rechazarParticipante(planId, usuarioId)
                cargarSolicitudesPlan(planId)
            } catch (e: Exception) { /* silencioso */ }
        }
    }

    fun enviarSolicitud(destinatarioId: Long, onSuccess: () -> Unit, onError: (String) -> Unit) {
        viewModelScope.launch {
            try {
                val response = ApiClient.api.enviarSolicitud(destinatarioId)
                if (response.isSuccessful) onSuccess()
                else onError("No se pudo enviar la solicitud")
            } catch (e: Exception) {
                onError("Error de conexión")
            }
        }
    }

    fun unirseAPlan(planId: Long, onSuccess: () -> Unit, onError: (String) -> Unit) {
        viewModelScope.launch {
            try {
                val response = ApiClient.api.unirseAPlan(planId)
                if (response.isSuccessful) {
                    _participando.value = true
                    onSuccess()
                } else {
                    onError("No se pudo unir al plan")
                }
            } catch (e: Exception) {
                onError("Error de conexión")
            }
        }
    }

    fun abandonarPlan(planId: Long, onSuccess: () -> Unit, onError: (String) -> Unit) {
        viewModelScope.launch {
            try {
                val response = ApiClient.api.abandonarPlan(planId)
                if (response.isSuccessful) {
                    _participando.value = false
                    onSuccess()
                } else {
                    onError("No se pudo abandonar el plan")
                }
            } catch (e: Exception) {
                onError("Error de conexión")
            }
        }
    }
}

fun PlanDto.toPlanCardData(): PlanCardData {
    val dia = fechaEvento ?: ""
    val hora = horaEvento?.take(5) ?: ""
    return PlanCardData(
        id = id,
        titulo = titulo,
        categoria = categoria ?: "",
        descripcion = descripcion ?: "",
        dia = dia,
        hora = hora,
        distanciaKm = "",
        ubicacion = ubicacionTexto,
        anfitrionNombre = anfitrionNombre ?: "",
        latitud = latitud,
        longitud = longitud
    )
}
