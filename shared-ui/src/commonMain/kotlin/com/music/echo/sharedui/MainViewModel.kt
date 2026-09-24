package com.music.echo.sharedui

import com.music.echo.sharedui.screens.DisplayAlbum
import com.music.echo.sharedui.screens.DisplayArtist
import com.music.echo.sharedui.screens.DisplayPlaylist
import com.music.echo.sharedui.screens.DisplayQuickPick
import com.music.echo.sharedui.screens.DisplayTrack
import com.music.echo.sharedui.screens.HomeChip
import com.music.echo.sharedui.screens.HomePageData
import com.music.echo.sharedui.screens.HomeSectionData
import com.music.echo.sharedui.screens.SearchFilter
import com.music.echo.sharedui.screens.SearchResultsData
import com.music.echo.sharedui.screens.SearchSuggestionsData
import com.music.innertube.YouTube
import com.music.innertube.models.AlbumItem
import com.music.innertube.models.ArtistItem
import com.music.innertube.models.PlaylistItem
import com.music.innertube.models.SongItem
import com.music.innertube.pages.SearchSummaryPage
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.async
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class MainViewModel(
    private val localRepository: com.music.echo.sharedui.data.LocalMusicRepository = com.music.echo.sharedui.data.InMemoryLocalMusicRepository()
) {
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Default)

    private val _homeTracks = MutableStateFlow<List<DisplayTrack>>(emptyList())
    val homeTracks: StateFlow<List<DisplayTrack>> = _homeTracks.asStateFlow()

    private val _homePageData = MutableStateFlow(HomePageData())
    val homePageData: StateFlow<HomePageData> = _homePageData.asStateFlow()

    private val _selectedChip = MutableStateFlow<HomeChip?>(null)
    val selectedChip: StateFlow<HomeChip?> = _selectedChip.asStateFlow()

    private val _searchResults = MutableStateFlow(SearchResultsData())
    val searchResults: StateFlow<SearchResultsData> = _searchResults.asStateFlow()

    private val _searchSuggestions = MutableStateFlow(SearchSuggestionsData())
    val searchSuggestions: StateFlow<SearchSuggestionsData> = _searchSuggestions.asStateFlow()

    private val _currentPlaylistSongs = MutableStateFlow<List<DisplayTrack>>(emptyList())
    val currentPlaylistSongs: StateFlow<List<DisplayTrack>> = _currentPlaylistSongs.asStateFlow()

    private val _selectedPlaylist = MutableStateFlow<DisplayPlaylist?>(null)
    val selectedPlaylist: StateFlow<DisplayPlaylist?> = _selectedPlaylist.asStateFlow()

    private val _likedSongs = MutableStateFlow<List<DisplayTrack>>(emptyList())
    val likedSongs: StateFlow<List<DisplayTrack>> = _likedSongs.asStateFlow()

    private val _likedSongIds = MutableStateFlow<Set<String>>(emptySet())
    val likedSongIds: StateFlow<Set<String>> = _likedSongIds.asStateFlow()

    private val _downloadedSongs = MutableStateFlow<List<DisplayTrack>>(emptyList())
    val downloadedSongs: StateFlow<List<DisplayTrack>> = _downloadedSongs.asStateFlow()

    private val _autoCachedSongs = MutableStateFlow<List<DisplayTrack>>(emptyList())
    val autoCachedSongs: StateFlow<List<DisplayTrack>> = _autoCachedSongs.asStateFlow()

    private val _historySongs = MutableStateFlow<List<DisplayTrack>>(emptyList())
    val historySongs: StateFlow<List<DisplayTrack>> = _historySongs.asStateFlow()

    private val _storageStats = MutableStateFlow<String>("0 songs • 0 MB")
    val storageStats: StateFlow<String> = _storageStats.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _isInitialLoading = MutableStateFlow(true)
    val isInitialLoading: StateFlow<Boolean> = _isInitialLoading.asStateFlow()

    init {
        scope.launch {
            YouTube.refreshVisitorData()
            // Hydrate persisted liked songs from disk
            val savedLiked = localRepository.getLikedSongs()
            if (savedLiked.isNotEmpty()) {
                _likedSongs.value = savedLiked
                _likedSongIds.value = savedLiked.map { it.id }.toSet()
            }
            _downloadedSongs.value = localRepository.getDownloadedSongs()
            _autoCachedSongs.value = localRepository.getAutoCachedSongs()
            _historySongs.value = localRepository.getRecentHistory()
            _storageStats.value = localRepository.getCacheStorageStats()
            loadCustomPlaylists()
            loadHome()
        }
    }

    fun isSongLiked(trackId: String): Boolean {
        return _likedSongIds.value.contains(trackId)
    }

    fun toggleLikeSong(track: DisplayTrack) {
        val currentList = _likedSongs.value.toMutableList()
        val currentSet = _likedSongIds.value.toMutableSet()
        val isCurrentlyLiked = currentSet.contains(track.id)

        if (isCurrentlyLiked) {
            currentSet.remove(track.id)
            currentList.removeAll { it.id == track.id }
        } else {
            currentSet.add(track.id)
            currentList.add(0, track)
        }

        _likedSongIds.value = currentSet
        _likedSongs.value = currentList

        // Persist immediately to disk (SQLite + JSON)
        scope.launch {
            localRepository.setSongLiked(track, !isCurrentlyLiked)
        }

        if (_selectedPlaylist.value?.id == "liked_songs") {
            _currentPlaylistSongs.value = currentList
            _selectedPlaylist.value = _selectedPlaylist.value?.copy(
                songCountText = "${currentList.size} songs"
            )
        }
    }

    fun openLikedSongs() {
        scope.launch {
            val saved = localRepository.getLikedSongs()
            if (saved.isNotEmpty() || _likedSongs.value.isEmpty()) {
                _likedSongs.value = saved
                _likedSongIds.value = saved.map { it.id }.toSet()
            }
            val songs = _likedSongs.value
            _selectedPlaylist.value = DisplayPlaylist(
                id = "liked_songs",
                title = "Liked Songs",
                author = "Your collection of favorite music",
                thumbnailUrl = null,
                songCountText = "${songs.size} songs"
            )
            _currentPlaylistSongs.value = songs
        }
    }

    fun openDownloadedSongs() {
        scope.launch {
            val downloaded = localRepository.getDownloadedSongs()
            _downloadedSongs.value = downloaded
            _storageStats.value = localRepository.getCacheStorageStats()
            _selectedPlaylist.value = DisplayPlaylist(
                id = "downloaded_songs",
                title = "Downloaded Music",
                author = "Offline available • Stored on local device",
                thumbnailUrl = null,
                songCountText = "${downloaded.size} songs"
            )
            _currentPlaylistSongs.value = downloaded
        }
    }

    fun openCachedSongs() {
        scope.launch {
            val cached = localRepository.getAutoCachedSongs()
            _autoCachedSongs.value = cached
            _storageStats.value = localRepository.getCacheStorageStats()
            _selectedPlaylist.value = DisplayPlaylist(
                id = "cached_songs",
                title = "Cached Songs",
                author = "Auto-saved for smooth playback",
                thumbnailUrl = null,
                songCountText = "${cached.size} songs"
            )
            _currentPlaylistSongs.value = cached
        }
    }

    fun openHistory() {
        scope.launch {
            val history = localRepository.getRecentHistory(50)
            _historySongs.value = history
            _selectedPlaylist.value = DisplayPlaylist(
                id = "history",
                title = "Listening History",
                author = "Recently played tracks",
                thumbnailUrl = null,
                songCountText = "${history.size} songs"
            )
            _currentPlaylistSongs.value = history
        }
    }

    fun clearOfflineCache() {
        scope.launch {
            localRepository.clearCache()
            _downloadedSongs.value = emptyList()
            _autoCachedSongs.value = emptyList()
            _storageStats.value = "0 songs • 0 MB"
            loadHome()
        }
    }

    fun clearAutoCache() {
        scope.launch {
            localRepository.clearAutoCache()
            _autoCachedSongs.value = emptyList()
            _storageStats.value = localRepository.getCacheStorageStats()
            if (_selectedPlaylist.value?.id == "cached_songs") {
                _currentPlaylistSongs.value = emptyList()
                _selectedPlaylist.value = _selectedPlaylist.value?.copy(songCountText = "0 songs")
            }
        }
    }

    fun downloadTrack(track: DisplayTrack, playbackController: PlaybackController) {
        scope.launch {
            if (!localRepository.isSongCached(track.id)) {
                val res = playbackController.resolveAudioStream(track.id)
                if (res != null) {
                    localRepository.cacheAudioStream(track, res.streamUrl, isExplicitDownload = true)
                    _downloadedSongs.value = localRepository.getDownloadedSongs()
                    _storageStats.value = localRepository.getCacheStorageStats()
                }
            } else {
                // If it was already cached, we still need to mark it as explicitly downloaded
                val res = playbackController.resolveAudioStream(track.id) // It will return quickly if cached or we can just pass dummy streamUrl
                if (res != null) {
                    localRepository.cacheAudioStream(track, res.streamUrl, isExplicitDownload = true)
                    _downloadedSongs.value = localRepository.getDownloadedSongs()
                    _autoCachedSongs.value = localRepository.getAutoCachedSongs()
                }
            }
        }
    }

    fun downloadPlaylist(tracks: List<DisplayTrack>, playbackController: PlaybackController) {
        scope.launch {
            for (track in tracks) {
                if (!localRepository.isSongCached(track.id)) {
                    val res = playbackController.resolveAudioStream(track.id)
                    if (res != null) {
                        localRepository.cacheAudioStream(track, res.streamUrl, isExplicitDownload = true)
                        _downloadedSongs.value = localRepository.getDownloadedSongs()
                        _storageStats.value = localRepository.getCacheStorageStats()
                    }
                } else {
                    val res = playbackController.resolveAudioStream(track.id)
                    if (res != null) {
                        localRepository.cacheAudioStream(track, res.streamUrl, isExplicitDownload = true)
                        _downloadedSongs.value = localRepository.getDownloadedSongs()
                        _autoCachedSongs.value = localRepository.getAutoCachedSongs()
                    }
                }
            }
        }
    }

    fun onTrackPlayed(track: DisplayTrack) {
        scope.launch {
            localRepository.recordPlayEvent(track, 0L, completed = false, skipped = false)
            _historySongs.value = localRepository.getRecentHistory()
            // Re-check downloaded songs shortly after track started
            kotlinx.coroutines.delay(2500)
            _downloadedSongs.value = localRepository.getDownloadedSongs()
            _autoCachedSongs.value = localRepository.getAutoCachedSongs()
            _storageStats.value = localRepository.getCacheStorageStats()
        }
    }

    fun onTrackCompleted(track: DisplayTrack) {
        scope.launch {
            localRepository.recordPlayEvent(track, 0L, completed = true, skipped = false)
            _historySongs.value = localRepository.getRecentHistory()
            _downloadedSongs.value = localRepository.getDownloadedSongs()
            _autoCachedSongs.value = localRepository.getAutoCachedSongs()
            _storageStats.value = localRepository.getCacheStorageStats()
        }
    }

    fun onTrackSkipped(track: DisplayTrack) {
        scope.launch {
            localRepository.recordPlayEvent(track, 0L, completed = false, skipped = true)
        }
    }

    fun loadPlaylist(playlist: DisplayPlaylist) {
        if (playlist.id == "liked_songs") {
            openLikedSongs()
            return
        }
        if (playlist.id == "downloaded_songs") {
            openDownloadedSongs()
            return
        }
        if (playlist.id == "cached_songs") {
            openCachedSongs()
            return
        }
        if (playlist.id == "history") {
            openHistory()
            return
        }
        if (playlist.id.startsWith("spotify_imported_")) {
            loadCustomPlaylistSongs(playlist.id)
            return
        }
        _selectedPlaylist.value = playlist
        _currentPlaylistSongs.value = emptyList()
        scope.launch {
            _isLoading.value = true
            YouTube.playlist(playlist.id).onSuccess { page ->
                val songs = page.songs.map { it.toDisplayTrack() }
                _currentPlaylistSongs.value = songs
            }.onFailure {
                it.printStackTrace()
            }
            _isLoading.value = false
        }
    }

    private val _importState = MutableStateFlow<String?>(null)
    val importState: StateFlow<String?> = _importState.asStateFlow()

    fun importSpotifyPlaylist(url: String) {
        scope.launch {
            _importState.value = "Fetching Spotify playlist..."
            try {
                val scraped = com.music.innertube.utils.SpotifyScraper.scrapePlaylist(url).getOrThrow()
                _importState.value = "Found ${scraped.tracks.size} tracks. Matching with Echo Music..."
                
                val displayTracks = mutableListOf<DisplayTrack>()
                var matched = 0
                for (track in scraped.tracks) {
                    try {
                        val query = "${track.title} ${track.artists}"
                        val searchResponse = YouTube.search(query, com.music.innertube.YouTube.SearchFilter.FILTER_SONG).getOrNull()
                        val firstSong = searchResponse?.items?.filterIsInstance<SongItem>()?.firstOrNull()
                        if (firstSong != null) {
                            displayTracks.add(firstSong.toDisplayTrack())
                            matched++
                        }
                        _importState.value = "Matched $matched / ${scraped.tracks.size} tracks..."
                    } catch (e: Exception) {
                        // Ignore individual track failure
                    }
                }
                
                if (displayTracks.isNotEmpty()) {
                    val playlistId = "spotify_imported_${kotlin.random.Random.nextInt()}"
                    val playlist = DisplayPlaylist(
                        id = playlistId,
                        title = scraped.title,
                        author = "Spotify Import",
                        thumbnailUrl = displayTracks.first().thumbnailUrl ?: "",
                        songCountText = "${displayTracks.size} songs"
                    )
                    localRepository.saveCustomPlaylist(playlist, displayTracks)
                    _importState.value = "Success! Playlist imported."
                    // Refresh custom playlists
                    loadCustomPlaylists()
                } else {
                    _importState.value = "Error: Could not match any tracks."
                }
            } catch (e: Exception) {
                _importState.value = "Error: ${e.message}"
            }
            
            // Clear message after 3 seconds
            kotlinx.coroutines.delay(3000)
            _importState.value = null
        }
    }

    private val _customPlaylists = MutableStateFlow<List<DisplayPlaylist>>(emptyList())
    val customPlaylists: StateFlow<List<DisplayPlaylist>> = _customPlaylists.asStateFlow()

    fun loadCustomPlaylists() {
        scope.launch {
            _customPlaylists.value = localRepository.getCustomPlaylists()
        }
    }

    fun loadCustomPlaylistSongs(playlistId: String) {
        scope.launch {
            _isLoading.value = true
            val songs = localRepository.getCustomPlaylistSongs(playlistId)
            val playlist = localRepository.getCustomPlaylists().find { it.id == playlistId }
            if (playlist != null) {
                _selectedPlaylist.value = playlist
                _currentPlaylistSongs.value = songs
            }
            _isLoading.value = false
        }
    }

    private fun parseSection(section: com.music.innertube.pages.HomePage.Section): HomeSectionData {
        val songs = mutableListOf<DisplayTrack>()
        val albums = mutableListOf<DisplayAlbum>()
        val playlists = mutableListOf<DisplayPlaylist>()
        val artists = mutableListOf<DisplayArtist>()

        for (item in section.items) {
            when (item) {
                is SongItem -> songs.add(item.toDisplayTrack())
                is AlbumItem -> albums.add(item.toDisplayAlbum())
                is PlaylistItem -> playlists.add(item.toDisplayPlaylist())
                is ArtistItem -> artists.add(item.toDisplayArtist())
            }
        }

        return HomeSectionData(
            title = section.title,
            label = section.label,
            thumbnail = section.thumbnail,
            songs = songs,
            albums = albums,
            playlists = playlists,
            artists = artists
        )
    }

    private fun searchSummaryToSections(summaryPage: SearchSummaryPage): List<HomeSectionData> {
        return summaryPage.summaries.map { summary ->
            val songs = mutableListOf<DisplayTrack>()
            val albums = mutableListOf<DisplayAlbum>()
            val playlists = mutableListOf<DisplayPlaylist>()
            val artists = mutableListOf<DisplayArtist>()

            for (item in summary.items) {
                when (item) {
                    is SongItem -> songs.add(item.toDisplayTrack())
                    is AlbumItem -> albums.add(item.toDisplayAlbum())
                    is PlaylistItem -> playlists.add(item.toDisplayPlaylist())
                    is ArtistItem -> artists.add(item.toDisplayArtist())
                }
            }

            HomeSectionData(
                title = summary.title,
                songs = songs,
                albums = albums,
                playlists = playlists,
                artists = artists
            )
        }.filter { !it.isEmpty }
    }

    fun loadHome(chip: HomeChip? = null) {
        _selectedChip.value = chip
        scope.launch {
            _isLoading.value = true
            try {
                val tasteProfile = localRepository.getUserTasteProfile()
                val history = localRepository.getRecentHistory(30)
                val liked = _likedSongs.value.ifEmpty { localRepository.getLikedSongs() }
                val downloaded = localRepository.getDownloadedSongs()
                _downloadedSongs.value = downloaded
                _storageStats.value = localRepository.getCacheStorageStats()

                val personalizedPage = com.music.echo.sharedui.recommendation.RecommendationEngine.generatePersonalizedHome(
                    tasteProfile = tasteProfile,
                    selectedChip = chip,
                    historyTracks = history,
                    likedTracks = liked,
                    downloadedTracks = downloaded
                )

                _homeTracks.value = personalizedPage.sections.flatMap { it.songs }.distinctBy { it.id }
                _homePageData.value = personalizedPage
            } catch (e: Exception) {
                e.printStackTrace()
            }
            _isLoading.value = false
            _isInitialLoading.value = false
        }
    }

    fun loadSuggestions(query: String) {
        if (query.isBlank()) {
            _searchSuggestions.value = SearchSuggestionsData()
            return
        }
        scope.launch {
            YouTube.searchSuggestions(query).onSuccess { res ->
                val artists = res.recommendedItems.filterIsInstance<ArtistItem>().map { it.toDisplayArtist() }
                _searchSuggestions.value = SearchSuggestionsData(
                    queries = res.queries,
                    recommendedArtists = artists
                )
            }.onFailure {
                _searchSuggestions.value = SearchSuggestionsData()
            }
        }
    }

    fun clearSuggestions() {
        _searchSuggestions.value = SearchSuggestionsData()
    }

    fun search(query: String, filter: SearchFilter = SearchFilter.ALL) {
        if (query.isBlank()) {
            _searchResults.value = SearchResultsData()
            return
        }
        scope.launch {
            _isLoading.value = true
            when (filter) {
                SearchFilter.ALL -> {
                    YouTube.searchSummary(query).onSuccess { searchSummaryPage ->
                        val songs = mutableListOf<DisplayTrack>()
                        val playlists = mutableListOf<DisplayPlaylist>()
                        val albums = mutableListOf<DisplayAlbum>()
                        val artists = mutableListOf<DisplayArtist>()

                        for (summary in searchSummaryPage.summaries) {
                            for (item in summary.items) {
                                when (item) {
                                    is SongItem -> songs.add(item.toDisplayTrack())
                                    is PlaylistItem -> playlists.add(item.toDisplayPlaylist())
                                    is AlbumItem -> albums.add(item.toDisplayAlbum())
                                    is ArtistItem -> artists.add(item.toDisplayArtist())
                                }
                            }
                        }

                        _searchResults.value = SearchResultsData(
                            songs = songs.distinctBy { it.id },
                            playlists = playlists.distinctBy { it.id },
                            albums = albums.distinctBy { it.id },
                            artists = artists.distinctBy { it.id }
                        )
                    }.onFailure {
                        it.printStackTrace()
                    }
                }
                SearchFilter.SONGS -> {
                    YouTube.search(query, YouTube.SearchFilter.FILTER_SONG).onSuccess { res ->
                        val songs = res.items.filterIsInstance<SongItem>().map { it.toDisplayTrack() }
                        _searchResults.value = SearchResultsData(songs = songs)
                    }.onFailure { it.printStackTrace() }
                }
                SearchFilter.PLAYLISTS -> {
                    YouTube.search(query, YouTube.SearchFilter.FILTER_COMMUNITY_PLAYLIST).onSuccess { res ->
                        val playlists = res.items.filterIsInstance<PlaylistItem>().map { it.toDisplayPlaylist() }
                        _searchResults.value = SearchResultsData(playlists = playlists)
                    }.onFailure { it.printStackTrace() }
                }
                SearchFilter.ALBUMS -> {
                    YouTube.search(query, YouTube.SearchFilter.FILTER_ALBUM).onSuccess { res ->
                        val albums = res.items.filterIsInstance<AlbumItem>().map { it.toDisplayAlbum() }
                        _searchResults.value = SearchResultsData(albums = albums)
                    }.onFailure { it.printStackTrace() }
                }
                SearchFilter.ARTISTS -> {
                    YouTube.search(query, YouTube.SearchFilter.FILTER_ARTIST).onSuccess { res ->
                        val artists = res.items.filterIsInstance<ArtistItem>().map { it.toDisplayArtist() }
                        _searchResults.value = SearchResultsData(artists = artists)
                    }.onFailure { it.printStackTrace() }
                }
                else -> {
                    YouTube.search(query, YouTube.SearchFilter.FILTER_SONG).onSuccess { res ->
                        val songs = res.items.filterIsInstance<SongItem>().map { it.toDisplayTrack() }
                        _searchResults.value = SearchResultsData(songs = songs)
                    }.onFailure { it.printStackTrace() }
                }
            }
            _isLoading.value = false
        }
    }
}

