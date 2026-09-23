package com.music.innertube.utils

import io.ktor.client.HttpClient
import io.ktor.client.request.get
import io.ktor.client.statement.bodyAsText
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.serialization.kotlinx.json.json
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.decodeFromJsonElement
import kotlinx.serialization.json.jsonArray
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive

data class SpotifyScrapedTrack(
    val title: String,
    val artists: String,
    val durationMs: Long
)

data class SpotifyScrapedPlaylist(
    val title: String,
    val description: String,
    val tracks: List<SpotifyScrapedTrack>
)

@OptIn(kotlin.io.encoding.ExperimentalEncodingApi::class)
object SpotifyScraper {
    private val client = HttpClient {
        install(ContentNegotiation) {
            json(Json { ignoreUnknownKeys = true })
        }
    }

    suspend fun scrapePlaylist(playlistUrl: String): Result<SpotifyScrapedPlaylist> = runCatching {
        // Fetch HTML
        val response = client.get(playlistUrl)
        val html = response.bodyAsText()

        // Extract <title> for playlist name
        val titleRegex = Regex("<title>(.*?)</title>")
        val pageTitle = titleRegex.find(html)?.groupValues?.get(1)?.replace(" | Spotify", "")?.trim() ?: "Spotify Playlist"

        // Extract <script id="initial-state" type="text/plain"> or initialState
        val scriptRegex = Regex("""<script id="initial-?State"\s*type="text/plain"[^>]*>(.*?)</script>""", setOf(RegexOption.IGNORE_CASE, RegexOption.DOT_MATCHES_ALL))
        val scriptContent = scriptRegex.find(html)?.groupValues?.get(1) 
            ?: throw Exception("Could not find initial-state JSON. Is the playlist public?")

        // Parse JSON
        val decoded = kotlin.io.encoding.Base64.decode(scriptContent.encodeToByteArray()).decodeToString()
        val json = Json { ignoreUnknownKeys = true }
        
        // Wait, Spotify encodes initial-state in Base64? Sometimes they do. Let's try parsing directly first, if it fails, base64 decode.
        val stateObj = try {
            json.parseToJsonElement(scriptContent).jsonObject
        } catch (e: Exception) {
            try {
                json.parseToJsonElement(decoded).jsonObject
            } catch (ex: Exception) {
                throw Exception("Failed to parse Spotify JSON state")
            }
        }

        // Navigate the JSON to extract tracks. The exact structure can vary.
        // It's usually in entities -> items -> spotify:playlist:ID -> tracks -> items
        val entities = stateObj["entities"]?.jsonObject?.get("items")?.jsonObject
        var foundTracks = mutableListOf<SpotifyScrapedTrack>()

        if (entities != null) {
            // Find the playlist object
            for ((key, value) in entities) {
                if (key.startsWith("spotify:playlist:")) {
                    val tracksObj = value.jsonObject["tracks"]?.jsonObject ?: value.jsonObject["trackList"]?.jsonObject
                    val contentObj = value.jsonObject["content"]?.jsonObject
                    
                    // Old structure: tracks -> items
                    val oldItems = tracksObj?.get("items")?.jsonArray
                    if (oldItems != null) {
                        for (item in oldItems) {
                            try {
                                val trackNode = item.jsonObject["track"]?.jsonObject ?: item.jsonObject
                                val name = trackNode["name"]?.jsonPrimitive?.content ?: continue
                                val artistsArray = trackNode["artists"]?.jsonObject?.get("items")?.jsonArray 
                                    ?: trackNode["artists"]?.jsonArray
                                
                                val artistNames = artistsArray?.mapNotNull { it.jsonObject["name"]?.jsonPrimitive?.content }?.joinToString(", ") ?: ""
                                val duration = trackNode["duration"]?.jsonObject?.get("totalMilliseconds")?.jsonPrimitive?.content?.toLongOrNull() 
                                    ?: trackNode["duration_ms"]?.jsonPrimitive?.content?.toLongOrNull() ?: 0L

                                foundTracks.add(SpotifyScrapedTrack(title = name, artists = artistNames, durationMs = duration))
                            } catch (e: Exception) {
                                // Skip unparseable track
                            }
                        }
                    }
                    
                    // New structure: content -> items -> itemV2 -> data
                    val contentItems = contentObj?.get("items")?.jsonArray
                    if (contentItems != null) {
                        for (item in contentItems) {
                            try {
                                val dataNode = item.jsonObject["itemV2"]?.jsonObject?.get("data")?.jsonObject ?: continue
                                val name = dataNode["name"]?.jsonPrimitive?.content ?: continue
                                val artistsArray = dataNode["artists"]?.jsonObject?.get("items")?.jsonArray
                                val artistNames = artistsArray?.mapNotNull { 
                                    it.jsonObject["profile"]?.jsonObject?.get("name")?.jsonPrimitive?.content 
                                }?.joinToString(", ") ?: ""
                                
                                val duration = dataNode["duration"]?.jsonObject?.get("totalMilliseconds")?.jsonPrimitive?.content?.toLongOrNull() ?: 0L
                                
                                foundTracks.add(SpotifyScrapedTrack(title = name, artists = artistNames, durationMs = duration))
                            } catch (e: Exception) {
                                // Skip unparseable track
                            }
                        }
                    }
                }
            }
        }

        if (foundTracks.isEmpty()) {
            throw Exception("Found 0 tracks. The playlist might be private or the format changed.")
        }

        SpotifyScrapedPlaylist(
            title = pageTitle,
            description = "Imported from Spotify",
            tracks = foundTracks
        )
    }
}
