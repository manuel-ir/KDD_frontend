package com.kdd.kdd_frontend.viewmodel

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.UserProfileChangeRequest
import com.kdd.kdd_frontend.data.TokenDataStore
import com.kdd.kdd_frontend.network.ApiClient
import com.kdd.kdd_frontend.network.dto.GoogleAuthRequest
import com.kdd.kdd_frontend.network.dto.LoginEmailDto
import com.kdd.kdd_frontend.network.dto.RegistroEmailDto
import com.kdd.kdd_frontend.network.dto.UsuarioDto
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

/**
 * ViewModel de autenticacion.
 *
 * Gestiona el flujo de inicio de sesion con Google:
 * 1. Recibe el idToken de Google Sign-In.
 * 2. Lo envia al backend (POST /api/auth/google).
 * 3. Guarda el token JWT y el userId en DataStore.
 * 4. Expone el estado del login para que la pantalla reaccione.
 *
 * Tambien gestiona el cierre de sesion (borrar el token guardado).
 */
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
                    "No se pudo conectar. Comprueba tu conexion a internet"
                )
            }
        }
    }

    private fun emailValido(email: String): Boolean {
        return android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()
    }

    // Login directo con email y contrasena (sin Firebase, endpoint propio del backend)
    fun loginConEmail(context: Context, email: String, password: String) {
        viewModelScope.launch {
            if (!emailValido(email)) {
                _authState.value = AuthState.Error("El formato del correo no es valido")
                return@launch
            }
            if (password.isBlank()) {
                _authState.value = AuthState.Error("Introduce la contrasena")
                return@launch
            }
            _authState.value = AuthState.Loading
            try {
                val response = ApiClient.api.loginConEmail(
                    LoginEmailDto(email = email.trim().lowercase(), password = password)
                )
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
                    val errorBody = response.errorBody()?.string() ?: ""
                    val msg = when {
                        errorBody.contains("contrasena incorrecta", ignoreCase = true) -> "Contrasena incorrecta"
                        errorBody.contains("no existe", ignoreCase = true) -> "No existe una cuenta con ese correo"
                        errorBody.contains("Google Sign-In", ignoreCase = true) -> "Esta cuenta usa Google. Inicia sesion con Google"
                        else -> "Correo o contrasena incorrectos"
                    }
                    _authState.value = AuthState.Error(msg)
                }
            } catch (e: Exception) {
                _authState.value = AuthState.Error("No se pudo conectar. Comprueba tu conexion a internet")
            }
        }
    }

    // Registro directo con email y contrasena (sin Firebase, endpoint propio del backend)
    fun registrarConEmail(context: Context, nombre: String, email: String, password: String) {
        viewModelScope.launch {
            if (nombre.isBlank()) {
                _authState.value = AuthState.Error("El nombre es obligatorio")
                return@launch
            }
            if (!emailValido(email)) {
                _authState.value = AuthState.Error("El formato del correo no es valido")
                return@launch
            }
            if (password.length < 6) {
                _authState.value = AuthState.Error("La contrasena debe tener al menos 6 caracteres")
                return@launch
            }
            _authState.value = AuthState.Loading
            try {
                val response = ApiClient.api.registroConEmail(
                    RegistroEmailDto(nombre = nombre.trim(), email = email.trim().lowercase(), password = password)
                )
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
                    val errorBody = response.errorBody()?.string() ?: ""
                    val msg = when {
                        errorBody.contains("ya existe", ignoreCase = true) -> "Ya existe una cuenta con ese correo"
                        errorBody.contains("6 caracteres", ignoreCase = true) -> "La contrasena debe tener al menos 6 caracteres"
                        else -> "Error al registrarse. Revisa los datos"
                    }
                    _authState.value = AuthState.Error(msg)
                }
            } catch (e: Exception) {
                _authState.value = AuthState.Error("No se pudo conectar. Comprueba tu conexion a internet")
            }
        }
    }

    fun resetState() {
        _authState.value = AuthState.Idle
    }

    private fun cargarPerfil() {
        viewModelScope.launch {
            try {
                val response = ApiClient.api.getMiPerfil()
                if (response.isSuccessful) {
                    _usuario.value = response.body()
                }
            } catch (e: Exception) {
                // ignorar si falla el perfil, el login ya fue exitoso
            }
        }
    }
}
