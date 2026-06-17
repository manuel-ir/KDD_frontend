package com.kdd.kdd_frontend

import android.app.Application
import com.google.android.libraries.places.api.Places

/**
 * Clase de aplicacion principal de Android.
 *
 * Android exige una clase que extienda Application para inicializar
 * recursos globales al arrancar la app. Aqui se inicializa el SDK
 * de Firebase antes de que se cargue cualquier pantalla.
 */
class KddApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        if (!Places.isInitialized()) {
            Places.initialize(applicationContext, BuildConfig.MAPS_API_KEY)
        }
    }
}
