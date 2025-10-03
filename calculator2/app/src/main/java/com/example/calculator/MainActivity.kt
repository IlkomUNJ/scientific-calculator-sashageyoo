package com.example.calculator

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.ElevatedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.calculator.ui.theme.CalculatorTheme
import androidx.compose.runtime.mutableStateOf
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonColors
import androidx.compose.material3.ButtonDefaults
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.sp
import java.text.DecimalFormat
import net.objecthunter.exp4j.ExpressionBuilder
import net.objecthunter.exp4j.function.Function
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.rememberScrollState
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.systemBars
import androidx.compose.foundation.layout.systemBarsPadding
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.systemBarsPadding

val HotPink = Color(0xFFF564A9)
val LightPink = Color(0xFFFAA4BD)
val LightYellow = Color(0xFFFAE3C6)
val LightOrange = Color(0xFFFFC29B)
val White = Color.White
val Black = Color(0xFF533B4D)


class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        WindowCompat.setDecorFitsSystemWindows(window, false)

        setContent {
            CalculatorTheme {
                CalcApp()
            }
        }
    }
}

@Composable
fun CalcApp() {
    CalculatorApp()
}

@Composable
fun Navbar(
    title: String,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(HotPink)
            .padding(13.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = title,
            fontSize = 24.sp,
            fontWeight = FontWeight.Bold,
            color = White
        )
    }
}

val factorial = object : Function("fact", 1) {
    override fun apply(vararg args: Double): Double {
        val n = args[0].toInt()
        if (n < 0 || n != args[0].toInt()) {
            throw IllegalArgumentException("Argument for factorial must be a non-negative integer")
        }
        var result = 1.0
        for (i in 2..n) {
            result *= i
        }
        return result
    }
}

val naturalLog = object : Function("ln", 1) {
    override fun apply(vararg args: Double): Double {
        val x = args[0]
        if (x <= 0) {
            throw IllegalArgumentException("Argument for ln must be positive")
        }
        return kotlin.math.ln(x) // Gunakan kotlin.math.ln untuk natural log
    }
}

// Tambahan: Fungsi custom untuk log (base 10)
val commonLog = object : Function("log", 1) {
    override fun apply(vararg args: Double): Double {
        val x = args[0]
        if (x <= 0) {
            throw IllegalArgumentException("Argument for log must be positive")
        }
        return kotlin.math.log10(x)
    }
}

@Composable
fun CalculatorButton(
    text: String,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    val (backgroundColor, textColor) = when (text) {
        "C", "CE", "⌫", "%" -> LightOrange to Black
        "÷", "×", "-", "+", "=" -> HotPink to White
        "inv" -> if (text == "inv") HotPink to White else LightOrange to Black
        else -> LightYellow to Black
    }

    val fontSize = when {
        text == "1/x" -> 20.sp
        text in listOf("inv","sin","cos","tan","ln","log","x!","xʸ","(",")","π", "sin ⁻¹", "cos⁻¹", "tan⁻¹") -> 24.sp
        else -> 33.sp
    }

    ElevatedButton(
        onClick = onClick,
        modifier = modifier.fillMaxSize(),
        shape = RoundedCornerShape(8.dp),
        colors = ButtonDefaults.elevatedButtonColors(
            containerColor = backgroundColor,
            contentColor = textColor
        ),
        elevation = ButtonDefaults.elevatedButtonElevation(
            defaultElevation = 4.dp,
            pressedElevation = 2.dp
        ),
        contentPadding = PaddingValues(4.dp)
    ) {
        Text(text = text, fontSize = fontSize, fontWeight = FontWeight.Medium)
    }
}

