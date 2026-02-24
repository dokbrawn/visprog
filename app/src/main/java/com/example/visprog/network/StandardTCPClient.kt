package com.example.visprog.network

import android.util.Log
import com.example.visprog.data.LocationData
import com.google.gson.Gson
import java.io.BufferedReader
import java.io.InputStreamReader
import java.io.OutputStreamWriter
import java.io.PrintWriter
import java.net.Socket

class StandardTCPClient(private val address: String, private val port: Int) {

    private val TAG = "StandardTCPClient"

    fun sendLocationData(locationData: LocationData) {
        try {
            Socket(address, port).use { socket ->
                Log.d(TAG, "Connected to server at $address:$port")

                // Setup streams
                val writer = PrintWriter(OutputStreamWriter(socket.outputStream, "UTF-8"), true)
                val reader = BufferedReader(InputStreamReader(socket.inputStream, "UTF-8"))

                // Convert data to JSON and send
                val gson = Gson()
                val json = gson.toJson(locationData)
                Log.d(TAG, "Sending: $json")
                writer.println(json) // Send data with a newline terminator

                // Wait for a reply from the server
                val reply = reader.readLine()
                Log.d(TAG, "Received reply: $reply")
            }
        } catch (e: Exception) {
            // Log any connection or IO errors
            Log.e(TAG, "Error communicating with server", e)
        }
    }
}
