package com.fluid.parkpin.ui.home

import android.Manifest
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Matrix
import android.media.ExifInterface
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.produceState
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.fluid.parkpin.data.LOW_ACCURACY_M
import com.fluid.parkpin.data.ParkingSpot
import com.fluid.parkpin.ui.SpotsViewModel
import com.fluid.parkpin.ui.displayName
import com.fluid.parkpin.ui.formatAccuracy
import com.fluid.parkpin.ui.relativeTime
import com.fluid.parkpin.ui.tagList
import com.fluid.parkpin.util.MapsLauncher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File

@Composable
fun HomeScreen(vm: SpotsViewModel, modifier: Modifier = Modifier) {
    val context = LocalContext.current
    val latest by vm.latest.collectAsStateWithLifecycle()
    val isSaving by vm.isSaving.collectAsStateWithLifecycle()

    var showRename by rememberSaveable { mutableStateOf(false) }
    // The camera app can restart ours, so these survive through rememberSaveable.
    var pendingPhotoPath by rememberSaveable { mutableStateOf<String?>(null) }
    var pendingPhotoSpotId by rememberSaveable { mutableStateOf<Long?>(null) }

    val permissions = arrayOf(
        Manifest.permission.ACCESS_FINE_LOCATION,
        Manifest.permission.ACCESS_COARSE_LOCATION
    )

    fun hasLocationPermission() = permissions.any {
        ContextCompat.checkSelfPermission(context, it) == PackageManager.PERMISSION_GRANTED
    }

    val permissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { result ->
        if (result.values.any { it }) vm.saveSpot()
        else vm.notify("Location permission is needed to save your spot.")
    }

    val cameraLauncher = rememberLauncherForActivityResult(ActivityResultContracts.TakePicture()) { ok ->
        val path = pendingPhotoPath
        val id = pendingPhotoSpotId
        if (ok && path != null && id != null) vm.attachPhoto(id, path)
        else path?.let { File(it).delete() }
        pendingPhotoPath = null
        pendingPhotoSpotId = null
    }

    fun onSaveClick() {
        if (hasLocationPermission()) vm.saveSpot() else permissionLauncher.launch(permissions)
    }

    fun onAddPhoto(spot: ParkingSpot) {
        val dir = File(context.filesDir, "photos").apply { mkdirs() }
        val file = File(dir, "spot_${spot.id}_${System.currentTimeMillis()}.jpg")
        pendingPhotoPath = file.absolutePath
        pendingPhotoSpotId = spot.id
        val uri = FileProvider.getUriForFile(context, "${context.packageName}.fileprovider", file)
        cameraLauncher.launch(uri)
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(24.dp)
    ) {
        Text("Wayback", style = MaterialTheme.typography.headlineMedium, color = MaterialTheme.colorScheme.primary)

        Button(
            onClick = { onSaveClick() },
            enabled = !isSaving,
            shape = CircleShape,
            modifier = Modifier.size(160.dp)
        ) {
            if (isSaving) {
                CircularProgressIndicator(
                    modifier = Modifier.size(28.dp),
                    strokeWidth = 3.dp,
                    color = MaterialTheme.colorScheme.onPrimary
                )
            } else {
                Text("Save spot", style = MaterialTheme.typography.titleMedium)
            }
        }

        val spot = latest
        if (spot == null) {
            Text(
                "No spot saved yet. Tap Save when you park.",
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        } else {
            ElevatedCard(Modifier.fillMaxWidth()) {
                Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text(spot.displayName(), style = MaterialTheme.typography.titleMedium)
                    Text(
                        "Saved ${relativeTime(spot.savedAt)} · ${formatAccuracy(spot.accuracyM)}",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    if (spot.accuracyM >= LOW_ACCURACY_M) {
                        Text(
                            "Low accuracy. Take a photo to help you find it.",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.error
                        )
                    }
                    spot.photoPath?.let { PhotoThumb(it) }
                    val tags = spot.tagList()
                    if (tags.isNotEmpty()) {
                        Text(
                            "Nearby: ${tags.joinToString(", ")}",
                            style = MaterialTheme.typography.bodyMedium
                        )
                    }
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        Button(onClick = { MapsLauncher.navigate(context, spot.lat, spot.lng) }) {
                            Text("Navigate")
                        }
                        OutlinedButton(onClick = { onAddPhoto(spot) }) {
                            Text(if (spot.photoPath == null) "Add photo" else "Retake")
                        }
                    }
                    TextButton(onClick = { showRename = true }) { Text("Rename") }
                }
            }
        }
    }

    val current = latest
    if (showRename && current != null) {
        RenameDialog(
            initial = current.label,
            onConfirm = {
                vm.rename(current.id, it)
                showRename = false
            },
            onDismiss = { showRename = false }
        )
    }
}

@Composable
private fun RenameDialog(initial: String, onConfirm: (String) -> Unit, onDismiss: () -> Unit) {
    var text by rememberSaveable { mutableStateOf(initial) }
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Rename spot") },
        text = {
            OutlinedTextField(
                value = text,
                onValueChange = { text = it },
                singleLine = true,
                placeholder = { Text("e.g. Block A, level 2") }
            )
        },
        confirmButton = { TextButton(onClick = { onConfirm(text) }) { Text("Save") } },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Cancel") } }
    )
}

@Composable
fun PhotoThumb(path: String, modifier: Modifier = Modifier) {
    // Decode a small version off the main thread.
    val bitmap by produceState<Bitmap?>(initialValue = null, path) {
        value = withContext(Dispatchers.IO) { decodeSampled(path) }
    }
    bitmap?.let {
        Image(
            bitmap = it.asImageBitmap(),
            contentDescription = "Photo of the parking spot",
            contentScale = ContentScale.Crop,
            modifier = modifier
                .fillMaxWidth()
                .height(160.dp)
                .clip(RoundedCornerShape(12.dp))
        )
    }
}

private fun decodeSampled(path: String): Bitmap? {
    val bounds = BitmapFactory.Options().apply { inJustDecodeBounds = true }
    BitmapFactory.decodeFile(path, bounds)
    var sample = 1
    while (bounds.outWidth / sample > 1024) sample *= 2
    val bitmap = BitmapFactory.decodeFile(path, BitmapFactory.Options().apply { inSampleSize = sample })
        ?: return null
    // Camera photos are often stored rotated; read the EXIF flag and turn them upright.
    val degrees = try {
        when (ExifInterface(path).getAttributeInt(ExifInterface.TAG_ORIENTATION, ExifInterface.ORIENTATION_NORMAL)) {
            ExifInterface.ORIENTATION_ROTATE_90 -> 90f
            ExifInterface.ORIENTATION_ROTATE_180 -> 180f
            ExifInterface.ORIENTATION_ROTATE_270 -> 270f
            else -> 0f
        }
    } catch (e: Exception) {
        0f
    }
    if (degrees == 0f) return bitmap
    val matrix = Matrix().apply { postRotate(degrees) }
    return Bitmap.createBitmap(bitmap, 0, 0, bitmap.width, bitmap.height, matrix, true)
}
