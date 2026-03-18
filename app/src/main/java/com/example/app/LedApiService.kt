package com.example.app

import io.ktor.client.*
import io.ktor.client.call.*
import io.ktor.client.engine.android.*
import io.ktor.client.plugins.contentnegotiation.*
import io.ktor.client.request.*
import io.ktor.http.*
import io.ktor.serialization.gson.*
import android.util.Log

// Datenklasse für die API-Antwort
data class LedStateResponse(
    val strip: Int,
    val state: String, // "on" oder "off"
    val r: Int,
    val g: Int,
    val b: Int,
    val hex: String
)

class LedApiService {

    private val client = HttpClient(Android) {
        install(ContentNegotiation) {
            gson()
        }
    }

    /**
     * Sendet den aktuellen Status an die LED
     */
    suspend fun sendLedUpdate(item: LED_item) {
        try {
            val baseUrl = if (item.apiAddress.startsWith("http")) item.apiAddress else "http://${item.apiAddress}"
            val url = "$baseUrl/api/strip/${item.gpioPin}"

            val color = item.getColor()
            val r = (color shr 16) and 0xFF
            val g = (color shr 8) and 0xFF
            val b = color and 0xFF
            val hex = String.format("#%06X", (0xFFFFFF and color))

            val state = if (item.isState()) "on" else "off"

            val response = client.post(url) {
                contentType(ContentType.Application.Json)
                setBody(
                    mapOf(
                        "pin" to (item.gpioPin.toIntOrNull() ?: 0), // Strip-ID muss in den Body!
                        "state" to state,
                        "r" to r,
                        "g" to g,
                        "b" to b,
                        "hex" to hex
                    )
                )
            }

            Log.d("LedApiService", "Update gesendet an $url: ${response.status}")

        } catch (e: Exception) {
            Log.e("LedApiService", "Fehler beim Senden: ${e.message}")
        }
    }

    /**
     * Fragt den aktuellen Status der LED ab
     */
    suspend fun fetchLedState(item: LED_item): LedStateResponse? {
        return try {
            val baseUrl = if (item.apiAddress.startsWith("http")) item.apiAddress else "http://${item.apiAddress}"
            val url = "$baseUrl/api/strip/${item.gpioPin}/status"
            
            val response: LedStateResponse = client.get(url).body()
            
            Log.d("LedApiService", "Status geladen von $url: ${response.state}")
            response
        } catch (e: Exception) {
            Log.e("LedApiService", "Fehler beim Abrufen: ${e.message}")
            null
        }
    }

    fun close() {
        client.close()
    }
}
