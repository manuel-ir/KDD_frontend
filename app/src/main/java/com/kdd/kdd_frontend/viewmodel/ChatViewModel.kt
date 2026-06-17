package com.kdd.kdd_frontend.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.kdd.kdd_frontend.data.TokenDataStore
import com.kdd.kdd_frontend.network.ApiClient
import com.kdd.kdd_frontend.network.dto.AmistadDto
import com.kdd.kdd_frontend.network.dto.ConversacionDto
import com.kdd.kdd_frontend.network.dto.MensajeDto
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

/**
 * ViewModel de mensajeria.
 *
 * Gestiona las conversaciones y mensajes entre usuarios:
 * - Cargar la lista de conversaciones activas (independiente de si son amigos).
 * - Cargar el historial de mensajes con un usuario concreto.
 * - Enviar nuevos mensajes.
 * - Borrar el historial de una conversacion.
 * - Eliminar a un amigo (sin borrar el chat).
 *
 * Implementa un sondeo cada 3 segundos para actualizar los mensajes
 * en tiempo real mientras la pantalla de chat esta abierta.
 * El sondeo se detiene automaticamente al salir de la pantalla.
 */

// Estado de la lista de conversaciones (historial de chats)
sealed class ConversacionesState {
    object Loading : ConversacionesState()
    data class Success(val conversaciones: List<ConversacionDto>) : ConversacionesState()
    data class Error(val mensaje: String) : ConversacionesState()
}

// Estado de la lista de amigos (para enviar solicitudes y aceptarlas)
sealed class AmigosState {
    object Loading : AmigosState()
    data class Success(val amigos: List<AmistadDto>) : AmigosState()
    data class Error(val mensaje: String) : AmigosState()
}

sealed class SolicitudesState {
    object Loading : SolicitudesState()
    data class Success(val solicitudes: List<AmistadDto>) : SolicitudesState()
    data class Error(val mensaje: String) : SolicitudesState()
}

sealed class MensajesState {
    object Loading : MensajesState()
    data class Success(val mensajes: List<MensajeDto>) : MensajesState()
    data class Error(val mensaje: String) : MensajesState()
}

class ChatViewModel(application: Application) : AndroidViewModel(application) {

    private val context = application.applicationContext

    // Lista de conversaciones activas (se usa en ChatsScreen en lugar de amigos)
    private val _conversacionesState = MutableStateFlow<ConversacionesState>(ConversacionesState.Loading)
    val conversacionesState: StateFlow<ConversacionesState> = _conversacionesState

    // Lista de amigos confirmados (se usa solo para mostrar solicitudes)
    private val _amigosState = MutableStateFlow<AmigosState>(AmigosState.Loading)
    val amigosState: StateFlow<AmigosState> = _amigosState

    private val _solicitudesState = MutableStateFlow<SolicitudesState>(SolicitudesState.Success(emptyList()))
    val solicitudesState: StateFlow<SolicitudesState> = _solicitudesState

    private val _mensajesState = MutableStateFlow<MensajesState>(MensajesState.Loading)
    val mensajesState: StateFlow<MensajesState> = _mensajesState

    var miUserId: Long = -1L
        private set

    private var pollingJob: Job? = null

    init {
        viewModelScope.launch {
            val token = TokenDataStore.getToken(context).first()
            if (token != null) ApiClient.jwtToken = token
            miUserId = TokenDataStore.getUserId(context).first() ?: -1L
            cargarConversaciones()
            cargarSolicitudes()
        }
    }

    /**
     * Carga conversaciones activas + amigos confirmados y los fusiona.
     *
     * Los amigos que aun no tienen mensajes aparecen al final con texto vacio,
     * para que el usuario pueda iniciar la conversacion desde aqui.
     */
    fun cargarConversaciones() {
        viewModelScope.launch {
            _conversacionesState.value = ConversacionesState.Loading
            try {
                val convResp = ApiClient.api.getConversaciones()
                val amigosResp = ApiClient.api.getAmigos()

                val conversaciones = if (convResp.isSuccessful) convResp.body() ?: emptyList() else emptyList()
                val amigos = if (amigosResp.isSuccessful) amigosResp.body() ?: emptyList() else emptyList()

                // IDs de usuarios con los que ya hay conversacion
                val idsConConversacion = conversaciones.map { it.usuarioId }.toSet()

                // Amigos confirmados sin conversacion todavia
                val amigosNuevos = amigos
                    .filter { it.estado == "confirmado" && it.idAmigo !in idsConConversacion }
                    .map { amigo ->
                        ConversacionDto(
                            usuarioId = amigo.idAmigo,
                            nombre = amigo.nombre,
                            fotoPerfil = amigo.fotoPerfil,
                            ultimoMensaje = "",
                            fechaUltimoMensaje = ""
                        )
                    }

                val listaFinal = conversaciones + amigosNuevos
                _conversacionesState.value = if (convResp.isSuccessful) {
                    ConversacionesState.Success(listaFinal)
                } else {
                    ConversacionesState.Error("Error ${convResp.code()}")
                }
            } catch (e: Exception) {
                _conversacionesState.value = ConversacionesState.Error("No se pudo conectar con el servidor")
            }
        }
    }

