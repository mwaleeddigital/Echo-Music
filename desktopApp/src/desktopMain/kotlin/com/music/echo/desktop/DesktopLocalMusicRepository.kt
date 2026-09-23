package com.music.echo.desktop

import app.cash.sqldelight.driver.jdbc.sqlite.JdbcSqliteDriver
import com.music.echo.sharedui.data.LocalMusicRepository
import com.music.echo.sharedui.data.UserTasteProfile
import com.music.echo.sharedui.screens.DisplayTrack
import echo.music.iad1tya.db.MusicDatabase
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.withContext
import java.io.File
import kotlin.math.ln

class DesktopLocalMusicRepository : LocalMusicRepository {
    private val mutex = Mutex()
    private val rootDir = File(System.getProperty("user.home"), ".echo_music").apply {
        if (!exists()) mkdirs()
    }

    private val dbFile = File(rootDir, "echo_music.db")
    private val likedSongsJsonFile = File(rootDir, "liked_songs.json")
    private val historyJsonFile = File(rootDir, "listening_history.json")
    private val statsJsonFile = File(rootDir, "listening_stats.json")
    private val cachedSongsJsonFile = File(rootDir, "cached_songs.json")
    val audioCacheDir = File(rootDir, "audio_cache").apply {
        if (!exists()) mkdirs()
    }

    private val database: MusicDatabase by lazy {
        val driver = JdbcSqliteDriver("jdbc:sqlite:${dbFile.absolutePath}")
        try {
            MusicDatabase.Schema.create(driver)
        } catch (_: Exception) {
            // Schema already exists
        }
        MusicDatabase(driver)
    }

    private val repoScope = kotlinx.coroutines.CoroutineScope(Dispatchers.IO + kotlinx.coroutines.SupervisorJob())

    // In-memory cache for ultra-responsive UI
    private val cachedLikedSongs = mutableMapOf<String, DisplayTrack>()
    private val cachedHistory = mutableListOf<DisplayTrack>()
    private val cachedDownloadedTracks = mutableMapOf<String, DisplayTrack>()
    private val playCounts = mutableMapOf<String, Int>()
    private val completionCounts = mutableMapOf<String, Int>()
    private val skipCounts = mutableMapOf<String, Int>()
    private val downloadingSet = java.util.concurrent.ConcurrentHashMap.newKeySet<String>()
    
    private val customPlaylists = mutableMapOf<String, Pair<com.music.echo.sharedui.screens.DisplayPlaylist, List<DisplayTrack>>>()

    init {
        loadInitialData()
    }

    private fun loadInitialData() {
        try {
            // 1. Try loading from SQLite
            try {
                val dbLiked = database.musicDatabaseQueries.likedSongsByCreateDateAsc().executeAsList()
                for (s in dbLiked) {
                    val durationText = if (s.duration > 0) {
                        "${s.duration / 60}:${(s.duration % 60).toString().padStart(2, '0')}"
                    } else ""
                    cachedLikedSongs[s.id] = DisplayTrack(
                        id = s.id,
                        title = s.title,
                        artist = s.albumName ?: "Unknown Artist",
                        duration = durationText,
                        thumbnailUrl = s.thumbnailUrl,
                        album = s.albumName,
                        isVideo = s.isVideo == 1L
                    )
                }
            } catch (e: Exception) {
                println("[DesktopRepo] SQLite query error: ${e.message}")
            }

            // 2. Hydrate from JSON backup if SQLite is empty or to complement
            if (likedSongsJsonFile.exists()) {
                val jsonContent = likedSongsJsonFile.readText()
                val parsed = parseTracksJson(jsonContent)
                for (t in parsed) {
                    cachedLikedSongs[t.id] = t
                }
            }

            // 3. Hydrate history
            if (historyJsonFile.exists()) {
                val jsonContent = historyJsonFile.readText()
                val parsed = parseTracksJson(jsonContent)
                cachedHistory.addAll(parsed)
            }

            // 4. Hydrate stats
            if (statsJsonFile.exists()) {
                val lines = statsJsonFile.readLines()
                for (line in lines) {
                    val parts = line.split("\t")
                    if (parts.size >= 4) {
                        val id = parts[0]
                        playCounts[id] = parts[1].toIntOrNull() ?: 0
                        completionCounts[id] = parts[2].toIntOrNull() ?: 0
                        skipCounts[id] = parts[3].toIntOrNull() ?: 0
                    }
                }
            }

            // 5. Hydrate downloaded songs metadata
            if (cachedSongsJsonFile.exists()) {
                val jsonContent = cachedSongsJsonFile.readText()
                val parsed = parseTracksJson(jsonContent)
                for (t in parsed) {
                    cachedDownloadedTracks[t.id] = t
                }
            }

            // Cross-check with files physically present in audioCacheDir
            val existingFiles = audioCacheDir.listFiles { _, name -> name.endsWith(".m4a") || name.endsWith(".mp3") } ?: emptyArray()
            val existingIds = existingFiles.map { it.nameWithoutExtension }.toSet()
            // Backfill any tracks present in history or liked songs that have audio files on disk
            for (id in existingIds) {
                if (!cachedDownloadedTracks.containsKey(id)) {
                    val match = cachedLikedSongs[id] ?: cachedHistory.find { it.id == id }
                    if (match != null) {
                        cachedDownloadedTracks[id] = match
                    }
                }
            }

            println("[DesktopRepo] Loaded ${cachedLikedSongs.size} liked, ${cachedHistory.size} history, ${cachedDownloadedTracks.size} downloaded tracks from disk")
        } catch (e: Exception) {
            println("[DesktopRepo] Error loading initial data: ${e.message}")
            e.printStackTrace()
        }
    }