@Composable
fun CalculatorApp(
    modifier: Modifier = Modifier
) {
    var display by rememberSaveable { mutableStateOf("0") }
    var expression by rememberSaveable { mutableStateOf("") }
    var isInverse by rememberSaveable { mutableStateOf(false) }

    fun evaluateExpression(exp: String): String {
        return try {
            val convertedExp = exp
                .replace("×", "*")
                .replace("÷", "/")
                .replace("√", "sqrt")
                .replace("%", "*0.01")
                .replace("π", Math.PI.toString())
                .replace(Regex("(\\d+)!")) { "fact(${it.groupValues[1]})" }

            val expressionBuilder = ExpressionBuilder(convertedExp)
                .function(factorial)
                .function(naturalLog)
                .function(commonLog)
                .build()

            val result = expressionBuilder.evaluate()

            val df = DecimalFormat("#.#######")
            df.format(result)
        } catch (e: Exception) {
            "Error"
        }

    }

    val onButtonClick: (String) -> Unit = { buttonText ->
        var currentText = buttonText
        if (isInverse) {
            currentText = when (buttonText) {
                "sin" -> "asin"
                "cos" -> "acos"
                "tan" -> "atan"
                else -> buttonText
            }
        }

        when (currentText) {
            "C" -> {
                expression = ""
                display = "0"
            }

            "CE" -> {
                expression = ""
                display = "0"
            }

            "⌫" -> {
                if (expression.isNotEmpty()) {
                    expression = expression.dropLast(1)
                    display = if (expression.isEmpty()) "0" else expression
                }
            }

            "=" -> {
                if (expression.isNotEmpty()) {
                    val result = evaluateExpression(expression)
                    display = result
                    expression = if (result != "Error") result else ""
                }
            }

            "inv" -> {
                isInverse = !isInverse
            }

            "x!" -> {
                expression += "!"
                display = expression
            }

            "xʸ" -> {
                expression += "^"
                display = expression

            }

            "1/x" -> {
                if (expression == "0" || expression == "Error") {
                    expression = "1/"
                } else {
                    expression = "1/($expression)"
                }
                display = expression
            }

            "sin", "cos", "tan", "asin", "acos", "atan", "log", "ln", "√" -> {
                if (expression == "0" || expression == "Error") {
                    expression = "$currentText("
                } else {
                    expression += "$currentText("
                }
                display = expression
            }

            else -> {
                if (display == "0" || expression == "Error") {
                    expression = currentText
                } else {
                    expression += currentText
                }
                display = expression
            }
        }
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(color = LightPink)
            .systemBarsPadding()
    ) {
        Column(
            modifier = Modifier.fillMaxSize()
        ) {
            Navbar(
                title = "Calculator",
                modifier = Modifier
            )
            Text(
                text = display,
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f) // Give weight to push the buttons down
                    .padding(top = 16.dp, bottom = 16.dp, start = 8.dp, end = 8.dp)
                    .horizontalScroll(rememberScrollState()),
                fontSize = if (display.length > 9) 55.sp else 89.sp,
                fontWeight = FontWeight.Light,
                color = Black,
                textAlign = TextAlign.End,
                maxLines = 1
            )

            // The rest of your Column content for buttons
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 8.dp),
                verticalArrangement = Arrangement.spacedBy(13.dp),
            ) {
                val aclabel = if (display == "0") "C" else "CE"
                val buttonRows = listOf(
                    listOf("inv", "sin", "cos", "tan","ln", "log"),
                    listOf("√","1/x", "xʸ", "x!", "(", ")", "π"),
                    listOf(aclabel, "⌫", "%", "÷"),
                    listOf("7", "8", "9", "×"),
                    listOf("4", "5", "6", "-"),
                    listOf("1", "2", "3", "+"),
                    listOf("0", ".", "=")
                )

                buttonRows.forEach { rowItems ->
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(13.dp)
                    ) {
                        rowItems.forEach { buttonText ->
                            val weight = if (buttonText == "0") 2.1f else 1f
                            val modifier = Modifier.weight(weight).aspectRatio(if(buttonText == "0") 2f else 1f)

                            val label = when {
                                buttonText == "inv" && isInverse -> "inv"
                                buttonText == "sin" -> if (isInverse) "sin ⁻¹" else "sin"
                                buttonText == "cos" -> if (isInverse) "cos⁻¹" else "cos"
                                buttonText == "tan" -> if (isInverse) "tan⁻¹" else "tan"
                                else -> buttonText
                            }

                            CalculatorButton(
                                text = label,
                                modifier = modifier,
                                onClick = { onButtonClick(buttonText) }
                            )
                        }
                    }
                }
            }
        }
    }
}


