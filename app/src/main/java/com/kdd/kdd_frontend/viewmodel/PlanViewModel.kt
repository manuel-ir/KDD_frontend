package com.kdd.kdd_frontend.viewmodel

import android.app.Application
import android.net.Uri
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.storage.FirebaseStorage
import com.kdd.kdd_frontend.data.TokenDataStore
import com.kdd.kdd_frontend.network.ApiClient
import com.kdd.kdd_frontend.network.dto.CrearPlanDto
import com.kdd.kdd_frontend.network.dto.ParticipanteDto
import com.kdd.kdd_frontend.network.dto.PlanDto
import com.kdd.kdd_frontend.ui.components.PlanCardData
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime
import java.time.format.DateTimeFormatter

/**
 * ViewModel de planes.
 *
 * Centraliza toda la logica de acceso a datos relacionada con los planes:
 * - Cargar la lista de planes disponibles (pantalla Explora y Mapa).
 * - Cargar los planes del usuario (Calendario).
 * - Crear, editar y eliminar planes.
 * - Unirse y abandonar planes.
 * - Marcar presencia de participantes.
 * - Cargar el detalle de un plan y su lista de participantes.
 *
 * Convierte los PlanDto del backend en PlanCardData para los componentes
 * visuales, anadiendo la distancia calculada desde la ubicacion del usuario.
 */
sealed class PlanesState {
    object Loading : PlanesState()
    data class Success(val planes: List<PlanCardData>) : PlanesState()
    data class Error(val mensaje: String) : PlanesState()
}

sealed class PlanDetalleState {
    object Loading : PlanDetalleState()
    data class Success(val plan: PlanDto) : PlanDetalleState()
    data class Error(val mensaje: String, val codigo: Int = 0) : PlanDetalleState()
}

class PlanViewModel(application: Application) : AndroidViewModel(application) {

    private val context = application.applicationContext

    var miUserId: Long = -1L
        private set

    private val _planesState = MutableStateFlow<PlanesState>(PlanesState.Loading)
    val planesState: StateFlow<PlanesState> = _planesState

    // Lista completa sin filtrar (para poder re-filtrar sin recargar del servidor)
    private val _todosLosPlanes = MutableStateFlow<List<PlanCardData>>(emptyList())

    // Filtros activos para Explora
    private val _filtroCategoria = MutableStateFlow("")
    val filtroCategoria: StateFlow<String> = _filtroCategoria
    private val _filtroFecha = MutableStateFlow<String?>(null)
    val filtroFecha: StateFlow<String?> = _filtroFecha
    private val _filtroEdadMin = MutableStateFlow(18)
    val filtroEdadMin: StateFlow<Int> = _filtroEdadMin
    private val _filtroEdadMax = MutableStateFlow(80)
    val filtroEdadMax: StateFlow<Int> = _filtroEdadMax

    private val _detalleState = MutableStateFlow<PlanDetalleState>(PlanDetalleState.Loading)
    val detalleState: StateFlow<PlanDetalleState> = _detalleState

    private val _misPlanes = MutableStateFlow<PlanesState>(PlanesState.Loading)
    val misPlanes: StateFlow<PlanesState> = _misPlanes

    private val _misPlanesCreados = MutableStateFlow<PlanesState>(PlanesState.Loading)
    val misPlanesCreados: StateFlow<PlanesState> = _misPlanesCreados

    private val _historial = MutableStateFlow<PlanesState>(PlanesState.Loading)
    val historial: StateFlow<PlanesState> = _historial

    private val _participando = MutableStateFlow(false)
    val participando: StateFlow<Boolean> = _participando

    private val _participantes = MutableStateFlow<List<ParticipanteDto>>(emptyList())
    val participantes: StateFlow<List<ParticipanteDto>> = _participantes

    private val _solicitudesPlan = MutableStateFlow<List<ParticipanteDto>>(emptyList())
    val solicitudesPlan: StateFlow<List<ParticipanteDto>> = _solicitudesPlan

    private val _solicitudesEnviadas = MutableStateFlow<Set<Long>>(emptySet())
    val solicitudesEnviadas: StateFlow<Set<Long>> = _solicitudesEnviadas

    private val _amigos = MutableStateFlow<Set<Long>>(emptySet())
    val amigos: StateFlow<Set<Long>> = _amigos

    // Lista de categorias disponibles para los filtros
    private val _categorias = MutableStateFlow<List<String>>(emptyList())
    val categorias: StateFlow<List<String>> = _categorias

    init {
        viewModelScope.launch {
            miUserId = TokenDataStore.getUserId(context).first() ?: -1L
            inicializarEstadosAmistad()
        }
        cargarPlanes()
        cargarCategorias()
    }

