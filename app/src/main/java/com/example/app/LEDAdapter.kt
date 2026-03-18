package com.example.app

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.ImageButton
import android.widget.TextView
import com.google.android.material.materialswitch.MaterialSwitch

class LEDAdapter(
    context: Context,
    private val ledItems: List<LED_item>,
    private val onColorClick: (LED_item) -> Unit,
    private val onSwitchChanged: (LED_item) -> Unit,
    private val onPlayClick: (LED_item) -> Unit // Neuer Callback für den Play-Button
) : ArrayAdapter<LED_item>(context, R.layout.list_item_led, ledItems) {

    override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
        val view = convertView ?: LayoutInflater.from(context).inflate(R.layout.list_item_led, parent, false)
        
        val item = ledItems[position]
        
        val nameTextView = view.findViewById<TextView>(R.id.ledName)
        val colorCircle = view.findViewById<View>(R.id.ledColorCircle)
        val ledSwitch = view.findViewById<MaterialSwitch>(R.id.ledSwitch)
        val playButton = view.findViewById<ImageButton>(R.id.btnPlayEffect)
        
        nameTextView.text = item.name
        colorCircle.backgroundTintList = android.content.res.ColorStateList.valueOf(item.color)
        
        colorCircle.setOnClickListener {
            onColorClick(item)
        }

        playButton.setOnClickListener {
            onPlayClick(item)
        }
        
        ledSwitch.setOnCheckedChangeListener(null)
        ledSwitch.isChecked = item.isState
        
        ledSwitch.setOnCheckedChangeListener { _, isChecked ->
            item.setState(isChecked)
            onSwitchChanged(item)
        }
        
        return view
    }
}
