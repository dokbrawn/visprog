package com.example.calculator

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Button
import android.widget.TextView

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val text = findViewById<TextView>(R.id.textView)
        val buttons = listOf(
            R.id.btn0, R.id.btn1, R.id.btn2, R.id.btn3, R.id.btn4,
            R.id.btn5, R.id.btn6, R.id.btn7, R.id.btn8, R.id.btn9,
            R.id.btnPlus, R.id.btnMinus, R.id.btnMul, R.id.btnDiv, R.id.btnEq, R.id.btnClear
        )

        for (id in buttons) {
            findViewById<Button>(id).setOnClickListener {
                val b = it as Button
                if (b.text == "=") {

                    val s = text.text.toString()
                    var op = ""
                    var a = ""
                    var bnum = ""
                    for (c in s) {
                        if (c == '+' || c == '-' || c == '*' || c == '/') {
                            op = c.toString()
                        } else {
                            if (op == "") a += c else bnum += c
                        }
                    }
                    if (a.isNotEmpty() && bnum.isNotEmpty()) {
                        val x = a.toDouble()
                        val y = bnum.toDouble()
                        val r = when (op) {
                            "+" -> x + y
                            "-" -> x - y
                            "*" -> x * y
                            "/" -> if (y != 0.0) x / y else 0.0
                            else -> 0.0
                        }
                        text.text = r.toString()
                    }

                } else if (b.text == "C") {
                    text.text = ""
                } else {
                    text.append(b.text)
                }
            }
        }
    }
}
