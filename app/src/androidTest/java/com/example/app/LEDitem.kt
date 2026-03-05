package com.example.app

data class LEDItem(
    val name: String,
    var state: Boolean = false,
    var colorHex: String,
    val stripId: Int = 0,
    val api: String = ""
)
