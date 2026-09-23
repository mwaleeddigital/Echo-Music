package echo.music.iad1tya.lyrics

interface LyricsProvider {
  val name: String

  fun isEnabled(): Boolean

  suspend fun getLyrics(
    id: String,
    title: String,
    artist: String,
    duration: Int,
    album: String? = null,
  ): Result<String>

  suspend fun getAllLyrics(
    id: String,
    title: String,
    artist: String,
    duration: Int,
    album: String? = null,
    callback: (String) -> Unit,
  ) {
    getLyrics(id, title, artist, duration, album).onSuccess(callback)
  }
}
