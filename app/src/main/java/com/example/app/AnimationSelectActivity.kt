package com.example.app

import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.google.android.material.materialswitch.MaterialSwitch
import kotlinx.coroutines.launch

class AnimationSelectActivity : AppCompatActivity() {

    private lateinit var item: LED_item
    private val apiService = LedApiService()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.animationselect)

        // Das LED_item aus dem Intent holen
        item = intent.getSerializableExtra("LED_ITEM") as LED_item

        val switchRainbow = findViewById<MaterialSwitch>(R.id.switch4)
        val switchBrightness = findViewById<MaterialSwitch>(R.id.switch5)
        val switchColor = findViewById<MaterialSwitch>(R.id.switch6)

        // Aktuelle Zustände setzen
        switchRainbow.isChecked = item.isRainbow
        switchBrightness.isChecked = item.isBrightnessFlicker
        switchColor.isChecked = item.isColorFlicker

        // Listener für Rainbow
        switchRainbow.setOnCheckedChangeListener { _, isChecked ->
            item.isRainbow = isChecked
            sendUpdate()
        }

        // Listener für Brightness Flicker
        switchBrightness.setOnCheckedChangeListener { _, isChecked ->
            item.isBrightnessFlicker = isChecked
            sendUpdate()
        }

        // Listener für Color Flicker
        switchColor.setOnCheckedChangeListener { _, isChecked ->
            item.isColorFlicker = isChecked
            sendUpdate()
        }
    }

    private fun sendUpdate() {
        lifecycleScope.launch {
            apiService.sendLedUpdate(item)
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        apiService.close()
    }
}