    override suspend fun getLikedSongs(): List<DisplayTrack> = withContext(Dispatchers.IO) {
        mutex.withLock {
            cachedLikedSongs.values.toList().reversed()
        }
    }

    override suspend fun isSongLiked(trackId: String): Boolean = withContext(Dispatchers.IO) {
        mutex.withLock {
            cachedLikedSongs.containsKey(trackId)
        }
    }

    override suspend fun setSongLiked(track: DisplayTrack, liked: Boolean) = withContext(Dispatchers.IO) {
        mutex.withLock {
            if (liked) {
                cachedLikedSongs[track.id] = track
            } else {
                cachedLikedSongs.remove(track.id)
            }

            // Persist to SQLite
            try {
                val durationSec = parseDurationToSeconds(track.duration)
                database.musicDatabaseQueries.insertSong(
                    id = track.id,
                    title = track.title,
                    duration = durationSec,
                    thumbnailUrl = track.thumbnailUrl,
                    albumId = null,
                    albumName = track.artist, // Store artist in albumName or companion
                    explicit = 0L,
                    year = null,
                    date = null,
                    dateModified = System.currentTimeMillis(),
                    liked = if (liked) 1L else 0L,
                    likedDate = if (liked) System.currentTimeMillis() else null,
                    totalPlayTime = 0L,
                    inLibrary = if (liked) 1L else null,
                    dateDownload = null,
                    isLocal = 0L,
                    libraryAddToken = null,
                    libraryRemoveToken = null,
                    lyricsOffset = 0L,
                    romanizeLyrics = 1L,
                    isDownloaded = 0L,
                    isUploaded = 0L,
                    isVideo = if (track.isVideo) 1L else 0L,
                    hideFromQuickPicks = 0L
                )
            } catch (e: Exception) {
                println("[DesktopRepo] SQLite insert error: ${e.message}")
            }

            // Persist to JSON backup immediately
            saveLikedSongsToJson()
        }
    }

    override suspend fun recordPlayEvent(
        track: DisplayTrack,
        playTimeMs: Long,
        completed: Boolean,
        skipped: Boolean
    ) = withContext(Dispatchers.IO) {
        mutex.withLock {
            cachedHistory.removeAll { it.id == track.id }
            cachedHistory.add(0, track)
            if (cachedHistory.size > 100) {
                cachedHistory.removeAt(cachedHistory.lastIndex)
            }

            playCounts[track.id] = (playCounts[track.id] ?: 0) + 1
            if (completed) {
                completionCounts[track.id] = (completionCounts[track.id] ?: 0) + 1
            }
            if (skipped) {
                skipCounts[track.id] = (skipCounts[track.id] ?: 0) + 1
            }

            // Save to SQLite events
            try {
                database.musicDatabaseQueries.insertSong(
                    id = track.id,
                    title = track.title,
                    duration = parseDurationToSeconds(track.duration),
                    thumbnailUrl = track.thumbnailUrl,
                    albumId = null,
                    albumName = track.artist,
                    explicit = 0L,
                    year = null,
                    date = null,
                    dateModified = System.currentTimeMillis(),
                    liked = if (cachedLikedSongs.containsKey(track.id)) 1L else 0L,
                    likedDate = null,
                    totalPlayTime = (playTimeMs / 1000L),
                    inLibrary = null,
                    dateDownload = null,
                    isLocal = 0L,
                    libraryAddToken = null,
                    libraryRemoveToken = null,
                    lyricsOffset = 0L,
                    romanizeLyrics = 1L,
                    isDownloaded = 0L,
                    isUploaded = 0L,
                    isVideo = if (track.isVideo) 1L else 0L,
                    hideFromQuickPicks = 0L
                )
            } catch (_: Exception) {}

            saveHistoryToJson()
            saveStatsToJson()
            if (isSongCached(track.id)) {
                cachedDownloadedTracks[track.id] = track
                saveCachedSongsToJson()
            }
        }
    }

