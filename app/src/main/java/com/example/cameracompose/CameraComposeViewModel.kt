package com.example.cameracompose

import android.content.Context
import android.util.Log
import androidx.camera.core.Camera
import androidx.camera.core.CameraSelector
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.EntryPoint
import dagger.hilt.EntryPoints
import dagger.hilt.InstallIn
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class CameraComposeViewModel @Inject constructor(@ApplicationContext private val application: Context) : ViewModel() {

    var cameraSettings: CameraSettings
    private val _cameraAdjust: MutableLiveData<CameraAdjust> = MutableLiveData(CameraAdjust.NONE)
    val cameraAdjust get() = _cameraAdjust

    @EntryPoint
    @InstallIn(SingletonComponent::class)
    interface CamInterface {
        fun cameraSettings(): CameraSettings
    }

    init {
        cameraSettings = EntryPoints.get(application, CamInterface::class.java).cameraSettings()
    }

    fun initCamera(camera: Camera) {
        cameraSettings.initCamera(camera)
    }

    fun adjustCamera() {
        cameraSettings.adjustCamera()
    }

    fun getCameraLens(): Int {
        return cameraSettings.lensFacing
    }

    fun updateCameraAdjust(cameraAdjust: CameraAdjust){
        _cameraAdjust.postValue(cameraAdjust)
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