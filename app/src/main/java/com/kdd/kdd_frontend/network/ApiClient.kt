package com.kdd.kdd_frontend.network

import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit

/**
 * Configuracion del cliente HTTP para comunicarse con el backend.
 *
 * Usa Retrofit2 con OkHttp para hacer las peticiones REST.
 * El interceptor de autenticacion anade automaticamente el token JWT
 * en la cabecera Authorization de cada peticion que lo requiere.
 *
 * La URL base apunta al emulador de Android (10.0.2.2 equivale a localhost
 * en el ordenador desde el emulador).
 */
object ApiClient {

    // Backend local para pruebas (10.0.2.2 = localhost del ordenador desde el emulador)
    // Cambiar a "https://kdd-backend.onrender.com/" antes de subir a produccion
    private const val BASE_URL = "http://10.0.2.2:8080/"

    // Token JWT guardado en memoria (se actualiza al hacer login)
    var jwtToken: String? = null

    private val loggingInterceptor = HttpLoggingInterceptor().apply {
        level = HttpLoggingInterceptor.Level.BODY
    }

    private val okHttpClient = OkHttpClient.Builder()
        .addInterceptor(loggingInterceptor)
        .addInterceptor { chain ->
            // Añade el JWT en la cabecera Authorization si existe
            val request = chain.request().newBuilder().apply {
                jwtToken?.let { token ->
                    addHeader("Authorization", "Bearer $token")
                }
            }.build()
            chain.proceed(request)
        }
        .connectTimeout(30, TimeUnit.SECONDS)
        .readTimeout(30, TimeUnit.SECONDS)
        .build()

    val api: ApiService by lazy {
        Retrofit.Builder()
            .baseUrl(BASE_URL)
            .client(okHttpClient)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(ApiService::class.java)
    }
}
