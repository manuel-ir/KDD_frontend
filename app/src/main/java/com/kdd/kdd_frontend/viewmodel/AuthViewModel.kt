package com.kdd.kdd_frontend.viewmodel

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.kdd.kdd_frontend.data.TokenDataStore
import com.kdd.kdd_frontend.network.ApiClient
import com.kdd.kdd_frontend.network.dto.GoogleAuthRequest
import com.kdd.kdd_frontend.network.dto.UsuarioDto
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

sealed class AuthState {
    object Idle : AuthState()
    object Loading : AuthState()
    object Success : AuthState()
    data class Error(val mensaje: String) : AuthState()
}

class AuthViewModel : ViewModel() {

    private val _authState = MutableStateFlow<AuthState>(AuthState.Idle)
    val authState: StateFlow<AuthState> = _authState

    private val _usuario = MutableStateFlow<UsuarioDto?>(null)
    val usuario: StateFlow<UsuarioDto?> = _usuario

    fun loginConGoogle(context: Context, idToken: String) {
        viewModelScope.launch {
            _authState.value = AuthState.Loading
            try {
                val response = ApiClient.api.loginConGoogle(GoogleAuthRequest(idToken))
                if (response.isSuccessful) {
                    val body = response.body()!!
                    TokenDataStore.saveSession(
                        context = context,
                        token = body.token,
                        userId = body.userId,
                        displayName = body.displayName,
                        email = body.email
                    )
                    ApiClient.jwtToken = body.token
                    cargarPerfil()
                    _authState.value = AuthState.Success
                } else {
                    val errorMsg = when (response.code()) {
                        401 -> "Token de Google no válido"
                        500 -> "Error interno del servidor"
                        else -> "Error ${response.code()}"
                    }
                    _authState.value = AuthState.Error(errorMsg)
                }
            } catch (e: Exception) {
                _authState.value = AuthState.Error(
                    "No se pudo conectar con el servidor.\nComprueba que el backend está en 
                    "No se pudo conectar con el servidor.\nComprueba que el backend está en marcha"
                )
            }
        }
    }

    private fun cargarPerfil() {
        viewModelScope.launch {
            try {
                val response = ApiClient.api.getPerfilPropio()
                if (response.isSuccessful) {
                    _usuario.value = response.body()
                }
            } catch (e: Exception) {
                // ignorar si falla el perfil, el login ya fue exitoso
            }
        }
    }
}
