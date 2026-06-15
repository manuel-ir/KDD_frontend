package com.kdd.kdd_frontend.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.kdd.kdd_frontend.data.TokenDataStore
import com.kdd.kdd_frontend.network.ApiClient
import com.kdd.kdd_frontend.network.dto.AmistadDto
import com.kdd.kdd_frontend.network.dto.MensajeDto
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

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

    private val _amigosState = MutableStateFlow<AmigosState>(AmigosState.Loading)
    val amigosState: StateFlow<AmigosState> = _amigosState

    private val _solicitudesState = MutableStateFlow<SolicitudesState>(SolicitudesState.Success(emptyList()))
    val solicitudesState: StateFlow<SolicitudesState> = _solicitudesState

    private val _mensajesState = MutableStateFlow<MensajesState>(MensajesState.Loading)
    val mensajesState: StateFlow<MensajesState> = _mensajesState

    var miUserId: Long = -1L
        private set

    init {
        viewModelScope.launch {
            val token = TokenDataStore.getToken(context).first()
            if (token != null) ApiClient.jwtToken = token
            miUserId = TokenDataStore.getUserId(context).first() ?: -1L
            cargarAmigos()
            cargarSolicitudes()
        }
    }

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