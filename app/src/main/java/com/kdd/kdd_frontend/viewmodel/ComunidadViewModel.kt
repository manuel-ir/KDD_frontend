package com.kdd.kdd_frontend.viewmodel

import android.app.Application
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.kdd.kdd_frontend.data.TokenDataStore
import com.kdd.kdd_frontend.network.ApiClient
import com.kdd.kdd_frontend.network.dto.ComunidadDto
import com.kdd.kdd_frontend.network.dto.CrearComunidadDto
import com.kdd.kdd_frontend.network.dto.MiembroComunidadDto
import com.kdd.kdd_frontend.network.dto.PlanDto
import com.kdd.kdd_frontend.ui.components.CommunityCardData
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.first
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

class ComunidadViewModel(application: Application) : AndroidViewModel(application) {

    private val context = application.applicationContext

    private val _comunidadesState = MutableStateFlow<ComunidadesState>(ComunidadesState.Loading)
    val comunidadesState: StateFlow<ComunidadesState> = _comunidadesState

    private val _detalleState = MutableStateFlow<ComunidadDetalleState>(ComunidadDetalleState.Loading)
    val detalleState: StateFlow<ComunidadDetalleState> = _detalleState

    private val _miembros = MutableStateFlow<List<MiembroComunidadDto>>(emptyList())
    val miembros: StateFlow<List<MiembroComunidadDto>> = _miembros

    private val _planesComunidad = MutableStateFlow<List<PlanDto>>(emptyList())
    val planesComunidad: StateFlow<List<PlanDto>> = _planesComunidad

    var miUserId: Long = -1L
        private set

    init {
        viewModelScope.launch {
            miUserId = TokenDataStore.getUserId(context).first() ?: -1L
        }
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

    fun cargarMiembros(id: Long) {
        viewModelScope.launch {
            try {
                val response = ApiClient.api.getMiembrosComunidad(id)
                if (response.isSuccessful) {
                    _miembros.value = response.body() ?: emptyList()
                }
            } catch (e: Exception) {
                Log.e("ComunidadVM", "Error cargando miembros: ${e.message}")
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

    fun abandonarComunidad(id: Long, onSuccess: () -> Unit, onError: (String) -> Unit) {
        viewModelScope.launch {
            try {
                val response = ApiClient.api.abandonarComunidad(id)
                if (response.isSuccessful) onSuccess()
                else onError("No se pudo abandonar la comunidad")
            } catch (e: Exception) {
                onError("Error de conexión")
            }
        }
    }

    fun enviarSolicitudAmistad(destinatarioId: Long, onSuccess: () -> Unit, onError: (String) -> Unit) {
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

    fun crearComunidad(
        nombre: String,
        descripcion: String,
        ubicacion: String,
        edadMin: Int,
        edadMax: Int,
        onSuccess: () -> Unit,
        onError: (String) -> Unit
    ) {
        viewModelScope.launch {
            try {
                val body = CrearComunidadDto(
                    nombre = nombre,
                    descripcion = descripcion,
                    ubicacion = ubicacion,
                    edadMin = edadMin,
                    edadMax = edadMax
                )
                val response = ApiClient.api.crearComunidad(body)
                if (response.isSuccessful) {
                    cargarComunidades()
                    onSuccess()
                } else {
                    val errorBody = response.errorBody()?.string() ?: ""
                    Log.e("ComunidadVM", "Error ${response.code()}: $errorBody")
                    onError("Error ${response.code()}: $errorBody")
                }
            } catch (e: Exception) {
                Log.e("ComunidadVM", "Excepcion: ${e.message}", e)
                onError("Sin conexión: ${e.message}")
            }
        }
    }

    fun cargarPlanesComunidad(id: Long) {
        viewModelScope.launch {
            try {
                val response = ApiClient.api.getPlanesComunidad(id)
                if (response.isSuccessful) {
                    _planesComunidad.value = response.body() ?: emptyList()
                }
            } catch (e: Exception) {
                Log.e("ComunidadVM", "Error cargando planes: ${e.message}")
            }
        }
    }
}

fun ComunidadDto.toCommunityCardData() = CommunityCardData(
    id = id,
    nombre = nombre,
    edadMin = edadMin ?: 18,
    edadMax = edadMax ?: 99,
    ubicacion = ubicacion ?: "",
    numMiembros = numMiembros,
    adminNombre = adminNombre ?: ""
)
