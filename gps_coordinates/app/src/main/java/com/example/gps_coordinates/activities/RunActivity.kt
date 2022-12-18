package com.example.gps_coordinates.activities

import android.graphics.Color
import android.os.Bundle
import android.util.Log
import android.view.View
import android.view.Window
import android.widget.Button
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.preference.PreferenceManager
import com.example.gps_coordinates.R
import com.example.gps_coordinates.database.DatabaseHandler
import com.example.gps_coordinates.locations_latitude
import com.example.gps_coordinates.locations_longtidude
import com.example.gps_coordinates.models.ActivityModel
import com.example.gps_coordinates.models.CoordinatesModel
import com.example.gps_coordinates.userLocations
import org.osmdroid.api.IMapController
import org.osmdroid.config.Configuration
import org.osmdroid.tileprovider.tilesource.TileSourceFactory
import org.osmdroid.util.GeoPoint
import org.osmdroid.views.MapView
import org.osmdroid.views.overlay.Marker
import org.osmdroid.views.overlay.OverlayItem
import org.osmdroid.views.overlay.Polyline
import java.time.LocalDateTime
import java.util.*
import java.util.concurrent.Executors
import java.util.concurrent.TimeUnit
import kotlin.collections.ArrayList
import kotlin.random.Random

//import org.osmdroid.bonuspack.routing

class RunActivity : AppCompatActivity() {
    // Class global variabels
    private lateinit var geolocationMap: MapView
    private lateinit var geolocationMapController: IMapController
    private lateinit var userRunPath: Polyline

    // create view
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // prepare map
        Configuration.getInstance().load(this, PreferenceManager.getDefaultSharedPreferences(this))
        // set view
        setContentView(R.layout.activity_run)

        // UI elements
        val startButton: Button = findViewById(R.id.btn_start)
        val stopButton: Button = findViewById(R.id.btn_stop)
        val addGPSButton: Button = findViewById(R.id.btn_addgps)

        // Map & Controller settings
        geolocationMap = findViewById(R.id.map)
        geolocationMap.setTileSource(TileSourceFactory.MAPNIK)
        geolocationMap.setBuiltInZoomControls(false)
        geolocationMap.setMultiTouchControls(true)
        geolocationMapController = geolocationMap.controller
        geolocationMapController.setZoom(18)
        geolocationMapController.setCenter(GeoPoint(userLocations.last().first, userLocations.last().second))

        // Current running path settings
        userRunPath = Polyline(geolocationMap)
        userRunPath.setColor(Color.RED)
        userRunPath.setWidth(5.0f)


        startButton.setOnClickListener {
            // change UI elements
            startButton.setVisibility(View.GONE)
            stopButton.setVisibility(View.VISIBLE)

            // Start running activity
            startNewRunningActivity()
        }

        stopButton.setOnClickListener{
            // change UI elements
            stopButton.setVisibility(View.GONE)
            startButton.setVisibility(View.VISIBLE)

            // Stop running activity
            stopCurrentRunningActivity()
        }

        addGPSButton.setOnClickListener{
            // test button
            postCoordinate()
            getActivityCoordinates()
        }

