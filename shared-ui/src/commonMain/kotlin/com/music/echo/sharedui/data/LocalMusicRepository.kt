package com.music.echo.sharedui.data

import com.music.echo.sharedui.screens.DisplayTrack

data class UserTasteProfile(
    val likedSongIds: Set<String> = emptySet(),
    val artistAffinityScores: Map<String, Float> = emptyMap(),
    val topSeedTrackIds: List<String> = emptyList(),
    val skippedSongIds: Set<String> = emptySet(),
    val recentPlayedTrackIds: List<String> = emptyList()
)

interface LocalMusicRepository {
    suspend fun getLikedSongs(): List<DisplayTrack>
    suspend fun isSongLiked(trackId: String): Boolean
    suspend fun setSongLiked(track: DisplayTrack, liked: Boolean)
    
    suspend fun recordPlayEvent(track: DisplayTrack, playTimeMs: Long, completed: Boolean, skipped: Boolean)
    suspend fun getRecentHistory(limit: Int = 50): List<DisplayTrack>
    suspend fun getTopPlayedTracks(limit: Int = 20): List<DisplayTrack>
    suspend fun getUserTasteProfile(): UserTasteProfile
    
    // Custom Playlists
    suspend fun getCustomPlaylists(): List<com.music.echo.sharedui.screens.DisplayPlaylist> = emptyList()
    suspend fun getCustomPlaylistSongs(playlistId: String): List<DisplayTrack> = emptyList()
    suspend fun saveCustomPlaylist(playlist: com.music.echo.sharedui.screens.DisplayPlaylist, songs: List<DisplayTrack>) {}

    // Offline & Storage Audio Caching
    suspend fun getDownloadedSongs(): List<DisplayTrack> = emptyList()
    suspend fun getAutoCachedSongs(): List<DisplayTrack> = emptyList()
    fun getCachedAudioUri(trackId: String): String? = null
    fun isSongCached(trackId: String): Boolean = false
    fun cacheAudioStream(track: DisplayTrack, streamUrl: String, isExplicitDownload: Boolean = false) {}
    suspend fun getCacheStorageStats(): String = "0 songs • 0 MB"
    suspend fun clearCache() {}
    suspend fun clearAutoCache() {}
}

class InMemoryLocalMusicRepository : LocalMusicRepository {
    private val likedSongsMap = mutableMapOf<String, DisplayTrack>()
    private val historyList = mutableListOf<DisplayTrack>()
    private val playCounts = mutableMapOf<String, Int>()
    private val completionCounts = mutableMapOf<String, Int>()
    private val skipCounts = mutableMapOf<String, Int>()
    
    private val customPlaylists = mutableMapOf<String, Pair<com.music.echo.sharedui.screens.DisplayPlaylist, List<DisplayTrack>>>()

    init {
        // Initial seeds
        val initialLiked = listOf(
            DisplayTrack("XXYlFuWEuKi", "Save Your Tears", "The Weeknd", "3:35", "https://i.ytimg.com/vi/XXYlFuWEuKi/maxresdefault.jpg", album = "After Hours"),
            DisplayTrack("4NRXx6U8ABQ", "Blinding Lights", "The Weeknd", "3:20", "https://i.ytimg.com/vi/4NRXx6U8ABQ/maxresdefault.jpg", album = "After Hours"),
            DisplayTrack("H5v3kku4y6Q", "As It Was", "Harry Styles", "2:47", "https://i.ytimg.com/vi/H5v3kku4y6Q/maxresdefault.jpg", album = "Harry's House"),
            DisplayTrack("5NV6Rdv1a3I", "Get Lucky", "Daft Punk ft. Pharrell Williams", "4:08", "https://i.ytimg.com/vi/5NV6Rdv1a3I/maxresdefault.jpg", album = "Random Access Memories")
        )
        initialLiked.forEach { likedSongsMap[it.id] = it }
    }

    override suspend fun getLikedSongs(): List<DisplayTrack> {
        return likedSongsMap.values.toList().reversed()
    }

    override suspend fun isSongLiked(trackId: String): Boolean {
        return likedSongsMap.containsKey(trackId)
    }

    override suspend fun setSongLiked(track: DisplayTrack, liked: Boolean) {
        if (liked) {
            likedSongsMap[track.id] = track
        } else {
            likedSongsMap.remove(track.id)
        }
    }

    override suspend fun recordPlayEvent(
        track: DisplayTrack,
        playTimeMs: Long,
        completed: Boolean,
        skipped: Boolean
    ) {
        historyList.removeAll { it.id == track.id }
        historyList.add(0, track)
        playCounts[track.id] = (playCounts[track.id] ?: 0) + 1
        if (completed) {
            completionCounts[track.id] = (completionCounts[track.id] ?: 0) + 1
        }
        if (skipped) {
            skipCounts[track.id] = (skipCounts[track.id] ?: 0) + 1
        }
    }

    override suspend fun getRecentHistory(limit: Int): List<DisplayTrack> {
        return historyList.take(limit)
    }

    override suspend fun getTopPlayedTracks(limit: Int): List<DisplayTrack> {
        val allTracks = (likedSongsMap.values + historyList).distinctBy { it.id }
        return allTracks.sortedByDescending { playCounts[it.id] ?: 0 }.take(limit)
    }

    override suspend fun getUserTasteProfile(): UserTasteProfile {
        val artistScores = mutableMapOf<String, Float>()

        // 1. Liked songs contribute +4.0 to artist affinity
        for (song in likedSongsMap.values) {
            val artistKey = song.artist.trim().lowercase()
            artistScores[artistKey] = (artistScores[artistKey] ?: 0f) + 4.0f
        }

        // 2. Play events and completions contribute +1.5 and +2.0
        for ((songId, count) in playCounts) {
            val song = likedSongsMap[songId] ?: historyList.find { it.id == songId } ?: continue
            val artistKey = song.artist.trim().lowercase()
            val completions = completionCounts[songId] ?: 0
            val skips = skipCounts[songId] ?: 0
            artistScores[artistKey] = (artistScores[artistKey] ?: 0f) + (count * 1.5f) + (completions * 2.0f) - (skips * 2.5f)
        }

        val skippedIds = skipCounts.filter { it.value > 1 }.keys

        return UserTasteProfile(
            likedSongIds = likedSongsMap.keys.toSet(),
            artistAffinityScores = artistScores,
            topSeedTrackIds = (likedSongsMap.keys + historyList.map { it.id }).distinct().take(10),
            skippedSongIds = skippedIds,
            recentPlayedTrackIds = historyList.take(15).map { it.id }
        )
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
