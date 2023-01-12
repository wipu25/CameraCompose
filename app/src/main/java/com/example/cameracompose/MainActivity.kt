package com.example.cameracompose

import android.Manifest
import android.annotation.SuppressLint
import android.hardware.camera2.CameraMetadata
import android.hardware.camera2.CaptureRequest
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.camera.camera2.interop.Camera2Interop
import androidx.camera.core.CameraSelector
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Surface
import androidx.compose.material.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.MutableLiveData
import com.example.cameracompose.ui.theme.CameraComposeTheme
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.rememberPermissionState
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.delay

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
        var cameraAdjust by remember { mutableStateOf(CameraAdjust.NONE) }

        cameraComposeViewModel.cameraAdjust.observe(lifecycleOwner) {
            cameraAdjust = it
        }

        DisposableEffect(
            key1 = lifecycleOwner, effect = {
                val observer = LifecycleEventObserver{_, event ->
                    if(event == Lifecycle.Event.ON_START) {
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
                lensFacing =  cameraComposeViewModel.getCameraLens(),
                onInitCameraPreview = {camera -> cameraComposeViewModel.initCamera(camera) },
                onCameraAdjust = {cameraAdjust -> cameraComposeViewModel.updateCameraAdjust(cameraAdjust) },
                cameraAdjust = cameraAdjust
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