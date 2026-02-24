package com.example.visprog

import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import org.zeromq.SocketType
import org.zeromq.ZContext
import org.zeromq.ZMQ
import kotlin.concurrent.thread

class SocketsActivity : AppCompatActivity() {

    private val logTag = "ZMQ_SOCKETS"
    private val context = ZContext()

    private lateinit var statusTextView: TextView
    private lateinit var startButton: Button
    private lateinit var messageEditText: EditText
    private val uiHandler = Handler(Looper.getMainLooper())

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_sockets)

        statusTextView = findViewById(R.id.tvSockets)
        startButton = findViewById(R.id.buttonStartSocket)
        messageEditText = findViewById(R.id.editTextSocketMessage)

        startButton.setOnClickListener {
            val ipAddress = messageEditText.text.toString()
            if (ipAddress.isNotBlank()) {
                startAndroidClient(ipAddress)
                it.isEnabled = false
            } else {
                startInternalTest()
                it.isEnabled = false
                statusTextView.text = "Running internal client-server test..."
            }
        }
    }

    private fun startAndroidClient(serverIp: String) {
        thread {
            val clientSocket = context.createSocket(SocketType.REQ)
            // ИЗМЕНЕНИЕ: Подключаемся к порту 5556
            val serverAddress = "tcp://$serverIp:5556"
            uiHandler.post { statusTextView.text = "Connecting to $serverAddress" }
            clientSocket.connect(serverAddress)

            val message = "Hello from Android!"
            clientSocket.send(message.toByteArray(ZMQ.CHARSET), 0)
            Log.d(logTag, "[CLIENT] Sent: $message")

            val reply = clientSocket.recv(0)
            val replyText = String(reply, ZMQ.CHARSET)
            Log.d(logTag, "[CLIENT] Received: $replyText")

            uiHandler.post {
                statusTextView.append("\nServer replied: $replyText")
                startButton.isEnabled = true // Можно отправить снова
            }

            clientSocket.close()
        }
    }

    private fun startInternalTest() {
        thread { startInternalServer() }
        Thread.sleep(500)
        thread { startInternalClient() }
    }

    private fun startInternalServer() {
        val serverSocket = context.createSocket(SocketType.REP)
        serverSocket.bind("tcp://*:2222")

        while (!Thread.currentThread().isInterrupted) {
            val requestBytes = serverSocket.recv(0)
            val request = String(requestBytes, ZMQ.CHARSET)
            Log.d(logTag, "[INTERNAL SERVER] Received: $request")
            uiHandler.post { statusTextView.text = "Internal Server Received: $request" }

            Thread.sleep(1000)

            val response = "Hello from internal Android Server!"
            serverSocket.send(response.toByteArray(ZMQ.CHARSET), 0)
        }
        serverSocket.close()
    }

    private fun startInternalClient() {
        val clientSocket = context.createSocket(SocketType.REQ)
        clientSocket.connect("tcp://localhost:2222")
        val request = "Hello from internal Android client!"

        for (i in 1..3) {
            clientSocket.send(request.toByteArray(ZMQ.CHARSET), 0)
            Log.d(logTag, "[INTERNAL CLIENT] Sent: $request")

            val reply = clientSocket.recv(0)
            val replyText = String(reply, ZMQ.CHARSET)
            Log.d(logTag, "[INTERNAL CLIENT] Received: $replyText")
            uiHandler.post { statusTextView.append("\nInternal client got reply.") }
            Thread.sleep(500)
        }
        clientSocket.close()
        uiHandler.post { startButton.isEnabled = true }
    }

    override fun onDestroy() {
        super.onDestroy()
        context.destroy()
    }
}
