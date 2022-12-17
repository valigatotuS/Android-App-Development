package com.example.gps_coordinates

import android.Manifest
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.location.Location
import android.location.LocationListener
import android.location.LocationManager
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat

private const val TAG = "Tar2"
var locations_longtidude = mutableListOf<Double>(0.0,0.0)
var locations_latitude = mutableListOf<Double>(0.0,0.0)
var time = System.currentTimeMillis();
var results = mutableListOf<Double>(0.0)

class MainActivity : AppCompatActivity(), LocationListener {
    private lateinit var locationManager: LocationManager
    private lateinit var tvGpsLocation: TextView
    private lateinit var tvSpeed: TextView
    private lateinit var tvDistance: TextView
    private lateinit var tvTime: TextView
    private val locationPermissionCode = 2
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        title = "Location App"
        val button: Button = findViewById(R.id.getLocation)
        button.setOnClickListener {
            getLocation()
        }
        val graphButton: Button = findViewById(R.id.goToGraph)

        graphButton.setOnClickListener{
            val intent = Intent(this, GraphActivity::class.java)
            startActivity(intent)
        }
    }
    private fun getLocation() {
        Log.i(TAG, "Getting location")
        locationManager = getSystemService(Context.LOCATION_SERVICE) as LocationManager
        if ((ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED)) {
            ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.ACCESS_FINE_LOCATION), locationPermissionCode)
        }
        locationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, 5000, 5f, this)
    }
    private fun calcDist(lat1: Double, lon1: Double, lat2: Double, lon2: Double): Double {
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

    private fun deg2rad(deg: Double): Double {
        return deg * Math.PI / 180.0
    }

    private fun rad2deg(rad: Double): Double {
        return rad * 180.0 / Math.PI
    }
    override fun onLocationChanged(location: Location) {
        Log.i(TAG, "onLocationChanged ${location.latitude}")
        tvGpsLocation = findViewById(R.id.textView)
        tvGpsLocation.text = "Latitude: " + location.latitude + " , Longitude: " + location.longitude
        //tvGpsLocation.text = "Location: " + location

        //update locations
        locations_longtidude[0] = locations_longtidude[1]
        locations_latitude[0] = locations_latitude[1]
        locations_latitude[1] = location.latitude
        locations_longtidude[1] = location.longitude

        var distance = calcDist(locations_latitude[1], locations_longtidude[1], locations_latitude[0],  locations_longtidude[0] )
        //var distance = calcDist(100.0, 50.0, 99.0,  50.0 )
        tvDistance = findViewById(R.id.distanceView)
        tvDistance.text = "Distance: " + "%.2f".format(distance) + " m"

        var timeElapsed = (System.currentTimeMillis() - time) / 1000 //ms to s
        tvTime = findViewById(R.id.timeView)
        tvTime.text = "Elapsed time: " + timeElapsed + "s"

        var speed = (3.6 * distance) / timeElapsed
        Log.i(TAG, "Distance $distance")

        tvSpeed = findViewById(R.id.speedView)
        tvSpeed.text = "Speed: " + "%.2f".format(speed) + " km/h"
        time = System.currentTimeMillis();

    }
    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == locationPermissionCode) {
            if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                Toast.makeText(this, "Permission Granted", Toast.LENGTH_SHORT).show()
            }
            else {
                Toast.makeText(this, "Permission Denied", Toast.LENGTH_SHORT).show()
            }
        }
    }
}