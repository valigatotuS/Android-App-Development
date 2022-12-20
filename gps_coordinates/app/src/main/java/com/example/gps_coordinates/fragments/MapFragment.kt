package com.example.gps_coordinates.fragments

import android.graphics.Color
import android.os.Bundle
import android.os.Handler
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import android.app.Activity
import android.widget.Toast
import androidx.preference.PreferenceManager
import com.example.gps_coordinates.R
import com.example.gps_coordinates.sportActivityStarted
import com.example.gps_coordinates.userLocation
import com.google.android.material.bottomnavigation.BottomNavigationView
import org.osmdroid.api.IMapController
import org.osmdroid.config.Configuration
import org.osmdroid.tileprovider.tilesource.TileSourceFactory
import org.osmdroid.util.GeoPoint
import org.osmdroid.views.MapView
import org.osmdroid.views.overlay.Polyline
import java.lang.Math.*
import java.time.Duration
import java.util.*
import java.util.concurrent.Executors
import java.util.concurrent.TimeUnit
import kotlin.math.pow


class MapFragment : Fragment() {
    private lateinit var durationTextV: TextView
    private lateinit var paceTextV: TextView
    private lateinit var distanceTextV: TextView
    private lateinit var durationlblTextV: TextView
    private lateinit var pacelblTextV: TextView
    private lateinit var distancelblTextV: TextView
    private lateinit var geolocationMap: MapView

    // Class global variabels
    private lateinit var geolocationMapController: IMapController
    private lateinit var userRunPath: Polyline
    var userCurrentPace: Double = 0.0
    var userCurrentDistance = 0.000
    var userCurrentDuration: Long = -5

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        var view: View = inflater.inflate(R.layout.fragment_map, container, false)

        // UI initialization
        geolocationMap = view.findViewById(R.id.geomap)
        durationTextV = view.findViewById(R.id.tv_duration)
        paceTextV = view.findViewById(R.id.tv_pace)
        distanceTextV = view.findViewById(R.id.tv_distance)
        durationlblTextV = view.findViewById(R.id.tv_duration_lbl)
        pacelblTextV = view.findViewById(R.id.tv_pace_lbl)
        distancelblTextV = view.findViewById(R.id.tv_distance_lbl)

        // Map initialization
        geolocationMap.setTileSource(TileSourceFactory.MAPNIK)
        geolocationMap.setBuiltInZoomControls(false)
        geolocationMap.setMultiTouchControls(true)
        geolocationMapController = geolocationMap.controller
        geolocationMapController.setZoom(18)
        if (userLocation != null){
            geolocationMapController.setCenter(GeoPoint(userLocation.first, userLocation.second))
        }
        // Line initialization (on map)
        userRunPath = Polyline(geolocationMap)
        userRunPath.setColor(Color.RED)
        userRunPath.setWidth(5.0f)

        if(sportActivityStarted){
            //Show UI elements
            pacelblTextV.setVisibility(View.VISIBLE)
            durationlblTextV.setVisibility(View.VISIBLE)
            distancelblTextV.setVisibility(View.VISIBLE)
            durationTextV.setVisibility(View.VISIBLE)
            distanceTextV.setVisibility(View.VISIBLE)
            paceTextV.setVisibility(View.VISIBLE)

             //background thread
            val executorService = Executors.newSingleThreadScheduledExecutor()
            executorService.scheduleAtFixedRate({
                updateCurrentRunningPath()
                livePathView()
                Log.e("logMAP", "thread running")
            }, 0, 1, TimeUnit.SECONDS)

            val timer = Timer()
            timer.scheduleAtFixedRate(object : TimerTask() {
                override fun run() {
                    getActivity()?.runOnUiThread {
                        userCurrentDuration += 5
                        updateLabels()
                        calcLiveStats(userRunPath.points.takeLast(5))
                    }
                }
            }, 0, 5000) // Update every second (1000 milliseconds)
        }
        else{
            pacelblTextV.setVisibility(View.GONE)
            durationlblTextV.setVisibility(View.GONE)
            distancelblTextV.setVisibility(View.GONE)
            durationTextV.setVisibility(View.GONE)
            distanceTextV.setVisibility(View.GONE)
            paceTextV.setVisibility(View.GONE)
        }

        return view
    }

    private fun updateCurrentRunningPath(){
        userRunPath.addPoint(GeoPoint(userLocation.first, userLocation.second))
    }

    private fun livePathView(){
        geolocationMap.getOverlays().clear()
        geolocationMapController.setCenter(GeoPoint(userLocation.first, userLocation.second))
        geolocationMap.getOverlays().add(userRunPath)
        geolocationMap.invalidate()
    }

    private fun updateLabels(){
        distanceTextV.text = String.format("%.3f", userCurrentDistance) + " km"
        durationTextV.text = convertSecondsToString(userCurrentDuration)
        paceTextV.text = convertSpeedToPace(userCurrentPace)
    }

    fun convertSecondsToString(seconds: Long): String {
        val hours = TimeUnit.SECONDS.toHours(seconds)
        val minutes = TimeUnit.SECONDS.toMinutes(seconds) % 60
        val secs = seconds % 60
        return String.format("%02d:%02d:%02d", hours, minutes, secs)
    }

    fun calcLiveStats(points: List<GeoPoint>){
        var distance = 0.0
        var totalDistance = 0.0
        var speeds = mutableListOf<Double>()
        var speed = 0.0

        if (points.size==5){
            for (i: kotlin.Int in 0..points.size-2){
                distance = calculateDistance(points[i], points[i+1])
                speeds.add(distance) // m/s
                Log.e("logDist", i.toString())
            }
            userCurrentPace = speeds.average()
            //Log.e("logSPEED", userCurrentPace.toString())
            userCurrentDistance += userCurrentPace * 5
        }
        // debug
        distancelblTextV.text = String.format("%.3f", distance) + " km"
        pacelblTextV.text = convertSpeedToPace(userCurrentPace)
    }

    fun calculateDistance(coord1: GeoPoint, coord2: GeoPoint): Double {
        val EARTH_RADIUS = 6371
        val lat1 = coord1.latitude
        val lon1 = coord1.longitude
        val lat2 = coord2.latitude
        val lon2 = coord2.longitude

        val latDistance = Math.toRadians(lat2 - lat1)
        val lonDistance = Math.toRadians(lon2 - lon1)
        val a = sin(latDistance / 2).pow(2.0) + cos(Math.toRadians(lat1)).pow(2.0) * cos(Math.toRadians(lat2)).pow(2.0) * sin(lonDistance / 2).pow(2.0)
        val c = 2 * asin(sqrt(a))

        return EARTH_RADIUS * c
    }

    fun convertSpeedToPace(speed: Double): String {
        if (speed < 0.01){ return "0'0'"}
        val pace = 1000.0 / speed // convert m/s to s/m
        val minutes = pace / 60.0 // convert s/m to min/km
        val seconds = (pace % 60) // get remainder in seconds
        return "${minutes.toInt()}'${seconds.toInt()}''"
    }
}