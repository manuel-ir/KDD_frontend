package com.kdd.kdd_frontend.viewmodel

import android.app.Application
import android.net.Uri
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.storage.FirebaseStorage
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
import kotlinx.coroutines.tasks.await

/**
 * ViewModel de comunidades.
 *
 * Gestiona la lista de comunidades, el detalle de cada una, sus miembros
 * y los planes asociados. Tambien gestiona la creacion de comunidades
 * y la logica de unirse o abandonarlas.
 */
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

    // Lista completa sin filtrar
    private val _todasLasComunidades = MutableStateFlow<List<CommunityCardData>>(emptyList())

    // Filtros activos para Comunidades
    private val _filtroComCiudad = MutableStateFlow("")
    val filtroComCiudad: StateFlow<String> = _filtroComCiudad
    private val _filtroComEdadMin = MutableStateFlow(18)
    val filtroComEdadMin: StateFlow<Int> = _filtroComEdadMin
    private val _filtroComEdadMax = MutableStateFlow(80)
    val filtroComEdadMax: StateFlow<Int> = _filtroComEdadMax
    private val _filtroComCategoria = MutableStateFlow("")
    val filtroComCategoria: StateFlow<String> = _filtroComCategoria

    private val _misComunidades = MutableStateFlow<ComunidadesState>(ComunidadesState.Loading)
    val misComunidades: StateFlow<ComunidadesState> = _misComunidades

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
                    _todasLasComunidades.value = lista
                    _comunidadesState.value = ComunidadesState.Success(aplicarFiltrosComunidadesLocales(lista))
                } else {
                    _comunidadesState.value = ComunidadesState.Error("Error ${response.code()}")
                }
            } catch (e: Exception) {
                _comunidadesState.value = ComunidadesState.Error("No se pudo conectar con el servidor")
            }
        }
    }

    private fun aplicarFiltrosComunidadesLocales(lista: List<CommunityCardData> = _todasLasComunidades.value): List<CommunityCardData> {
        val ciudad = _filtroComCiudad.value
        val eMin = _filtroComEdadMin.value
        val eMax = _filtroComEdadMax.value
        val categoria = _filtroComCategoria.value
        return lista.filter { com ->
            val pasaCiudad = ciudad.isBlank() || com.ubicacion.contains(ciudad, ignoreCase = true)
            val pasaEdad = com.edadMin <= eMax && com.edadMax >= eMin
            val pasaCategoria = categoria.isBlank() || com.categoria.equals(categoria, ignoreCase = true)
            pasaCiudad && pasaEdad && pasaCategoria
        }
    }

    fun aplicarFiltrosComunidades(ciudad: String, edadMin: Int, edadMax: Int, categoria: String = "") {
        _filtroComCiudad.value = ciudad
        _filtroComEdadMin.value = edadMin
        _filtroComEdadMax.value = edadMax
        _filtroComCategoria.value = categoria
        val todas = _todasLasComunidades.value
        if (todas.isNotEmpty()) {
            _comunidadesState.value = ComunidadesState.Success(aplicarFiltrosComunidadesLocales(todas))
        }
    }

    fun limpiarFiltrosComunidades() {
        aplicarFiltrosComunidades("", 18, 80, "")
    }

    fun cargarMisComunidades() {
        viewModelScope.launch {
            _misComunidades.value = ComunidadesState.Loading
            try {
                val response = ApiClient.api.getMisComunidades()
                if (response.isSuccessful) {
                    val lista = response.body()?.map { it.toCommunityCardData() } ?: emptyList()
                    _misComunidades.value = ComunidadesState.Success(lista)
                } else {
                    _misComunidades.value = ComunidadesState.Error("Error ${response.code()}")
                }
            } catch (e: Exception) {
                _misComunidades.value = ComunidadesState.Error("No se pudo conectar con el servidor")
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
                else {
                    val errorBody = response.errorBody()?.string() ?: ""
                    val msg = try {
                        val json = org.json.JSONObject(errorBody)
                        json.optString("error", json.optString("mensaje", "No se pudo unir a la comunidad"))
                    } catch (_: Exception) {
                        if (errorBody.isNotBlank()) errorBody else "No se pudo unir a la comunidad"
                    }
                    onError(msg)
                }
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

    fun subirFotoComunidad(imageUri: Uri, onSuccess: (String) -> Unit, onError: (String) -> Unit) {
        val user = FirebaseAuth.getInstance().currentUser ?: run {
            onError("Sesión no iniciada")
            return
        }
        val timestamp = System.currentTimeMillis()
        val ref = FirebaseStorage.getInstance().reference
            .child("fotos_comunidades/${user.uid}_$timestamp.jpg")
        viewModelScope.launch {
            try {
                ref.putFile(imageUri).await()
                val url = ref.downloadUrl.await().toString()
                onSuccess(url)
            } catch (e: Exception) {
                onError("Error al subir la foto: ${e.message}")
            }
        }
    }

    fun crearComunidad(
        nombre: String,
        descripcion: String,
        ubicacion: String,
        edadMin: Int,
        edadMax: Int,
        fotoComunidadUrl: String? = null,
        categoria: String? = null,
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
                    edadMax = edadMax,
                    fotoComunidadUrl = fotoComunidadUrl,
                    categoria = categoria
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
    adminNombre = adminNombre ?: "",
    categoria = categoria ?: ""
)