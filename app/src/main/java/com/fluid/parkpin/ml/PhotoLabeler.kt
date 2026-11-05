package com.fluid.parkpin.ml

import android.content.Context
import android.net.Uri
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.label.ImageLabeling
import com.google.mlkit.vision.label.defaults.ImageLabelerOptions
import kotlinx.coroutines.suspendCancellableCoroutine
import java.io.File
import java.io.IOException
import kotlin.coroutines.resume

/**
 * On-device image labeling with Google ML Kit. Tags what surrounds the parking spot
 * ("Building", "Tree", "Parking lot") so the user can recognise the place later.
 * No network is used. To remove the AI feature, delete this file and the photo parts of HomeScreen.
 */
class PhotoLabeler(private val context: Context) {

    suspend fun label(path: String): List<String> {
        val labeler = ImageLabeling.getClient(
            ImageLabelerOptions.Builder().setConfidenceThreshold(0.7f).build()
        )
        return try {
            val image = InputImage.fromFilePath(context, Uri.fromFile(File(path)))
            suspendCancellableCoroutine<List<String>> { cont ->
                labeler.process(image)
                    .addOnSuccessListener { labels ->
                        if (cont.isActive) cont.resume(labels.take(5).map { it.text })
                    }
                    .addOnFailureListener { if (cont.isActive) cont.resume(emptyList()) }
            }
        } catch (e: IOException) {
            emptyList()
        } finally {
            labeler.close()
        }
    }
}
