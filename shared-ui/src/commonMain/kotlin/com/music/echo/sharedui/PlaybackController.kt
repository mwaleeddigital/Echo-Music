package com.music.echo.sharedui

import com.music.echo.sharedui.screens.DisplayPlaylist
import com.music.echo.sharedui.screens.DisplayTrack
import com.music.innertube.YouTube
import com.music.innertube.models.WatchEndpoint
import com.music.innertube.models.YouTubeClient
import com.music.innertube.pages.NewPipeExtractor
import echo.music.iad1tya.playback.AudioItem
import echo.music.iad1tya.playback.AudioPlayer
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.launch

data class AudioStreamResolution(
    val streamUrl: String,
    val durationMs: Long,
    val artworkUrl: String?
)

class PlaybackController(
    private val audioPlayer: AudioPlayer,
    private val localRepository: com.music.echo.sharedui.data.LocalMusicRepository? = null,
    private val onTrackStarted: ((DisplayTrack) -> Unit)? = null,
    private val onTrackCompleted: ((DisplayTrack) -> Unit)? = null,
    private val onTrackSkipped: ((DisplayTrack) -> Unit)? = null
) {
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Default)
    private val streamCache = mutableMapOf<String, AudioStreamResolution>()
    private var radioJob: Job? = null
    private var preBufferJob: Job? = null
    private var queuedRelativeItems = mutableListOf<AudioItem>()
    private var preBufferedTrackId: String? = null

    private var activeTrack: DisplayTrack? = null
    private var hasReportedCompletion = false

    private val _queueTracks = MutableStateFlow<List<DisplayTrack>>(emptyList())
    val queueTracks: StateFlow<List<DisplayTrack>> = _queueTracks.asStateFlow()

    init {
        audioPlayer.setStreamResolver { videoId ->
            resolveAudioStream(videoId)?.streamUrl
        }

        // Monitor playback position to pre-buffer the next song and track completion
        scope.launch {
            combine(
                audioPlayer.currentPositionMs,
                audioPlayer.durationMs,
                audioPlayer.currentItem
            ) { pos, dur, item ->
                Triple(pos, dur, item)
            }.collect { (pos, dur, item) ->
                if (item != null && dur > 15000L) {
                    val remainingMs = dur - pos
                    if (remainingMs in 1L..15000L) {
                        // We are within 15 seconds of the song ending
                        preBufferNextSong()
                    }
                }

                // Check completion threshold (>60% of duration)
                if (item != null && dur > 0 && !hasReportedCompletion) {
                    val fraction = pos.toFloat() / dur.toFloat()
                    if (fraction >= 0.6f) {
                        hasReportedCompletion = true
                        activeTrack?.let { onTrackCompleted?.invoke(it) }
                    }
                }
            }
        }

        // Update queue tracks when current item advances
        scope.launch {
            audioPlayer.currentItem.collect { current ->
                if (current != null) {
                    val list = _queueTracks.value
                    val idx = list.indexOfFirst { it.id == current.id }
                    if (idx >= 0) {
                        _queueTracks.value = list.drop(idx + 1)
                    }
                }
            }
        }
    }

    val isPlaying = audioPlayer.isPlaying
    val currentPositionMs = audioPlayer.currentPositionMs
    val currentItem = audioPlayer.currentItem

    private suspend fun resolveAudioStream(videoId: String): AudioStreamResolution? {
        val cached = streamCache[videoId]
        if (cached != null) return cached

        // 0. Check local disk cache first (Instant offline & storage playback)
        val localAudioUri = localRepository?.getCachedAudioUri(videoId)
        if (localAudioUri != null) {
            println("[PlaybackController] Playing track from local disk cache: $videoId ($localAudioUri)")
            val res = AudioStreamResolution(
                streamUrl = localAudioUri,
                durationMs = 0L,
                artworkUrl = null
            )
            streamCache[videoId] = res
            return res
        }

        // 1. First priority: NewPipeExtractor direct extraction (deobfuscated, whole-file streams)
        try {
            val streams = NewPipeExtractor.newPipePlayer(videoId)
            val aacStream = streams.find { it.first == 140 }
                ?: streams.find { it.first == 141 }
                ?: streams.find { it.first == 139 }
                ?: streams.firstOrNull()

            if (aacStream != null && aacStream.second.isNotBlank()) {
                println("[PlaybackController] NewPipeExtractor successfully resolved stream: itag=${aacStream.first}")
                val res = AudioStreamResolution(
                    streamUrl = aacStream.second,
                    durationMs = 0L,
                    artworkUrl = null
                )
                streamCache[videoId] = res
                activeTrack?.let { if (it.id == videoId) localRepository?.cacheAudioStream(it, aacStream.second) }
                return res
            }
        } catch (e: Exception) {
            println("[PlaybackController] NewPipeExtractor failed for $videoId: ${e.message}")
        }

        // 2. Ensure visitorData is initialized for InnerTube clients
        if (YouTube.visitorData.isNullOrBlank()) {
            YouTube.refreshVisitorData()
        }

        // 3. InnerTube clients in order of whole-file capability
        val clients = listOf(
            YouTubeClient.VISIONOS,
            YouTubeClient.ANDROID_VR_1_65_10,
            YouTubeClient.TVHTML5,
            YouTubeClient.WEB_REMIX,
            YouTubeClient.IOS
        )

        for (client in clients) {
            try {
                val result = YouTube.player(videoId = videoId, client = client)
                if (result.isSuccess) {
                    val response = result.getOrNull()
                    val streamingData = response?.streamingData
                    if (streamingData != null) {
                        val formats = (streamingData.adaptiveFormats.orEmpty() + streamingData.formats.orEmpty())

                        val audioFormats = formats.filter { format ->
                            (format.mimeType.startsWith("audio/") || format.itag in listOf(140, 141, 139, 251, 250, 249)) &&
                            !format.mimeType.startsWith("video/")
                        }

                        val aacAudioFormats = audioFormats.filter {
                            it.itag == 140 || it.itag == 141 || it.itag == 139 ||
                            it.mimeType.startsWith("audio/mp4") || it.mimeType.contains("mp4a")
                        }

                        val candidates = (aacAudioFormats.sortedByDescending { it.bitrate } +
                                         audioFormats.sortedByDescending { it.bitrate }).distinctBy { it.itag }

                        for (format in candidates) {
                            val streamUrl = format.url ?: NewPipeExtractor.getStreamUrl(format, videoId)
                            if (!streamUrl.isNullOrBlank()) {
                                println("[PlaybackController] Selected stream: itag=${format.itag}, mime=${format.mimeType}, bitrate=${format.bitrate}, client=${client.clientName}")
                                val durationMs = (response.videoDetails?.lengthSeconds?.toLongOrNull() ?: 0L) * 1000L
                                val artworkUrl = response.videoDetails?.thumbnail?.thumbnails?.lastOrNull()?.url
                                val res = AudioStreamResolution(
                                    streamUrl = streamUrl,
                                    durationMs = durationMs,
                                    artworkUrl = artworkUrl
                                )
                                streamCache[videoId] = res
                                activeTrack?.let { if (it.id == videoId) localRepository?.cacheAudioStream(it, streamUrl) }
                                return res
                            }
                        }
                    }
                }
            } catch (e: Exception) {
                println("[PlaybackController] Client ${client.clientName} failed for $videoId: ${e.message}")
            }
        }

        return null
    }

    private fun preBufferNextSong() {
        val nextSong = queuedRelativeItems.firstOrNull { it.id != currentItem.value?.id } ?: return
        if (preBufferedTrackId == nextSong.id) return // Already prepared

        preBufferedTrackId = nextSong.id
        preBufferJob?.cancel()
        preBufferJob = scope.launch(Dispatchers.Default) {
            println("[PlaybackController] Auto-preparing next song 15s ahead: ${nextSong.title} (${nextSong.id})")
            val resolution = resolveAudioStream(nextSong.id)
            if (resolution != null) {
                println("[PlaybackController] Successfully prepared next song: ${nextSong.title}")
            }
        }
    }

    fun playTrack(track: DisplayTrack) {
        radioJob?.cancel()
        preBufferJob?.cancel()
        preBufferedTrackId = null
        queuedRelativeItems.clear()

        activeTrack = track
        hasReportedCompletion = false
        onTrackStarted?.invoke(track)

        scope.launch {
            val resolution = resolveAudioStream(track.id)
            if (resolution != null) {
                val audioItem = AudioItem(
                    id = track.id,
                    title = track.title,
                    artist = track.artist,
                    album = null,
                    durationMs = if (resolution.durationMs > 0) resolution.durationMs else 0L,
                    sourceUrl = resolution.streamUrl,
                    artworkUrl = resolution.artworkUrl ?: track.thumbnailUrl
                )
                audioPlayer.playItem(audioItem)

                // Automatic continuation: fetch relative songs queue (radio) for the played track
                fetchAndQueueRelativeSongs(track.id)
            } else {
                println("Failed to resolve stream for track: ${track.title} (${track.id})")
            }
        }
    }

    private fun fetchAndQueueRelativeSongs(seedVideoId: String) {
        radioJob = scope.launch(Dispatchers.Default) {
            try {
                YouTube.next(WatchEndpoint(videoId = seedVideoId)).onSuccess { nextResult ->
                    val relativeSongs = nextResult.items.filter { it.id != seedVideoId }
                    if (relativeSongs.isNotEmpty()) {
                        val audioItems = relativeSongs.map { song ->
                            AudioItem(
                                id = song.id,
                                title = song.title,
                                artist = song.artists.joinToString(", ") { it.name },
                                album = song.album?.name,
                                durationMs = (song.duration?.toLong() ?: 0L) * 1000L,
                                sourceUrl = null,
                                artworkUrl = song.thumbnail
                            )
                        }

                        queuedRelativeItems.clear()
                        queuedRelativeItems.addAll(audioItems)
                        audioPlayer.enqueue(audioItems)

                        val displayQueue = relativeSongs.map { song ->
                            val durationText = song.duration?.let { sec ->
                                val m = sec / 60
                                val s = sec % 60
                                "$m:${s.toString().padStart(2, '0')}"
                            } ?: ""
                            DisplayTrack(
                                id = song.id,
                                title = song.title,
                                artist = song.artists.joinToString(", ") { it.name },
                                duration = durationText,
                                thumbnailUrl = song.thumbnail,
                                album = song.album?.name,
                                isVideo = song.isVideoSong
                            )
                        }
                        _queueTracks.value = displayQueue

                        println("[PlaybackController] Enqueued ${audioItems.size} relative songs for continuous playback")

                        // Pre-resolve first relative song ahead of time
                        audioItems.firstOrNull()?.let { firstRelative ->
                            launch {
                                resolveAudioStream(firstRelative.id)
                                preBufferedTrackId = firstRelative.id
                            }
                        }
                    }
                }.onFailure {
                    it.printStackTrace()
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    fun playTracks(tracks: List<DisplayTrack>, startIndex: Int = 0) {
        if (tracks.isEmpty()) return
        radioJob?.cancel()
        preBufferJob?.cancel()
        preBufferedTrackId = null
        queuedRelativeItems.clear()

        val safeIndex = startIndex.coerceIn(0, tracks.lastIndex)
        val selectedTrack = tracks[safeIndex]

        scope.launch {
            val resolution = resolveAudioStream(selectedTrack.id)
            if (resolution != null) {
                val audioItems = tracks.mapIndexed { index, track ->
                    AudioItem(
                        id = track.id,
                        title = track.title,
                        artist = track.artist,
                        album = track.album,
                        durationMs = if (index == safeIndex && resolution.durationMs > 0) resolution.durationMs else 0L,
                        sourceUrl = if (index == safeIndex) resolution.streamUrl else null,
                        artworkUrl = track.thumbnailUrl
                    )
                }
                queuedRelativeItems.addAll(audioItems.drop(safeIndex + 1))
                audioPlayer.playItems(audioItems, safeIndex)
                _queueTracks.value = tracks.drop(safeIndex + 1)
            }
        }
    }

    fun playPlaylist(playlist: DisplayPlaylist, songsFallback: List<DisplayTrack> = emptyList()) {
        if (playlist.id == "liked_songs" || playlist.id.startsWith("local_")) {
            if (songsFallback.isNotEmpty()) {
                playTracks(songsFallback, 0)
            }
            return
        }

        radioJob?.cancel()
        preBufferJob?.cancel()
        preBufferedTrackId = null
        queuedRelativeItems.clear()

        scope.launch {
            YouTube.playlist(playlist.id).onSuccess { playlistPage ->
                val songs = playlistPage.songs
                if (songs.isNotEmpty()) {
                    val firstSong = songs.first()
                    val resolution = resolveAudioStream(firstSong.id)
                    if (resolution != null) {
                        val audioItems = songs.mapIndexed { index, song ->
                            AudioItem(
                                id = song.id,
                                title = song.title,
                                artist = song.artists.joinToString(", ") { it.name },
                                album = song.album?.name,
                                durationMs = (song.duration?.toLong() ?: 0L) * 1000L,
                                sourceUrl = if (index == 0) resolution.streamUrl else null,
                                artworkUrl = song.thumbnail
                            )
                        }
                        queuedRelativeItems.addAll(audioItems.drop(1))
                        audioPlayer.playItems(audioItems, 0)

                        val displayQueue = songs.drop(1).map { song ->
                            val durationText = song.duration?.let { sec ->
                                val m = sec / 60
                                val s = sec % 60
                                "$m:${s.toString().padStart(2, '0')}"
                            } ?: ""
                            DisplayTrack(
                                id = song.id,
                                title = song.title,
                                artist = song.artists.joinToString(", ") { it.name },
                                duration = durationText,
                                thumbnailUrl = song.thumbnail,
                                album = song.album?.name,
                                isVideo = song.isVideoSong
                            )
                        }
                        _queueTracks.value = displayQueue

                        // Pre-resolve second item if available
                        audioItems.getOrNull(1)?.let { secondSong ->
                            launch { resolveAudioStream(secondSong.id) }
                        }
                    }
                }
            }.onFailure {
                if (songsFallback.isNotEmpty()) {
                    playTracks(songsFallback, 0)
                } else {
                    it.printStackTrace()
                }
            }
        }
    }

    fun addToQueue(track: DisplayTrack) {
        val current = _queueTracks.value.toMutableList()
        current.add(track)
        _queueTracks.value = current

        val audioItem = AudioItem(
            id = track.id,
            title = track.title,
            artist = track.artist,
            album = track.album,
            durationMs = 0L,
            sourceUrl = null,
            artworkUrl = track.thumbnailUrl
        )
        queuedRelativeItems.add(audioItem)
        audioPlayer.enqueue(audioItem)
    }

    fun removeFromQueue(track: DisplayTrack) {
        val current = _queueTracks.value.toMutableList()
        current.removeAll { it.id == track.id }
        _queueTracks.value = current
        queuedRelativeItems.removeAll { it.id == track.id }
    }

    fun playPause() {
        audioPlayer.togglePlayPause()
    }

    fun next() {
        val pos = audioPlayer.currentPositionMs.value
        val dur = audioPlayer.durationMs.value
        if (pos in 1L..25000L && dur > 30000L && !hasReportedCompletion) {
            activeTrack?.let { onTrackSkipped?.invoke(it) }
        }
        audioPlayer.skipToNext()
    }

    fun previous() {
        audioPlayer.skipToPrevious()
    }

    fun seekTo(positionMs: Long) {
        audioPlayer.seekTo(positionMs)
    }

    fun setVolume(volume: Float) {
        audioPlayer.setVolume(volume.coerceIn(0f, 1f))
    }

    fun setShuffle(enabled: Boolean) {
        audioPlayer.setShuffleModeEnabled(enabled)
    }

    fun setRepeatMode(mode: echo.music.iad1tya.playback.RepeatMode) {
        audioPlayer.setRepeatMode(mode)
    }
}
