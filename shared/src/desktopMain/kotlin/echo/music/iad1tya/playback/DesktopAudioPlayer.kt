package echo.music.iad1tya.playback

import javafx.application.Platform
import javafx.scene.media.Media
import javafx.scene.media.MediaPlayer
import javafx.util.Duration
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch

class DesktopAudioPlayer : AudioPlayer {

    init {
        try {
            Platform.startup {
                try {
                    Platform.setImplicitExit(false)
                } catch (_: Exception) {}
            }
        } catch (e: IllegalStateException) {
            try {
                Platform.setImplicitExit(false)
            } catch (_: Exception) {}
        }
    }

    private val scope = CoroutineScope(Dispatchers.Default)
    private var progressJob: Job? = null
    private var loadJob: Job? = null
    private var mediaPlayer: MediaPlayer? = null
    private var currentTempFile: java.io.File? = null
    private var streamResolver: (suspend (String) -> String?)? = null

    private val _playbackState = MutableStateFlow(PlaybackState.IDLE)
    override val playbackState: StateFlow<PlaybackState> = _playbackState.asStateFlow()

    private val _isPlaying = MutableStateFlow(false)
    override val isPlaying: StateFlow<Boolean> = _isPlaying.asStateFlow()

    private val _currentItem = MutableStateFlow<AudioItem?>(null)
    override val currentItem: StateFlow<AudioItem?> = _currentItem.asStateFlow()

    private val _currentPositionMs = MutableStateFlow(0L)
    override val currentPositionMs: StateFlow<Long> = _currentPositionMs.asStateFlow()

    private val _durationMs = MutableStateFlow(0L)
    override val durationMs: StateFlow<Long> = _durationMs.asStateFlow()

    private val _shuffleModeEnabled = MutableStateFlow(false)
    override val shuffleModeEnabled: StateFlow<Boolean> = _shuffleModeEnabled.asStateFlow()

    private val _repeatMode = MutableStateFlow(RepeatMode.OFF)
    override val repeatMode: StateFlow<RepeatMode> = _repeatMode.asStateFlow()

    private val _error = MutableStateFlow<String?>(null)
    override val error: StateFlow<String?> = _error.asStateFlow()

    private val queue = mutableListOf<AudioItem>()
    private var currentIndex: Int = -1
    private var currentVolume = 1.0f

    override fun setStreamResolver(resolver: suspend (String) -> String?) {
        this.streamResolver = resolver
    }

    private fun startProgressTracker() {
        progressJob?.cancel()
        progressJob = scope.launch {
            while (isActive) {
                Platform.runLater {
                    mediaPlayer?.let { player ->
                        _currentPositionMs.value = player.currentTime.toMillis().toLong()
                    }
                }
                delay(200)
            }
        }
    }

    private fun handleTrackEnded() {
        _isPlaying.value = false
        _playbackState.value = PlaybackState.ENDED
        when (_repeatMode.value) {
            RepeatMode.ONE -> {
                seekTo(0)
                play()
            }
            RepeatMode.ALL -> {
                if (currentIndex + 1 < queue.size) {
                    skipToNext()
                } else if (queue.isNotEmpty()) {
                    currentIndex = 0
                    loadAndPlay(queue[0])
                }
            }
            RepeatMode.OFF -> {
                if (currentIndex + 1 < queue.size) {
                    skipToNext()
                }
            }
        }
    }

    private fun getUserAgentForUrl(url: String): String {
        return when {
            url.contains("c=IOS") || url.contains("ios") -> "com.google.ios.youtube/21.03.1 (iPhone16,2; U; CPU iOS 18_2 like Mac OS X;)"
            url.contains("c=ANDROID_VR") || url.contains("oculus") -> "com.google.android.apps.youtube.vr.oculus/1.65.10 (Linux; U; Android 12L; eureka-user Build/SQ3A.220605.009.A1) gzip"
            url.contains("c=ANDROID") -> "com.google.android.youtube/21.03.38 (Linux; U; Android 14) gzip"
            url.contains("c=TVHTML5") -> "Mozilla/5.0(SMART-TV; Linux; Tizen 4.0.0.2) AppleWebkit/605.1.15 (KHTML, like Gecko) SamsungBrowser/9.2 TV Safari/605.1.15"
            url.contains("c=VISIONOS") -> "Mozilla/5.0 (Macintosh; Intel Mac OS X 10_15_7) AppleWebKit/605.1.15 (KHTML, like Gecko) Version/18.0 Safari/605.1.15"
            else -> "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/130.0.0.0 Safari/537.36"
        }
    }

