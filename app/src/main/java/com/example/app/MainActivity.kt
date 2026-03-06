package com.example.app

import android.content.Context
import android.graphics.Color
import android.os.Bundle
import android.view.View
import android.view.WindowManager
import android.widget.Button
import android.widget.EditText
import android.widget.ListView
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.ui.platform.ComposeView
import com.example.app.ui.ColorPickerDialogContent
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken

class MainActivity : AppCompatActivity() {

    private lateinit var ledList: MutableList<LED_item>
    private lateinit var adapter: LEDAdapter
    private val gson = Gson()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_main)

        ledList = loadData()
        setupListView()
        setupFAB()
    }

    private fun setupListView() {
        val listView = findViewById<ListView>(R.id.listView)
        adapter = LEDAdapter(
            context = this,
            ledItems = ledList,
            onColorClick = { item -> showColorPickerDialog(item) },
            onSwitchChanged = { updateUIAndSave() }
        )
        listView.adapter = adapter
        listView.setOnItemLongClickListener { _, _, position, _ ->
            showLedSettingsDialog(ledList[position], position)
            true
        }
    }

    private fun setupFAB() {
        findViewById<FloatingActionButton>(R.id.floatingActionButton).setOnClickListener {
            showLedSettingsDialog()
        }
    }

    private fun showColorPickerDialog(item: LED_item) {
        val composeView = ComposeView(this)
        val dialog = AlertDialog.Builder(this)
            .setView(composeView)
            .create()

        composeView.setContent {
            ColorPickerDialogContent(
                initialColor = item.color,
                onColorSelected = { newColor: Int ->
                    item.color = newColor
                    updateUIAndSave()
                    dialog.dismiss()
                },
                onDismiss = { dialog.dismiss() }
            )
        }

        dialog.show()
        
        // FIX: Dies erlaubt der Tastatur, im Compose-Dialog zu erscheinen
        dialog.window?.clearFlags(WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE or WindowManager.LayoutParams.FLAG_ALT_FOCUSABLE_IM)
        dialog.window?.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_VISIBLE)
    }

    private fun showLedSettingsDialog(item: LED_item? = null, position: Int = -1) {
        val dialogView = layoutInflater.inflate(R.layout.led_settings, null)
        val dialog = AlertDialog.Builder(this)
            .setView(dialogView)
            .create()

        setupDialogLogic(dialogView, dialog, item, position)
        dialog.show()
    }

    private fun setupDialogLogic(view: View, dialog: AlertDialog, item: LED_item?, position: Int) {
        val nameET = view.findViewById<EditText>(R.id.nameEditText)
        val apiET = view.findViewById<EditText>(R.id.apiEditText)
        val pinET = view.findViewById<EditText>(R.id.pinEditText)
        val saveBtn = view.findViewById<Button>(R.id.saveButton)
        val deleteBtn = view.findViewById<Button>(R.id.delete)

        item?.let {
            nameET.setText(it.name)
            apiET.setText(it.apiAddress)
            pinET.setText(it.gpioPin)
            deleteBtn.visibility = View.VISIBLE
        } ?: run {
            deleteBtn.visibility = View.GONE
        }

        saveBtn.setOnClickListener {
            if (validateAndSave(nameET, apiET, pinET, item)) {
                dialog.dismiss()
            }
        }

        deleteBtn.setOnClickListener {
            confirmDeletion(item, position) {
                dialog.dismiss()
            }
        }
    }

    private fun validateAndSave(nameET: EditText, apiET: EditText, pinET: EditText, item: LED_item?): Boolean {
        val name = nameET.text.toString().trim()
        val api = apiET.text.toString().trim()
        val pin = pinET.text.toString().trim()

        if (name.isEmpty()) {
            nameET.error = "Name wird benötigt"
            return false
        }

        if (item == null) {
            ledList.add(LED_item(name, false, Color.WHITE, api, pin))
        } else {
            item.name = name
            item.apiAddress = api
            item.gpioPin = pin
        }

        updateUIAndSave()
        return true
    }

    private fun confirmDeletion(item: LED_item?, position: Int, onDeleted: () -> Unit) {
        AlertDialog.Builder(this)
            .setTitle("LED löschen")
            .setMessage("Möchtest du '${item?.name}' wirklich löschen?")
            .setPositiveButton("Löschen") { _, _ ->
                if (position != -1) {
                    ledList.removeAt(position)
                    updateUIAndSave()
                    onDeleted()
                }
            }
            .setNegativeButton("Abbrechen", null)
            .show()
    }

    private fun updateUIAndSave() {
        adapter.notifyDataSetChanged()
        saveData()
    }

    override fun onPause() {
        super.onPause()
        saveData()
    }

    private fun saveData() {
        val sharedPreferences = getSharedPreferences("LED_APP_PREFS", Context.MODE_PRIVATE)
        sharedPreferences.edit().putString("led_list", gson.toJson(ledList)).apply()
    }

    private fun loadData(): MutableList<LED_item> {
        val sharedPreferences = getSharedPreferences("LED_APP_PREFS", Context.MODE_PRIVATE)
        val json = sharedPreferences.getString("led_list", null)
        val type = object : TypeToken<MutableList<LED_item>>() {}.type
        return json?.let { gson.fromJson(it, type) } ?: mutableListOf()
    }
}
