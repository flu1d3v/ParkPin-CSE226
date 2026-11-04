package com.fluid.parkpin.location

import android.annotation.SuppressLint
import android.content.Context
import android.location.Location
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority
import com.google.android.gms.tasks.CancellationTokenSource
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlin.coroutines.resume

data class Fix(val lat: Double, val lng: Double, val accuracyM: Float)

private fun Location.toFix() = Fix(latitude, longitude, accuracy)

/** Fused Location Provider. Callers must hold the location permission. */
class LocationSource(context: Context) {

    private val client = LocationServices.getFusedLocationProviderClient(context)

    /** One fresh high-accuracy fix. Cancelling the coroutine cancels the request. */
    @SuppressLint("MissingPermission")
    suspend fun currentFix(): Fix? = suspendCancellableCoroutine { cont ->
        val tokenSource = CancellationTokenSource()
        cont.invokeOnCancellation { tokenSource.cancel() }
        try {
            client.getCurrentLocation(Priority.PRIORITY_HIGH_ACCURACY, tokenSource.token)
                .addOnSuccessListener { if (cont.isActive) cont.resume(it?.toFix()) }
                .addOnFailureListener { if (cont.isActive) cont.resume(null) }
        } catch (e: SecurityException) {
            if (cont.isActive) cont.resume(null)
        }
    }

    /** Cached location from the last time any app asked. May be old. */
    @SuppressLint("MissingPermission")
    suspend fun lastKnown(): Fix? = suspendCancellableCoroutine { cont ->
        try {
            client.lastLocation
                .addOnSuccessListener { if (cont.isActive) cont.resume(it?.toFix()) }
                .addOnFailureListener { if (cont.isActive) cont.resume(null) }
        } catch (e: SecurityException) {
            if (cont.isActive) cont.resume(null)
        }
    }
}