    private fun loadAndPlay(item: AudioItem) {
        loadJob?.cancel()
        _currentItem.value = item
        _currentPositionMs.value = 0L
        _durationMs.value = if (item.durationMs > 0) item.durationMs else 0L
        _playbackState.value = PlaybackState.BUFFERING
        _error.value = null

        loadJob = scope.launch(Dispatchers.IO) {
            try {
                var url = item.sourceUrl
                if (url.isNullOrBlank() && streamResolver != null) {
                    println("[DesktopAudioPlayer] Resolving stream URL for track: ${item.title} (${item.id})...")
                    url = streamResolver?.invoke(item.id)
                }

                if (url.isNullOrBlank()) {
                    _error.value = "No playable source URL for track: ${item.title}"
                    _playbackState.value = PlaybackState.ERROR
                    return@launch
                }

                val streamUrl = url.trim()
                println("[DesktopAudioPlayer] Starting playback for track: ${item.title}")

                fun playMediaUri(uri: String, onFail: (() -> Unit)? = null) {
                    Platform.runLater {
                        try {
                            mediaPlayer?.stop()
                            mediaPlayer?.dispose()

                            val media = Media(uri)
                            val player = MediaPlayer(media)
                            player.volume = currentVolume.toDouble()

                            player.setOnReady {
                                val duration = media.duration.toMillis().toLong()
                                if (duration > 0) {
                                    _durationMs.value = duration
                                }
                                player.play()
                                _isPlaying.value = true
                                _playbackState.value = PlaybackState.READY
                                _error.value = null
                                println("[DesktopAudioPlayer] Media ready and playing! Duration: ${_durationMs.value}ms")
                                startProgressTracker()
                            }

                            player.setOnEndOfMedia {
                                handleTrackEnded()
                            }

                            player.setOnError {
                                val err = player.error?.message ?: "Unknown error"
                                println("[DesktopAudioPlayer] JavaFX Media Error on $uri: $err")
                                try {
                                    java.io.File(System.getProperty("user.home"), "Desktop/EchoMusic_Debug.txt").appendText("JavaFX Media Error: $err\n")
                                } catch (e: Exception) {}
                                if (onFail != null) {
                                    onFail()
                                } else {
                                    _error.value = "Playback error: $err"
                                    _playbackState.value = PlaybackState.ERROR
                                }
                            }

                            mediaPlayer = player
                        } catch (e: Exception) {
                            println("[DesktopAudioPlayer] Failed to instantiate Media with $uri: ${e.message}")
                            try {
                                java.io.File(System.getProperty("user.home"), "Desktop/EchoMusic_Debug.txt").appendText("Media Init Error: ${e.message}\n")
                            } catch (ex: Exception) {}
                            if (onFail != null) {
                                onFail()
                            } else {
                                _error.value = "Failed to create MediaPlayer: ${e.message}"
                                _playbackState.value = PlaybackState.ERROR
                                e.printStackTrace()
                            }
                        }
                    }
                }

                val audioCacheDir = java.io.File(System.getProperty("user.home"), ".echo_music/audio_cache").apply {
                    if (!exists()) mkdirs()
                }
                val sanitizedId = item.id.filter { it.isLetterOrDigit() || it == '-' || it == '_' }.ifEmpty { "track" }
                val persistentCacheFile = java.io.File(audioCacheDir, "$sanitizedId.m4a")

                if (persistentCacheFile.exists() && persistentCacheFile.length() > 32768L) {
                    println("[DesktopAudioPlayer] Playing track from local storage cache: ${item.title} (${persistentCacheFile.length() / 1024} KB)")
                    playMediaUri(persistentCacheFile.toURI().toString(), onFail = null)
                    return@launch
                }

                // Strategy 1: Attempt direct URI streaming (starts in ~300ms)
                // Strategy 2: If direct URI fails with a media error, download to local persistent cache and play
                playMediaUri(streamUrl, onFail = {
                    scope.launch(Dispatchers.IO) {
                        try {
                            println("[DesktopAudioPlayer] Direct streaming failed. Downloading audio stream to persistent cache...")
                            val userAgent = getUserAgentForUrl(streamUrl)
                            val client = java.net.http.HttpClient.newBuilder()
                                .followRedirects(java.net.http.HttpClient.Redirect.ALWAYS)
                                .connectTimeout(java.time.Duration.ofSeconds(15))
                                .build()

                            val request = java.net.http.HttpRequest.newBuilder()
                                .uri(java.net.URI.create(streamUrl))
                                .header("User-Agent", userAgent)
                                .header("Accept", "*/*")
                                .header("Accept-Encoding", "identity;q=1, *;q=0")
                                .GET()
                                .build()

                            val tempFile = java.io.File(audioCacheDir, "${sanitizedId}.m4a.tmp")
                            val response = client.send(request, java.net.http.HttpResponse.BodyHandlers.ofFile(tempFile.toPath()))
                            if (response.statusCode() in 200..299 && tempFile.length() > 32768L) {
                                if (persistentCacheFile.exists()) persistentCacheFile.delete()
                                val renamed = tempFile.renameTo(persistentCacheFile)
                                val playFile = if (renamed) persistentCacheFile else tempFile
                                playMediaUri(playFile.toURI().toString(), onFail = null)
                            } else {
                                tempFile.delete()
                                _error.value = "Failed to download stream: HTTP ${response.statusCode()}"
                                _playbackState.value = PlaybackState.ERROR
                                try {
                                    java.io.File(System.getProperty("user.home"), "Desktop/EchoMusic_Debug.txt").appendText("Download Error: HTTP ${response.statusCode()}\n")
                                } catch (e: Exception) {}
                            }
                        } catch (e: Exception) {
                            _error.value = e.message
                            _playbackState.value = PlaybackState.ERROR
                            try {
                                java.io.File(System.getProperty("user.home"), "Desktop/EchoMusic_Debug.txt").appendText("Download Exception: ${e.message}\n")
                            } catch (ex: Exception) {}
                        }
                    }
                })

            } catch (e: kotlinx.coroutines.CancellationException) {
                throw e
            } catch (e: Exception) {
                if (!isActive) return@launch
                System.err.println("[DesktopAudioPlayer] Playback error: ${e.message}")
                e.printStackTrace()
                _error.value = e.message
                _playbackState.value = PlaybackState.ERROR
                try {
                    java.io.File(System.getProperty("user.home"), "Desktop/EchoMusic_Debug.txt").appendText("General Exception: ${e.message}\n")
                } catch (ex: Exception) {}
            }
        }
    }

