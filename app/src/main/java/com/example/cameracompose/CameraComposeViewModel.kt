package com.example.cameracompose

import android.content.Context
import android.hardware.camera2.CameraCaptureSession
import androidx.camera.core.Camera
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import dagger.hilt.EntryPoint
import dagger.hilt.EntryPoints
import dagger.hilt.InstallIn
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Inject

@HiltViewModel
class CameraComposeViewModel @Inject constructor(@ApplicationContext private val application: Context) :
    ViewModel() {

    var cameraSettings: CameraSettings
    private val _cameraCurrentSettings: MutableLiveData<CameraCurrentSettings> =
        MutableLiveData(null)
    val cameraCurrentSettings get() = _cameraCurrentSettings

    @EntryPoint
    @InstallIn(SingletonComponent::class)
    interface CamInterface {
        fun cameraSettings(): CameraSettings
    }

    init {
        cameraSettings = EntryPoints.get(application, CamInterface::class.java).cameraSettings()
        cameraSettings.cameraCurrentSettings.observeForever {
            _cameraCurrentSettings.postValue(it)
        }
    }

    fun getCameraInfo(camera: Camera): CameraInfoModel {
        return cameraSettings.getCameraInfo(camera)
    }

    fun adjustCamera(cameraAdjust: CameraAdjust, value: Int) {
        cameraSettings.adjustCamera(cameraAdjust, value)
    }

    fun captureCallback(): CameraCaptureSession.CaptureCallback {
        return cameraSettings.captureCallback
    }
}

enum class CameraAdjust {
    NONE,
    ISO,
    EV,
    AWB,
    S,
    AF
}