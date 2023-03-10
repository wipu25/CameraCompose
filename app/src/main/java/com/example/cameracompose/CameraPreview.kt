package com.example.cameracompose

import android.annotation.SuppressLint
import android.content.Context
import android.hardware.camera2.CameraCaptureSession
import android.util.Log
import androidx.camera.camera2.interop.Camera2Interop
import androidx.camera.core.Camera
import androidx.camera.core.CameraSelector
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.compose.foundation.background
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.OutlinedButton
import androidx.compose.material.Text
import androidx.compose.material.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableStateOf
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
import kotlin.math.roundToInt

@SuppressLint("UnsafeOptInUsageError")
@Composable
// should contain only camera specific things
fun CameraPreview(
    onInitCameraPreview: (camera: Camera) -> CameraInfoModel,
    captureCallback: CameraCaptureSession.CaptureCallback,
    cameraCurrentSettings: CameraCurrentSettings?
) {
    val lifecycleOwner = LocalLifecycleOwner.current
    val currentContext = LocalContext.current
    val previewView = remember { PreviewView(currentContext) }
    val cameraInfo = remember { mutableStateOf<CameraInfoModel?>(null) }
    val cameraAdjust = remember { mutableStateOf(CameraAdjust.NONE) }
    val lensFacing = remember { mutableStateOf(CameraSelector.LENS_FACING_BACK) }
    val i = 100

    //binding camera here with lensFacing key to make sure when lens change it re-instantiate camera preview
    LaunchedEffect(lensFacing.value) {
        val cameraProvider = currentContext.getCameraProvider()
        val surfaceProvider = previewView.surfaceProvider
        val preview = Preview.Builder()

        //set capture callback getting camera settings
        val previewExtender = Camera2Interop.Extender(preview)
        previewExtender.setSessionCaptureCallback(captureCallback)
        val previewBuild = preview.build()

        //unbind any others camera usage and bind what we going to use
        cameraProvider.unbindAll()
        val camera = cameraProvider.bindToLifecycle(
            lifecycleOwner,
            CameraSelector.Builder()
                .requireLensFacing(lensFacing.value)
                .build(),
            previewBuild,
        )

        previewBuild.setSurfaceProvider(surfaceProvider)

        //keep camera state to camera settings class
        cameraInfo.value = onInitCameraPreview(camera)
    }

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier.background(Color.Black)
    ) {
        Box(modifier = Modifier.weight(8.5f)) {
            AndroidView(
                factory = { previewView },
                modifier = Modifier.fillMaxSize()
            )
            Column(modifier = Modifier.align(Alignment.BottomCenter)) {
                if (cameraAdjust.value != CameraAdjust.NONE && cameraInfo.value != null)
                    Row(
                        modifier = Modifier.horizontalScroll(rememberScrollState())
                    ) {
                        when (cameraAdjust.value) {
                            CameraAdjust.ISO -> {
                                var i = cameraInfo.value!!.lowerISO
                                while (i <= cameraInfo.value!!.upperISO) {
                                    Text(i.toString(), modifier = Modifier.padding(Dp(4F)))
                                    i += i
                                }
                            }
                            CameraAdjust.EV -> {
                                for (i in cameraInfo.value!!.lowerAE..cameraInfo.value!!.upperAE step 1) {
                                    Text(i.toString(), modifier = Modifier.padding(Dp(4F)))
                                }
                            }
//                            CameraAdjust.AWB -> {
//                                for(i in cameraInfo.lowerISO..cameraInfo.upperISO step 1000) {
//                                    Text(i.toString())
//                                }
//                            }
//                            CameraAdjust.AF -> {
//                                for(i in cameraInfo.lowerISO..cameraInfo.upperISO step 1000) {
//                                    Text(i.toString())
//                                }
//                            }
                            CameraAdjust.S -> {
                                var i = cameraInfo.value!!.upperShutterSpeed
                                var countoverSpeed = 1
                                while (i >= cameraInfo.value!!.lowerShutterSpeed) {
                                    val displayNum = i.toFloat().div(1000000)
                                    if (displayNum.roundToInt() > 1) {
                                        Text(
                                            "1/${displayNum.roundToInt()} s",
                                            modifier = Modifier.padding(Dp(4F))
                                        )
                                    } else {
                                        Log.d("Display num", i.toString())
                                        Text(
                                            countoverSpeed.toString(),
                                            modifier = Modifier.padding(Dp(4F))
                                        )
                                        countoverSpeed *= 2
                                    }
                                    i /= 2
                                }
                                Log.d("Display num", "END")
                            }
                        }
                    }
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(Color.Black.copy(alpha = 0.5f))
                        .padding(Dp(8f)),
                    horizontalArrangement = Arrangement.SpaceEvenly
                ) {
                    TextButton(
                        onClick = { cameraAdjust.value = CameraAdjust.ISO }) {
                        Column {
                            Text(text = "ISO")
                            Text(text = "${cameraCurrentSettings?.iso}")
                        }
                    }
                    TextButton(onClick = { cameraAdjust.value = CameraAdjust.S }) {
                        Column {
                            Text(text = "S")
                            Text(text = "${cameraCurrentSettings?.shutterSpeed}")
                        }
                    }
                    TextButton(onClick = { cameraAdjust.value = CameraAdjust.EV }) {
                        Column {
                            Text(text = "EV")
                            Text(text = "${cameraCurrentSettings?.ae}")
                        }
                    }
                    TextButton(onClick = { cameraAdjust.value = CameraAdjust.AF }) {
                        Column {
                            Text(text = "AF")
                            Text(text = "${cameraCurrentSettings?.af}")
                        }
                    }
                    TextButton(onClick = { cameraAdjust.value = CameraAdjust.AWB }) {
                        Column {
                            Text(text = "AWB")
                            Text(text = "${cameraCurrentSettings?.awb}")
                        }
                    }
                }
            }
        }
        Box(modifier = Modifier.weight(1.5f)) {
            OutlinedButton(
                shape = CircleShape,
                modifier = Modifier
                    .padding(Dp(20f))
                    .size(Dp(60f)),
                onClick = {
                    if (lensFacing.value == CameraSelector.LENS_FACING_BACK) {
                        lensFacing.value = CameraSelector.LENS_FACING_FRONT
                    } else {
                        lensFacing.value = CameraSelector.LENS_FACING_BACK
                    }
                }
            ) { }
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