    override fun play() {
        Platform.runLater {
            mediaPlayer?.play()
            _isPlaying.value = true
            _playbackState.value = PlaybackState.READY
            startProgressTracker()
        }
    }

    override fun pause() {
        Platform.runLater {
            mediaPlayer?.pause()
            _isPlaying.value = false
            progressJob?.cancel()
        }
    }

    override fun togglePlayPause() {
        if (_isPlaying.value) {
            pause()
        } else {
            play()
        }
    }

    override fun seekTo(positionMs: Long) {
        Platform.runLater {
            mediaPlayer?.seek(Duration.millis(positionMs.toDouble()))
            _currentPositionMs.value = positionMs
        }
    }

    override fun skipToNext() {
        if (queue.isEmpty()) return
        if (_shuffleModeEnabled.value && queue.size > 1) {
            val nextIndex = (0 until queue.size).filter { it != currentIndex }.random()
            currentIndex = nextIndex
            loadAndPlay(queue[currentIndex])
        } else if (currentIndex + 1 < queue.size) {
            currentIndex++
            loadAndPlay(queue[currentIndex])
        } else if (_repeatMode.value == RepeatMode.ALL) {
            currentIndex = 0
            loadAndPlay(queue[0])
        }
    }

    override fun skipToPrevious() {
        if (queue.isEmpty()) return
        if (_currentPositionMs.value > 3000) {
            seekTo(0)
            return
        }
        if (currentIndex > 0) {
            currentIndex--
            loadAndPlay(queue[currentIndex])
        } else {
            seekTo(0)
        }
    }

    override fun playItem(item: AudioItem) {
        queue.clear()
        queue.add(item)
        currentIndex = 0
        loadAndPlay(item)
    }

    override fun playItems(items: List<AudioItem>, startIndex: Int) {
        if (items.isEmpty()) return
        queue.clear()
        queue.addAll(items)
        currentIndex = startIndex.coerceIn(0, items.lastIndex)
        loadAndPlay(queue[currentIndex])
    }

    override fun enqueueNext(item: AudioItem) {
        if (currentIndex in 0..queue.lastIndex) {
            queue.add(currentIndex + 1, item)
        } else {
            enqueue(item)
        }
    }

    override fun enqueueNext(items: List<AudioItem>) {
        if (currentIndex in 0..queue.lastIndex) {
            queue.addAll(currentIndex + 1, items)
        } else {
            enqueue(items)
        }
    }

    override fun enqueue(item: AudioItem) {
        queue.add(item)
        if (_currentItem.value == null) {
            currentIndex = 0
            loadAndPlay(item)
        }
    }

    override fun enqueue(items: List<AudioItem>) {
        val wasEmpty = queue.isEmpty()
        queue.addAll(items)
        if (wasEmpty && items.isNotEmpty()) {
            currentIndex = 0
            loadAndPlay(items[0])
        }
    }

    override fun setShuffleModeEnabled(enabled: Boolean) {
        _shuffleModeEnabled.value = enabled
    }

    override fun setRepeatMode(mode: RepeatMode) {
        _repeatMode.value = mode
    }

    override fun setVolume(volume: Float) {
        currentVolume = volume.coerceIn(0f, 1f)
        Platform.runLater {
            mediaPlayer?.volume = currentVolume.toDouble()
        }
    }

    override fun release() {
        progressJob?.cancel()
        Platform.runLater {
            mediaPlayer?.stop()
            mediaPlayer?.dispose()
            mediaPlayer = null
        }
        queue.clear()
        currentIndex = -1
        _currentItem.value = null
        _isPlaying.value = false
        _playbackState.value = PlaybackState.IDLE
    }
}
