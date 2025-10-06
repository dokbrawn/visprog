package com.example.simplecalculator

import android.os.Bundle
import android.widget.Button
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity

class MainActivity : AppCompatActivity() {

    private lateinit var tvDisplay: TextView
    private val operators = listOf('+', '-', '*', '/')

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        tvDisplay = findViewById(R.id.tvDisplay)

        val digits = listOf(
            R.id.btn0, R.id.btn1, R.id.btn2, R.id.btn3, R.id.btn4,
            R.id.btn5, R.id.btn6, R.id.btn7, R.id.btn8, R.id.btn9
        )
        for (id in digits) {
            findViewById<Button>(id).setOnClickListener { digitClicked((it as Button).text.toString()) }
        }

        findViewById<Button>(R.id.btnDot).setOnClickListener { dotClicked() }
        findViewById<Button>(R.id.btnAdd).setOnClickListener { operatorClicked("+") }
        findViewById<Button>(R.id.btnSub).setOnClickListener { operatorClicked("-") }
        findViewById<Button>(R.id.btnMul).setOnClickListener { operatorClicked("*") }
        findViewById<Button>(R.id.btnDiv).setOnClickListener { operatorClicked("/") }
        findViewById<Button>(R.id.btnClear).setOnClickListener { clearDisplay() }
        findViewById<Button>(R.id.btnEq).setOnClickListener { equalsClicked() }
    }

    private fun digitClicked(d: String) {
        val cur = tvDisplay.text.toString()
        tvDisplay.text = if (cur == "0") d else cur + d
    }

    private fun dotClicked() {
        val cur = tvDisplay.text.toString()
        val opIndex = findOperatorIndex(cur)
        val part = if (opIndex >= 0) cur.substring(opIndex + 1) else cur
        if (!part.contains('.')) tvDisplay.text = cur + "."
    }

    private fun operatorClicked(op: String) {
        val cur = tvDisplay.text.toString()
        if (findOperatorIndex(cur) >= 0) return
        tvDisplay.text = if (cur == "0") "0" else cur + op
    }

    private fun clearDisplay() { tvDisplay.text = "0" }

    private fun equalsClicked() {
        val expr = tvDisplay.text.toString()
        val idx = findOperatorIndex(expr)
        if (idx < 0) return
        val op = expr[idx]
 
       val left = expr.substring(0, idx).toDoubleOrNull()
        val right = expr.substring(idx + 1).toDoubleOrNull()
        if (left == null || right == null) { tvDisplay.text = "Ошибка"; return }
        val result = when (op) {
 



           '+' -> left + right
            '-' -> left - right
            '*' -> left * right
            '/' -> if (right == 0.0) { tvDisplay.text = "Дел/0"; return } else left / right
 



           else -> return
        }
        tvDisplay.text = if (result % 1.0 == 0.0) result.toLong().toString() else result.toString()
 


   }

    private fun findOperatorIndex(s: String): Int {
        for (i in s.indices) if (operators.contains(s[i])) return i
 



       return -1
    }
}
