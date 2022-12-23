package com.example.gps_coordinates

import android.Manifest
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Color
import android.hardware.*
import android.location.Location
import android.location.LocationListener
import android.location.LocationManager
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.Gravity
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
import com.example.gps_coordinates.database.DatabaseHandler
import com.example.gps_coordinates.databinding.ActivityMainBinding
import com.example.gps_coordinates.fragments.HistoryFragment
import com.example.gps_coordinates.fragments.HomeFragment
import com.example.gps_coordinates.fragments.MapFragment
import com.example.gps_coordinates.fragments.ProfileFragment
import com.example.gps_coordinates.models.ActivityModel
import com.example.gps_coordinates.models.CoordinatesModel
import com.google.android.material.bottomappbar.BottomAppBar
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.android.material.floatingactionbutton.FloatingActionButton
import org.osmdroid.config.Configuration
import org.osmdroid.util.GeoPoint
import org.osmdroid.views.overlay.Polyline
import java.lang.Math.sqrt
import java.text.SimpleDateFormat
import java.time.LocalDateTime
import java.util.*

public var sportActivityStarted = false
lateinit var userLocation: Pair<Double, Double>

private const val TAG = "Tar2"

class HomeActivity : AppCompatActivity(), LocationListener {
    //public var sportActivityStarted = false
    private lateinit var locationManager: LocationManager
    private val locationPermissionCode = 2
    private var motionDetected = true

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
            // start activity
            if (!sportActivityStarted){
                    // change UI
                    navBar.animate().translationY(200.0f).setDuration(400).withEndAction {
                    navBar.setVisibility(View.GONE)
                    actionButton.setColorFilter(Color.LTGRAY)

                    // start running activity
                    startSportActivity(type=1) // type 1 = running activity
                }

                // start activity
                replaceFragment(MapFragment()) // go to map view
                sportActivityStarted = true
            }
            else{
                    // change UI
                    navBar.animate().translationY(0.0f).setDuration(400).withStartAction {
                    navBar.setVisibility(View.VISIBLE)
                    actionButton.setColorFilter(Color.WHITE)
                    navBar.isSelected()

                    // stop activity
                    getSportActivities()
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
    fun calcDist(lat1: Double, lon1: Double, lat2: Double, lon2: Double): Double {
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
        motion()
        /*if (motionDetected){
            motionDetected = false
            userLocation = Pair(location.latitude, location.longitude)
        }*/
        userLocation = Pair(location.latitude, location.longitude)
        //Log.e("logACCELEROMETER", calculateAcceleration().toString())
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

    private fun startSportActivity(type:Int){
        motion()
        val date = Date().toString()
        val activityRecord = ActivityModel(0, type, date, "Afternoon run", userLocation.first, userLocation.second)
        val dbHandler = DatabaseHandler(this)
        val addActivityResult = dbHandler.addActivity(activityRecord)

        if (addActivityResult > 0){
            var toast = Toast.makeText(this, "Running activity just started", Toast.LENGTH_SHORT)
            toast.setGravity(Gravity.TOP or Gravity.CENTER_HORIZONTAL, 0, 200)
            toast.show()
        }
        else{
            Toast.makeText(this, "Running activity started unsuccesfuly", Toast.LENGTH_SHORT).show()
        }
    }


    fun getSportActivities():ArrayList<ActivityModel>{
        val dbHandler = DatabaseHandler(this)
        val getSportActivitiesList : ArrayList<ActivityModel> = dbHandler.getSportActivity()

        if (getSportActivitiesList.size > 0){
            for(i in getSportActivitiesList){
                Log.e("array", i.toString())
            }
        }
        return getSportActivitiesList
    }

    private fun postCoordinates(coordinates: List<GeoPoint>){
        val dbHandler = DatabaseHandler(this)
        val addCoordinateResult = dbHandler.addCoordinates(coordinates, 1)

        if (addCoordinateResult.size == coordinates.size){
            Toast.makeText(this, "coordinates posted succesfuly", Toast.LENGTH_SHORT).show()
        }
        else{
            Toast.makeText(this, "coordinates posted unsuccesfuly", Toast.LENGTH_SHORT).show()
        }
    }

    fun getLatestActivity(): Int{
        val dbHandler = DatabaseHandler(this)
        val current_activity =dbHandler.getLatestActivity()
        return current_activity
    }

    private fun postCoordinates(coordinates: List<GeoPoint>, activity_id:Int){
        val dbHandler = DatabaseHandler(this)
        val addCoordinateResult = dbHandler.addCoordinates(coordinates, activity_id)

        if (addCoordinateResult.size == coordinates.size){
            Toast.makeText(this, "coordinates posted succesfuly", Toast.LENGTH_SHORT).show()
        }
        else{
            Toast.makeText(this, "coordinates posted unsuccesfuly", Toast.LENGTH_SHORT).show()
        }
    }

    fun getActivityCoordinates(activity_id: Int): ArrayList<GeoPoint>{
        val dbHandler = DatabaseHandler(this)
        val activityCoordinatesList : ArrayList<GeoPoint> = dbHandler.getActivityCoordinates(activity_id)
        // debug
        Log.e("logCoord", activityCoordinatesList.toString())

        if (activityCoordinatesList.size > 0){
            for(i in activityCoordinatesList){
                Log.e("logCoord", i.toString())
            }
            return activityCoordinatesList
        }

        return ArrayList<GeoPoint>()
    }

    // Calculate the acceleration of the device
    fun calculateAcceleration(): Double {
        val accelerometer = getSystemService(Context.SENSOR_SERVICE) as SensorManager
        val accelerometerSensor = accelerometer.getDefaultSensor(Sensor.TYPE_ACCELEROMETER)
        val accelerometerValues = FloatArray(3)
        accelerometer.registerListener(object : SensorEventListener {
            override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {
            }
            override fun onSensorChanged(event: SensorEvent?) {
                accelerometerValues[0] = event!!.values[0]
                accelerometerValues[1] = event.values[1]
                accelerometerValues[2] = event.values[2]
            }
        }, accelerometerSensor, SensorManager.SENSOR_DELAY_NORMAL)
        val acceleration = sqrt((accelerometerValues[0] * accelerometerValues[0] + accelerometerValues[1] * accelerometerValues[1] + accelerometerValues[2] * accelerometerValues[2]).toDouble())
        return acceleration
    }

    private fun motion(){
        val sensorManager = getSystemService(Context.SENSOR_SERVICE) as SensorManager
        val mSensor: Sensor? = sensorManager.getDefaultSensor(Sensor.TYPE_SIGNIFICANT_MOTION)
        val triggerEventListener = object : TriggerEventListener() {
            override fun onTrigger(event: TriggerEvent?) {
                Log.e("logMOTION", "motion has been detected")
                motionDetected = true
            }
        }
        mSensor?.also { sensor ->
            sensorManager.requestTriggerSensor(triggerEventListener, sensor)
        }
    }

}