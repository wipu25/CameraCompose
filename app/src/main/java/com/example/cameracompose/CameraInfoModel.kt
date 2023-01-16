package com.example.cameracompose

data class CameraInfoModel(
    val upperAE: Int,
    val lowerAE: Int,
    val upperISO: Int,
    val lowerISO: Int,
    val upperShutterSpeed: Long,
    val lowerShutterSpeed: Long,
    val awbMode: List<Int>,
    val afMode: List<Int>,
)

data class CameraCurrentSettings(
    val ae: Int,
    val iso: Int,
    val shutterSpeed: Long,
    val awb: Int,
    val af: Int
)