    override suspend fun getRecentHistory(limit: Int): List<DisplayTrack> = withContext(Dispatchers.IO) {
        mutex.withLock {
            cachedHistory.take(limit)
        }
    }

    override suspend fun getTopPlayedTracks(limit: Int): List<DisplayTrack> = withContext(Dispatchers.IO) {
        mutex.withLock {
            val all = (cachedLikedSongs.values + cachedHistory).distinctBy { it.id }
            all.sortedByDescending { playCounts[it.id] ?: 0 }.take(limit)
        }
    }

    override suspend fun getUserTasteProfile(): UserTasteProfile = withContext(Dispatchers.IO) {
        mutex.withLock {
            val artistScores = mutableMapOf<String, Float>()

            // 1. Liked songs contribute +4.0 base weight to artist affinity
            for (song in cachedLikedSongs.values) {
                val artist = song.artist.trim().lowercase()
                artistScores[artist] = (artistScores[artist] ?: 0f) + 4.0f
            }

            // 2. Play events and completions contribute log-scaled playcount and completion boost
            for ((songId, count) in playCounts) {
                val song = cachedLikedSongs[songId] ?: cachedHistory.find { it.id == songId } ?: continue
                val artist = song.artist.trim().lowercase()
                val completions = completionCounts[songId] ?: 0
                val skips = skipCounts[songId] ?: 0
                val playWeight = ln(1.0 + count).toFloat() * 2.0f
                val completionWeight = completions * 1.5f
                val skipPenalty = skips * 2.0f
                artistScores[artist] = (artistScores[artist] ?: 0f) + playWeight + completionWeight - skipPenalty
            }

            val skippedIds = skipCounts.filter { it.value >= 2 }.keys

            UserTasteProfile(
                likedSongIds = cachedLikedSongs.keys.toSet(),
                artistAffinityScores = artistScores,
                topSeedTrackIds = (cachedLikedSongs.keys + cachedHistory.map { it.id }).distinct().take(10),
                skippedSongIds = skippedIds,
                recentPlayedTrackIds = cachedHistory.take(15).map { it.id }
            )
        }
    }

    override suspend fun getDownloadedSongs(): List<DisplayTrack> = withContext(Dispatchers.IO) {
        mutex.withLock {
            // Filter to tracks whose audio file exists on disk
            cachedDownloadedTracks.values.filter { track ->
                isSongCached(track.id)
            }.reversed()
        }
    }

    override fun getCachedAudioUri(trackId: String): String? {
        val sanitized = trackId.filter { it.isLetterOrDigit() || it == '-' || it == '_' }.ifEmpty { trackId }
        val possibleFiles = listOf(
            File(audioCacheDir, "$trackId.m4a"),
            File(audioCacheDir, "$sanitized.m4a"),
            File(audioCacheDir, "$trackId.mp3"),
            File(audioCacheDir, "$sanitized.mp3")
        )
        for (f in possibleFiles) {
            if (f.exists() && f.length() > 32768L) {
                return f.toURI().toString()
            }
        }
        return null
    }

    override fun isSongCached(trackId: String): Boolean {
        return getCachedAudioUri(trackId) != null
    }

