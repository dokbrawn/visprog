package com.example.sibgutiandroidvisp

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import androidx.appcompat.app.AppCompatActivity

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        findViewById<Button>(R.id.btn_calculator).setOnClickListener {
            goToActivity(Calculator::class.java)
        }
        /*findViewById<Button>(R.id.btn_player).setOnClickListener {
            goToActivity(PlayerActivity::class.java)
        }
        findViewById<Button>(R.id.btn_views).setOnClickListener {
            goToActivity(ViewsActivity::class.java)
        }*/
    }

    private fun goToActivity(activityClass: Class<*>) {
        val randomIntent = Intent(this, activityClass)
        startActivity(randomIntent)
    }
}