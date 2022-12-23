package com.example.gps_coordinates.models

data class ActivityModel (
    val id: Int,
    val type:Int,
    val timestamp: String,
    val title: String,
    val longitude: Double,
    val latitude: Double,
)