    override fun cacheAudioStream(track: DisplayTrack, streamUrl: String) {
        val id = track.id
        if (isSongCached(id) || downloadingSet.contains(id)) return
        downloadingSet.add(id)

        // Immediately register track metadata
        repoScope.launch {
            mutex.withLock {
                cachedDownloadedTracks[id] = track
                saveCachedSongsToJson()
            }

            try {
                println("[DesktopRepo] Starting background cache download for: ${track.title} ($id)")
                val sanitized = id.filter { it.isLetterOrDigit() || it == '-' || it == '_' }.ifEmpty { "track" }
                val targetFile = File(audioCacheDir, "$sanitized.m4a")
                val tempFile = File(audioCacheDir, "$sanitized.m4a.tmp")

                val client = java.net.http.HttpClient.newBuilder()
                    .followRedirects(java.net.http.HttpClient.Redirect.ALWAYS)
                    .connectTimeout(java.time.Duration.ofSeconds(15))
                    .build()

                val userAgent = when {
                    streamUrl.contains("c=IOS") || streamUrl.contains("ios") -> "com.google.ios.youtube/21.03.1 (iPhone16,2; U; CPU iOS 18_2 like Mac OS X;)"
                    streamUrl.contains("c=ANDROID") -> "com.google.android.youtube/21.03.38 (Linux; U; Android 14) gzip"
                    else -> "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/130.0.0.0 Safari/537.36"
                }

                val request = java.net.http.HttpRequest.newBuilder()
                    .uri(java.net.URI.create(streamUrl))
                    .header("User-Agent", userAgent)
                    .header("Accept", "*/*")
                    .header("Accept-Encoding", "identity;q=1, *;q=0")
                    .GET()
                    .build()

                val response = client.send(request, java.net.http.HttpResponse.BodyHandlers.ofFile(tempFile.toPath()))
                if (response.statusCode() in 200..299 && tempFile.length() > 32768L) {
                    if (targetFile.exists()) targetFile.delete()
                    val renamed = tempFile.renameTo(targetFile)
                    if (renamed) {
                        println("[DesktopRepo] Successfully cached song to disk: ${track.title} (${targetFile.length() / 1024} KB)")
                    } else {
                        tempFile.copyTo(targetFile, overwrite = true)
                        tempFile.delete()
                        println("[DesktopRepo] Successfully copied cached song to disk: ${track.title} (${targetFile.length() / 1024} KB)")
                    }
                } else {
                    tempFile.delete()
                    println("[DesktopRepo] Failed to cache stream: HTTP ${response.statusCode()}")
                }
            } catch (e: Exception) {
                println("[DesktopRepo] Exception caching audio: ${e.message}")
            } finally {
                downloadingSet.remove(id)
            }
        }
    }

    override suspend fun getCacheStorageStats(): String = withContext(Dispatchers.IO) {
        val files = audioCacheDir.listFiles { _, name -> name.endsWith(".m4a") || name.endsWith(".mp3") } ?: emptyArray()
        val totalBytes = files.sumOf { it.length() }
        val mb = totalBytes / (1024 * 1024)
        "${files.size} songs • $mb MB"
    }

    override suspend fun clearCache(): Unit = withContext(Dispatchers.IO) {
        mutex.withLock {
            val files = audioCacheDir.listFiles { _, name -> name.endsWith(".m4a") || name.endsWith(".mp3") || name.endsWith(".tmp") } ?: emptyArray()
            files.forEach { it.delete() }
            cachedDownloadedTracks.clear()
            saveCachedSongsToJson()
        }
    }

    private fun saveCachedSongsToJson() {
        try {
            val json = tracksToJson(cachedDownloadedTracks.values.toList())
            cachedSongsJsonFile.writeText(json)
        } catch (e: Exception) {
            println("[DesktopRepo] Error writing cached_songs.json: ${e.message}")
        }
    }

    private fun saveLikedSongsToJson() {
        try {
            val json = tracksToJson(cachedLikedSongs.values.toList())
            likedSongsJsonFile.writeText(json)
        } catch (e: Exception) {
            println("[DesktopRepo] Error writing liked_songs.json: ${e.message}")
        }
    }

    private fun saveHistoryToJson() {
        try {
            val json = tracksToJson(cachedHistory)
            historyJsonFile.writeText(json)
        } catch (e: Exception) {
            println("[DesktopRepo] Error writing listening_history.json: ${e.message}")
        }
    }

    private fun saveStatsToJson() {
        try {
            val sb = StringBuilder()
            val allIds = (playCounts.keys + completionCounts.keys + skipCounts.keys).distinct()
            for (id in allIds) {
                val p = playCounts[id] ?: 0
                val c = completionCounts[id] ?: 0
                val s = skipCounts[id] ?: 0
                sb.append("$id\t$p\t$c\t$s\n")
            }
            statsJsonFile.writeText(sb.toString())
        } catch (e: Exception) {
            println("[DesktopRepo] Error writing stats.json: ${e.message}")
        }
    }

