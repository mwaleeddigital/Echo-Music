package com.music.echo.sharedui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.hoverable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsHoveredAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material.icons.automirrored.rounded.List
import androidx.compose.material.icons.rounded.AccessTime
import androidx.compose.material.icons.rounded.AddCircleOutline
import androidx.compose.material.icons.rounded.ArrowDownward
import androidx.compose.material.icons.rounded.CheckCircle
import androidx.compose.material.icons.rounded.Download
import androidx.compose.material.icons.rounded.Favorite
import androidx.compose.material.icons.rounded.History
import androidx.compose.material.icons.rounded.MoreHoriz
import androidx.compose.material.icons.rounded.MusicNote
import androidx.compose.material.icons.rounded.Pause
import androidx.compose.material.icons.rounded.PlayArrow
import androidx.compose.material.icons.rounded.Search
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil3.compose.AsyncImage
import com.music.echo.sharedui.theme.DarkSurfaceElevated
import com.music.echo.sharedui.theme.PureBlack
import com.music.echo.sharedui.theme.TextPrimary
import com.music.echo.sharedui.theme.TextSecondary

@Composable
fun PlaylistScreen(
    playlist: DisplayPlaylist,
    songs: List<DisplayTrack>,
    isLoading: Boolean = false,
    currentTrackId: String? = null,
    isPlaying: Boolean = false,
    isSongLiked: (String) -> Boolean = { false },
    onToggleLikeSong: (DisplayTrack) -> Unit = {},
    onTrackClick: (DisplayTrack) -> Unit,
    onPlayAllClick: () -> Unit,
    onBackClick: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    val listState = rememberLazyListState()
    val isScrolledPastHeader by remember {
        derivedStateOf { listState.firstVisibleItemIndex > 0 }
    }

    var isFavorite by remember { mutableStateOf(false) }
    var isDownloaded by remember { mutableStateOf(false) }

    // Playlist Theme Gradient (Dark Plum / Purple / Liked Songs Deep Indigo)
    val headerThemeColor = remember(playlist.id) {
        when (playlist.id) {
            "liked_songs" -> Color(0xFF450AF5)
            "downloaded_songs" -> Color(0xFF0F9D58)
            "history" -> Color(0xFFE65100)
            else -> {
                when ((playlist.id.hashCode() % 4 + 4) % 4) {
                    0 -> Color(0xFF5A1C48) // Plum Purple (like Daily Mix 4)
                    1 -> Color(0xFF1B4E42) // Deep Emerald
                    2 -> Color(0xFF1E3264) // Deep Blue
                    else -> Color(0xFF531E48)
                }
            }
        }
    }

    val totalDurationSec = remember(songs) {
        songs.sumOf { track ->
            val parts = track.duration.split(":")
            if (parts.size == 2) {
                (parts[0].toIntOrNull() ?: 0) * 60 + (parts[1].toIntOrNull() ?: 0)
            } else 0
        }
    }

    val totalDurationText = remember(totalDurationSec, songs.size) {
        val hrs = totalDurationSec / 3600
        val mins = (totalDurationSec % 3600) / 60
        if (hrs > 0) "about $hrs hr $mins min" else "$mins min"
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(PureBlack)
    ) {
        LazyColumn(
            state = listState,
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(bottom = 120.dp)
        ) {
            // -------------------------------------------------------------
            // 1. HERO HEADER AREA (Artwork, Title, Author, Gradient)
            // -------------------------------------------------------------
            item {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(
                            Brush.verticalGradient(
                                listOf(headerThemeColor, headerThemeColor.copy(alpha = 0.5f), PureBlack)
                            )
                        )
                        .padding(horizontal = 24.dp, vertical = 24.dp)
                ) {
                    Column {
                        // Back Button
                        Row(
                            modifier = Modifier
                                .clip(CircleShape)
                                .background(Color(0x33000000))
                                .clickable { onBackClick() }
                                .padding(8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Rounded.ArrowBack,
                                contentDescription = "Back",
                                tint = Color.White,
                                modifier = Modifier.size(20.dp)
                            )
                        }

                        Spacer(modifier = Modifier.height(16.dp))

                        // Header Content (Artwork + Metadata)
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.Bottom,
                            horizontalArrangement = Arrangement.spacedBy(24.dp)
                        ) {
                            // Large Square Artwork
                            Box(
                                modifier = Modifier
                                    .size(200.dp)
                                    .shadow(24.dp, RoundedCornerShape(8.dp))
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(
                                        when (playlist.id) {
                                            "liked_songs" -> Brush.linearGradient(
                                                listOf(Color(0xFF450AF5), Color(0xFF8E8EE5), Color(0xFFC4B5FD))
                                            )
                                            "downloaded_songs" -> Brush.linearGradient(
                                                listOf(Color(0xFF0F9D58), Color(0xFF1DB954), Color(0xFF34D399))
                                            )
                                            "history" -> Brush.linearGradient(
                                                listOf(Color(0xFFE65100), Color(0xFFFF9800), Color(0xFFFFCC80))
                                            )
                                            else -> Brush.linearGradient(listOf(Color(0xFF282828), Color(0xFF181818)))
                                        }
                                    ),
                                contentAlignment = Alignment.Center
                            ) {
                                when (playlist.id) {
                                    "liked_songs" -> {
                                        Icon(
                                            imageVector = Icons.Rounded.Favorite,
                                            contentDescription = "Liked Songs",
                                            tint = Color.White,
                                            modifier = Modifier.size(72.dp)
                                        )
                                    }
                                    "downloaded_songs" -> {
                                        Icon(
                                            imageVector = Icons.Rounded.Download,
                                            contentDescription = "Downloaded Music",
                                            tint = Color.White,
                                            modifier = Modifier.size(72.dp)
                                        )
                                    }
                                    "history" -> {
                                        Icon(
                                            imageVector = Icons.Rounded.History,
                                            contentDescription = "Listening History",
                                            tint = Color.White,
                                            modifier = Modifier.size(72.dp)
                                        )
                                    }
                                    else -> {
                                        if (!playlist.thumbnailUrl.isNullOrBlank()) {
                                            AsyncImage(
                                                model = playlist.thumbnailUrl,
                                                contentDescription = playlist.title,
                                                contentScale = ContentScale.Crop,
                                                modifier = Modifier.fillMaxSize()
                                            )
                                        } else {
                                            Icon(
                                                imageVector = Icons.Rounded.MusicNote,
                                                contentDescription = null,
                                                tint = Color.White,
                                                modifier = Modifier.size(64.dp)
                                            )
                                        }
                                    }
                                }
                            }

                            // Metadata Column
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = "Public Playlist",
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color.White
                                )

                                Spacer(modifier = Modifier.height(4.dp))

                                Text(
                                    text = playlist.title,
                                    fontSize = 44.sp,
                                    fontWeight = FontWeight.Black,
                                    color = Color.White,
                                    maxLines = 2,
                                    overflow = TextOverflow.Ellipsis,
                                    lineHeight = 48.sp
                                )

                                Spacer(modifier = Modifier.height(12.dp))

                                Text(
                                    text = playlist.author.ifBlank { "Arijit Singh, Atif Aslam, Pritam and more" },
                                    fontSize = 14.sp,
                                    color = Color.White.copy(alpha = 0.75f),
                                    maxLines = 2,
                                    overflow = TextOverflow.Ellipsis
                                )

                                Spacer(modifier = Modifier.height(8.dp))

                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                                ) {
                                    val isSpecialPlaylist = playlist.id in listOf("liked_songs", "downloaded_songs", "history")
                                    Box(
                                        modifier = Modifier
                                            .size(22.dp)
                                            .clip(CircleShape)
                                            .background(if (isSpecialPlaylist) Color(0xFF1ED760) else Color(0xFFE91E63)),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Text(
                                            text = if (isSpecialPlaylist) "Y" else "E",
                                            color = PureBlack,
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 11.sp
                                        )
                                    }

                                    Text(
                                        text = if (isSpecialPlaylist) "You" else "Echo Music",
                                        fontSize = 13.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = Color.White
                                    )

                                    val countText = if (songs.isNotEmpty()) {
                                        "• ${songs.size} ${if (songs.size == 1) "song" else "songs"}, $totalDurationText"
                                    } else if (playlist.songCountText != null && playlist.songCountText != "0 songs") {
                                        "• ${playlist.songCountText}"
                                    } else {
                                        "• 0 songs"
                                    }

                                    Text(
                                        text = countText,
                                        fontSize = 13.sp,
                                        color = Color.White.copy(alpha = 0.8f)
                                    )
                                }
                            }
                        }
                    }
                }
            }

            // -------------------------------------------------------------
            // 2. ACTION BAR (Big Play Button, Heart, Download, Options)
            // -------------------------------------------------------------
            item {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 24.dp, vertical = 16.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(20.dp)
                    ) {
                        // Big Circular Green Play Button
                        Box(
                            modifier = Modifier
                                .size(56.dp)
                                .shadow(8.dp, CircleShape)
                                .clip(CircleShape)
                                .background(Color(0xFF1ED760))
                                .clickable { onPlayAllClick() },
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = if (isPlaying && songs.any { it.id == currentTrackId }) Icons.Rounded.Pause else Icons.Rounded.PlayArrow,
                                contentDescription = "Play Playlist",
                                tint = PureBlack,
                                modifier = Modifier.size(32.dp)
                            )
                        }

                        // Favorite Button
                        Box(
                            modifier = Modifier
                                .size(40.dp)
                                .clip(CircleShape)
                                .clickable { isFavorite = !isFavorite },
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = if (isFavorite) Icons.Rounded.Favorite else Icons.Rounded.AddCircleOutline,
                                contentDescription = "Save to Library",
                                tint = if (isFavorite) Color(0xFF1ED760) else Color(0xFFB3B3B3),
                                modifier = Modifier.size(28.dp)
                            )
                        }

                        // Download Button
                        Box(
                            modifier = Modifier
                                .size(40.dp)
                                .clip(CircleShape)
                                .clickable { isDownloaded = !isDownloaded },
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = if (isDownloaded) Icons.Rounded.CheckCircle else Icons.Rounded.ArrowDownward,
                                contentDescription = "Download",
                                tint = if (isDownloaded) Color(0xFF1ED760) else Color(0xFFB3B3B3),
                                modifier = Modifier.size(24.dp)
                            )
                        }

                        // Options '...'
                        Box(
                            modifier = Modifier
                                .size(40.dp)
                                .clip(CircleShape)
                                .clickable { },
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Rounded.MoreHoriz,
                                contentDescription = "More Options",
                                tint = Color(0xFFB3B3B3),
                                modifier = Modifier.size(28.dp)
                            )
                        }
                    }

                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Rounded.Search,
                            contentDescription = "Search",
                            tint = Color(0xFFB3B3B3),
                            modifier = Modifier.size(20.dp).clickable { }
                        )
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(4.dp),
                            modifier = Modifier.clickable { }
                        ) {
                            Text(text = "Custom order", fontSize = 13.sp, color = Color(0xFFB3B3B3))
                            Icon(
                                imageVector = Icons.AutoMirrored.Rounded.List,
                                contentDescription = "Sort",
                                tint = Color(0xFFB3B3B3),
                                modifier = Modifier.size(18.dp)
                            )
                        }
                    }
                }
            }

            // -------------------------------------------------------------
            // 3. TABLE COLUMN HEADERS (#, Title, Album, Duration)
            // -------------------------------------------------------------
            item {
                Column(modifier = Modifier.padding(horizontal = 24.dp)) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "#",
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Medium,
                            color = TextSecondary,
                            modifier = Modifier.width(36.dp)
                        )

                        Text(
                            text = "Title",
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Medium,
                            color = TextSecondary,
                            modifier = Modifier.weight(1f)
                        )

                        Text(
                            text = "Album",
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Medium,
                            color = TextSecondary,
                            modifier = Modifier.weight(0.7f)
                        )

                        Box(
                            modifier = Modifier.width(48.dp),
                            contentAlignment = Alignment.CenterEnd
                        ) {
                            Icon(
                                imageVector = Icons.Rounded.AccessTime,
                                contentDescription = "Duration",
                                tint = TextSecondary,
                                modifier = Modifier.size(16.dp)
                            )
                        }
                    }

                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(1.dp)
                            .background(Color(0x22FFFFFF))
                    )

                    Spacer(modifier = Modifier.height(8.dp))
                }
            }

            // Loading Spinner
            if (isLoading && songs.isEmpty()) {
                item {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 48.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        CircularProgressIndicator(
                            color = Color(0xFF1ED760),
                            modifier = Modifier.size(36.dp)
                        )
                    }
                }
            }

            // Empty State (e.g. for Liked Songs on first run)
            if (!isLoading && songs.isEmpty()) {
                item {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 56.dp, horizontal = 24.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        Box(
                            modifier = Modifier
                                .size(64.dp)
                                .clip(CircleShape)
                                .background(Color(0x18FFFFFF)),
                            contentAlignment = Alignment.Center
                        ) {
                            val emptyIcon = when (playlist.id) {
                                "liked_songs" -> Icons.Rounded.Favorite
                                "downloaded_songs" -> Icons.Rounded.Download
                                "history" -> Icons.Rounded.History
                                else -> Icons.Rounded.MusicNote
                            }
                            val emptyTint = when (playlist.id) {
                                "liked_songs" -> Color(0xFF1ED760)
                                "downloaded_songs" -> Color(0xFF1DB954)
                                "history" -> Color(0xFFFF9800)
                                else -> TextSecondary
                            }
                            Icon(
                                imageVector = emptyIcon,
                                contentDescription = null,
                                tint = emptyTint,
                                modifier = Modifier.size(32.dp)
                            )
                        }
                        Spacer(modifier = Modifier.height(16.dp))
                        val emptyTitle = when (playlist.id) {
                            "liked_songs" -> "Songs you like will appear here"
                            "downloaded_songs" -> "No downloaded songs yet"
                            "history" -> "No listening history yet"
                            else -> "This playlist is empty"
                        }
                        Text(
                            text = emptyTitle,
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold,
                            color = TextPrimary
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        val emptySubtitle = when (playlist.id) {
                            "liked_songs" -> "Save songs by clicking the heart icon on any track or in the player bar."
                            "downloaded_songs" -> "Play songs online to automatically save them to local storage for offline playback."
                            "history" -> "Songs you play will appear here so you can easily revisit them."
                            else -> "Find and add songs to build your playlist."
                        }
                        Text(
                            text = emptySubtitle,
                            fontSize = 13.sp,
                            color = TextSecondary
                        )
                    }
                }
            }

            // -------------------------------------------------------------
            // 4. PLAYLIST TRACKLIST ROWS
            // -------------------------------------------------------------
            itemsIndexed(songs) { index, track ->
                PlaylistTrackRow(
                    index = index + 1,
                    track = track,
                    isPlaying = (track.id == currentTrackId && isPlaying),
                    isCurrent = (track.id == currentTrackId),
                    isLiked = isSongLiked(track.id),
                    onToggleLike = { onToggleLikeSong(track) },
                    onClick = { onTrackClick(track) },
                    modifier = Modifier.padding(horizontal = 24.dp, vertical = 2.dp)
                )
            }
        }

        // -------------------------------------------------------------
        // 5. STICKY TOP SCROLL BAR (Colored Bar + Small Play + Title)
        // -------------------------------------------------------------
        AnimatedVisibility(
            visible = isScrolledPastHeader,
            enter = fadeIn(),
            exit = fadeOut()
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(64.dp)
                    .background(headerThemeColor)
                    .padding(horizontal = 24.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Small Circular Play Button
                Box(
                    modifier = Modifier
                        .size(44.dp)
                        .shadow(4.dp, CircleShape)
                        .clip(CircleShape)
                        .background(Color(0xFF1ED760))
                        .clickable { onPlayAllClick() },
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = if (isPlaying && songs.any { it.id == currentTrackId }) Icons.Rounded.Pause else Icons.Rounded.PlayArrow,
                        contentDescription = "Play",
                        tint = PureBlack,
                        modifier = Modifier.size(24.dp)
                    )
                }

                Text(
                    text = playlist.title,
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }
    }
}

