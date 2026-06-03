package com.kdd.kdd_frontend.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.kdd.kdd_frontend.network.ApiClient
import com.kdd.kdd_frontend.network.dto.ComunidadDto
import com.kdd.kdd_frontend.ui.components.CommunityCardData
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

sealed class ComunidadesState {
    object Loading : ComunidadesState()
    data class Success(val comunidades: List<CommunityCardData>) : ComunidadesState()
    data class Error(val mensaje: String) : ComunidadesState()
}

sealed class ComunidadDetalleState {
    object Loading : ComunidadDetalleState()
    data class Success(val comunidad: ComunidadDto) : ComunidadDetalleState()
    data class Error(val mensaje: String) : ComunidadDetalleState()
}

class ComunidadViewModel : ViewModel() {

    private val _comunidadesState = MutableStateFlow<ComunidadesState>(ComunidadesState.Loading)
    val comunidadesState: StateFlow<ComunidadesState> = _comunidadesState

    private val _detalleState = MutableStateFlow<ComunidadDetalleState>(ComunidadDetalleState.Loading)
    val detalleState: StateFlow<ComunidadDetalleState> = _detalleState

    init {
        cargarComunidades()
    }

    fun cargarComunidades() {
        viewModelScope.launch {
            _comunidadesState.value = ComunidadesState.Loading
            try {
                val response = ApiClient.api.getComunidades()
                if (response.isSuccessful) {
                    val lista = response.body()?.map { it.toCommunityCardData() } ?: emptyList()
                    _comunidadesState.value = ComunidadesState.Success(lista)
                } else {
                    _comunidadesState.value = ComunidadesState.Error("Error ${response.code()}")
                }
            } catch (e: Exception) {
                _comunidadesState.value = ComunidadesState.Error("No se pudo conectar con el servidor")
            }
        }
    }

    fun cargarDetalle(id: Long) {
        viewModelScope.launch {
            _detalleState.value = ComunidadDetalleState.Loading
            try {
                val response = ApiClient.api.getComunidad(id)
                if (response.isSuccessful) {
                    _detalleState.value = ComunidadDetalleState.Success(response.body()!!)
                } else {
                    _detalleState.value = ComunidadDetalleState.Error("Error ${response.code()}")
                }
            } catch (e: Exception) {
                _detalleState.value = ComunidadDetalleState.Error("No se pudo conectar con el servidor")
            }
        }
    }

    fun unirseAComunidad(id: Long, onSuccess: () -> Unit, onError: (String) -> Unit) {
        viewModelScope.launch {
            try {
                val response = ApiClient.api.unirseAComunidad(id)
                if (response.isSuccessful) onSuccess()
                else onError("No se pudo unir a la comunidad")
            } catch (e: Exception) {
                onError("Error de conexión")
            }
        }
    }

    fun crearComunidad(
        nombre: String,
        descripcion: String,
        edadMin: Int,
        edadMax: Int,
        onSuccess: () -> Unit,
        onError: (String) -> Unit
    ) {
        viewModelScope.launch {
            try {
                val body = mapOf(
                    "nombre" to nombre,
                    "descripcion" to descripcion,
                    "edadMin" to edadMin,
                    "edadMax" to edadMax
                )
                val response = ApiClient.api.crearComunidad(body)
                if (response.isSuccessful) {
                    cargarComunidades()
                    onSuccess()
                } else {
                    onError("Error ${response.code()}")
                }
            } catch (e: Exception) {
                onError("No se pudo conectar con el servidor")
            }
        }
    }
}

fun ComunidadDto.toCommunityCardData() = CommunityCardData(
    id = id,
    nombre = nombre,
    edadMin = edadMin ?: 18,
    edadMax = edadMax ?: 99,
    ubicacion = "",
    numMiembros = numMiembros,
    adminNombre = adminNombre ?: ""
)
