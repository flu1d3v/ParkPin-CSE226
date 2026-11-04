package com.fluid.parkpin.util

import android.content.ActivityNotFoundException
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.widget.Toast
import java.util.Locale

/** Opens Google Maps with walking directions to the saved spot. */
object MapsLauncher {
    fun navigate(context: Context, lat: Double, lng: Double) {
        val coords = String.format(Locale.US, "%.6f,%.6f", lat, lng)
        val directions = Intent(Intent.ACTION_VIEW, Uri.parse("google.navigation:q=$coords&mode=w"))
            .setPackage("com.google.android.apps.maps")
        try {
            context.startActivity(directions)
        } catch (e: ActivityNotFoundException) {
            val geo = Intent(Intent.ACTION_VIEW, Uri.parse("geo:$coords?q=$coords(Parked)"))
            try {
                context.startActivity(geo)
            } catch (e2: ActivityNotFoundException) {
                Toast.makeText(context, "No maps app found", Toast.LENGTH_SHORT).show()
            }
        }
    }
}
