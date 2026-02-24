package com.example.visprog.supp

import android.util.Log
import com.example.visprog.data.LocationData
import com.google.gson.Gson
import java.io.File
import java.io.FileWriter

object FileUtils {

    private const val TAG = "FileUtils"

    fun saveLocationToFile(file: File, locationData: LocationData) {
        val gson = Gson()
        val json = gson.toJson(locationData)
        try {
            val writer = FileWriter(file, true)
            writer.append(json).append("\n")
            writer.close()
            Log.d(TAG, "Location data saved: $json")
        } catch (e: Exception) {
            Log.e(TAG, "Error saving location data to file", e)
        }
    }
}
