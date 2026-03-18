package com.example.app

import android.content.Context
import android.graphics.Color
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.ListView
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.example.app.ui.showColorPickerDialog
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import kotlinx.coroutines.launch

class MainActivity : AppCompatActivity() {

    private lateinit var ledList: MutableList<LED_item>
    private lateinit var adapter: LEDAdapter
    private val gson = Gson()
    private val apiService = LedApiService()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_main)

        ledList = loadData()
        setupListView()
        setupFAB()
    }

    override fun onResume() {
        super.onResume()
        refreshAllStrips()
    }

    private fun refreshAllStrips() {
        lifecycleScope.launch {
            for (item in ledList) {
                launch {
                    val response = apiService.fetchLedState(item)
                    response?.let {
                        item.setState(it.state == "on")
                        try {
                            item.setColor(Color.parseColor(it.hex))
                        } catch (e: Exception) { }
                        
                        runOnUiThread { adapter.notifyDataSetChanged() }
                    }
                }
            }
        }
    }

    private fun setupListView() {
        val listView = findViewById<ListView>(R.id.listView)
        adapter = LEDAdapter(
            context = this,
            ledItems = ledList,
            onColorClick = { item -> 
                showColorPickerDialog(this, item) { 
                    updateUIAndSave(item) 
                } 
            },
            onSwitchChanged = { item -> 
                updateUIAndSave(item)
            }
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
        val stripIdET = view.findViewById<EditText>(R.id.stripIdEditText)
        val saveBtn = view.findViewById<Button>(R.id.saveButton)
        val deleteBtn = view.findViewById<Button>(R.id.delete)

        item?.let {
            nameET.setText(it.getName())
            apiET.setText(it.getApiAddress())
            stripIdET.setText(it.getStripId())
            deleteBtn.visibility = View.VISIBLE
        } ?: run {
            deleteBtn.visibility = View.GONE
        }

        saveBtn.setOnClickListener {
            if (validateAndSave(nameET, apiET, stripIdET, item)) {
                dialog.dismiss()
            }
        }

        deleteBtn.setOnClickListener {
            confirmDeletion(item, position) {
                dialog.dismiss()
            }
        }
    }

    private fun validateAndSave(nameET: EditText, apiET: EditText, stripIdET: EditText, item: LED_item?): Boolean {
        val name = nameET.text.toString().trim()
        val api = apiET.text.toString().trim()
        val stripId = stripIdET.text.toString().trim()

        if (name.isEmpty()) {
            nameET.error = "Name wird benötigt"
            return false
        }

        if (item == null) {
            val newItem = LED_item(name, false, Color.WHITE, api, stripId)
            ledList.add(newItem)
            updateUIAndSave(newItem)
        } else {
            item.setName(name)
            item.setApiAddress(api)
            item.setStripId(stripId)
            updateUIAndSave(item)
        }

        return true
    }

    private fun confirmDeletion(item: LED_item?, position: Int, onDeleted: () -> Unit) {
        AlertDialog.Builder(this)
            .setTitle("LED löschen")
            .setMessage("Möchtest du '${item?.getName()}' wirklich löschen?")
            .setPositiveButton("Löschen") { _, _ ->
                if (position != -1) {
                    ledList.removeAt(position)
                    adapter.notifyDataSetChanged()
                    saveData()
                    onDeleted()
                }
            }
            .setNegativeButton("Abbrechen", null)
            .show()
    }

    private fun updateUIAndSave(item: LED_item) {
        adapter.notifyDataSetChanged()
        saveData()
        
        lifecycleScope.launch {
            apiService.sendLedUpdate(item)
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        apiService.close()
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
