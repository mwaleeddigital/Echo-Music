package com.music.echo.sharedui

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.focusable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.input.key.Key
import androidx.compose.ui.input.key.KeyEventType
import androidx.compose.ui.input.key.key
import androidx.compose.ui.input.key.onKeyEvent
import androidx.compose.ui.input.key.type
import androidx.compose.ui.unit.dp
import com.music.echo.sharedui.component.DesktopBottomPlayer
import com.music.echo.sharedui.component.DesktopQueuePanel
import com.music.echo.sharedui.component.DesktopSidebar
import com.music.echo.sharedui.component.DesktopTopBar
import com.music.echo.sharedui.component.NavTab
import com.music.echo.sharedui.screens.DisplayPlaylist
import com.music.echo.sharedui.screens.DisplayTrack
import com.music.echo.sharedui.screens.HomeScreen
import com.music.echo.sharedui.screens.LibraryScreen
import com.music.echo.sharedui.screens.PlayerScreen
import com.music.echo.sharedui.screens.PlaylistScreen
import com.music.echo.sharedui.screens.SearchFilter
import com.music.echo.sharedui.screens.SearchScreen
import com.music.echo.sharedui.screens.SettingsScreen
import com.music.echo.sharedui.screens.AccountScreen
import com.music.echo.sharedui.theme.EchoTheme
import com.music.echo.sharedui.theme.PureBlack