    fun cargarCategorias() {
        viewModelScope.launch {
            try {
                val response = ApiClient.api.getCategorias()
                if (response.isSuccessful) {
                    _categorias.value = response.body() ?: emptyList()
                }
            } catch (_: Exception) { /* silencioso */ }
        }
    }

    fun inicializarEstadosAmistad() {
        viewModelScope.launch {
            try {
                val enviadas = ApiClient.api.getSolicitudesEnviadas()
                if (enviadas.isSuccessful) {
                    _solicitudesEnviadas.value = enviadas.body()?.map { it.idAmigo }?.toSet() ?: emptySet()
                }
            } catch (_: Exception) {}
            try {
                val amigosResp = ApiClient.api.getAmigos()
                if (amigosResp.isSuccessful) {
                    _amigos.value = amigosResp.body()?.map { it.idAmigo }?.toSet() ?: emptySet()
                }
            } catch (_: Exception) {}
        }
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

    fun cargarHistorial() {
        viewModelScope.launch {
            _historial.value = PlanesState.Loading
            try {
                val response = ApiClient.api.getHistorial()
                if (response.isSuccessful) {
                    val planes = response.body()?.map { it.toPlanCardData() } ?: emptyList()
                    _historial.value = PlanesState.Success(planes)
                } else {
                    _historial.value = PlanesState.Error("Error ${response.code()}")
                }
            } catch (e: Exception) {
                _historial.value = PlanesState.Error("No se pudo conectar con el servidor")
            }
        }
    }

    fun cargarMisPlanesCreados() {
        viewModelScope.launch {
            _misPlanesCreados.value = PlanesState.Loading
            try {
                val response = ApiClient.api.getMisPlanesCreados()
                if (response.isSuccessful) {
                    val planes = response.body()?.map { it.toPlanCardData() } ?: emptyList()
                    _misPlanesCreados.value = PlanesState.Success(planes)
                } else {
                    _misPlanesCreados.value = PlanesState.Error("Error ${response.code()}")
                }
            } catch (e: Exception) {
                _misPlanesCreados.value = PlanesState.Error("No se pudo conectar con el servidor")
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
                    _todosLosPlanes.value = planes
                    _planesState.value = PlanesState.Success(aplicarFiltrosLocales(planes))
                } else {
                    _planesState.value = PlanesState.Error("Error ${response.code()}")
                }
            } catch (e: Exception) {
                _planesState.value = PlanesState.Error("No se pudo conectar con el servidor")
            }
        }
    }

    private fun planHaCaducado(plan: PlanCardData): Boolean {
        val fmt = DateTimeFormatter.ofPattern("yyyy-MM-dd")
        val timeFmt = DateTimeFormatter.ofPattern("HH:mm")
        val fecha = runCatching { LocalDate.parse(plan.dia, fmt) }.getOrNull() ?: return false
        val ahora = LocalDateTime.now()
        val horaFin = plan.horaHasta?.let { runCatching { LocalTime.parse(it.take(5), timeFmt) }.getOrNull() }
        val horaIni = plan.hora.takeIf { it.isNotBlank() }
            ?.let { runCatching { LocalTime.parse(it.take(5), timeFmt) }.getOrNull() }
        return if (horaFin != null) {
            ahora.isAfter(LocalDateTime.of(fecha, horaFin))
        } else {
            ahora.isAfter(LocalDateTime.of(fecha, horaIni ?: LocalTime.MIDNIGHT).plusHours(24))
        }
    }

    private fun aplicarFiltrosLocales(planes: List<PlanCardData> = _todosLosPlanes.value): List<PlanCardData> {
        val cat = _filtroCategoria.value
        val fecha = _filtroFecha.value
        return planes.filter { plan ->
            if (planHaCaducado(plan)) return@filter false
            val pasaCategoria = cat.isBlank() || plan.categoria.equals(cat, ignoreCase = true)
            val pasaFecha = fecha == null || plan.dia == fecha
            pasaCategoria && pasaFecha
        }
    }

    fun aplicarFiltrosExplora(categoria: String, fecha: String?, edadMin: Int, edadMax: Int) {
        _filtroCategoria.value = categoria
        _filtroFecha.value = fecha
        _filtroEdadMin.value = edadMin
        _filtroEdadMax.value = edadMax
        val todos = _todosLosPlanes.value
        if (todos.isNotEmpty()) {
            _planesState.value = PlanesState.Success(aplicarFiltrosLocales(todos))
        }
    }

