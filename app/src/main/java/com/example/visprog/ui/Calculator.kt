package com.example.visprog.ui

import android.os.Bundle
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.example.visprog.R
import com.google.android.material.button.MaterialButton

class Calculator : AppCompatActivity() {

    private lateinit var tvResult: TextView
    private var lastValue: Double = 0.0
    private var currentOperator: Char? = null
    private var isEnteringNewNumber = true

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_calculator)

        tvResult = findViewById(R.id.tvResult)

        val buttons = listOf(
            R.id.btn_0, R.id.btn_1, R.id.btn_2, R.id.btn_3, R.id.btn_4,
            R.id.btn_5, R.id.btn_6, R.id.btn_7, R.id.btn_8, R.id.btn_9
        )

        buttons.forEach { id ->
            findViewById<MaterialButton>(id).setOnClickListener {
                val digit = (it as MaterialButton).text.toString()
                appendDigit(digit)
            }
        }

        findViewById<MaterialButton>(R.id.btn_dot).setOnClickListener { appendDigit(".") }
        findViewById<MaterialButton>(R.id.btn_add).setOnClickListener { setOperator('+') }
        findViewById<MaterialButton>(R.id.btn_subtract).setOnClickListener { setOperator('-') }
        findViewById<MaterialButton>(R.id.btn_multiply).setOnClickListener { setOperator('*') }
        findViewById<MaterialButton>(R.id.btn_divide).setOnClickListener { setOperator('/') }
        findViewById<MaterialButton>(R.id.btn_equals).setOnClickListener { calculateResult() }
        findViewById<MaterialButton>(R.id.btn_clear).setOnClickListener {
            tvResult.text = "0"
            lastValue = 0.0
            currentOperator = null
            isEnteringNewNumber = true
        }
    }

    private fun appendDigit(digit: String) {
        if (isEnteringNewNumber) {
            tvResult.text = digit
            isEnteringNewNumber = false
        } else {
            if (digit == "." && tvResult.text.contains(".")) return
            tvResult.append(digit)
        }
    }

    private fun setOperator(op: Char) {
        lastValue = tvResult.text.toString().toDoubleOrNull() ?: 0.0
        currentOperator = op
        isEnteringNewNumber = true
    }

    private fun calculateResult() {
        val currentValue = tvResult.text.toString().toDoubleOrNull() ?: 0.0
        val result = when (currentOperator) {
            '+' -> lastValue + currentValue
            '-' -> lastValue - currentValue
            '*' -> lastValue * currentValue
            '/' -> if (currentValue != 0.0) lastValue / currentValue else Double.NaN
            else -> currentValue
        }
        tvResult.text = if (result.isNaN()) "Err" else result.toString()
        isEnteringNewNumber = true
        currentOperator = null
    }
}
