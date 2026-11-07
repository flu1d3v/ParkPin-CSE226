package com.fluid.parkpin

import android.graphics.Color
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.SystemBarStyle
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.lifecycle.viewmodel.compose.viewModel
import com.fluid.parkpin.ui.AppRoot
import com.fluid.parkpin.ui.SpotsViewModel
import com.fluid.parkpin.ui.theme.ParkPinTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge(
            statusBarStyle = SystemBarStyle.dark(Color.TRANSPARENT),
            navigationBarStyle = SystemBarStyle.dark(Color.TRANSPARENT)
        )
        setContent {
            ParkPinTheme {
                val vm: SpotsViewModel = viewModel(factory = SpotsViewModel.Factory)
                AppRoot(vm)
            }
        }
    }
}
