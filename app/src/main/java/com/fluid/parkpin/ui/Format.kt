package com.fluid.parkpin.ui

import android.text.format.DateUtils
import com.fluid.parkpin.data.ParkingSpot
import kotlin.math.roundToInt

fun ParkingSpot.displayName(): String = label.ifBlank { "Parking spot" }

fun formatAccuracy(m: Float): String = "within ${m.roundToInt()} m"

fun relativeTime(millis: Long): String =
    DateUtils.getRelativeTimeSpanString(millis, System.currentTimeMillis(), DateUtils.MINUTE_IN_MILLIS).toString()

fun ParkingSpot.tagList(): List<String> =
    tags?.split(",")?.map { it.trim() }?.filter { it.isNotEmpty() } ?: emptyList()
