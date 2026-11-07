package com.fluid.parkpin

import android.app.Application
import android.content.Context
import com.fluid.parkpin.data.AppDatabase
import com.fluid.parkpin.data.SpotRepository
import com.fluid.parkpin.location.LocationSource
import com.fluid.parkpin.ml.PhotoLabeler

/** Manual dependency container (no Hilt). */
class AppContainer(appContext: Context) {
    val spotRepository: SpotRepository by lazy {
        SpotRepository(AppDatabase.getInstance(appContext).spotDao())
    }
    val locationSource: LocationSource by lazy { LocationSource(appContext) }
    val photoLabeler: PhotoLabeler by lazy { PhotoLabeler(appContext) }
}

class ParkPinApp : Application() {
    lateinit var container: AppContainer
        private set

    override fun onCreate() {
        super.onCreate()
        container = AppContainer(applicationContext)
    }
}
