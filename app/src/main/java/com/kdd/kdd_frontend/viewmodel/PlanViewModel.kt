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

class PlanViewModel : ViewModel() {

    private val _planesState = MutableStateFlow<PlanesState>(PlanesState.Loading)
    val planesState: StateFlow<PlanesState> = _planesState

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
