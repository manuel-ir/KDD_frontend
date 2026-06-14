package com.kdd.kdd_frontend.viewmodel

import android.content.Context
import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.storage.FirebaseStorage
import com.kdd.kdd_frontend.data.TokenDataStore
import com.kdd.kdd_frontend.network.ApiClient
import com.kdd.kdd_frontend.network.dto.UsuarioDto
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

sealed class PerfilState {
    object Loading : PerfilState()
    data class Success(val usuario: UsuarioDto) : PerfilState()
    data class Error(val mensaje: String) : PerfilState()
}

class PerfilViewModel : ViewModel() {

    private val _perfilState = MutableStateFlow<PerfilState>(PerfilState.Loading)
    val perfilState: StateFlow<PerfilState> = _perfilState

    private val _subiendoFoto = MutableStateFlow(false)
    val subiendoFoto: StateFlow<Boolean> = _subiendoFoto

    init {
        cargarPerfil()
    }

    fun cargarPerfil() {
        viewModelScope.launch {
            _perfilState.value = PerfilState.Loading
            try {
                val response = ApiClient.api.getMiPerfil()
                if (response.isSuccessful) {
                    _perfilState.value = PerfilState.Success(response.body()!!)
                } else {
                    _perfilState.value = PerfilState.Error("Error ${response.code()}")
                }
            } catch (e: Exception) {
                _perfilState.value = PerfilState.Error("No se pudo conectar con el servidor")
            }
        }
    }

    fun editarPerfil(
        nombre: String,
        nombreUsuario: String?,
        descripcion: String,
        fechaNacimiento: String?,
        onSuccess: () -> Unit,
        onError: (String) -> Unit
    ) {
        viewModelScope.launch {
            try {
                val body = mutableMapOf<String, Any>("nombre" to nombre, "descripcion" to descripcion)
                if (!nombreUsuario.isNullOrBlank()) body["nombreUsuario"] = nombreUsuario
                if (!fechaNacimiento.isNullOrBlank()) body["fechaNacimiento"] = fechaNacimiento
                val response = ApiClient.api.editarPerfil(body)
                if (response.isSuccessful) {
                    _perfilState.value = PerfilState.Success(response.body()!!)
                    onSuccess()
                } else {
                    onError("Error ${response.code()}")
                }
            } catch (e: Exception) {
                onError("No se pudo actualizar el perfil")
            }
        }
    }

    fun subirFotoPerfil(imageUri: Uri, onSuccess: () -> Unit, onError: (String) -> Unit) {
        _subiendoFoto.value = true
        val user = FirebaseAuth.getInstance().currentUser ?: run {
            _subiendoFoto.value = false
            onError("Sesión no iniciada en Firebase")
            return
        }
        val ref = FirebaseStorage.getInstance().reference.child("fotos_perfil/${user.uid}.jpg")
        viewModelScope.launch {
            try {
                ref.putFile(imageUri).await()
                val url = ref.downloadUrl.await().toString()
                val body = mutableMapOf<String, Any>("fotoPerfil" to url)
                val response = ApiClient.api.editarPerfil(body)
                if (response.isSuccessful) {
                    _perfilState.value = PerfilState.Success(response.body()!!)
                    onSuccess()
                } else {
                    onError("Error al guardar la foto en el servidor")
                }
            } catch (e: Exception) {
                onError("Error al subir la foto: ${e.message}")
            } finally {
                _subiendoFoto.value = false
            }
        }
    }

    fun cerrarSesion(context: Context, onLogout: () -> Unit) {
        viewModelScope.launch {
            TokenDataStore.clearSession(context)
            _perfilState.value = PerfilState.Loading
            onLogout()
        }
    }
}
