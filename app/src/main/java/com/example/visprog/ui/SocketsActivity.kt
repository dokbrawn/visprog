package com.example.visprog.ui

import android.os.Bundle
import android.util.Log
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.example.visprog.databinding.ActivitySocketsBinding
import kotlinx.coroutines.*
import org.zeromq.SocketType
import org.zeromq.ZContext
import org.zeromq.ZMQ

class SocketsActivity : AppCompatActivity() {
    private lateinit var binding: ActivitySocketsBinding
    private val ctx = ZContext()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        binding = ActivitySocketsBinding.inflate(layoutInflater)
        setContentView(binding.root)
        binding.buttonStartSocket.setOnClickListener { start(binding.editTextSocketMessage.text.toString().trim()) }
    }

    private fun start(ip: String) {
        binding.tvSockets.text = "Starting..."
        lifecycleScope.launch(Dispatchers.IO) {
            try {
                ctx.createSocket(SocketType.REP).use { s ->
                    s.bind("tcp://*:2222")
                    while (isActive) {
                        s.recvStr(ZMQ.DONTWAIT)?.let { msg ->
                            withContext(Dispatchers.Main) { binding.tvSockets.append("\n[S] $msg") }
                            s.send("ACK")
                        }
                        delay(100)
                    }
                }
            } catch (e: Exception) { Log.e("ZMQ", "S: ${e.message}") }
        }
        lifecycleScope.launch(Dispatchers.IO) {
            delay(500)
            try {
                ctx.createSocket(SocketType.REQ).use { s ->
                    s.connect(if (ip.isEmpty()) "tcp://localhost:2222" else "tcp://$ip:5555")
                    s.receiveTimeOut = 2000
                    repeat(5) { i ->
                        s.send("Hello #$i")
                        val r = s.recvStr()
                        withContext(Dispatchers.Main) { binding.tvSockets.append("\n[C] $r") }
                        delay(1000)
                    }
                }
            } catch (e: Exception) { Log.e("ZMQ", "C: ${e.message}") }
        }
    }

    override fun onDestroy() { ctx.destroy(); super.onDestroy() }
}
