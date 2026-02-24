package com.example.visprog

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import androidx.appcompat.app.AppCompatActivity

class MainActivity : AppCompatActivity() {

    private lateinit var calculatorButton: Button
    private lateinit var playerButton: Button
    private lateinit var locationButton: Button
    private lateinit var socketsButton: Button
    private lateinit var telephonyButton: Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        calculatorButton = findViewById(R.id.btn_calculator)
        playerButton = findViewById(R.id.btn_player)
        locationButton = findViewById(R.id.btn_location)
        socketsButton = findViewById(R.id.btn_sockets)
        telephonyButton = findViewById(R.id.btn_telephony)

        calculatorButton.setOnClickListener {
            startActivity(Intent(this, Calculator::class.java))
        }

        playerButton.setOnClickListener {
            startActivity(Intent(this, MediaPlayerActivity::class.java))
        }

        locationButton.setOnClickListener {
            startActivity(Intent(this, LocationActivity::class.java))
        }

        socketsButton.setOnClickListener {
            startActivity(Intent(this, SocketsActivity::class.java))
        }

        telephonyButton.setOnClickListener {
            startActivity(Intent(this, TelephonyActivity::class.java))
        }
    }
}