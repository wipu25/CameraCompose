package com.example.cameracompose

import android.annotation.SuppressLint
import android.hardware.camera2.*
import android.util.Log
import androidx.camera.camera2.interop.Camera2CameraControl
import androidx.camera.camera2.interop.Camera2CameraInfo
import androidx.camera.camera2.interop.CaptureRequestOptions
import androidx.camera.core.Camera
import androidx.camera.core.CameraSelector
import androidx.lifecycle.MutableLiveData
import javax.inject.Inject

class CameraSettings @Inject constructor() {


    private lateinit var camera: Camera
    private var _lensFacing: MutableLiveData<Int> = MutableLiveData(CameraSelector.LENS_FACING_BACK)
    private var _cameraCurrentSettings: MutableLiveData<CameraCurrentSettings?> =
        MutableLiveData(null)
    val cameraCurrentSettings get() = _cameraCurrentSettings
    val lensFacing get() = _lensFacing

    @SuppressLint("UnsafeOptInUsageError")
    fun getCameraInfo(camera: Camera): CameraInfoModel {
        this.camera = camera
        Camera2CameraInfo.from(camera.cameraInfo).apply {
            val aeRange =
                getCameraCharacteristic(CameraCharacteristics.CONTROL_AE_COMPENSATION_RANGE)!!
            val isoRange =
                getCameraCharacteristic(CameraCharacteristics.SENSOR_INFO_SENSITIVITY_RANGE)!!
            val shutterSpeedRange =
                getCameraCharacteristic(CameraCharacteristics.SENSOR_INFO_EXPOSURE_TIME_RANGE)!!
            Log.d("Done", "Done get camera info")
            return CameraInfoModel(
                upperAE = aeRange.upper,
                lowerAE = aeRange.lower,
                upperISO = isoRange.upper,
                lowerISO = isoRange.lower,
                lowerShutterSpeed = shutterSpeedRange.lower,
                upperShutterSpeed = shutterSpeedRange.upper,
                awbMode = getCameraCharacteristic(CameraCharacteristics.CONTROL_AWB_AVAILABLE_MODES)?.toList()
                    ?: emptyList(),
                afMode = getCameraCharacteristic(CameraCharacteristics.CONTROL_AF_AVAILABLE_MODES)?.toList()
                    ?: emptyList()
            )
        }
    }

    val captureCallback = object : CameraCaptureSession.CaptureCallback() {
        override fun onCaptureCompleted(
            session: CameraCaptureSession,
            request: CaptureRequest,
            result: TotalCaptureResult
        ) {
            result.apply {
                _cameraCurrentSettings.postValue(
                    CameraCurrentSettings(
                        ae = get(CaptureResult.CONTROL_AE_EXPOSURE_COMPENSATION)!!,
                        af = get(CaptureResult.CONTROL_AF_MODE)!!,
                        awb = get(CaptureResult.CONTROL_AWB_MODE)!!,
                        iso = get(CaptureResult.SENSOR_SENSITIVITY)!!,
                        shutterSpeed = get(CaptureResult.SENSOR_EXPOSURE_TIME)!!,
                    )
                )
            }
            Log.d("AF_mode", result.get(CaptureResult.CONTROL_AF_MODE).toString())
            super.onCaptureCompleted(session, request, result)
        }
    }

    fun adjustCamera(cameraAdjust: CameraAdjust, value: Int) {
        Camera2CameraControl.from(camera.cameraControl).captureRequestOptions =
            CaptureRequestOptions.Builder().apply {
                when (cameraAdjust) {
                    CameraAdjust.ISO -> setCaptureRequestOption(
                        CaptureRequest.SENSOR_SENSITIVITY,
                        value
                    )
                    CameraAdjust.EV -> setCaptureRequestOption(
                        CaptureRequest.CONTROL_AE_EXPOSURE_COMPENSATION,
                        value
                    )
                    CameraAdjust.S -> setCaptureRequestOption(
                        CaptureRequest.SENSOR_EXPOSURE_TIME,
                        value.toLong()
                    )
                }
            }.build()
    }

    fun switchCameraLens() {
        if (lensFacing.value == CameraSelector.LENS_FACING_BACK) {
            lensFacing.postValue(CameraSelector.LENS_FACING_FRONT)
        } else {
            lensFacing.postValue(CameraSelector.LENS_FACING_BACK)
        }
    }
}