package com.example.visprog.data

import org.json.JSONObject

data class LocationData(
    val latitude: Double,
    val longitude: Double,
    val altitude: Double,
    val accuracy: Float,
    val time: Long
)
{
    fun toJSONObject(): JSONObject
    {
        val data = JSONObject()
        data.put("latitude", latitude)
        data.put("longitude", longitude)
        data.put("altitude", altitude)
        data.put("accuracy", accuracy)
        data.put("time", time)

        return data
    }

    override fun toString(): String
    {
        return "Lat: $latitude\nLon: $longitude\nAlt: $altitude\nAcc: $accuracy\nTime: $time"
    }
}
