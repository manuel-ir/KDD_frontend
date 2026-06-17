package com.kdd.kdd_frontend.data

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.longPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.kdd.kdd_frontend.network.ApiClient
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

val Context.tokenDataStore: DataStore<Preferences> by preferencesDataStore(name = "kdd_session")

/**
 * Almacen persistente del token JWT del usuario.
 *
 * Usa Jetpack DataStore (Preferences) para guardar el token de sesion
 * de forma segura en el dispositivo. Es la alternativa moderna a SharedPreferences.
 *
 * El token se guarda cuando el usuario inicia sesion y se borra al cerrarla.
 * Al arrancar la app, se lee este token para saber si hay sesion activa.
 */
object TokenDataStore {

    private val KEY_JWT        = stringPreferencesKey("jwt_token")
    private val KEY_USER_ID    = longPreferencesKey("user_id")
    private val KEY_USER_NAME  = stringPreferencesKey("user_name")
    private val KEY_USER_EMAIL = stringPreferencesKey("user_email")

    suspend fun saveSession(
        context: Context,
        token: String,
        userId: Long,
        displayName: String,
        email: String
    ) {
        context.tokenDataStore.edit { prefs ->
            prefs[KEY_JWT]        = token
            prefs[KEY_USER_ID]    = userId
            prefs[KEY_USER_NAME]  = displayName
            prefs[KEY_USER_EMAIL] = email
        }
        ApiClient.jwtToken = token
    }

    fun getToken(context: Context): Flow<String?> =
        context.tokenDataStore.data.map { it[KEY_JWT] }

    fun getUserId(context: Context): Flow<Long?> =
        context.tokenDataStore.data.map { it[KEY_USER_ID] }

    fun getUserName(context: Context): Flow<String?> =
        context.tokenDataStore.data.map { it[KEY_USER_NAME] }

    fun getUserEmail(context: Context): Flow<String?> =
        context.tokenDataStore.data.map { it[KEY_USER_EMAIL] }

    suspend fun clearSession(context: Context) {
        context.tokenDataStore.edit { it.clear() }
        ApiClient.jwtToken = null
    }

    suspend fun hasSession(context: Context): Boolean {
        var token: String? = null
        context.tokenDataStore.data.map { it[KEY_JWT] }.collect {
            token = it
            return@collect
        }
        return !token.isNullOrBlank()
    }
}
