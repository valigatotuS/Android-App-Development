package com.example.gps_coordinates

import android.Manifest
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Color
import android.location.Location
import android.location.LocationListener
import android.location.LocationManager
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.MenuItem
import android.view.MenuItem.OnMenuItemClickListener
import android.view.View
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.preference.PreferenceManager
import com.example.gps_coordinates.activities.GraphActivity
import com.example.gps_coordinates.databinding.ActivityMainBinding
import com.example.gps_coordinates.fragments.HistoryFragment
import com.example.gps_coordinates.fragments.HomeFragment
import com.example.gps_coordinates.fragments.MapFragment
import com.example.gps_coordinates.fragments.ProfileFragment
import com.google.android.material.bottomappbar.BottomAppBar
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.android.material.floatingactionbutton.FloatingActionButton
import org.osmdroid.config.Configuration

public var sportActivityStarted = false
lateinit var userLocation: Pair<Double, Double>

private const val TAG = "Tar2"

class HomeActivity : AppCompatActivity(), LocationListener {
    //public var sportActivityStarted = false
    private lateinit var locationManager: LocationManager
    private val locationPermissionCode = 2

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // prepare map
        Configuration.getInstance().load(this, PreferenceManager.getDefaultSharedPreferences(this))
        setContentView(R.layout.activity_home)

        //UI elements
        val testButton: Button = findViewById(R.id.btn_test)
        val actionButton: FloatingActionButton = findViewById(R.id.btn_action)
        val navBar: BottomAppBar = findViewById(R.id.bottomAppBar)

        getLocation()

        // test button execution
        testButton.setOnClickListener{
            startActivity(Intent(this, MainActivity::class.java))
        }

        // action button execution
        actionButton.setOnClickListener{
            replaceFragment(MapFragment()) // go to map view
            // start activity
            if (!sportActivityStarted){
                // change UI
                navBar.animate().translationY(200.0f).setDuration(400).withEndAction {
                    navBar.setVisibility(View.GONE)
                    actionButton.setColorFilter(Color.LTGRAY)
                }

                // start activity
                sportActivityStarted = true
            }
            else{
                navBar.animate().translationY(0.0f).setDuration(400).withStartAction {
                    navBar.setVisibility(View.VISIBLE)
                    actionButton.setColorFilter(Color.WHITE)
                    navBar.isSelected()
                }
                replaceFragment(HistoryFragment())
                sportActivityStarted = false
            }
        }

        val bottomNav: BottomNavigationView = findViewById(R.id.bottomNavigationView)
        bottomNav.setOnItemSelectedListener {
            when (it.itemId) {
                R.id.home -> {
                    replaceFragment(HomeFragment())
                    true
                }
                R.id.map -> {
                    replaceFragment(MapFragment())
                    true
                }
                R.id.history -> {
                    replaceFragment(HistoryFragment())
                    true
                }
                R.id.profile -> {
                    replaceFragment(ProfileFragment())
                    true
                }
                else -> {false}
            }
        }
    }

    private fun replaceFragment(fragment: Fragment){
        val fragmentManager = supportFragmentManager
        val fragmentTransaction = fragmentManager.beginTransaction()
        fragmentTransaction.replace(R.id.frame_container, fragment)
        fragmentTransaction.commit()
    }

    // GPS functions

    // Function to request location updates
    private fun getLocation() {
        locationManager = getSystemService(Context.LOCATION_SERVICE) as LocationManager

        // Check for location permission
        if ((ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED)) {
            ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.ACCESS_FINE_LOCATION), locationPermissionCode)
        }
        locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 1000, 5f, this)
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
        userLocation = Pair(location.latitude, location.longitude)
//        Log.e("logMAP", userLocation.toString() + " " + System.currentTimeMillis().toString())
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