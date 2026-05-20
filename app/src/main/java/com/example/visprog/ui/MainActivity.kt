package com.example.visprog.ui

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Button
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.example.visprog.R

class MainActivity : AppCompatActivity() {

    private val logTag = "MainActivity"
    private lateinit var bGoToPlayerActivity: Button
    private lateinit var bLocationExample: Button
    private lateinit var bTelephonyExample: Button
    private lateinit var bSocketsExample: Button
    private lateinit var bCalculator: Button

    override fun onCreate(savedInstanceState: Bundle?) {
        Log.d(logTag, "вызывается onCreate method")
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_main)
        
        val rootView = findViewById<android.view.ViewGroup>(android.R.id.content).getChildAt(0)
        ViewCompat.setOnApplyWindowInsetsListener(rootView) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        bGoToPlayerActivity = findViewById(R.id.btn_player)
        bLocationExample = findViewById(R.id.btn_location)
        bTelephonyExample = findViewById(R.id.btn_telephony)
        bSocketsExample = findViewById(R.id.btn_sockets)
        bCalculator = findViewById(R.id.btn_calculator)
    }

    override fun onResume() {
        super.onResume()
        Log.d(logTag, "вызывается onResume method")

        bGoToPlayerActivity.setOnClickListener {
            startActivity(Intent(this, MediaPlayerActivity::class.java))
        }

        bLocationExample.setOnClickListener {
            startActivity(Intent(this, LocationActivity::class.java))
        }

        bTelephonyExample.setOnClickListener {
            startActivity(Intent(this, TelephonyActivity::class.java))
        }

        bSocketsExample.setOnClickListener {
            startActivity(Intent(this, SocketsActivity::class.java))
        }

        bCalculator.setOnClickListener {
            startActivity(Intent(this, Calculator::class.java))
        }
    }

    override fun onStart() {
        super.onStart()
        Log.d(logTag, "вызывается onStart method")
    }

    override fun onPause() {
        super.onPause()
        Log.d(logTag, "вызывается onPause method")
    }

    override fun onStop() {
        super.onStop()
        Log.d(logTag, "вызывается onStop method")
    }

    override fun onDestroy() {
        super.onDestroy()
        Log.d(logTag, "вызывается onDestroy method")
    }
}
