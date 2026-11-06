package com.fluid.parkpin.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.ViewModelProvider.AndroidViewModelFactory.Companion.APPLICATION_KEY
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.fluid.parkpin.ParkPinApp
import com.fluid.parkpin.data.LOW_ACCURACY_M
import com.fluid.parkpin.data.ParkingSpot
import com.fluid.parkpin.data.SpotRepository
import com.fluid.parkpin.location.LocationSource
import com.fluid.parkpin.ml.PhotoLabeler
import kotlinx.coroutines.CoroutineExceptionHandler
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlinx.coroutines.withTimeoutOrNull
import java.io.File

class SpotsViewModel(
    private val repository: SpotRepository,
    private val location: LocationSource,
    private val labeler: PhotoLabeler
) : ViewModel() {

    val spots: StateFlow<List<ParkingSpot>> = repository.spots
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    /** The newest spot is the one you are going back to. */
    val latest: StateFlow<ParkingSpot?> = spots
        .map { it.firstOrNull() }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), null)

    private val _isSaving = MutableStateFlow(false)
    val isSaving: StateFlow<Boolean> = _isSaving.asStateFlow()

    // One-off messages for the snackbar
    private val _messages = MutableSharedFlow<String>(extraBufferCapacity = 4)
    val messages: SharedFlow<String> = _messages.asSharedFlow()

    private val handler = CoroutineExceptionHandler { _, t ->
        notify("Something went wrong: ${t.message ?: "unknown error"}")
    }

    fun notify(text: String) {
        _messages.tryEmit(text)
    }

    fun saveSpot() {
        if (_isSaving.value) return
        _isSaving.value = true
        viewModelScope.launch(handler) {
            try {
                // GPS can hang indoors: wait 10 s, then fall back to the cached location.
                val fix = withTimeoutOrNull(10_000L) { location.currentFix() }
                    ?: location.lastKnown()?.let { it.copy(accuracyM = maxOf(it.accuracyM, LOW_ACCURACY_M)) }
                if (fix == null) {
                    notify("Couldn't get your location. Turn on GPS and try again.")
                    return@launch
                }
                repository.save(
                    ParkingSpot(
                        savedAt = System.currentTimeMillis(),
                        lat = fix.lat,
                        lng = fix.lng,
                        accuracyM = fix.accuracyM
                    )
                )
                notify("Spot saved")
            } finally {
                _isSaving.value = false
            }
        }
    }

    fun rename(id: Long, label: String) {
        viewModelScope.launch(handler) { repository.rename(id, label.trim()) }
    }

    /** Called after the camera returns a photo: label it with ML Kit, then store path and tags. */
    fun attachPhoto(id: Long, path: String) {
        viewModelScope.launch(handler) {
            val old = spots.value.firstOrNull { it.id == id }?.photoPath
            val tags = labeler.label(path)
            repository.setPhoto(id, path, tags.joinToString(","))
            if (old != null && old != path) withContext(Dispatchers.IO) { File(old).delete() }
            notify(if (tags.isEmpty()) "Photo added" else "Photo added and labeled")
        }
    }

    fun delete(spot: ParkingSpot) {
        viewModelScope.launch(handler) {
            repository.delete(spot)
            spot.photoPath?.let { path -> withContext(Dispatchers.IO) { File(path).delete() } }
        }
    }

    companion object {
        val Factory: ViewModelProvider.Factory = viewModelFactory {
            initializer {
                val app = this[APPLICATION_KEY] as ParkPinApp
                val c = app.container
                SpotsViewModel(c.spotRepository, c.locationSource, c.photoLabeler)
            }
        }
    }
}
