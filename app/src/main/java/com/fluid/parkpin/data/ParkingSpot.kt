package com.fluid.parkpin.data

import androidx.room.Entity
import androidx.room.PrimaryKey

/** Accuracy at or above this many metres is shown as "Low accuracy". */
const val LOW_ACCURACY_M = 50f

@Entity(tableName = "parking_spots")
data class ParkingSpot(
    @PrimaryKey(autoGenerate = true) val id: Long = 0L,
    val savedAt: Long,              // epoch milliseconds
    val lat: Double,
    val lng: Double,
    val accuracyM: Float,
    val label: String = "",
    val photoPath: String? = null,  // photo taken at the spot
    val tags: String? = null        // ML Kit labels, comma separated
)
