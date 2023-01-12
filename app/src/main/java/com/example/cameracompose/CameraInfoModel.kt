package com.example.cameracompose

data class CameraInfoModel(
    val upperAE: Int,
    val lowerAE: Int,
    val upperISO: Int,
    val lowerISO: Int,
    val upperShutterSpeed: Long,
    val lowerShutterSpeed: Long,
    val awbMode: List<Int>,
    val afMode: List<Int>
)