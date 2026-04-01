package com.example.app

import android.content.Context
import android.content.res.ColorStateList
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.TextView
import com.google.android.material.materialswitch.MaterialSwitch

class LEDAdapter(
    context: Context,
    private val ledItems: List<LED_item>,
    private val onColorClick: (LED_item) -> Unit,
    private val onSwitchChanged: (LED_item) -> Unit
) : ArrayAdapter<LED_item>(context, R.layout.list_item_led, ledItems) {

    override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
        val view = convertView ?: LayoutInflater.from(context).inflate(R.layout.list_item_led, parent, false)
        
        val item = ledItems[position]
        
        val nameTextView = view.findViewById<TextView>(R.id.ledName)
        val colorCircle = view.findViewById<View>(R.id.ledColorCircle)
        val ledSwitch = view.findViewById<MaterialSwitch>(R.id.ledSwitch)
        
        nameTextView.text = item.getName()

        // Rainbow Logik für den Indicator
        if (item.isRainbow()) {
            colorCircle.setBackgroundResource(R.drawable.led_rainbow_indicator)
            colorCircle.backgroundTintList = null // Wichtig: Tint entfernen, sonst überdeckt er den Regenbogen
        } else {
            colorCircle.setBackgroundResource(R.drawable.led_indicator)
            colorCircle.backgroundTintList = ColorStateList.valueOf(item.getColor())
        }
        
        colorCircle.setOnClickListener {
            onColorClick(item)
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
