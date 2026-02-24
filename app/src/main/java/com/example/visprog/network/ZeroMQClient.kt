package com.example.visprog.network

import android.util.Log
import com.example.visprog.data.LocationData
import com.google.gson.Gson
import org.zeromq.ZMQ

class ZeroMQClient(private val address: String, private val port: Int) {

    private var context: ZMQ.Context? = null
    private var socket: ZMQ.Socket? = null
    private val TAG = "ZeroMQClient"

    fun start() {
        try {
            context = ZMQ.context(1)
            socket = context?.socket(ZMQ.REQ)

            // Disable TCP Keep-Alive to avoid hidden API restrictions
            socket?.setTCPKeepAlive(0)

            val connectionString = "tcp://$address:$port"
            Log.d(TAG, "Connecting to server at $connectionString")
            socket?.connect(connectionString)
        } catch (e: Exception) {
            Log.e(TAG, "Error during ZMQ start", e)
        }
    }

    fun sendLocationData(locationData: LocationData) {
        if (socket == null) {
            Log.e(TAG, "Socket is not initialized")
            return
        }

        val gson = Gson()
        val json = gson.toJson(locationData)

        try {
            Log.d(TAG, "Sending: $json")
            socket?.send(json.toByteArray(ZMQ.CHARSET))
            val reply = socket?.recv(0)
            Log.d(TAG, "Received reply: ${String(reply ?: byteArrayOf())}")
        } catch (e: Exception) {
            Log.e(TAG, "Error sending location data", e)
            reconnect()
        }
    }

    private fun reconnect() {
        Log.d(TAG, "Attempting to reconnect...")
        stop()
        start()
    }

    fun stop() {
        try {
            socket?.close()
            context?.term()
        } catch (e: Exception) {
            Log.e(TAG, "Error closing ZMQ", e)
        }
        socket = null
        context = null
        Log.d(TAG, "ZeroMQ client stopped")
    }
}
