package com.music.innertube.pages

import com.music.innertube.models.response.PlayerResponse

actual object NewPipeExtractor {
    actual fun init() {}
    actual fun getSignatureTimestamp(videoId: String): Result<Int> = Result.failure(Exception("Not implemented on Web"))
    actual fun getStreamUrl(format: PlayerResponse.StreamingData.Format, videoId: String): String? = null
    actual fun newPipePlayer(videoId: String): List<Pair<Int, String>> = emptyList()
}
