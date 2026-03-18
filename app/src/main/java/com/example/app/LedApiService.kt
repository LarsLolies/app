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
     * Sendet ein Update (Farbe/Status) an einen Strip
     */
    suspend fun sendLedUpdate(item: LED_item) {
        val baseUrl = if (item.getApiAddress().startsWith("http")) item.getApiAddress() else "http://${item.getApiAddress()}"
        val url = "$baseUrl/api/strip/${item.getStripId()}"
        
        try {
            val isOn = item.isState()
            val stateToSend = if (isOn) "on" else "off"
            
            // Fix: Wenn Status "off", senden wir 0 für die Farben, 
            // damit der Python-Server den Status korrekt erkennt.
            val r = if (isOn) (item.getColor() shr 16 and 0xFF) else 0
            val g = if (isOn) (item.getColor() shr 8 and 0xFF) else 0
            val b = if (isOn) (item.getColor() and 0xFF) else 0
            val hex = if (isOn) String.format("#%06X", (0xFFFFFF and item.getColor())) else "#000000"

            Log.d("LedApiService", "Sende an $url -> State: $stateToSend, RGB: ($r,$g,$b)")

            val response = client.post(url) {
                contentType(ContentType.Application.Json)
                setBody(
                    mapOf(
                        "strip" to (item.getStripId().toIntOrNull() ?: -1),
                        "state" to stateToSend,
                        "r" to r,
                        "g" to g,
                        "b" to b,
                        "hex" to hex
                    )
                )
            }

            Log.d("LedApiService", "POST erfolgreich: ${response.status}")

        } catch (e: Exception) {
            Log.e("LedApiService", "Fehler beim POST: ${e.message}")
        }
    }

    /**
     * Fragt den aktuellen Status ab
     */
    suspend fun fetchLedState(item: LED_item): LedStateResponse? {
        val baseUrl = if (item.getApiAddress().startsWith("http")) item.getApiAddress() else "http://${item.getApiAddress()}"
        val url = "$baseUrl/api/strip/${item.getStripId()}/status"
        
        return try {
            val response: LedStateResponse = client.get(url).body()
            Log.d("LedApiService", "GET erfolgreich: ${response.state}")
            response
        } catch (e: Exception) {
            Log.e("LedApiService", "Fehler beim GET: ${e.message}")
            null
        }
    }

    fun close() {
        client.close()
    }
}
