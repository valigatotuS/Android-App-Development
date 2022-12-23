package com.example.gps_coordinates.fragments


import android.graphics.Color
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.widget.ThemedSpinnerAdapter.Helper
import androidx.fragment.app.Fragment
import com.example.gps_coordinates.*
import com.example.gps_coordinates.database.DatabaseHandler
import org.osmdroid.api.IMapController
import org.osmdroid.tileprovider.tilesource.TileSourceFactory
import org.osmdroid.util.GeoPoint
import org.osmdroid.views.MapView
import org.osmdroid.views.overlay.Marker
import org.osmdroid.views.overlay.Polyline
import java.lang.Math.*
import java.util.*
import java.util.concurrent.Executors
import java.util.concurrent.TimeUnit
import kotlin.collections.ArrayList
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
    var userCurrentDuration: Long = -1
    var userCurrentActivityID : Int = 2

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
        drawLastRun() // draw latest run on map

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

            //
            userCurrentActivityID = (activity as HomeActivity).getLatestActivity()

             //background thread
            val executorService = Executors.newSingleThreadScheduledExecutor()
            executorService.scheduleAtFixedRate({

                Log.e("logMAP", "thread running")
            }, 0, 1, TimeUnit.SECONDS)

            val timer = Timer()
            timer.scheduleAtFixedRate(object : TimerTask() {
                override fun run() {
                    getActivity()?.runOnUiThread {
                        userCurrentDuration += 1
                        updateCurrentRunningPath()
                        if (userCurrentDuration % 30 == 0L){
                            Log.e("idL", userCurrentActivityID.toString())
                            postCoordinates(userRunPath.points.takeLast(30), userCurrentActivityID)
                            calcLiveStats(userRunPath.points.takeLast(31))
                        }
                        if (userCurrentDuration % 5 === 1L){
                            livePathView()
                        }
                        updateLabels()
                    }
                }
            }, 200, 1000) // Update every second (1000 milliseconds)
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
        addUserPositionMarker()
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

        if (points.size>29){
            for (i: kotlin.Int in 0..points.size-2){
                totalDistance += calculateDistance(points[i], points[i+1])
                //Log.e("logDist", i.toString())
                //distancelblTextV.text = distance.toString()
            }

            Log.e("logSPEED", userCurrentPace.toString())
            userCurrentDistance += totalDistance
            userCurrentPace = totalDistance / 30
        }
        // debug
//        distancelblTextV.text = String.format("%.3f", distance) + " km"
//        pacelblTextV.text = convertSpeedToPace(userCurrentPace)
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
        val pace = 1000.0 / speed // convert m/s to s/m
        val minutes = pace / 60.0 // convert s/m to min/km
        val seconds = (pace % 60) // get remainder in seconds
        if (minutes > 60){ return "0'0''"}
        return "${minutes.toInt()}'${seconds.toInt()}''"
    }

    private fun postCoordinates(coordinates: List<GeoPoint>, activity_id:Int){
        val ctx = context
        val dbHandler = ctx?.let { DatabaseHandler(it) }
        val addCoordinateResult = dbHandler?.addCoordinates(coordinates, activity_id)

        if (addCoordinateResult != null) {
            if (addCoordinateResult.size == coordinates.size){
                Toast.makeText(ctx, "coordinates posted succesfuly", Toast.LENGTH_SHORT).show()
            }
            else{
                Toast.makeText(ctx, "coordinates posted unsuccesfuly", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun drawLastRun(){
        val coordinates = (activity as HomeActivity).getActivityCoordinates((activity as HomeActivity).getLatestActivity()-1)

        if (coordinates.size < 10){return}
        var polyline = Polyline(geolocationMap)
        polyline.color = Color.RED
        polyline.width = 5.0f
        polyline.setPoints(coordinates)
        geolocationMap.overlays.clear()
        geolocationMap.overlays.add(polyline)
        geolocationMap.invalidate()
    }

    private fun addUserPositionMarker() {
        val center = GeoPoint(userLocation.first, userLocation.second) // + Random.nextDouble(until = 0.001))
        val marker = Marker(geolocationMap)
        marker.position = center
        marker.setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM)
        marker.icon = resources.getDrawable(R.drawable.ic_baseline_adjust_24)
        geolocationMap.getOverlays().add(marker)
        geolocationMap.invalidate()
    }
}