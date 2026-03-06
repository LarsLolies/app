package com.example.app

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.activity.enableEdgeToEdge
import com.google.android.material.floatingactionbutton.FloatingActionButton

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        
        // Hier wird die XML-Datei als Layout gesetzt
        setContentView(R.layout.activity_main)

        // Beispiel: Zugriff auf den FAB aus der XML
        val fab = findViewById<FloatingActionButton>(R.id.floatingActionButton)
        fab.setOnClickListener {
            // Hier könntest du später den ColorPicker aufrufen
        }
    }
}
