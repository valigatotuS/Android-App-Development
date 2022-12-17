package com.example.gps_coordinates.lib

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.location.LocationManager
import android.location.Location
import android.location.LocationListener
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.content.ContextCompat.getSystemService


// Function to calculate distance between two GPS coordinates
public fun calcDist(lat1: Double, lon1: Double, lat2: Double, lon2: Double): Double {
    val theta = lon1 - lon2
    var dist = (Math.sin(deg2rad(lat1))
            * Math.sin(deg2rad(lat2))
            + (Math.cos(deg2rad(lat1))
            * Math.cos(deg2rad(lat2))
            * Math.cos(deg2rad(theta))))
    dist = Math.acos(dist)
    dist = rad2deg(dist)
    dist = dist * 60 * 1.1515 * 1.60934 * 1000
    return dist
}

public fun deg2rad(deg: Double): Double {
    return deg * Math.PI / 180.0
}

public fun rad2deg(rad: Double): Double {
    return rad * 180.0 / Math.PI
}



