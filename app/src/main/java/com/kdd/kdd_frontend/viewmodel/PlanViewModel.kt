package com.kdd.kdd_frontend.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.kdd.kdd_frontend.network.ApiClient
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

    private val _participando = MutableStateFlow(false)
    val participando: StateFlow<Boolean> = _participando

    init {
        cargarPlanes()
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
                    _detalleState.value = PlanDetalleState.Success(response.body()!!)
                } else {
                    _detalleState.value = PlanDetalleState.Error("Error ${response.code()}")
                }
            } catch (e: Exception) {
                _detalleState.value = PlanDetalleState.Error("No se pudo conectar con el servidor")
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
        anfitrionNombre = anfitrionNombre ?: ""
    )
}
