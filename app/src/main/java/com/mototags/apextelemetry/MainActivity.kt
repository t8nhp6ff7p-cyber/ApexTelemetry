package com.mototags.apextelemetry

import android.Manifest
import android.content.pm.PackageManager
import android.content.res.Configuration
import android.os.Build
import android.os.Bundle
import android.view.OrientationEventListener
import android.view.Surface
import android.view.WindowManager
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.core.content.ContextCompat
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat
import com.mototags.apextelemetry.ui.HUDDashboardScreen
import com.mototags.apextelemetry.ui.TelemetryViewModel
import com.mototags.apextelemetry.ui.theme.ApexTelemetryTheme

class MainActivity : ComponentActivity() {

    private val viewModel: TelemetryViewModel by viewModels()

    private val requestPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        val fineLocationGranted = permissions[Manifest.permission.ACCESS_FINE_LOCATION] ?: false
        val coarseLocationGranted = permissions[Manifest.permission.ACCESS_COARSE_LOCATION] ?: false

        if (fineLocationGranted || coarseLocationGranted) {
            viewModel.startLocation()
        }
    }

    private var orientationEventListener: OrientationEventListener? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Keep screen awake while mounted on motorcycle handlebars
        window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)

        // True Immersive Sticky Fullscreen (Hide Status Bar & Navigation Bar)
        enableImmersiveFullscreen()

        // Listen for hardware auto-rotation changes
        setupOrientationListener()
        updateDisplayRotation()

        // Check & request location permissions
        checkLocationPermissions()

        setContent {
            val telemetryData by viewModel.telemetryState.collectAsState()
            ApexTelemetryTheme(selectedTheme = telemetryData.selectedTheme) {
                HUDDashboardScreen(viewModel = viewModel)
            }
        }
    }

    private fun enableImmersiveFullscreen() {
        WindowCompat.setDecorFitsSystemWindows(window, false)
        val insetsController = WindowCompat.getInsetsController(window, window.decorView)
        insetsController.systemBarsBehavior =
            WindowInsetsControllerCompat.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE
        insetsController.hide(WindowInsetsCompat.Type.systemBars())
    }

    private fun setupOrientationListener() {
        orientationEventListener = object : OrientationEventListener(this) {
            override fun onOrientationChanged(orientation: Int) {
                if (orientation == ORIENTATION_UNKNOWN) return
                updateDisplayRotation()
            }
        }
    }

    private fun updateDisplayRotation() {
        val rotation = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            display?.rotation ?: Surface.ROTATION_0
        } else {
            @Suppress("DEPRECATION")
            windowManager.defaultDisplay.rotation
        }
        viewModel.setDisplayRotation(rotation)
    }

    private fun checkLocationPermissions() {
        val hasFine = ContextCompat.checkSelfPermission(
            this,
            Manifest.permission.ACCESS_FINE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED

        if (hasFine) {
            viewModel.startLocation()
        } else {
            requestPermissionLauncher.launch(
                arrayOf(
                    Manifest.permission.ACCESS_FINE_LOCATION,
                    Manifest.permission.ACCESS_COARSE_LOCATION
                )
            )
        }
    }

    override fun onResume() {
        super.onResume()
        enableImmersiveFullscreen()
        orientationEventListener?.enable()
        viewModel.startTelemetry()
        updateDisplayRotation()
    }

    override fun onPause() {
        super.onPause()
        orientationEventListener?.disable()
        viewModel.stopTelemetry()
    }

    override fun onWindowFocusChanged(hasFocus: Boolean) {
        super.onWindowFocusChanged(hasFocus)
        if (hasFocus) {
            enableImmersiveFullscreen()
        }
    }

    override fun onConfigurationChanged(newConfig: Configuration) {
        super.onConfigurationChanged(newConfig)
        enableImmersiveFullscreen()
        updateDisplayRotation()
    }
}