    // Carga la lista de amigos confirmados (para gestion de amistad)
    fun cargarAmigos() {
        viewModelScope.launch {
            _amigosState.value = AmigosState.Loading
            try {
                val response = ApiClient.api.getAmigos()
                if (response.isSuccessful) {
                    _amigosState.value = AmigosState.Success(response.body() ?: emptyList())
                } else {
                    _amigosState.value = AmigosState.Error("Error ${response.code()}")
                }
            } catch (e: Exception) {
                _amigosState.value = AmigosState.Error("No se pudo conectar con el servidor")
            }
        }
    }

    fun cargarSolicitudes() {
        viewModelScope.launch {
            try {
                val response = ApiClient.api.getSolicitudes()
                if (response.isSuccessful) {
                    _solicitudesState.value = SolicitudesState.Success(response.body() ?: emptyList())
                } else {
                    _solicitudesState.value = SolicitudesState.Error("Error ${response.code()}")
                }
            } catch (e: Exception) {
                _solicitudesState.value = SolicitudesState.Error("No se pudo conectar con el servidor")
            }
        }
    }

    fun aceptarSolicitud(amigoPotencialId: Long) {
        viewModelScope.launch {
            try {
                val response = ApiClient.api.aceptarAmistad(amigoPotencialId)
                if (response.isSuccessful) {
                    cargarSolicitudes()
                    cargarConversaciones()
                }
            } catch (e: Exception) { /* silencioso */ }
        }
    }

    // Elimina la amistad pero NO borra el chat; el historial persiste
    fun eliminarAmigo(amigoId: Long, onSuccess: () -> Unit, onError: (String) -> Unit) {
        viewModelScope.launch {
            try {
                val response = ApiClient.api.eliminarAmistad(amigoId)
                if (response.isSuccessful) {
                    // No llamamos a cargarConversaciones() aqui para que el chat siga visible
                    onSuccess()
                } else {
                    onError("No se pudo eliminar al amigo")
                }
            } catch (e: Exception) {
                onError("Error de conexion")
            }
        }
    }

    fun rechazarSolicitud(amigoPotencialId: Long) {
        viewModelScope.launch {
            try {
                val response = ApiClient.api.eliminarAmistad(amigoPotencialId)
                if (response.isSuccessful) {
                    cargarSolicitudes()
                }
            } catch (e: Exception) { /* silencioso */ }
        }
    }

    fun cargarConversacion(userId: Long) {
        viewModelScope.launch {
            _mensajesState.value = MensajesState.Loading
            try {
                val response = ApiClient.api.getConversacion(userId)
                if (response.isSuccessful) {
                    _mensajesState.value = MensajesState.Success(response.body() ?: emptyList())
                } else {
                    _mensajesState.value = MensajesState.Error("Error ${response.code()}")
                }
            } catch (e: Exception) {
                _mensajesState.value = MensajesState.Error("No se pudo conectar con el servidor")
            }
        }
    }

    /** Inicia sondeo cada 3 s. Llamar al entrar al chat, stopPolling() al salir. */
    fun startPolling(userId: Long) {
        pollingJob?.cancel()
        pollingJob = viewModelScope.launch {
            while (true) {
                delay(3_000)
                try {
                    val response = ApiClient.api.getConversacion(userId)
                    if (response.isSuccessful) {
                        val nuevos = response.body() ?: emptyList()
                        val actuales = (_mensajesState.value as? MensajesState.Success)?.mensajes
                        if (nuevos != actuales) {
                            _mensajesState.value = MensajesState.Success(nuevos)
                        }
                    }
                } catch (_: Exception) { /* silencioso, reintenta en 3s */ }
            }
        }
    }

    fun stopPolling() {
        pollingJob?.cancel()
        pollingJob = null
    }

    fun borrarMensajes(otroUserId: Long, onSuccess: () -> Unit) {
        viewModelScope.launch {
            try {
                val response = ApiClient.api.borrarConversacion(otroUserId)
                if (response.isSuccessful) {
                    _mensajesState.value = MensajesState.Success(emptyList())
                    cargarConversaciones()
                    onSuccess()
                }
            } catch (e: Exception) { /* silencioso */ }
        }
    }

    fun enviarMensaje(destinatarioId: Long, contenido: String, onSuccess: () -> Unit) {
        viewModelScope.launch {
            try {
                val body = mapOf("contenido" to contenido)
                val response = ApiClient.api.enviarMensaje(destinatarioId, body)
                if (response.isSuccessful) {
                    cargarConversacion(destinatarioId)
                    onSuccess()
                }
            } catch (e: Exception) { /* silencioso */ }
        }
    }
}
