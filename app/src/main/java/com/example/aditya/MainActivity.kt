package com.example.aditya

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlin.math.*

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            ScientificCalculatorApp()
        }
    }
}

@Composable
fun ScientificCalculatorApp() {
    var expression by remember { mutableStateOf("") }
    var result by remember { mutableStateOf("") }
    var isScientificMode by remember { mutableStateOf(false) }

    val onButtonClick: (String) -> Unit = { value ->
        when (value) {
            "=" -> {
                try {
                    result = evaluateExpression(expression).toString()
                } catch (e: Exception) {
                    result = "Error"
                }
            }
            "AC" -> {
                expression = ""
                result = ""
            }
            "DEL" -> {
                if (expression.isNotEmpty()) {
                    expression = expression.dropLast(1)
                }
            }
            "Sci" -> {
                isScientificMode = !isScientificMode
            }
            else -> {
                expression += value
            }
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF1E1E1E))
            .padding(16.dp),
        verticalArrangement = Arrangement.SpaceBetween
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 16.dp),
            horizontalAlignment = Alignment.End
        ) {
            Text(
                text = expression,
                fontSize = 24.sp,
                color = Color.White.copy(alpha = 0.7f),
                maxLines = 2,
                textAlign = TextAlign.End
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = result,
                fontSize = 36.sp,
                color = Color(0xFFFF9500),
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.End
            )
        }

        if (isScientificMode) {
            ScientificButtons(onButtonClick)
        }

        StandardButtons(onButtonClick, isScientificMode)
    }
}

@Composable
fun ScientificButtons(onButtonClick: (String) -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .verticalScroll(rememberScrollState())
    ) {
        val buttons = listOf(
            listOf("sin", "cos", "tan", "π"),
            listOf("log", "ln", "√", "^"),
            listOf("%", "(", ")")
        )
        buttons.forEach { row ->
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 4.dp),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                row.forEach { label ->
                    CalculatorButton(text = label, isFunction = true) {
                        onButtonClick(label)
                    }
                }
            }
        }
    }
}

@Composable
fun StandardButtons(onButtonClick: (String) -> Unit, isScientificMode: Boolean) {
    Column(
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 4.dp),
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            CalculatorButton("Sci", isFunction = true) { onButtonClick("Sci") }
            CalculatorButton("AC", isFunction = true) { onButtonClick("AC") }
            CalculatorButton("DEL", isFunction = true) { onButtonClick("DEL") }
            if (!isScientificMode) {
                CalculatorButton("(", isFunction = true) { onButtonClick("(") }
                CalculatorButton(")", isFunction = true) { onButtonClick(")") }
            }
        }

        val buttons = listOf(
            listOf("7", "8", "9", "/"),
            listOf("4", "5", "6", "*"),
            listOf("1", "2", "3", "-"),
            listOf("0", ".", "=", "+")
        )
        buttons.forEach { row ->
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 4.dp),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                row.forEach { label ->
                    CalculatorButton(label) { onButtonClick(label) }
                }
            }
        }
    }
}

@Composable
fun CalculatorButton(text: String, isFunction: Boolean = false, onClick: () -> Unit) {
    Box(
        contentAlignment = Alignment.Center,
        modifier = Modifier
            .size(if (isFunction) 70.dp else 80.dp)
            .background(
                color = when {
                    text in listOf("AC", "DEL", "Sci") -> Color.DarkGray
                    text in listOf("+", "-", "*", "/", "=") -> Color(0xFFFF9500)
                    isFunction -> Color(0xFF444444)
                    else -> Color(0xFF333333)
                },
                shape = MaterialTheme.shapes.medium
            )
            .clickable { onClick() }
    ) {
        Text(
            text = text,
            fontSize = if (isFunction) 18.sp else 24.sp,
            color = Color.White,
            fontWeight = if (isFunction) FontWeight.Normal else FontWeight.SemiBold
        )
    }
}

fun evaluateExpression(expr: String): Double {
    if (expr.isEmpty()) return 0.0

    var processedExpr = expr.replace("π", PI.toString())
        .replace("e", E.toString())

    processedExpr = processedExpr.replace("√", "sqrt")

    val functionRegex = "(sin|cos|tan|log|ln|sqrt|%)".toRegex()
    processedExpr = functionRegex.replace(processedExpr) { " ${it.value} " }

    val tokens = tokenize(processedExpr)
    val rpn = toRPN(tokens)
    return evaluateRPN(rpn)
}

fun tokenize(expr: String): List<String> {
    val tokens = mutableListOf<String>()
    var number = ""
    var functionName = ""
    for (char in expr) {
        when {
            char.isDigit() || char == '.' -> number += char
            char.isLetter() -> functionName += char
            else -> {
                if (number.isNotEmpty()) {
                    tokens.add(number)
                    number = ""
                }
                if (functionName.isNotEmpty()) {
                    tokens.add(functionName)
                    functionName = ""
                }
                if (char != ' ') tokens.add(char.toString())
            }
        }
    }
    if (number.isNotEmpty()) tokens.add(number)
    if (functionName.isNotEmpty()) tokens.add(functionName)
    return tokens
}

fun toRPN(tokens: List<String>): List<String> {
    val output = mutableListOf<String>()
    val stack = ArrayDeque<String>()
    val precedence = mapOf(
        "=" to 0,
        "+" to 1,
        "-" to 1,
        "*" to 2,
        "/" to 2,
        "^" to 3
    )
    val functions = setOf("sin", "cos", "tan", "log", "ln", "sqrt", "%")

    for (token in tokens) {
        when {
            token.toDoubleOrNull() != null -> output.add(token)
            token in functions -> stack.addLast(token)
            token == "(" -> stack.addLast(token)
            token == ")" -> {
                while (stack.isNotEmpty() && stack.last() != "(") {
                    output.add(stack.removeLast())
                }
                if (stack.isNotEmpty()) stack.removeLast()
            }
            token in precedence -> {
                while (stack.isNotEmpty() && stack.last() in precedence && precedence[stack.last()]!! >= precedence[token]!!) {
                    output.add(stack.removeLast())
                }
                stack.addLast(token)
            }
        }
    }
    while (stack.isNotEmpty()) output.add(stack.removeLast())
    return output
}

fun evaluateRPN(tokens: List<String>): Double {
    val stack = ArrayDeque<Double>()
    for (token in tokens) {
        when {
            token.toDoubleOrNull() != null -> stack.addLast(token.toDouble())
            token in setOf("+", "-", "*", "/", "^") -> {
                val b = stack.removeLast()
                val a = stack.removeLast()
                val res = when (token) {
                    "+" -> a + b
                    "-" -> a - b
                    "*" -> a * b
                    "/" -> a / b
                    "^" -> a.pow(b)
                    else -> 0.0
                }
                stack.addLast(res)
            }
            token in setOf("sin", "cos", "tan", "log", "ln", "sqrt", "%") -> {
                val a = stack.removeLast()
                val res = when (token) {
                    "sin" -> sin(Math.toRadians(a))
                    "cos" -> cos(Math.toRadians(a))
                    "tan" -> tan(Math.toRadians(a))
                    "log" -> log10(a)
                    "ln" -> ln(a)
                    "sqrt" -> sqrt(a)
                    "%" -> a / 100
                    else -> 0.0
                }
                stack.addLast(res)
            }
        }
    }
    return stack.lastOrNull() ?: throw IllegalArgumentException("Invalid expression")
}