@Composable
fun EchoApp(
    audioPlayer: echo.music.iad1tya.playback.AudioPlayer,
    repository: com.music.echo.sharedui.data.LocalMusicRepository? = null,
    modifier: Modifier = Modifier
) {
    val localRepository = remember { repository ?: com.music.echo.sharedui.data.InMemoryLocalMusicRepository() }
    val mainViewModel = remember { MainViewModel(localRepository) }
    val playbackController = remember {
        PlaybackController(
            audioPlayer = audioPlayer,
            localRepository = localRepository,
            onTrackStarted = { mainViewModel.onTrackPlayed(it) },
            onTrackCompleted = { mainViewModel.onTrackCompleted(it) },
            onTrackSkipped = { mainViewModel.onTrackSkipped(it) }
        )
    }
    val rootFocusRequester = remember { FocusRequester() }

    LaunchedEffect(Unit) {
        rootFocusRequester.requestFocus()
    }

    val homeTracks by mainViewModel.homeTracks.collectAsState(initial = emptyList())
    val homePageData by mainViewModel.homePageData.collectAsState()
    val selectedHomeChip by mainViewModel.selectedChip.collectAsState()
    val searchResults by mainViewModel.searchResults.collectAsState()
    val searchSuggestions by mainViewModel.searchSuggestions.collectAsState()
    val selectedPlaylist by mainViewModel.selectedPlaylist.collectAsState()
    val currentPlaylistSongs by mainViewModel.currentPlaylistSongs.collectAsState()
    val downloadedSongs by mainViewModel.downloadedSongs.collectAsState()
    val autoCachedSongs by mainViewModel.autoCachedSongs.collectAsState()
    val historySongs by mainViewModel.historySongs.collectAsState()
    val storageStats by mainViewModel.storageStats.collectAsState()
    val importState by mainViewModel.importState.collectAsState()
    val customPlaylists by mainViewModel.customPlaylists.collectAsState()
    val likedSongs by mainViewModel.likedSongs.collectAsState()
    val isLoading by mainViewModel.isLoading.collectAsState()
    val isInitialLoading by mainViewModel.isInitialLoading.collectAsState()

    val isPlaying by playbackController.isPlaying.collectAsState(initial = false)
    val currentPositionMs by playbackController.currentPositionMs.collectAsState(initial = 0L)
    val currentItem by playbackController.currentItem.collectAsState(initial = null)
    val queueTracks by playbackController.queueTracks.collectAsState(initial = emptyList())
    
    // Derived state for active playing track
    val currentTrack = currentItem?.let {
        val durationText = if (it.durationMs > 0) {
            val sec = it.durationMs / 1000
            val m = sec / 60
            val s = sec % 60
            "$m:${s.toString().padStart(2, '0')}"
        } else ""
        DisplayTrack(
            id = it.id,
            title = it.title,
            artist = it.artist,
            duration = durationText,
            thumbnailUrl = it.artworkUrl
        )
    }
    
    val durationMs = currentItem?.durationMs ?: 0L

    var selectedTab by remember { mutableStateOf(NavTab.HOME) }
    var searchQuery by remember { mutableStateOf("") }
    var selectedFilter by remember { mutableStateOf(SearchFilter.ALL) }
    var isPlayerExpanded by remember { mutableStateOf(false) }
    var isLyricsOpen by remember { mutableStateOf(false) }
    var isQueueOpen by remember { mutableStateOf(false) }
    var isMiniPlayerOpen by remember { mutableStateOf(false) }
    var currentVolume by remember { mutableStateOf(0.85f) }

    val navHistory = remember { mutableStateListOf<NavTab>(NavTab.HOME) }
    var navHistoryIndex by remember { mutableStateOf(0) }
    var isNavigatingBackOrForward by remember { mutableStateOf(false) }

    LaunchedEffect(selectedTab) {
        if (!isNavigatingBackOrForward) {
            if (navHistoryIndex < navHistory.lastIndex) {
                while (navHistory.lastIndex > navHistoryIndex) {
                    navHistory.removeAt(navHistory.lastIndex)
                }
            }
            if (navHistory.isEmpty() || navHistory.last() != selectedTab) {
                navHistory.add(selectedTab)
                navHistoryIndex = navHistory.lastIndex
            }
        }
        isNavigatingBackOrForward = false
    }

    EchoTheme(pureBlack = true) {
        Box(
            modifier = modifier
                .fillMaxSize()
                .background(PureBlack)
                .focusRequester(rootFocusRequester)
                .focusable()
                .onKeyEvent { keyEvent ->
                    if (keyEvent.type == KeyEventType.KeyDown) {
                        when (keyEvent.key) {
                            Key.Spacebar -> {
                                playbackController.playPause()
                                true
                            }
                            Key.PageUp -> {
                                playbackController.previous()
                                true
                            }
                            Key.PageDown -> {
                                playbackController.next()
                                true
                            }
                            Key.DirectionUp -> {
                                currentVolume = (currentVolume + 0.05f).coerceIn(0f, 1f)
                                playbackController.setVolume(currentVolume)
                                true
                            }
                            Key.DirectionDown -> {
                                currentVolume = (currentVolume - 0.05f).coerceIn(0f, 1f)
                                playbackController.setVolume(currentVolume)
                                true
                            }
                            else -> false
                        }
                    } else {
                        false
                    }
                }
        ) {
            Column(modifier = Modifier.fillMaxSize()) {
                // -------------------------------------------------------------
                // 1. TOP HEADER BAR (Back/Forward, Home, Search input, Profile)
                // -------------------------------------------------------------
                DesktopTopBar(
                    searchQuery = searchQuery,
                    onSearchQueryChange = { query ->
                        searchQuery = query
                        mainViewModel.loadSuggestions(query)
                        if (query.isBlank()) {
                            mainViewModel.clearSuggestions()
                        }
                    },
                    onSearchSubmit = { query ->
                        searchQuery = query
                        selectedTab = NavTab.SEARCH
                        mainViewModel.search(query, selectedFilter)
                    },
                    searchSuggestions = searchSuggestions,
                    onSuggestionClick = { query ->
                        searchQuery = query
                        selectedTab = NavTab.SEARCH
                        mainViewModel.search(query, selectedFilter)
                    },
                    onArtistSuggestionClick = { artist ->
                        searchQuery = artist.name
                        selectedTab = NavTab.SEARCH
                        selectedFilter = SearchFilter.SONGS
                        mainViewModel.search(artist.name, SearchFilter.SONGS)
                    },
                    isHomeActive = (selectedTab == NavTab.HOME && searchQuery.isBlank()),
                    onHomeClick = {
                        searchQuery = ""
                        selectedTab = NavTab.HOME
                        mainViewModel.clearSuggestions()
                        mainViewModel.search("", SearchFilter.ALL)
                        mainViewModel.loadHome(null)
                    },
                    onBackClick = {
                        if (navHistoryIndex > 0) {
                            isNavigatingBackOrForward = true
                            navHistoryIndex--
                            selectedTab = navHistory[navHistoryIndex]
                        }
                    },
                    onForwardClick = {
                        if (navHistoryIndex < navHistory.lastIndex) {
                            isNavigatingBackOrForward = true
                            navHistoryIndex++
                            selectedTab = navHistory[navHistoryIndex]
                        }
                    },
                    onBrowseClick = {
                        selectedTab = NavTab.SEARCH
                    },
                    onNotificationsClick = {
                        selectedTab = NavTab.SETTINGS
                    },
                    onFriendsClick = {},
                    onProfileClick = {
                        selectedTab = NavTab.SETTINGS
                    },
                    storageStats = storageStats,
                    onOpenDownloadedClick = {
                        selectedTab = NavTab.PLAYLIST
                        mainViewModel.openDownloadedSongs()
                    },
                    onClearCacheClick = {
                        mainViewModel.clearOfflineCache()
                    }
                )

                // -------------------------------------------------------------
                // 2. MAIN BODY (Left Sidebar Rail + Right Content Canvas)
                // -------------------------------------------------------------
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f)
                ) {
                    // Left Library Sidebar
                    DesktopSidebar(
                        onLibraryClick = {
                            selectedTab = NavTab.LIBRARY
                        },
                        onLikedSongsClick = {
                            selectedTab = NavTab.PLAYLIST
                            mainViewModel.openLikedSongs()
                        },
                        onDownloadedSongsClick = {
                            selectedTab = NavTab.PLAYLIST
                            mainViewModel.openDownloadedSongs()
                        },
                        onPlaylistClick = { pl ->
                            val displayPl = DisplayPlaylist(id = pl.id, title = pl.title, thumbnailUrl = pl.thumbnailUrl)
                            selectedTab = NavTab.PLAYLIST
                            mainViewModel.loadPlaylist(displayPl)
                        },
                        onCreatePlaylist = {
                            searchQuery = ""
                            selectedFilter = SearchFilter.PLAYLISTS
                            selectedTab = NavTab.SEARCH
                        },
                        onBrowsePodcasts = {
                            searchQuery = "Podcasts"
                            selectedFilter = SearchFilter.PODCASTS
                            selectedTab = NavTab.SEARCH
                            mainViewModel.search("Podcasts", SearchFilter.PODCASTS)
                        },
                        onSpotifyImportSubmit = { url ->
                            mainViewModel.importSpotifyPlaylist(url)
                        },
                        userPlaylists = customPlaylists.map { 
                            com.music.echo.sharedui.component.SidebarPlaylist(
                                id = it.id, 
                                title = it.title, 
                                thumbnailUrl = it.thumbnailUrl
                            ) 
                        },
                        selectedPlaylistId = selectedPlaylist?.id
                    )

                    // Right Main Content Area
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxSize()
                    ) {
                        when (selectedTab) {
                            NavTab.HOME -> {
                                HomeScreen(
                                    homePageData = homePageData,
                                    selectedChip = selectedHomeChip,
                                    onSelectChip = { chip ->
                                        mainViewModel.loadHome(chip)
                                    },
                                    isLoading = isLoading,
                                    currentTrackId = currentTrack?.id,
                                    onTrackClick = { track ->
                                        playbackController.playTrack(track)
                                    },
                                    onPlaylistClick = { playlist ->
                                        selectedTab = NavTab.PLAYLIST
                                        mainViewModel.loadPlaylist(playlist)
                                    },
                                    onAlbumClick = { album ->
                                        searchQuery = album.title
                                        selectedFilter = SearchFilter.ALBUMS
                                        selectedTab = NavTab.SEARCH
                                        mainViewModel.search(album.title, SearchFilter.ALBUMS)
                                    },
                                    onArtistClick = { artist ->
                                        searchQuery = artist.name
                                        selectedFilter = SearchFilter.SONGS
                                        selectedTab = NavTab.SEARCH
                                        mainViewModel.search(artist.name, SearchFilter.SONGS)
                                    },
                                    onShowAllClick = { category ->
                                        searchQuery = category.replace("_", " ")
                                        selectedFilter = SearchFilter.ALL
                                        selectedTab = NavTab.SEARCH
                                        mainViewModel.search(searchQuery, SearchFilter.ALL)
                                    }
                                )
                            }
                            NavTab.SEARCH -> {
                                SearchScreen(
                                    searchQuery = searchQuery,
                                    selectedFilter = selectedFilter,
                                    onFilterChange = { filter ->
                                        selectedFilter = filter
                                        if (searchQuery.isNotBlank()) {
                                            mainViewModel.search(searchQuery, filter)
                                        }
                                    },
                                    searchResults = searchResults,
                                    isLoading = isLoading,
                                    currentTrackId = currentTrack?.id,
                                    onCategoryClick = { categoryQuery ->
                                        searchQuery = categoryQuery
                                        selectedFilter = SearchFilter.ALL
                                        selectedTab = NavTab.SEARCH
                                        mainViewModel.search(categoryQuery, SearchFilter.ALL)
                                    },
                                    onTrackClick = { track ->
                                        playbackController.playTrack(track)
                                    },
                                    onPlaylistClick = { playlist ->
                                        selectedTab = NavTab.PLAYLIST
                                        mainViewModel.loadPlaylist(playlist)
                                    },
                                    onAlbumClick = { album ->
                                        searchQuery = album.title
                                        selectedFilter = SearchFilter.SONGS
                                        mainViewModel.search(album.title, SearchFilter.SONGS)
                                    },
                                    onArtistClick = { artist ->
                                        searchQuery = artist.name
                                        selectedFilter = SearchFilter.SONGS
                                        selectedTab = NavTab.SEARCH
                                        mainViewModel.search(artist.name, SearchFilter.SONGS)
                                    }
                                )
                            }
                            NavTab.PLAYLIST -> {
                                selectedPlaylist?.let { pl ->
                                    PlaylistScreen(
                                        playlist = pl,
                                        songs = currentPlaylistSongs,
                                        isLoading = isLoading,
                                        currentTrackId = currentTrack?.id,
                                        isPlaying = isPlaying,
                                        isSongLiked = { trackId -> mainViewModel.isSongLiked(trackId) },
                                        isSongDownloaded = { trackId -> downloadedSongs.any { it.id == trackId } },
                                        onToggleLikeSong = { track -> mainViewModel.toggleLikeSong(track) },
                                        onDownloadTrack = { track -> mainViewModel.downloadTrack(track, playbackController) },
                                        onDownloadPlaylist = { tracks -> mainViewModel.downloadPlaylist(tracks, playbackController) },
                                        onClearCacheClick = { mainViewModel.clearAutoCache() },
                                        onTrackClick = { track ->
                                            playbackController.playTrack(track)
                                        },
                                        onPlayAllClick = {
                                            playbackController.playPlaylist(pl, currentPlaylistSongs)
                                        },
                                        onBackClick = {
                                            selectedTab = NavTab.HOME
                                        }
                                    )
                                }
                            }
                            NavTab.LIBRARY -> {
                                LibraryScreen(
                                    currentTrackId = currentTrack?.id,
                                    downloadedSongs = downloadedSongs,
                                    likedSongs = likedSongs,
                                    historySongs = historySongs,
                                    autoCachedSongs = autoCachedSongs,
                                    storageStats = storageStats,
                                    onTrackClick = { track ->
                                        playbackController.playTrack(track)
                                    },
                                    onLikedSongsClick = {
                                        selectedTab = NavTab.PLAYLIST
                                        mainViewModel.openLikedSongs()
                                    },
                                    onDownloadedSongsClick = {
                                        selectedTab = NavTab.PLAYLIST
                                        mainViewModel.openDownloadedSongs()
                                    },
                                    onCachedSongsClick = {
                                        selectedTab = NavTab.PLAYLIST
                                        mainViewModel.openCachedSongs()
                                    },
                                    onHistoryClick = {
                                        selectedTab = NavTab.PLAYLIST
                                        mainViewModel.openHistory()
                                    }
                                )
                            }
                            NavTab.SETTINGS -> {
                                SettingsScreen(
                                    onAccountClick = {
                                        selectedTab = NavTab.ACCOUNT
                                    }
                                )
                            }
                            NavTab.ACCOUNT -> {
                                AccountScreen(
                                    onBackClick = {
                                        selectedTab = NavTab.SETTINGS
                                    }
                                )
                            }
                        }
                    }

                    // Right Queue Side Panel (Spotify styled)
                    AnimatedVisibility(
                        visible = isQueueOpen,
                        enter = androidx.compose.animation.expandHorizontally(expandFrom = Alignment.End) + androidx.compose.animation.fadeIn(),
                        exit = androidx.compose.animation.shrinkHorizontally(shrinkTowards = Alignment.End) + androidx.compose.animation.fadeOut()
                    ) {
                        DesktopQueuePanel(
                            currentTrack = currentTrack,
                            isPlaying = isPlaying,
                            queueTracks = queueTracks,
                            onTrackClick = { track -> playbackController.playTrack(track) },
                            onClose = { isQueueOpen = false },
                            isSongLiked = { trackId -> mainViewModel.isSongLiked(trackId) },
                            onToggleLikeSong = { track -> mainViewModel.toggleLikeSong(track) },
                            onRemoveFromQueue = { track -> playbackController.removeFromQueue(track) },
                            onAddToQueue = { track -> playbackController.addToQueue(track) },
                            onGoToArtist = { artistName ->
                                searchQuery = artistName
                                selectedFilter = SearchFilter.SONGS
                                selectedTab = NavTab.SEARCH
                                mainViewModel.search(artistName, SearchFilter.SONGS)
                            },
                            onGoToAlbum = { albumName ->
                                searchQuery = albumName
                                selectedFilter = SearchFilter.ALBUMS
                                selectedTab = NavTab.SEARCH
                                mainViewModel.search(albumName, SearchFilter.ALBUMS)
                            },
                            onCreatePlaylist = {
                                searchQuery = ""
                                selectedFilter = SearchFilter.PLAYLISTS
                                selectedTab = NavTab.SEARCH
                            }
                        )
                    }
                }

                // -------------------------------------------------------------
                // 3. FULL-WIDTH BOTTOM PLAYER BAR (Spotify style)
                // -------------------------------------------------------------
                DesktopBottomPlayer(
                    track = currentTrack,
                    isPlaying = isPlaying,
                    currentPositionMs = currentPositionMs,
                    durationMs = durationMs,
                    currentVolume = currentVolume,
                    onVolumeChange = { newVol ->
                        currentVolume = newVol
                        playbackController.setVolume(newVol)
                    },
                    isLiked = currentTrack?.let { mainViewModel.isSongLiked(it.id) } == true,
                    onToggleLike = {
                        currentTrack?.let { mainViewModel.toggleLikeSong(it) }
                    },
                    onPlayPauseClick = { playbackController.playPause() },
                    onPreviousClick = { playbackController.previous() },
                    onNextClick = { playbackController.next() },
                    onSeekTo = { posMs -> playbackController.seekTo(posMs) },
                    onLyricsToggle = { isLyricsOpen = !isLyricsOpen },
                    isLyricsActive = isLyricsOpen,
                    onQueueToggle = { isQueueOpen = !isQueueOpen },
                    isQueueActive = isQueueOpen,
                    onMiniPlayerToggle = { isMiniPlayerOpen = !isMiniPlayerOpen },
                    isMiniPlayerActive = isMiniPlayerOpen,
                    onFullscreenToggle = { isPlayerExpanded = !isPlayerExpanded }
                )
            }

            // Floating Desktop MiniPlayer (when toggled)
            AnimatedVisibility(
                visible = isMiniPlayerOpen && currentTrack != null,
                enter = androidx.compose.animation.fadeIn() + slideInVertically(initialOffsetY = { it / 2 }),
                exit = androidx.compose.animation.fadeOut() + slideOutVertically(targetOffsetY = { it / 2 }),
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .padding(end = 24.dp, bottom = 96.dp)
            ) {
                currentTrack?.let { track ->
                    com.music.echo.sharedui.component.DesktopMiniPlayer(
                        track = track,
                        isPlaying = isPlaying,
                        currentPositionMs = currentPositionMs,
                        durationMs = durationMs,
                        onPlayPauseClick = { playbackController.playPause() },
                        onPreviousClick = { playbackController.previous() },
                        onNextClick = { playbackController.next() },
                        onSeekTo = { posMs -> playbackController.seekTo(posMs) },
                        onClose = { isMiniPlayerOpen = false },
                        onExpandFull = {
                            isMiniPlayerOpen = false
                            isPlayerExpanded = true
                        }
                    )
                }
            }

            // Full Player / Lyrics Overlay (when expanded)
            AnimatedVisibility(
                visible = (isPlayerExpanded || isLyricsOpen) && currentTrack != null,
                enter = slideInVertically(initialOffsetY = { it }),
                exit = slideOutVertically(targetOffsetY = { it })
            ) {
                currentTrack?.let { track ->
                    PlayerScreen(
                        track = track,
                        isPlaying = isPlaying,
                        currentPositionMs = currentPositionMs,
                        durationMs = durationMs,
                        onPlayPauseClick = { playbackController.playPause() },
                        onPreviousClick = { playbackController.previous() },
                        onNextClick = { playbackController.next() },
                        isLiked = mainViewModel.isSongLiked(track.id),
                        isDownloaded = downloadedSongs.any { it.id == track.id },
                        onToggleLike = { mainViewModel.toggleLikeSong(track) },
                        onDownload = { mainViewModel.downloadTrack(track, playbackController) },
                        onSeekTo = { posMs -> playbackController.seekTo(posMs) },
                        onDismiss = {
                            isPlayerExpanded = false
                            isLyricsOpen = false
                        }
                    )
                }
            }

            // Initial Startup Screen (Splash loader while first home data is preparing)
            androidx.compose.animation.AnimatedVisibility(
                visible = isInitialLoading,
                enter = androidx.compose.animation.fadeIn(),
                exit = androidx.compose.animation.fadeOut(
                    animationSpec = androidx.compose.animation.core.tween(500)
                )
            ) {
                com.music.echo.sharedui.component.DesktopSplashScreen()
            }
            // Spotify Import Status Toast
            androidx.compose.animation.AnimatedVisibility(
                visible = importState != null,
                enter = androidx.compose.animation.fadeIn() + androidx.compose.animation.slideInVertically(initialOffsetY = { -it }),
                exit = androidx.compose.animation.fadeOut() + androidx.compose.animation.slideOutVertically(targetOffsetY = { -it }),
                modifier = Modifier.align(Alignment.TopCenter).padding(top = 16.dp)
            ) {
                Box(
                    modifier = Modifier
                        .clip(androidx.compose.foundation.shape.RoundedCornerShape(8.dp))
                        .background(com.music.echo.sharedui.theme.NothingRed)
                        .padding(horizontal = 16.dp, vertical = 8.dp)
                ) {
                    androidx.compose.material3.Text(
                        text = importState ?: "",
                        color = androidx.compose.ui.graphics.Color.White,
                        fontWeight = androidx.compose.ui.text.font.FontWeight.Bold
                    )
                }
            }
        }
    }
}
