package com.fluid.parkpin.ui.history

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.fluid.parkpin.data.ParkingSpot
import com.fluid.parkpin.ui.SpotsViewModel
import com.fluid.parkpin.ui.displayName
import com.fluid.parkpin.ui.formatAccuracy
import com.fluid.parkpin.ui.home.PhotoThumb
import com.fluid.parkpin.ui.relativeTime
import com.fluid.parkpin.ui.tagList
import com.fluid.parkpin.util.MapsLauncher

@Composable
fun HistoryScreen(vm: SpotsViewModel, modifier: Modifier = Modifier) {
    val context = LocalContext.current
    val spots by vm.spots.collectAsStateWithLifecycle()
    var toDelete by remember { mutableStateOf<ParkingSpot?>(null) }

    Column(modifier.fillMaxSize().padding(horizontal = 16.dp)) {
        Text(
            "History",
            style = MaterialTheme.typography.headlineMedium,
            modifier = Modifier.padding(vertical = 16.dp)
        )
        if (spots.isEmpty()) {
            Text(
                "Nothing here yet. Saved spots will show up in this list.",
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        } else {
            LazyColumn(
                contentPadding = PaddingValues(bottom = 16.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                items(spots, key = { it.id }) { spot ->
                    ElevatedCard(Modifier.fillMaxWidth()) {
                        Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            Text(spot.displayName(), style = MaterialTheme.typography.titleMedium)
                            Text(
                                "${relativeTime(spot.savedAt)} · ${formatAccuracy(spot.accuracyM)}",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            spot.photoPath?.let { PhotoThumb(it) }
                            val tags = spot.tagList()
                            if (tags.isNotEmpty()) {
                                Text("Nearby: ${tags.joinToString(", ")}", style = MaterialTheme.typography.bodyMedium)
                            }
                            Row(
                                horizontalArrangement = Arrangement.spacedBy(8.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Button(onClick = { MapsLauncher.navigate(context, spot.lat, spot.lng) }) {
                                    Text("Navigate")
                                }
                                OutlinedButton(onClick = { toDelete = spot }) { Text("Delete") }
                            }
                        }
                    }
                }
            }
        }
    }

    toDelete?.let { spot ->
        AlertDialog(
            onDismissRequest = { toDelete = null },
            title = { Text("Delete this spot?") },
            text = { Text("It will be removed from your history.") },
            confirmButton = {
                TextButton(onClick = {
                    vm.delete(spot)
                    toDelete = null
                }) { Text("Delete", color = MaterialTheme.colorScheme.error) }
            },
            dismissButton = { TextButton(onClick = { toDelete = null }) { Text("Cancel") } }
        )
    }
}
