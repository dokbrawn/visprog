package com.example.visprog

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity

class MainActivity : AppCompatActivity() {

    private lateinit var calculatorButton: Button
    private lateinit var playerButton: Button
    private lateinit var locationButton: Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        calculatorButton = findViewById(R.id.btn_calculator)
        playerButton = findViewById(R.id.btn_player)
        locationButton = findViewById(R.id.btn_location)

        calculatorButton.setOnClickListener {
            startActivity(Intent(this, Calculator::class.java))
        }

        playerButton.setOnClickListener {
            startActivity(Intent(this, MediaPlayerActivity::class.java))
        }

        locationButton.setOnClickListener {
            Toast.makeText(this, "Opening Location...", Toast.LENGTH_SHORT).show()
            startActivity(Intent(this, LocationActivity::class.java))
        }
    }
}