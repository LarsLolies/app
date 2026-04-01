package com.example.app

import io.ktor.client.*
import io.ktor.client.call.*
import io.ktor.client.engine.android.*
import io.ktor.client.plugins.contentnegotiation.*
import io.ktor.client.request.*
import io.ktor.http.*
import io.ktor.serialization.gson.*
import android.util.Log

data class LedStateResponse(
    val strip: Int,
    val state: String,
    val r: Int,
    val g: Int,
    val b: Int,
    val hex: String,
    val animations: List<String> = emptyList()
)

class LedApiService {

    private val client = HttpClient(Android) {
        install(ContentNegotiation) {
            gson()
        }
    }

    suspend fun sendLedUpdate(item: LED_item) {
        val baseUrl = if (item.getApiAddress().startsWith("http")) item.getApiAddress() else "http://${item.getApiAddress()}"
        
        try {
            val isOn = item.isState()
            val stateUrl = "$baseUrl/api/strip/${item.getStripId()}"
            
            // 1. Wenn die LED AUS geschaltet wird: Alles stoppen und auf 0 setzen
            if (!isOn) {
                // Erst Animationen löschen, damit der Microcontroller nicht wieder einschaltet
                val clearUrl = "$baseUrl/api/strip/${item.getStripId()}/animation/clear"
                client.post(clearUrl) { contentType(ContentType.Application.Json) }

                // Dann Status auf OFF und Farben auf 0
                client.post(stateUrl) {
                    contentType(ContentType.Application.Json)
                    setBody(mapOf(
                        "strip" to (item.getStripId().toIntOrNull() ?: -1),
                        "state" to "off",
                        "r" to 0, "g" to 0, "b" to 0
                    ))
                }
                Log.d("LedApiService", "LED komplett ausgeschaltet (inkl. Animationen)")
                return // Beenden, da wir nicht mehr prüfen müssen ob Rainbow an ist
            }

            // 2. Wenn die LED AN ist: Farbe und Animationen senden
            client.post(stateUrl) {
                contentType(ContentType.Application.Json)
                setBody(mapOf(
                    "strip" to (item.getStripId().toIntOrNull() ?: -1),
                    "state" to "on",
                    "r" to (item.getColor() shr 16 and 0xFF),
                    "g" to (item.getColor() shr 8 and 0xFF),
                    "b" to (item.getColor() and 0xFF)
                ))
            }

            if (item.isRainbow()) {
                val animAddUrl = "$baseUrl/api/strip/${item.getStripId()}/animation/add"
                client.post(animAddUrl) {
                    contentType(ContentType.Application.Json)
                    setBody(mapOf("animation" to "rainbow"))
                }
            } else {
                val animRemoveUrl = "$baseUrl/api/strip/${item.getStripId()}/animation/remove"
                client.post(animRemoveUrl) {
                    contentType(ContentType.Application.Json)
                    setBody(mapOf("animation" to "rainbow"))
                }
            }

        } catch (e: Exception) {
            Log.e("LedApiService", "Fehler beim API-Update: ${e.message}")
        }
    }

    suspend fun fetchLedState(item: LED_item): LedStateResponse? {
        val baseUrl = if (item.getApiAddress().startsWith("http")) item.getApiAddress() else "http://${item.getApiAddress()}"
        val url = "$baseUrl/api/strip/${item.getStripId()}/status"
        return try {
            client.get(url).body()
        } catch (e: Exception) {
            null
        }
    }

    fun close() = client.close()
}