    private fun parseDurationToSeconds(duration: String): Long {
        val parts = duration.split(":")
        return if (parts.size == 2) {
            val m = parts[0].toLongOrNull() ?: 0L
            val s = parts[1].toLongOrNull() ?: 0L
            m * 60 + s
        } else 0L
    }

    private fun tracksToJson(tracks: List<DisplayTrack>): String {
        val sb = StringBuilder()
        sb.append("[\n")
        tracks.forEachIndexed { i, t ->
            val thumb = t.thumbnailUrl
            val alb = t.album
            sb.append("  {")
            sb.append("\"id\":\"${escapeJson(t.id)}\",")
            sb.append("\"title\":\"${escapeJson(t.title)}\",")
            sb.append("\"artist\":\"${escapeJson(t.artist)}\",")
            sb.append("\"duration\":\"${escapeJson(t.duration)}\",")
            sb.append("\"thumbnailUrl\":${if (thumb != null) "\"${escapeJson(thumb)}\"" else "null"},")
            sb.append("\"album\":${if (alb != null) "\"${escapeJson(alb)}\"" else "null"},")
            sb.append("\"isVideo\":${t.isVideo}")
            sb.append("}")
            if (i < tracks.lastIndex) sb.append(",")
            sb.append("\n")
        }
        sb.append("]")
        return sb.toString()
    }

    private fun parseTracksJson(json: String): List<DisplayTrack> {
        val result = mutableListOf<DisplayTrack>()
        if (json.isBlank() || !json.contains("{")) return result

        val objectPattern = Regex("\\{([^}]+)\\}")
        val fieldPattern = Regex("\"([a-zA-Z0-9]+)\"\\s*:\\s*(\"[^\"]*\"|true|false|null|[0-9]+)")

        for (match in objectPattern.findAll(json)) {
            val body = match.groupValues[1]
            var id = ""
            var title = ""
            var artist = ""
            var duration = ""
            var thumbnailUrl: String? = null
            var album: String? = null
            var isVideo = false

            for (fieldMatch in fieldPattern.findAll(body)) {
                val key = fieldMatch.groupValues[1]
                val rawVal = fieldMatch.groupValues[2]
                val value = if (rawVal.startsWith("\"") && rawVal.endsWith("\"")) {
                    unescapeJson(rawVal.substring(1, rawVal.length - 1))
                } else rawVal

                when (key) {
                    "id" -> id = value
                    "title" -> title = value
                    "artist" -> artist = value
                    "duration" -> duration = value
                    "thumbnailUrl" -> if (value != "null") thumbnailUrl = value
                    "album" -> if (value != "null") album = value
                    "isVideo" -> isVideo = value.toBoolean()
                }
            }

            if (id.isNotBlank() && title.isNotBlank()) {
                result.add(
                    DisplayTrack(
                        id = id,
                        title = title,
                        artist = artist,
                        duration = duration,
                        thumbnailUrl = thumbnailUrl,
                        album = album,
                        isVideo = isVideo
                    )
                )
            }
        }
        return result
    }

    private fun escapeJson(s: String): String {
        return s.replace("\\", "\\\\")
            .replace("\"", "\\\"")
            .replace("\b", "\\b")
            .replace("\n", "\\n")
            .replace("\r", "\\r")
            .replace("\t", "\\t")
    }

    private fun unescapeJson(s: String): String {
        return s.replace("\\\"", "\"")
            .replace("\\\\", "\\")
            .replace("\\n", "\n")
            .replace("\\r", "\r")
            .replace("\\t", "\t")
    }
    override suspend fun getCustomPlaylists(): List<com.music.echo.sharedui.screens.DisplayPlaylist> {
        return customPlaylists.values.map { it.first }
    }

    override suspend fun getCustomPlaylistSongs(playlistId: String): List<DisplayTrack> {
        return customPlaylists[playlistId]?.second ?: emptyList()
    }

    override suspend fun saveCustomPlaylist(
        playlist: com.music.echo.sharedui.screens.DisplayPlaylist,
        songs: List<DisplayTrack>
    ) {
        customPlaylists[playlist.id] = playlist to songs
    }
}
