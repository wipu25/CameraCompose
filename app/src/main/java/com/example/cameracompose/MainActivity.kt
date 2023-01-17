package com.example.cameracompose

import android.Manifest
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Surface
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import com.example.cameracompose.ui.theme.CameraComposeTheme
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.rememberPermissionState
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    private val cameraComposeViewModel: CameraComposeViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            CameraComposeTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colors.background
                ) {
                    SimpleCameraPreview()
                }
            }
        }
    }

    @OptIn(ExperimentalPermissionsApi::class)
    @Composable
    fun SimpleCameraPreview() {
        val cameraPermissionState =
            rememberPermissionState(permission = Manifest.permission.CAMERA)
        val lifecycleOwner = LocalLifecycleOwner.current
        var cameraCurrentSettings by remember { mutableStateOf<CameraCurrentSettings?>(null) }

        cameraComposeViewModel.cameraCurrentSettings.observe(lifecycleOwner) {
            if (it != null) {
                cameraCurrentSettings = it
            }
        }

        cameraComposeViewModel

        DisposableEffect(
            key1 = lifecycleOwner, effect = {
                val observer = LifecycleEventObserver { _, event ->
                    if (event == Lifecycle.Event.ON_START) {
                        cameraPermissionState.launchPermissionRequest()
                    }
                }
                lifecycleOwner.lifecycle.addObserver(observer)
                onDispose {
                    lifecycleOwner.lifecycle.removeObserver(observer)
                }
            })
        when {
            cameraPermissionState.hasPermission -> CameraPreview(
                onInitCameraPreview = { camera -> cameraComposeViewModel.getCameraInfo(camera) },
                cameraCurrentSettings = cameraCurrentSettings,
                captureCallback = cameraComposeViewModel.captureCallback(),
            )
            cameraPermissionState.shouldShowRationale -> TextAlert(
                text = "Permission denied please accept camera permission",
            )
            !cameraPermissionState.shouldShowRationale && !cameraPermissionState.hasPermission -> TextAlert(
                text = "Permission denied please allow in settings",
            )
        }
    }
}