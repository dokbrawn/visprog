package com.example.visprog.network

import android.util.Log
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.zeromq.SocketType
import org.zeromq.ZContext
import org.zeromq.ZMQ

class ZmqManager {
    private val context = ZContext()
    private val TAG = "ZmqManager"

    suspend fun sendString(address: String, message: String, type: SocketType = SocketType.PUSH): Boolean = withContext(Dispatchers.IO) {
        try {
            context.createSocket(type).use { socket ->
                socket.connect(address)
                socket.send(message.toByteArray(ZMQ.CHARSET), 0)
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error sending ZMQ: ${e.message}")
            false
        }
    }

    fun createPersistentSocket(type: SocketType, address: String, isBind: Boolean = false): ZMQ.Socket {
        val socket = context.createSocket(type)
        if (isBind) socket.bind(address) else socket.connect(address)
        return socket
    }

    fun destroy() {
        try {
            context.destroy()
        } catch (e: Exception) {
            Log.e(TAG, "Error destroying ZMQ context: ${e.message}")
        }
    }
}
