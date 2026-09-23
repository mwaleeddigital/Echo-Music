package echo.music.iad1tya.playback

import kotlinx.browser.document
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import org.w3c.dom.Audio
import org.w3c.dom.HTMLAudioElement
import org.w3c.dom.events.Event

class WebAudioPlayer : AudioPlayer {

    private val audio: HTMLAudioElement = Audio()
    private val scope = CoroutineScope(Dispatchers.Main)

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

    private val onTimeUpdate: (Event) -> Unit = {
        val currentSeconds = audio.currentTime
        _currentPositionMs.value = (currentSeconds * 1000).toLong()
    }

    private val onLoadedMetadata: (Event) -> Unit = {
        val dur = audio.duration
        if (!dur.isNaN() && dur > 0) {
            _durationMs.value = (dur * 1000).toLong()
        }
        _playbackState.value = PlaybackState.READY
    }

    private val onPlay: (Event) -> Unit = {
        _isPlaying.value = true
        _playbackState.value = PlaybackState.READY
        _error.value = null
    }

    private val onPause: (Event) -> Unit = {
        _isPlaying.value = false
    }

    private val onWaiting: (Event) -> Unit = {
        _playbackState.value = PlaybackState.BUFFERING
    }

    private val onPlaying: (Event) -> Unit = {
        _isPlaying.value = true
        _playbackState.value = PlaybackState.READY
    }

    private val onEnded: (Event) -> Unit = {
        _isPlaying.value = false
        _playbackState.value = PlaybackState.ENDED
        handleTrackEnded()
    }

    private val onError: (Event) -> Unit = {
        val err = audio.error
        val msg = if (err != null) "Audio error code: ${err.code}" else "Unknown playback error"
        _error.value = msg
        _playbackState.value = PlaybackState.ERROR
        _isPlaying.value = false
    }

    init {
        audio.addEventListener("timeupdate", onTimeUpdate)
        audio.addEventListener("loadedmetadata", onLoadedMetadata)
        audio.addEventListener("play", onPlay)
        audio.addEventListener("pause", onPause)
        audio.addEventListener("waiting", onWaiting)
        audio.addEventListener("playing", onPlaying)
        audio.addEventListener("ended", onEnded)
        audio.addEventListener("error", onError)
    }

    private fun handleTrackEnded() {
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

    private var streamResolver: (suspend (String) -> String?)? = null

    override fun setStreamResolver(resolver: suspend (String) -> String?) {
        this.streamResolver = resolver
    }

    private fun loadAndPlay(item: AudioItem) {
        _currentItem.value = item
        _currentPositionMs.value = 0L
        _durationMs.value = if (item.durationMs > 0) item.durationMs else 0L
        _playbackState.value = PlaybackState.BUFFERING

        scope.launch {
            var url = item.sourceUrl
            if (url.isNullOrBlank() && streamResolver != null) {
                url = streamResolver?.invoke(item.id)
            }

            if (!url.isNullOrBlank()) {
                audio.src = url
                audio.load()
                audio.play().catch { e ->
                    _error.value = "Playback failed: ${e.message}"
                    _playbackState.value = PlaybackState.ERROR
                }
            } else {
                _error.value = "No playable source URL for track: ${item.title}"
                _playbackState.value = PlaybackState.ERROR
            }
        }
    }

    override fun play() {
        if (_currentItem.value == null && queue.isNotEmpty()) {
            currentIndex = 0
            loadAndPlay(queue[0])
        } else {
            audio.play().catch { e ->
                _error.value = "Playback error: ${e.message}"
            }
        }
    }

    override fun pause() {
        audio.pause()
    }

    override fun togglePlayPause() {
        if (_isPlaying.value) {
            pause()
        } else {
            play()
        }
    }

    override fun seekTo(positionMs: Long) {
        val targetSeconds = positionMs / 1000.0
        audio.currentTime = targetSeconds
        _currentPositionMs.value = positionMs
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
        audio.volume = volume.coerceIn(0f, 1f).toDouble()
    }

    override fun release() {
        audio.pause()
        audio.removeEventListener("timeupdate", onTimeUpdate)
        audio.removeEventListener("loadedmetadata", onLoadedMetadata)
        audio.removeEventListener("play", onPlay)
        audio.removeEventListener("pause", onPause)
        audio.removeEventListener("waiting", onWaiting)
        audio.removeEventListener("playing", onPlaying)
        audio.removeEventListener("ended", onEnded)
        audio.removeEventListener("error", onError)
        queue.clear()
        currentIndex = -1
        _currentItem.value = null
        _isPlaying.value = false
        _playbackState.value = PlaybackState.IDLE
    }
}