    fun limpiarFiltrosExplora() {
        aplicarFiltrosExplora("", null, 18, 80)
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
                    _detalleState.value = PlanDetalleState.Error("Error ${response.code()}", response.code())
                }
            } catch (e: Exception) {
                _detalleState.value = PlanDetalleState.Error("No se pudo conectar con el servidor")
            }
        }
    }

    fun crearPlan(dto: CrearPlanDto, onSuccess: (Long) -> Unit, onError: (String) -> Unit) {
        viewModelScope.launch {
            try {
                val response = ApiClient.api.crearPlan(dto)
                if (response.isSuccessful) {
                    val planId = response.body()!!.id
                    cargarPlanes()
                    onSuccess(planId)
                } else {
                    val errorBody = response.errorBody()?.string() ?: ""
                    val msg = extractMessage(errorBody).ifBlank { "Error al crear el plan (${response.code()})" }
                    onError(msg)
                }
            } catch (e: Exception) {
                onError("No se pudo conectar con el servidor")
            }
        }
    }

    fun editarPlan(planId: Long, dto: CrearPlanDto, onSuccess: () -> Unit, onError: (String) -> Unit) {
        viewModelScope.launch {
            try {
                val response = ApiClient.api.editarPlan(planId, dto)
                if (response.isSuccessful) {
                    cargarDetalle(planId)
                    onSuccess()
                } else {
                    onError("Error al editar el plan (${response.code()})")
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
                if (response.isSuccessful) {
                    _solicitudesEnviadas.value = _solicitudesEnviadas.value + destinatarioId
                    onSuccess()
                } else onError("No se pudo enviar la solicitud")
            } catch (e: Exception) {
                onError("Error de conexión")
            }
        }
    }

    fun marcarPresente(planId: Long, usuarioId: Long, onSuccess: () -> Unit, onError: (String) -> Unit = {}) {
        viewModelScope.launch {
            try {
                val response = ApiClient.api.marcarPresente(planId, usuarioId)
                if (response.isSuccessful) {
                    cargarParticipantes(planId)
                    onSuccess()
                } else {
                    val errorBody = response.errorBody()?.string() ?: ""
                    onError(extractMessage(errorBody).ifBlank { "No se pudo confirmar la asistencia (${response.code()})" })
                }
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
                    val errorBody = response.errorBody()?.string() ?: ""
                    val msg = when {
                        errorBody.contains("edad mínima") -> extractMessage(errorBody)
                        errorBody.contains("edad máxima") -> extractMessage(errorBody)
                        errorBody.contains("completo") -> "Este plan ya está completo"
                        else -> "No se pudo unir al plan"
                    }
                    onError(msg)
                }
            } catch (e: Exception) {
                onError("Error de conexión")
            }
        }
    }

    fun subirFotoPlan(imageUri: Uri, onSuccess: (String) -> Unit, onError: (String) -> Unit) {
        val user = FirebaseAuth.getInstance().currentUser ?: run {
            onError("Sesión no iniciada en Firebase")
            return
        }
        viewModelScope.launch {
            try {
                val ref = FirebaseStorage.getInstance().reference
                    .child("fotos_planes/${user.uid}_${System.currentTimeMillis()}.jpg")
                ref.putFile(imageUri).await()
                val url = ref.downloadUrl.await().toString()
                onSuccess(url)
            } catch (e: Exception) {
                onError("Error al subir la foto: ${e.message}")
            }
        }
    }

    fun eliminarPlan(planId: Long, onSuccess: () -> Unit, onError: (String) -> Unit) {
        viewModelScope.launch {
            try {
                val response = ApiClient.api.eliminarPlan(planId)
                if (response.isSuccessful) {
                    cargarPlanes()
                    cargarMisPlanes()
                    onSuccess()
                } else {
                    val errorBody = response.errorBody()?.string() ?: ""
                    onError(extractMessage(errorBody).ifBlank { "Error al eliminar el plan" })
                }
            } catch (e: Exception) {
                onError("No se pudo conectar con el servidor")
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

    private fun extractMessage(errorBody: String): String {
        // Backend devuelve JSON: {"error": "mensaje"} o {"mensaje": "mensaje"}
        return try {
            val json = org.json.JSONObject(errorBody)
            json.optString("error", json.optString("mensaje", errorBody))
        } catch (e: Exception) {
            errorBody
        }
    }
}

fun PlanDto.toPlanCardData(): PlanCardData {
    val dia = fechaEvento ?: ""
    val hora = horaEvento?.take(5) ?: ""
    val hasta = horaHasta?.take(5)
    return PlanCardData(
        id = id,
        titulo = titulo,
        categoria = categoria ?: "",
        descripcion = descripcion ?: "",
        dia = dia,
        hora = hora,
        horaHasta = hasta,
        distanciaKm = "",
        ubicacion = ubicacionTexto,
        fotoUrl = fotoPlanUrl,
        anfitrionNombre = anfitrionNombre ?: "",
        latitud = latitud,
        longitud = longitud
    )
}