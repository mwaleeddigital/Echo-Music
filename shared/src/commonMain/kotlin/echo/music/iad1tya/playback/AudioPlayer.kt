package echo.music.iad1tya.playback

import kotlinx.coroutines.flow.StateFlow

data class AudioItem(
    val id: String,
    val title: String,
    val artist: String,
    val album: String?,
    val durationMs: Long,
    val sourceUrl: String?,
    val artworkUrl: String?
)

enum class PlaybackState {
    IDLE,
    BUFFERING,
    READY,
    ENDED,
    ERROR
}

enum class RepeatMode {
    OFF,
    ONE,
    ALL
}

interface AudioPlayer {
    // State
    val playbackState: StateFlow<PlaybackState>
    val isPlaying: StateFlow<Boolean>
    val currentItem: StateFlow<AudioItem?>
    val currentPositionMs: StateFlow<Long>
    val durationMs: StateFlow<Long>
    
    val shuffleModeEnabled: StateFlow<Boolean>
    val repeatMode: StateFlow<RepeatMode>
    
    val error: StateFlow<String?>

    // Stream URL resolution callback (for on-demand lazy resolution)
    fun setStreamResolver(resolver: suspend (String) -> String?) {}

    // Actions
    fun play()
    fun pause()
    fun togglePlayPause()
    fun seekTo(positionMs: Long)
    fun skipToNext()
    fun skipToPrevious()
    
    // Queue Management
    fun playItem(item: AudioItem)
    fun playItems(items: List<AudioItem>, startIndex: Int = 0)
    fun enqueueNext(item: AudioItem)
    fun enqueueNext(items: List<AudioItem>)
    fun enqueue(item: AudioItem)
    fun enqueue(items: List<AudioItem>)
    
    // Options
    fun setShuffleModeEnabled(enabled: Boolean)
    fun setRepeatMode(mode: RepeatMode)
    fun setVolume(volume: Float)
    
    fun release()
}