        // refresh map and draw userpath every second
        val executorService = Executors.newSingleThreadScheduledExecutor()
        executorService.scheduleAtFixedRate({
            updateCurrentRunningPath()
            liveMapView()
        }, 0, 5, TimeUnit.SECONDS)
    }


    private fun startSportActivity(){
        // add userinput instead of random values !!
        val activityRecord = ActivityModel(1, 1, "01/02/2021 17:54:45", "Afternoon run", 4.4699, 50.5039)
        val dbHandler = DatabaseHandler(this)
        val addActivityResult = dbHandler.addActivity(activityRecord)

        if (addActivityResult > 0){
            Toast.makeText(this, "New activity started succesfuly", Toast.LENGTH_SHORT).show()
        }
        else{
            Toast.makeText(this, "New activity started unsuccesfuly", Toast.LENGTH_SHORT).show()
        }
    }

    private fun getSportActivities(){
        val dbHandler = DatabaseHandler(this)
        val getSportActivitiesList : ArrayList<ActivityModel> = dbHandler.getSportActivity()

        if (getSportActivitiesList.size > 0){
            for(i in getSportActivitiesList){
                Log.e("logDB", i.toString())
            }
        }
    }

    private fun postCoordinate(){
        val coordinate = CoordinatesModel(1,1, userLocations.last().first, userLocations.last().second)
        val dbHandler = DatabaseHandler(this)
        val addCoordinateResult = dbHandler.addCoordinate(coordinate)

        if (addCoordinateResult > 0){
            Toast.makeText(this, "coordinate posted succesfuly", Toast.LENGTH_SHORT).show()
        }
        else{
            Toast.makeText(this, "coordinate posted unsuccesfuly", Toast.LENGTH_SHORT).show()
        }
    }

    private fun getActivityCoordinates(){
        val dbHandler = DatabaseHandler(this)
        val getActivityCoordinatesList : ArrayList<CoordinatesModel> = dbHandler.getActivityCoordinates(1)

        if (getActivityCoordinatesList.size > 0){
            for(i in getActivityCoordinatesList){
                Log.e("array", i.toString())
            }
        }
    }

    private fun addMarker(title: String) { //center: GeoPoint?,
        val center = GeoPoint(locations_latitude[1], locations_longtidude[1]) // + Random.nextDouble(until = 0.001))
        val marker = Marker(geolocationMap)
        marker.position = center
        marker.setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM)
        //marker.icon = resources.getDrawable(R.drawable.ic_launcher_background)
        //geolocationMap.getOverlays().clear()
        geolocationMap.getOverlays().add(marker)
        geolocationMap.invalidate()
        marker.title = title

    }

    private fun addLine() {
        val line = Polyline(geolocationMap)
        val startPoint = GeoPoint(GeoPoint(userLocations[userLocations.size-2].first, userLocations[userLocations.size-2].second)) //50.601677,4.1628139)//
        val endPoint = GeoPoint(userLocations.last().first, userLocations.last().second)
        line.addPoint(startPoint)
        line.addPoint(endPoint)
        line.setColor(Color.RED)
        line.setWidth(5.0f)
        geolocationMap.getOverlays().add(line)
        geolocationMap.invalidate()
    }

    private fun drawPath() {
        geolocationMap.getOverlays().clear()
        userRunPath.addPoint(GeoPoint(userLocations.last().first, userLocations.last().second))
        geolocationMap.getOverlays().add(userRunPath)
        geolocationMap.invalidate()
    }

    private fun updateCurrentRunningPath(){
        userRunPath.addPoint(GeoPoint(userLocations.last().first, userLocations.last().second))
    }

    private fun liveMapView(){
        geolocationMap.getOverlays().clear()
        geolocationMapController.setCenter(GeoPoint(userLocations.last().first, userLocations.last().second))
        geolocationMap.getOverlays().add(userRunPath)
        geolocationMap.invalidate()
    }

    private fun drawActivityPath(activitity_id:Int){
        val dbHandler = DatabaseHandler(this)
        val activityCoordinatesList : ArrayList<CoordinatesModel> = dbHandler.getActivityCoordinates(activitity_id)

        if (activityCoordinatesList.size > 0){
            for(i in activityCoordinatesList){
                userRunPath.addPoint(GeoPoint(i.longitude, i.latitude))
                Log.e("log", "lat/long: " + i.latitude.toString() + "/" + i.longitude.toString())
            }
        }
        geolocationMap.getOverlays().clear()
        geolocationMap.getOverlays().add(userRunPath)
        geolocationMap.invalidate()
    }

    private fun startNewRunningActivity(){
        val activityRecord = ActivityModel(0, 1, Calendar.getInstance().time.toString(), "Run", userLocations.last().first, userLocations.last().second)
        val dbHandler = DatabaseHandler(this)
        val addActivityResult = dbHandler.addActivity(activityRecord)

        if (addActivityResult > 0){
            Toast.makeText(this, "New activity started succesfuly", Toast.LENGTH_SHORT).show()
        }
        else{
            Toast.makeText(this, "New activity started unsuccesfuly", Toast.LENGTH_SHORT).show()
        }
    }

    private fun stopCurrentRunningActivity(){
        Toast.makeText(this, "Activity stopped", Toast.LENGTH_SHORT).show()
        // show the running stats, switch to new activity page
        geolocationMap.getOverlays().clear()
    }
}
