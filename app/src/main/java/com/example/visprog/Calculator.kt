 package com.example.visprog
 
 import android.os.Bundle
 import android.view.View
 import android.widget.Button
 import android.widget.TextView
 import androidx.appcompat.app.AppCompatActivity
 
 class Calculator : AppCompatActivity() {
     private lateinit var tvResult: TextView
     private var lastNumeric: Boolean = false
     private var isOperatorAdded: Boolean = false
 
     override fun onCreate(savedInstanceState: Bundle?) {
         super.onCreate(savedInstanceState)
         setContentView(R.layout.activity_calculator)
 
         tvResult = findViewById(R.id.tvResult)
         setupButtons()
     }
 
     private fun setupButtons() {
         val numberAndDotButtons = listOf<Int>(
             R.id.btn_0, R.id.btn_1, R.id.btn_2, R.id.btn_3, R.id.btn_4,
             R.id.btn_5, R.id.btn_6, R.id.btn_7, R.id.btn_8, R.id.btn_9, R.id.btn_dot
         )
 
         val operatorButtons = listOf<Int>(
             R.id.btn_add, R.id.btn_subtract, R.id.btn_multiply, R.id.btn_divide
         )
 
         numberAndDotButtons.forEach { id ->
             findViewById<Button>(id).setOnClickListener { onDigit(it) }
         }
 
         operatorButtons.forEach { id ->
             findViewById<Button>(id).setOnClickListener { onOperator(it) }
         }
 
         findViewById<Button>(R.id.btn_clear).setOnClickListener { onClear() }
         findViewById<Button>(R.id.btn_equals).setOnClickListener { onEqual() }
     }
 
 
     fun onDigit(view: View) {
         val buttonText = (view as Button).text
 
         if (tvResult.text.toString() == "0" && buttonText != ".") {
             tvResult.text = buttonText
         } else if (buttonText == ".") {
             if (!tvResult.text.contains('.')) {
                 tvResult.append(buttonText)
             }
         }
         else {
             tvResult.append(buttonText)
         }
         lastNumeric = true
     }
 
     fun onOperator(view: View) {
         val operator = (view as Button).text
 
         if (lastNumeric && !isOperatorAdded) {
             tvResult.append(operator)
             isOperatorAdded = true
             lastNumeric = false
         }
     }
 
     fun onClear() {
         tvResult.text = "0"
         lastNumeric = false
         isOperatorAdded = false
     }
 
     fun onEqual() {
         if (!lastNumeric || !isOperatorAdded) {
             return
         }
 
         val expression = tvResult.text.toString()
         var num1String = ""
         var num2String = ""
         var operator: Char? = null
 
         for (char in expression) {
             if (char.isDigit() || char == '.') {
                 if (operator == null) {
                     num1String += char
                 } else {
                     num2String += char
                 }
             } else if (char == '+' || char == '-' || char == '*' || char == '/') {
                 if (char == '-' && num1String.isEmpty() && operator == null) {
                     num1String += char
                 } else if (operator == null) {
                     operator = char
                 } else {
                     tvResult.text = "Input Error"
                     onClear()
                     return
                 }
             }
         }
 
         if (operator == null || num1String.isEmpty() || num2String.isEmpty()) {
             tvResult.text = "Input Error"
             onClear()
             return
         }
         
         if (num1String.length > 1 && num1String.startsWith('-') && num1String.replace("-", "").isEmpty()) {
             tvResult.text = "Input Error"
             onClear()
             return
         }

         val num1 = num1String.toDoubleOrNull()
         val num2 = num2String.toDoubleOrNull()
         var result = 0.0
         
         if (num1 == null || num2 == null) {
             tvResult.text = "Input Error"
             onClear()
             return
         }

         if (operator == '+') {
             result = num1 + num2
         } else if (operator == '-') {
             result = num1 - num2
         } else if (operator == '*') {
             result = num1 * num2
         } else if (operator == '/') {
             if (num2 == 0.0) {
                 tvResult.text = "Error: Div by zero"
                 onClear()
                 return
             }
             result = num1 / num2
         }
 
         tvResult.text = if (result % 1.0 == 0.0) {
            result.toLong().toString()
        } else {
            result.toString()
        }

        isOperatorAdded = false
        lastNumeric = true
    }
}