fun SongItem.toDisplayTrack(): DisplayTrack {
    val durationText = duration?.let { sec ->
        val m = sec / 60
        val s = sec % 60
        "$m:${s.toString().padStart(2, '0')}"
    } ?: ""
    return DisplayTrack(
        id = this.id,
        title = this.title,
        artist = this.artists.joinToString(", ") { it.name },
        duration = durationText,
        thumbnailUrl = this.thumbnail,
        album = this.album?.name,
        isVideo = this.isVideoSong
    )
}

fun PlaylistItem.toDisplayPlaylist(): DisplayPlaylist {
    return DisplayPlaylist(
        id = this.id,
        title = this.title,
        author = this.author?.name ?: "",
        songCountText = this.songCountText,
        thumbnailUrl = this.thumbnail
    )
}

fun AlbumItem.toDisplayAlbum(): DisplayAlbum {
    return DisplayAlbum(
        id = this.id,
        title = this.title,
        artist = this.artists?.joinToString(", ") { it.name } ?: "",
        year = this.year?.toString(),
        thumbnailUrl = this.thumbnail
    )
}

fun ArtistItem.toDisplayArtist(): DisplayArtist {
    return DisplayArtist(
        id = this.id,
        name = this.title,
        thumbnailUrl = this.thumbnail
    )
}
