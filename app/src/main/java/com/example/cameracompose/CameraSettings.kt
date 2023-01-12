package com.example.cameracompose

import android.annotation.SuppressLint
import android.hardware.camera2.CameraCharacteristics
import android.hardware.camera2.CameraMetadata
import android.hardware.camera2.CaptureRequest
import android.util.Log
import androidx.camera.camera2.interop.Camera2CameraControl
import androidx.camera.camera2.interop.Camera2CameraInfo
import androidx.camera.camera2.interop.Camera2Interop
import androidx.camera.camera2.interop.CaptureRequestOptions
import androidx.camera.core.*
import androidx.camera.lifecycle.ProcessCameraProvider
import javax.inject.Inject

class CameraSettings @Inject constructor() {


    private lateinit var camera: Camera
    val lensFacing = CameraSelector.LENS_FACING_BACK
    private var _cameraInfoModel: CameraInfoModel? = null
    val cameraInfoModel get() = _cameraInfoModel

    @SuppressLint("UnsafeOptInUsageError")
    fun initCamera(camera: Camera) {
        this.camera = camera
        Camera2CameraInfo.from(camera.cameraInfo).apply {
            val aeRange = getCameraCharacteristic(CameraCharacteristics.CONTROL_AE_COMPENSATION_RANGE)!!
            val isoRange = getCameraCharacteristic(CameraCharacteristics.SENSOR_INFO_SENSITIVITY_RANGE)!!
            val shutterSpeedRange = getCameraCharacteristic(CameraCharacteristics.SENSOR_INFO_EXPOSURE_TIME_RANGE)!!
            _cameraInfoModel = CameraInfoModel(
                upperAE = aeRange.upper,
                lowerAE = aeRange.lower,
                upperISO = isoRange.upper,
                lowerISO = isoRange.lower,
                lowerShutterSpeed = shutterSpeedRange.lower,
                upperShutterSpeed = shutterSpeedRange.lower,
                awbMode = getCameraCharacteristic(CameraCharacteristics.CONTROL_AWB_AVAILABLE_MODES)?.toList()
                    ?: emptyList(),
                afMode = getCameraCharacteristic(CameraCharacteristics.CONTROL_AF_AVAILABLE_MODES)?.toList()
                    ?: emptyList()
            )
        }
    }

    fun adjustCamera() {
        Camera2CameraControl.from(camera.cameraControl).captureRequestOptions =
            CaptureRequestOptions.Builder().apply {
                setCaptureRequestOption(CaptureRequest.CONTROL_AF_MODE,CaptureRequest.CONTROL_AF_MODE_OFF)
                setCaptureRequestOption(CaptureRequest.CONTROL_AWB_MODE,CaptureRequest.CONTROL_AWB_MODE_OFF)
            }.build()
    }
}