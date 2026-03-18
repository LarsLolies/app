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
     * URL-Format: POST /api/strip/{id}
     */
    suspend fun sendLedUpdate(item: LED_item) {
        val baseUrl = if (item.getApiAddress().startsWith("http")) item.getApiAddress() else "http://${item.getApiAddress()}"
        // POST an /api/strip/0 (ohne /status!)
        val url = "$baseUrl/api/strip/${item.getStripId()}"
        
        try {
            val color = item.getColor()
            val r = (color shr 16) and 0xFF
            val g = (color shr 8) and 0xFF
            val b = color and 0xFF
            val hex = String.format("#%06X", (0xFFFFFF and color))

            val response = client.post(url) {
                contentType(ContentType.Application.Json)
                setBody(
                    mapOf(
                        "strip" to (item.getStripId().toIntOrNull() ?: 0),

                        "r" to r,
                        "g" to g,
                        "b" to b,
                        "hex" to hex
                    )
                )
            }

            Log.d("LedApiService", "POST erfolgreich an $url: ${response.status}")

        } catch (e: Exception) {
            Log.e("LedApiService", "Fehler beim POST an $url: ${e.message}")
        }
    }

    /**
     * Fragt den Status ab
     * URL-Format: GET /api/strip/{id}/status
     */
    suspend fun fetchLedState(item: LED_item): LedStateResponse? {
        val baseUrl = if (item.getApiAddress().startsWith("http")) item.getApiAddress() else "http://${item.getApiAddress()}"
        val url = "$baseUrl/api/strip/${item.getStripId()}/status"
        
        return try {
            val response: LedStateResponse = client.get(url).body()
            Log.d("LedApiService", "GET erfolgreich von $url: ${response.state}")
            response
        } catch (e: Exception) {
            Log.e("LedApiService", "Fehler beim GET von $url: ${e.message}")
            null
        }
    }

    fun close() {
        client.close()
    }
}
