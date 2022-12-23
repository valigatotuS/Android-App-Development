package com.example.gps_coordinates.fragments

import android.content.Intent
import android.graphics.Typeface
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.fragment.app.Fragment
import com.example.gps_coordinates.HomeActivity
import com.example.gps_coordinates.R
import com.example.gps_coordinates.activities.GraphActivity
import com.example.gps_coordinates.models.ActivityModel
import com.example.gps_coordinates.models.CoordinatesModel
import com.example.gps_coordinates.activities.RunActivity
import com.example.gps_coordinates.database.DatabaseHandler


// TODO: Rename parameter arguments, choose names that match
// the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
private const val ARG_PARAM1 = "param1"
private const val ARG_PARAM2 = "param2"

/**
 * A simple [Fragment] subclass.
 * Use the [HistoryFragment.newInstance] factory method to
 * create an instance of this fragment.
 */
class HistoryFragment : Fragment() {
    // TODO: Rename and change types of parameters
    private var param1: String? = null
    private var param2: String? = null
    private lateinit var textviews: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            param1 = it.getString(ARG_PARAM1)
            param2 = it.getString(ARG_PARAM2)
        }

    }

    override fun onCreateView(

        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        var view: View = inflater.inflate(R.layout.fragment_history, container, false)
        val layout = view.findViewById<LinearLayout>(R.id.llMain)

        val activity_list = (activity as HomeActivity).getSportActivities()
        val sizeee = activity_list.size
        Log.e("activity_list", "activity_list $sizeee ")

        for (i in 0 until activity_list.size) {

            val j = activity_list.size - i - 1
            val activity_info = getActivityCoordinates(activity_list[j].id)


            if (activity_info != null) {
                if (activity_info.size > 5) {
                    val coord_groups = coordGroups(activity_info)
                    val distances = distanceGroups(coord_groups)
                    val speeds = speedCalcs(distances)
                    val average_speed = speeds.average()
                    val total_distance = distances.sum()/974
                    val pace = paceFromSpeed(average_speed)

                    Log.e("average_speed", "average_speed $average_speed ")
                    Log.e("total_distance", "total_distance $total_distance ")

                    val imageView = ImageView(context)
                    imageView.setImageResource(R.drawable.ic_running_drawing)
                    imageView.layoutParams = ViewGroup.LayoutParams(
                        ViewGroup.LayoutParams.WRAP_CONTENT,
                        ViewGroup.LayoutParams.WRAP_CONTENT

                    )
                    imageView.layoutParams.width = 150 //sets the width to 200 pixels
                    imageView.layoutParams.height = 150 //sets the height to 300 pixels
                    imageView.scaleType = ImageView.ScaleType.CENTER_CROP
                    layout.addView(imageView)

                    val params: LinearLayout.LayoutParams = LinearLayout.LayoutParams(
                        ViewGroup.LayoutParams.WRAP_CONTENT,
                        ViewGroup.LayoutParams.WRAP_CONTENT,
                    )
                    params.setMargins(200, -130, 0, 0)

                    val outdoor_text = TextView(context)
                    outdoor_text.text = "Outdoor run"
                    outdoor_text.setLayoutParams(params)
                    layout.addView(outdoor_text)

                    val params1: LinearLayout.LayoutParams = LinearLayout.LayoutParams(
                        ViewGroup.LayoutParams.WRAP_CONTENT,
                        ViewGroup.LayoutParams.WRAP_CONTENT,
                    )
                    params1.setMargins(200, 10, 0, 0)
                    val running_km = TextView(context)
                    running_km.setTypeface(null, Typeface.BOLD)
                    running_km.text = String.format("%.2f", total_distance) + " km"
                    running_km.setLayoutParams(params1)
                    layout.addView(running_km)

                    val params2: LinearLayout.LayoutParams = LinearLayout.LayoutParams(
                        ViewGroup.LayoutParams.WRAP_CONTENT,
                        ViewGroup.LayoutParams.WRAP_CONTENT,
                    )
                    params2.setMargins(600, -60, 0, 0)
                    val running_time = TextView(context)
                    val stringTime = getTimeString(activity_info.size-1)
                    running_time.text = "$stringTime"
                    running_time.setLayoutParams(params2)
                    layout.addView(running_time)

                    val params3: LinearLayout.LayoutParams = LinearLayout.LayoutParams(
                        ViewGroup.LayoutParams.WRAP_CONTENT,
                        ViewGroup.LayoutParams.WRAP_CONTENT,
                    )
                    params3.setMargins(900, -150, 0, 0)
                    val actual_time = TextView(context)
                    actual_time.text = activity_list[j].timestamp
                    actual_time.setLayoutParams(params3)
                    layout.addView(actual_time)

                    val params4: LinearLayout.LayoutParams = LinearLayout.LayoutParams(
                        ViewGroup.LayoutParams.WRAP_CONTENT,
                        ViewGroup.LayoutParams.WRAP_CONTENT,
                    )
                    params4.setMargins(900, 10, 0, 100)
                    val running_pace = TextView(context)

                    running_pace.text = "$pace /km"
                    running_pace.setLayoutParams(params4)
                    layout.addView(running_pace)

                    val views =
                        listOf(
                            imageView,
                            outdoor_text,
                            running_km,
                            running_time,
                            actual_time,
                            running_pace
                        )

                    for (view_n in views) {
                        view_n.setOnClickListener {
                            val intent = Intent(context, GraphActivity::class.java)
                            //intent.putExtra("id", activity_list[j].id)
                            val speed_array: DoubleArray = speeds.toDoubleArray()
                            intent.putExtra("speeds", speed_array)
                            intent.putExtra("int", 5)
                            startActivity(intent)

                        }
                    }
                }
            }
        }
        return view
    }

    companion object {
        /**
         * Use this factory method to create a new instance of
         * this fragment using the provided parameters.
         *
         * @param param1 Parameter 1.
         * @param param2 Parameter 2.
         * @return A new instance of fragment HistoryFragment.
         */
        // TODO: Rename and change types and number of parameters
        @JvmStatic
        fun newInstance(param1: String, param2: String) =
            HistoryFragment().apply {
                arguments = Bundle().apply {
                    putString(ARG_PARAM1, param1)
                    putString(ARG_PARAM2, param2)
                }
            }
    }

    private fun getActivityCoordinates(activity_id: Int): ArrayList<CoordinatesModel>? {
        val dbHandler = context?.let { DatabaseHandler(it) }
        val getActivityCoordinatesList: ArrayList<CoordinatesModel>? =
            dbHandler?.getActivityCoordinates_coordmodel(activity_id)

        if (getActivityCoordinatesList != null) {
            if (getActivityCoordinatesList.size > 0) {
                for (i in getActivityCoordinatesList) {
                    Log.e("array", i.toString())
                }
            }
        }
        return getActivityCoordinatesList
    }

    private fun speedCalcs(distances:ArrayList<Double>):ArrayList<Double>{
        val speeds = ArrayList<Double>()
        for (i in distances){
            if (i<100){
                speeds.add((i*3.7)/5)
            }
            else{
                speeds.add(0.0)
            }
        }
        return speeds
    }

    private fun distanceGroups(ArrayL:ArrayList<List<CoordinatesModel>>):ArrayList<Double>{
        var distance_array = ArrayList<Double>()
        for (i in 1 until ArrayL.size){
            distance_array.add((activity as HomeActivity).calcDist(ArrayL[i-1][0].latitude,ArrayL[i-1][0].longitude,ArrayL[i][0].latitude,ArrayL[i][0].longitude))
        }
        return distance_array
    }

    private fun coordGroups(ArrayL: ArrayList<CoordinatesModel>?): ArrayList<List<CoordinatesModel>> {

        val indexGroups = ArrayList<List<CoordinatesModel>>()
        var currentGroup = ArrayList<CoordinatesModel>()

        if (ArrayL != null) {
            for (i in 0 until ArrayL.size) {
                // Add the index to the current group
                currentGroup.add(ArrayL[i])

                // If the index is divisible by 5, add the current group to the list of index groups and start a new group
                if (i % 5 == 0) {
                    indexGroups.add(currentGroup)
                    currentGroup = ArrayList()
                }
            }
        }

// If there are any remaining indexes in the current group, add them to the list of index groups
        if (currentGroup.isNotEmpty()) {
            indexGroups.add(currentGroup)
        }
        return indexGroups
    }

    fun paceFromSpeed(speed: Double): String {
        val paceMinutes = (60 / speed).toInt()
        val paceSeconds = ((60 / speed) - paceMinutes) * 60
        return "${paceMinutes.toString().padStart(2, '0')}'${paceSeconds.toInt().toString().padStart(2, '0')}''"
    }

    fun getTimeString(seconds: Int): String {
        val hours = seconds / 3600
        val minutes = (seconds % 3600) / 60
        val remainingSeconds = seconds % 60
        return "${hours.toString().padStart(2, '0')}:${minutes.toString().padStart(2, '0')}:${remainingSeconds.toString().padStart(2, '0')}"
    }

}