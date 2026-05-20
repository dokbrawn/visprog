package com.example.visprog.network

import android.content.Context
import com.example.visprog.data.MobileNetworkDataList
import com.example.visprog.data.LocationData
import okhttp3.*
import org.json.JSONObject
import java.util.concurrent.TimeUnit

class WebSocketClient(private val context: Context) {
    private var webSocket: WebSocket? = null
    private var isConnected = false

    private val client = OkHttpClient.Builder()
        .pingInterval(30, TimeUnit.SECONDS)
        .build()

    private val apiClient = ApiClient(context)
    private var listener: ConnectionListener? = null

    interface ConnectionListener {
        fun onConnected()
        fun onDisconnected()
        fun onError(message: String)
        fun onMessage(message: String)
    }

    fun setListener(listener: ConnectionListener) {
        this.listener = listener
    }

    fun connect() {
        val serverAddress = apiClient.getServerAddress() ?: run {
            listener?.onError("config_error")
            return
        }

        val url = "ws://$serverAddress/api/mobile-network/ws"
        val request = Request.Builder().url(url).build()

        webSocket = client.newWebSocket(request, object : WebSocketListener() {
            override fun onOpen(webSocket: WebSocket, response: Response) {
                isConnected = true
                listener?.onConnected()
            }

            override fun onMessage(webSocket: WebSocket, text: String) {
                listener?.onMessage(text)
            }

            override fun onClosed(webSocket: WebSocket, code: Int, reason: String) {
                isConnected = false
                listener?.onDisconnected()
            }

            override fun onFailure(webSocket: WebSocket, t: Throwable, response: Response?) {
                isConnected = false
                listener?.onError(t.message ?: "")
            }
        })
    }

    fun disconnect() {
        webSocket?.close(1000, "close")
        webSocket = null
        isConnected = false
    }

    fun isConnected(): Boolean = isConnected

    fun sendData(mobileNetworkDataList: MobileNetworkDataList, locationData: LocationData): Boolean {
        if (!isConnected) return false

        val jsonData = JSONObject()
        jsonData.put("mobile_network_data_list", mobileNetworkDataList.toJSONObject())
        jsonData.put("location_data", locationData.toJSONObject())

        return webSocket?.send(jsonData.toString()) ?: false
    }
}
