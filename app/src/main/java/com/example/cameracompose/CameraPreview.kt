package com.example.cameracompose

import android.content.Context
import android.view.GestureDetector
import android.widget.StackView
import androidx.camera.core.Camera
import androidx.camera.core.CameraSelector
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.Button
import androidx.compose.material.OutlinedButton
import androidx.compose.material.Text
import androidx.compose.material.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.viewinterop.AndroidView
import kotlin.coroutines.resume
import kotlin.coroutines.suspendCoroutine

@Composable
// should contain only camera specific things
fun CameraPreview(
    lensFacing: Int,
    onInitCameraPreview: (camera: Camera) -> Unit,
    onCameraAdjust: (cameraAdjust: CameraAdjust) -> Unit,
    cameraAdjust: CameraAdjust
) {
    val lifecycleOwner = LocalLifecycleOwner.current
    val currentContext = LocalContext.current
    val previewView = remember { PreviewView(currentContext) }

    //binding camera here with lensFacing key to make sure when lens change it re-instantiate camera preview
    LaunchedEffect(lensFacing) {
        val cameraProvider = currentContext.getCameraProvider()
        val surfaceProvider = previewView.surfaceProvider
        val preview = Preview.Builder()
        val previewBuild = preview.build()

        //unbind any others camera usage and bind what we going to use
        cameraProvider.unbindAll()
        val camera = cameraProvider.bindToLifecycle(
            lifecycleOwner,
            CameraSelector.Builder()
                .requireLensFacing(lensFacing)
                .build(),
            previewBuild,
        )

        previewBuild.setSurfaceProvider(surfaceProvider)

        //keep camera state to camera settings class
        onInitCameraPreview(camera)
    }

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier.background(Color.Black)
    ) {
        Box(modifier = Modifier.weight(8.5f),) {
            AndroidView(
                factory = { previewView },
                modifier = Modifier.fillMaxSize()
            )
            Column(modifier = Modifier.align(Alignment.BottomCenter)) {
                if(cameraAdjust != CameraAdjust.NONE)
                    Text(text = cameraAdjust.name, modifier = Modifier.padding(Dp(20f)))
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(Color.Black.copy(alpha = 0.5f))
                            .padding(Dp(8f)),
                        horizontalArrangement = Arrangement.SpaceEvenly
                    ) {
                        TextButton(
                            onClick = { onCameraAdjust(CameraAdjust.ISO) }) {
                            Text(text = "ISO")
                        }
                        TextButton(onClick = { onCameraAdjust(CameraAdjust.S) }) {
                            Text(text = "S")
                        }
                        TextButton(onClick = { onCameraAdjust(CameraAdjust.EV) }) {
                            Text(text = "EV")
                        }
                        TextButton(onClick = { onCameraAdjust(CameraAdjust.AF) }) {
                            Text(text = "AF")
                        }
                        TextButton(onClick = { onCameraAdjust(CameraAdjust.AWB) }) {
                            Text(text = "AWB")
                        }
                    }
            }
        }
        Box (modifier =  Modifier.weight(1.5f)){
            OutlinedButton(
                shape = CircleShape,
                modifier = Modifier
                    .padding(Dp(20f))
                    .size(Dp(60f)),
                onClick = { /*TODO*/ }
            ) {}
        }
    }
}

@Composable
fun TextAlert(text: String) {
    Text(
        text = text,
        textAlign = TextAlign.Center
    )
}

//since need to wait for future so get cameraProvider create as extension for clean code
suspend fun Context.getCameraProvider(): ProcessCameraProvider = suspendCoroutine { continuation ->
    ProcessCameraProvider.getInstance(this).also { cameraProvider ->
        cameraProvider.addListener({
            continuation.resume(cameraProvider.get())
        }, this.mainExecutor)
    }
}