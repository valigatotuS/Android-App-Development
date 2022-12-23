package com.example.gps_coordinates.database

import android.annotation.SuppressLint
import android.content.ContentValues
import android.content.Context
import android.database.Cursor
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteException
import android.database.sqlite.SQLiteOpenHelper
import android.util.Log
import com.example.gps_coordinates.models.CoordinatesModel
import com.example.gps_coordinates.models.ActivityModel
import org.osmdroid.util.GeoPoint

class DatabaseHandler(context: Context) :
    SQLiteOpenHelper(context, DATABASE_NAME, null, DATABASE_VERSION) {

    companion object {
        // Database metadata
        private const val DATABASE_VERSION = 1
        private const val DATABASE_NAME = "ActivitiesDatabase"
        private const val TABLE_COORDINATES = "CoordinatesTable"
        private const val TABLE_ACTIVITIES = "ActivitiesTable"

        // Columns names
        private const val KEY_ID = "_id"
        private const val KEY_TYPE = "type"
        private const val KEY_TIMESTAMP = "timestamp"
        private const val KEY_TITLE = "title"
        private const val KEY_LATITUDE = "latitude"
        private const val KEY_LONGITUDE = "longitude"
        private const val KEY_ACTIVITY_ID = "activity_id"
    }

    override fun onCreate(db: SQLiteDatabase?) {
        val CREATE_ACTIVITIES_TABLE = ("CREATE TABLE IF NOT EXISTS " + TABLE_ACTIVITIES + " (" +
                KEY_ID + " INTEGER PRIMARY KEY AUTOINCREMENT," +
                KEY_TYPE + " INTEGER," +
                KEY_TIMESTAMP + " TEXT," +
                KEY_TITLE + " TEXT," +
                KEY_LATITUDE + " TEXT," +
                KEY_LONGITUDE + " TEXT" + ");")

        val CREATE_COORDINATES_TABLE = ("CREATE TABLE IF NOT EXISTS " + TABLE_COORDINATES + " (" +
                KEY_ID + " INTEGER PRIMARY KEY AUTOINCREMENT," +
                KEY_ACTIVITY_ID + " INTEGER," +
                KEY_LATITUDE + " TEXT," +
                KEY_LONGITUDE + " TEXT" + ");")

        db?.execSQL(CREATE_ACTIVITIES_TABLE)
        db?.execSQL(CREATE_COORDINATES_TABLE)
    }

    override fun onUpgrade(db: SQLiteDatabase?, oldVersion: Int, newVersion: Int) {
        db!!.execSQL("DROP TABLE IF EXISTS $TABLE_ACTIVITIES")
        db!!.execSQL("DROP TABLE IF EXISTS $TABLE_COORDINATES")
    }

    fun addActivity(activity_: ActivityModel): Long {
        val db = this.writableDatabase

        val contentValues = ContentValues()
        contentValues.put(KEY_TYPE, activity_.type)
        contentValues.put(KEY_TIMESTAMP, activity_.timestamp)
        contentValues.put(KEY_TITLE, activity_.title)
        contentValues.put(KEY_LATITUDE, activity_.latitude)
        contentValues.put(KEY_LONGITUDE, activity_.longitude)

        val result = db.insert(TABLE_ACTIVITIES, null, contentValues)
        db.close()
        return result
    }

    fun addCoordinate(coord: CoordinatesModel): Long {
        val db = this.writableDatabase

        val contentValues = ContentValues()
        contentValues.put(KEY_ACTIVITY_ID, coord.activity_id)
        contentValues.put(KEY_LATITUDE, coord.latitude)
        contentValues.put(KEY_LONGITUDE, coord.longitude)

        val result = db.insert(TABLE_COORDINATES, null, contentValues)
        db.close()
        return result
    }

    fun addCoordinates(coordinates: List<GeoPoint>, activityId: Int): List<Long> {
        val db = this.writableDatabase
        val results = mutableListOf<Long>()

        db.beginTransaction()
        try {
            for (coord in coordinates) {
                val contentValues = ContentValues()
                contentValues.put(KEY_ACTIVITY_ID, activityId)
                contentValues.put(KEY_LATITUDE, coord.latitude)
                contentValues.put(KEY_LONGITUDE, coord.longitude)

                val result = db.insertWithOnConflict(TABLE_COORDINATES, null, contentValues, SQLiteDatabase.CONFLICT_IGNORE)
                results.add(result)
            }
            db.setTransactionSuccessful()
        } finally {
            db.endTransaction()
        }

        db.close()
        return results
    }

    @SuppressLint("Range")
    fun getSportActivity():ArrayList<ActivityModel>{
        val sportActivitiesList = ArrayList<ActivityModel>()
        val selectQuery = "SELECT * FROM $TABLE_ACTIVITIES"
        val db = this.readableDatabase

        try{
            val cursor : Cursor = db.rawQuery(selectQuery, null)
            if(cursor.moveToFirst()){
                do{
                    val sportActivity = ActivityModel(
                        cursor.getInt(cursor.getColumnIndex(KEY_ID)),
                        cursor.getInt(cursor.getColumnIndex(KEY_TYPE)),
                        cursor.getString(cursor.getColumnIndex(KEY_TIMESTAMP)),
                        cursor.getString(cursor.getColumnIndex(KEY_TITLE)),
                        cursor.getDouble(cursor.getColumnIndex(KEY_LONGITUDE)),
                        cursor.getDouble(cursor.getColumnIndex(KEY_LATITUDE))
                    )
                    sportActivitiesList.add(sportActivity)
                }while(cursor.moveToNext())
            }
            cursor.close()
        }catch (e: SQLiteException){
            db.execSQL(selectQuery)
            return ArrayList()
        }
        return sportActivitiesList
    }

    @SuppressLint("Range")
    fun getActivityCoordinates(activity_id: Int):ArrayList<GeoPoint>{
        val activityCoordinatesList = ArrayList<GeoPoint>()
        val selectQuery = "SELECT * FROM $TABLE_COORDINATES WHERE $KEY_ACTIVITY_ID = $activity_id"
        val db = this.readableDatabase

        try{
            val cursor : Cursor = db.rawQuery(selectQuery, null)
            if(cursor.moveToFirst()){
                do{
                    val longitude = cursor.getDouble(cursor.getColumnIndex(KEY_LONGITUDE))
                    val latitude = cursor.getDouble(cursor.getColumnIndex(KEY_LATITUDE))
                    val geoPoint = GeoPoint(latitude, longitude)
                    activityCoordinatesList.add(geoPoint)
                    Log.e("logGetCoord", geoPoint.toString())
                }while(cursor.moveToNext())
            }
            cursor.close()
        }catch (e: SQLiteException){
            db.execSQL(selectQuery)
            return ArrayList()
        }
        return activityCoordinatesList
    }

    @SuppressLint("Range")
    fun getLastActivityId(): Int{
        val db = this.readableDatabase
        val query = "SELECT * FROM $TABLE_ACTIVITIES ORDER BY $KEY_ID DESC LIMIT 1"
        val cursor = db.rawQuery(query, null)
        var lastId = -1L
        if (cursor.moveToFirst()) {
            lastId = cursor.getLong(cursor.getColumnIndex(KEY_ID))
        }
        cursor.close()
        db.close()
        return lastId.toInt()
    }

    @SuppressLint("Range")
    fun getLatestActivity(): Int {
        val db = this.readableDatabase
        try {
            val cursor = db.rawQuery(
                "SELECT $KEY_ID FROM $TABLE_ACTIVITIES WHERE $KEY_ID = (SELECT MAX($KEY_ID) FROM $TABLE_ACTIVITIES)",
                null
            )
            if (cursor.moveToFirst()) {
                // cursor is now pointing at the row with the highest ID
                val id = cursor.getInt(cursor.getColumnIndex(KEY_ID))
                return id
                // do something with the data
            }
            cursor.close()
            db.close()
        }
        catch (e: SQLiteException){
            return 0
        }


        return 100
    }
}
