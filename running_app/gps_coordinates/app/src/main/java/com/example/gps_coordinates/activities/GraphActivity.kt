package com.example.gps_coordinates.activities

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import com.example.gps_coordinates.R
import com.jjoe64.graphview.GraphView
import com.jjoe64.graphview.series.DataPoint
import com.jjoe64.graphview.series.LineGraphSeries

class GraphActivity : AppCompatActivity() {
    // on below line we are creating
    // variables for our graph view
    lateinit var lineGraphView: GraphView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_graph)
        val speeds = intent.getDoubleArrayExtra("speeds")
        //Log.e("speeds", "$speeds")

        // on below line we are initializing
        // our variable with their ids.
        lineGraphView = findViewById(R.id.idGraphView)

        // on below line we are adding data to our graph view.
        val series = LineGraphSeries<DataPoint>()
        if (speeds != null) {
            series.appendData(DataPoint(0.0, 0.0), true, speeds.size)
        }
        var timer = 0.0
        if (speeds != null) {
            for (i in 0 until speeds.size) {
                timer += 5.0
                val x = timer/60
                val y = speeds[i]

                val dataPoint = DataPoint(x, y)
                series.appendData(dataPoint, false, speeds.size+2)
            }
        }

        lineGraphView.viewport.isScrollable = true

        // on below line we are setting scalable.
        //lineGraphView.viewport.isScalable = true

        // on below line adding animation
        //lineGraphView.animate()

        // on below line we are setting scrollable
        // for point graph view

        // on below line we are setting color for series.
        series.color = R.color.purple_200

        // on below line we are adding
        // data series to our graph view.
        lineGraphView.addSeries(series)

    }
}