package com.example.app

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import androidx.appcompat.app.AppCompatActivity

class SettingsActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.led_settings)

        val nameEditText = findViewById<EditText>(R.id.nameEditText)
        val apiEditText = findViewById<EditText>(R.id.apiEditText)
        val stripIdEditText = findViewById<EditText>(R.id.stripIdEditText)
        val saveButton = findViewById<Button>(R.id.saveButton)

        saveButton.setOnClickListener {
            val name = nameEditText.text.toString()
            val api = apiEditText.text.toString()
            val stripId = stripIdEditText.text.toString()

            if (name.isNotEmpty()) {
                val resultIntent = Intent()
                val newItem = LED_item(name, false, android.graphics.Color.WHITE, api, stripId)
                resultIntent.putExtra("NEW_LED_ITEM", newItem)
                setResult(Activity.RESULT_OK, resultIntent)
                finish()
            }
        }
    }
}
