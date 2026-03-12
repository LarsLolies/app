package com.example.app.ui

import android.content.Context
import android.view.WindowManager
import androidx.appcompat.app.AlertDialog
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.app.LED_item
import com.github.skydoves.colorpicker.compose.*

fun showColorPickerDialog(
    context: Context,
    item: LED_item,
    onColorUpdated: () -> Unit
) {
    val composeView = ComposeView(context)
    val dialog = AlertDialog.Builder(context)
        .setView(composeView)
        .create()

    composeView.setContent {
        ColorPickerDialogContent(
            initialColor = item.color,
            onColorSelected = { newColor ->
                item.color = newColor
                onColorUpdated()
                dialog.dismiss()
            },
            onDismiss = { dialog.dismiss() }
        )
    }

    dialog.show()

    // Erlaubt der Tastatur zu erscheinen
    dialog.window?.clearFlags(WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE or WindowManager.LayoutParams.FLAG_ALT_FOCUSABLE_IM)
    dialog.window?.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_VISIBLE)
}

@Composable
fun ColorPickerDialogContent(
    initialColor: Int,
    onColorSelected: (Int) -> Unit,
    onDismiss: () -> Unit
) {
    val controller = rememberColorPickerController()
    var selectedColor by remember { mutableStateOf(Color(initialColor)) }
    var hexInput by remember { mutableStateOf(colorToHex(Color(initialColor))) }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(Color(0xFF2C2C2C))
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Box(
            modifier = Modifier
                .size(60.dp)
                .clip(RoundedCornerShape(8.dp))
                .background(selectedColor)
        )
        
        Spacer(modifier = Modifier.height(16.dp))

        HsvColorPicker(
            modifier = Modifier
                .fillMaxWidth()
                .height(250.dp),
            controller = controller,
            initialColor = Color(initialColor),
            onColorChanged = { colorEnvelope ->
                selectedColor = colorEnvelope.color
                val newHex = colorToHex(colorEnvelope.color)
                if (hexInput != newHex) hexInput = newHex
            }
        )

        Spacer(modifier = Modifier.height(16.dp))

        BrightnessSlider(
            modifier = Modifier
                .fillMaxWidth()
                .height(35.dp),
            controller = controller
        )

        Spacer(modifier = Modifier.height(16.dp))

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

        Spacer(modifier = Modifier.height(24.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.End
        ) {
            Button(onClick = onDismiss) {
                Text("Abbrechen")
            }
            Spacer(modifier = Modifier.width(8.dp))
            Button(onClick = { 
                onColorSelected(selectedColor.toArgb()) 
            }) {
                Text("OK")
            }
        }
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
        label = { Text("Hex Code", color = Color.White) },
        prefix = { Text("#", color = Color.White) },
        singleLine = true,
        textStyle = TextStyle(
            color = Color.White,
            fontFamily = FontFamily.Monospace,
            fontWeight = FontWeight.Bold,
            fontSize = 18.sp
        ),
        colors = OutlinedTextFieldDefaults.colors(
            focusedBorderColor = Color.White,
            unfocusedBorderColor = Color.White.copy(alpha = 0.7f),
            cursorColor = Color.White
        ),
        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Ascii),
        modifier = Modifier.width(160.dp)
    )
}

fun colorToHex(color: Color): String {
    return String.format("%06X", (0xFFFFFF and color.toArgb()))
}
