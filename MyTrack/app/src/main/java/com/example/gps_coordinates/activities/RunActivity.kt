package com.example.gps_coordinates.activities

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.View
import android.view.View.OnClickListener
import android.widget.Button
import android.widget.Toast
import com.example.gps_coordinates.R
import com.example.gps_coordinates.database.DatabaseHandler
import com.example.gps_coordinates.models.ActivityModel
import com.example.gps_coordinates.models.CoordinatesModel
import com.example.gps_coordinates.MainActivity
import com.example.gps_coordinates.locations_latitude
import com.example.gps_coordinates.locations_longtidude

class RunActivity : AppCompatActivity() {
    // create view
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_run)

        // UI elements
        val startButton: Button = findViewById(R.id.btn_start)
        val addGPSButton: Button = findViewById(R.id.btn_addgps)

        startButton.setOnClickListener {
            startSportActivity()
            getSportActivities()
        }

        addGPSButton.setOnClickListener{
            postCoordinate()
            getActivityCoordinates()
        }
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
                Log.e("array", i.toString())
            }
        }
    }

    private fun postCoordinate(){
        val coordinate = CoordinatesModel(1,1, locations_longtidude[0], locations_latitude[0])
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

}
