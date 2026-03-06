package com.example.app.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.github.skydoves.colorpicker.compose.*

@Composable
fun ColorPickerScreen(modifier: Modifier = Modifier) {
    val controller = rememberColorPickerController()
    var selectedColor by remember { mutableStateOf(Color.White) }
    var hexInput by remember { mutableStateOf(colorToHex(Color.White)) }

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        ColorPreview(selectedColor)
        Spacer(modifier = Modifier.height(24.dp))
        ColorControls(
            controller = controller,
            onColorChanged = { color ->
                selectedColor = color
                val newHex = colorToHex(color)
                if (hexInput != newHex) hexInput = newHex
            }
        )
        Spacer(modifier = Modifier.height(24.dp))
        HexInputField(
            hexValue = hexInput,
            onValueChange = { newHex ->
                hexInput = newHex
                if (newHex.length == 6) {
                    try {
                        val parsedColor = Color(android.graphics.Color.parseColor("#$newHex"))
                        selectedColor = parsedColor
                        controller.selectByColor(parsedColor, false)
                    } catch (e: Exception) { }
                }
            }
        )
    }
}

@Composable
fun ColorPreview(color: Color) {
    Box(
        modifier = Modifier
            .size(80.dp)
            .clip(RoundedCornerShape(12.dp))
            .background(color)
    )
}

@Composable
fun ColorControls(
    controller: ColorPickerController,
    onColorChanged: (Color) -> Unit
) {
    Column {
        HsvColorPicker(
            modifier = Modifier
                .fillMaxWidth()
                .height(300.dp),
            controller = controller,
            onColorChanged = { colorEnvelope ->
                onColorChanged(colorEnvelope.color)
            }
        )
        Spacer(modifier = Modifier.height(16.dp))
        BrightnessSlider(
            modifier = Modifier
                .fillMaxWidth()
                .height(35.dp)
                .clip(RoundedCornerShape(8.dp)),
            controller = controller
        )
    }
}

@Composable
fun HexInputField(
    hexValue: String,
    onValueChange: (String) -> Unit
) {
    OutlinedTextField(
        value = hexValue,
        onValueChange = { newValue ->
            val filtered = newValue.uppercase().filter { it in "0123456789ABCDEF" }.take(6)
            onValueChange(filtered)
        },
        label = { Text("Hex Code") },
        prefix = { Text("#") },
        singleLine = true,
        textStyle = TextStyle(
            fontFamily = FontFamily.Monospace,
            fontWeight = FontWeight.Bold,
            fontSize = 18.sp
        ),
        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Ascii),
        modifier = Modifier.width(160.dp)
    )
}

fun colorToHex(color: Color): String {
    return String.format("%06X", (0xFFFFFF and color.toArgb()))
}
