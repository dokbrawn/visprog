package com.example.visprog.ui

import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.util.TypedValue
import android.view.View
import android.widget.Button
import android.widget.SeekBar
import android.widget.SeekBar.OnSeekBarChangeListener
import android.widget.TextView
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.example.visprog.R

class ViewExamples : AppCompatActivity(), View.OnClickListener {

    private lateinit var bBackToMain: Button
    private lateinit var tvView_01: TextView
    private lateinit var bExample: Button
    private lateinit var bExample2: Button
    private lateinit var bExample3: Button
    private lateinit var sbMySeekBar: SeekBar

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_view_examples)
        
        val rootView = findViewById<android.view.ViewGroup>(android.R.id.content).getChildAt(0)
        ViewCompat.setOnApplyWindowInsetsListener(rootView) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        bBackToMain = findViewById(R.id.button_back_to_main)
        tvView_01 = findViewById(R.id.textView)
        bExample = findViewById(R.id.button)
        bExample2 = findViewById(R.id.button2)
        bExample3 = findViewById(R.id.button3)
        sbMySeekBar = findViewById(R.id.seekBarTest)

        sbMySeekBar.setOnSeekBarChangeListener(object : OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar, progress: Int, fromUser: Boolean) {
                tvView_01.text = progress.toString()
            }
            override fun onStartTrackingTouch(seekBar: SeekBar) {}
            override fun onStopTrackingTouch(seekBar: SeekBar) {}
        })

        tvView_01.setBackgroundColor(Color.rgb(255, 22, 10))
    }

    override fun onResume() {
        super.onResume()

        bBackToMain.setOnClickListener {
            val backToMain = Intent(this, MainActivity::class.java)
            startActivity(backToMain)
        }

        textViewExamples()
        buttonExamples()
    }

    private fun textViewExamples(){
        tvView_01.text = "Hello from Code"
        tvView_01.setTextSize(TypedValue.COMPLEX_UNIT_DIP, 30F)
        tvView_01.setTextColor(Color.rgb(255, 192, 0))
        tvView_01.setOnClickListener {
            tvView_01.setTextColor(Color.rgb(55, 12, 0))
        }
    }

    private fun buttonExamples(){
        bExample3.setOnClickListener(this)
        bExample2.setOnClickListener {
            bExample2.text = "2nd method"
        }
        bExample.setOnClickListener {
            bExample.text = "1st method"
        }
    }

    override fun onClick(v: View?){
        if (v?.id == R.id.button3) {
            bExample3.text = "3rd method"
        }
    }
}