@Composable
private fun PlaylistTrackRow(
    index: Int,
    track: DisplayTrack,
    isPlaying: Boolean,
    isCurrent: Boolean,
    isLiked: Boolean,
    onToggleLike: () -> Unit,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val interactionSource = remember { MutableInteractionSource() }
    val isHovered by interactionSource.collectIsHoveredAsState()

    Row(
        modifier = modifier
            .fillMaxWidth()
            .height(56.dp)
            .clip(RoundedCornerShape(4.dp))
            .background(if (isHovered) Color(0x1FFFFFFF) else Color.Transparent)
            .hoverable(interactionSource)
            .clickable(onClick = onClick)
            .padding(horizontal = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Track Index or Play Icon
        Box(
            modifier = Modifier.width(28.dp),
            contentAlignment = Alignment.CenterStart
        ) {
            if (isHovered) {
                Icon(
                    imageVector = if (isPlaying) Icons.Rounded.Pause else Icons.Rounded.PlayArrow,
                    contentDescription = "Play",
                    tint = Color.White,
                    modifier = Modifier.size(18.dp)
                )
            } else if (isCurrent) {
                Text(
                    text = index.toString(),
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF1ED760)
                )
            } else {
                Text(
                    text = index.toString(),
                    fontSize = 14.sp,
                    color = TextSecondary
                )
            }
        }

        // Song Thumbnail + Title + Artist
        Row(
            modifier = Modifier.weight(1f),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .clip(RoundedCornerShape(4.dp))
                    .background(DarkSurfaceElevated),
                contentAlignment = Alignment.Center
            ) {
                if (!track.thumbnailUrl.isNullOrBlank()) {
                    AsyncImage(
                        model = track.thumbnailUrl,
                        contentDescription = track.title,
                        contentScale = ContentScale.Crop,
                        modifier = Modifier.fillMaxSize()
                    )
                } else {
                    Icon(
                        imageVector = Icons.Rounded.MusicNote,
                        contentDescription = null,
                        tint = Color.White,
                        modifier = Modifier.size(20.dp)
                    )
                }
            }

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = track.title,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = if (isCurrent) Color(0xFF1ED760) else Color.White,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    text = track.artist,
                    fontSize = 12.sp,
                    color = TextSecondary,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }

        // Album Column
        Text(
            text = track.album ?: track.title,
            fontSize = 13.sp,
            color = TextSecondary,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            modifier = Modifier.weight(0.7f).padding(horizontal = 8.dp)
        )

        // Heart Icon (revealed on hover or when liked) + Duration
        Row(
            modifier = Modifier.width(64.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.End
        ) {
            if (isHovered || isLiked) {
                Icon(
                    imageVector = if (isLiked) Icons.Rounded.Favorite else Icons.Rounded.AddCircleOutline,
                    contentDescription = if (isLiked) "Remove from Liked Songs" else "Save to Liked Songs",
                    tint = if (isLiked) Color(0xFF1ED760) else TextSecondary,
                    modifier = Modifier
                        .size(16.dp)
                        .clickable { onToggleLike() }
                )
                Spacer(modifier = Modifier.width(12.dp))
            }

            Text(
                text = track.duration.ifBlank { "3:30" },
                fontSize = 13.sp,
                color = TextSecondary
            )
        }
    }
}
