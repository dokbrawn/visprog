@file:Suppress("DEPRECATION")

package com.example.visprog.ui

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.Bundle
import android.widget.Button
import android.widget.TextView
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import com.example.visprog.R
import com.example.visprog.services.BackgroundService

class ServiceActivity : AppCompatActivity() {

    private lateinit var bStart: Button
    private lateinit var bStop: Button
    private lateinit var tvTextFromBg: TextView

    private val mMessageReceiver: BroadcastReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent) {
            val message = intent.getStringExtra("Status")
            tvTextFromBg.text = message
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_service)
        
        val rootView = findViewById<android.view.ViewGroup>(android.R.id.content).getChildAt(0)
        ViewCompat.setOnApplyWindowInsetsListener(rootView) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        LocalBroadcastManager.getInstance(applicationContext).registerReceiver(
            mMessageReceiver, IntentFilter("BackGroundUpdate")
        )

        bStart = findViewById(R.id.bStartBg)
        bStop = findViewById(R.id.bStopBg)
        tvTextFromBg = findViewById(R.id.tvDataFromService)
    }

    override fun onResume() {
        super.onResume()

        bStart.setOnClickListener {
            val startBgIntent = Intent(this, BackgroundService::class.java)
            startService(startBgIntent)
        }

        bStop.setOnClickListener {
            val stopBgIntent = Intent(this, BackgroundService::class.java)
            stopService(stopBgIntent)
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        LocalBroadcastManager.getInstance(applicationContext).unregisterReceiver(mMessageReceiver)
    }
}
