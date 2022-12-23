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
import com.example.gps_coordinates.activities.GraphActivity
import com.example.gps_coordinates.activities.RunActivity
import com.example.gps_coordinates.database.DatabaseHandler
import com.example.gps_coordinates.models.ActivityModel
import java.util.concurrent.Executors
import java.util.concurrent.TimeUnit

private const val TAG = "Tar2"
var locations_longtidude = mutableListOf<Double>(0.0,0.0)
var locations_latitude = mutableListOf<Double>(0.0,0.0)
public var userLocations = ArrayList<Pair<Double, Double>>()
var time = System.currentTimeMillis();
var results = mutableListOf<Double>(0.0)

class MainActivity : AppCompatActivity(), LocationListener {
    // variables to store UI elements
    private lateinit var locationManager: LocationManager
    private lateinit var tvGpsLocation: TextView
    private lateinit var tvSpeed: TextView
    private lateinit var tvDistance: TextView
    private lateinit var tvTime: TextView

    // constant for location permission request code
    private val locationPermissionCode = 2



    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        title = "Location App"
        // constants to store UI elements
        val graphButton: Button = findViewById(R.id.goToGraph)
        val runButton: Button = findViewById(R.id.btn_run)

        // Set up click listener for "Get Location" button
        val button: Button = findViewById(R.id.getLocation)
        button.setOnClickListener {
            getLocation()
        }

        // Set up click listener for "Go to Graph" button
        graphButton.setOnClickListener{
            val intent = Intent(this, GraphActivity::class.java)
            startActivity(intent)
        }

        // Set up click listener for "Run" button
        runButton.setOnClickListener{
            val intent = Intent(this, RunActivity::class.java)
            startActivity(intent)
        }


    }

    // Function to request location updates
    private fun getLocation() {
        Log.i(TAG, "Getting location")
        locationManager = getSystemService(Context.LOCATION_SERVICE) as LocationManager

        // Check for location permission
        if ((ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED)) {
            ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.ACCESS_FINE_LOCATION), locationPermissionCode)
        }
        locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 5000, 5f, this)
    }

    // Function to calculate distance between two GPS coordinates
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

        userLocations.add(Pair(location.latitude, location.longitude))

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