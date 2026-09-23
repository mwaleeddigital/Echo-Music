package com.music.echo.sharedui.component

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
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.QueueMusic
import androidx.compose.material.icons.rounded.Add
import androidx.compose.material.icons.rounded.Album
import androidx.compose.material.icons.rounded.Block
import androidx.compose.material.icons.rounded.Check
import androidx.compose.material.icons.rounded.ChevronRight
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material.icons.rounded.DeleteOutline
import androidx.compose.material.icons.rounded.Favorite
import androidx.compose.material.icons.rounded.Info
import androidx.compose.material.icons.rounded.Laptop
import androidx.compose.material.icons.rounded.MoreHoriz
import androidx.compose.material.icons.rounded.MusicNote
import androidx.compose.material.icons.rounded.Person
import androidx.compose.material.icons.rounded.PlayArrow
import androidx.compose.material.icons.rounded.Radio
import androidx.compose.material.icons.rounded.Search
import androidx.compose.material.icons.rounded.Share
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
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
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Popup
import androidx.compose.ui.window.PopupProperties
import coil3.compose.AsyncImage
import com.music.echo.sharedui.screens.DisplayTrack
import com.music.echo.sharedui.theme.DarkSurface
import com.music.echo.sharedui.theme.DarkSurfaceElevated
import com.music.echo.sharedui.theme.PureBlack
import com.music.echo.sharedui.theme.TextPrimary
import com.music.echo.sharedui.theme.TextSecondary

@Composable
fun DesktopQueuePanel(
    currentTrack: DisplayTrack?,
    isPlaying: Boolean,
    queueTracks: List<DisplayTrack>,
    onTrackClick: (DisplayTrack) -> Unit,
    onClose: () -> Unit,
    isSongLiked: (String) -> Boolean = { false },
    onToggleLikeSong: (DisplayTrack) -> Unit = {},
    onRemoveFromQueue: (DisplayTrack) -> Unit = {},
    onAddToQueue: (DisplayTrack) -> Unit = {},
    onGoToArtist: (String) -> Unit = {},
    onGoToAlbum: (String) -> Unit = {},
    onCreatePlaylist: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    var selectedMenuTrack by remember { mutableStateOf<DisplayTrack?>(null) }

    Box(
        modifier = modifier
            .width(360.dp)
            .fillMaxHeight()
            .clip(RoundedCornerShape(8.dp))
            .background(DarkSurface)
            .padding(16.dp)
    ) {
        Column(modifier = Modifier.fillMaxSize()) {
            // ---------------------------------------------------------
            // 1. HEADER (Queue title + Close button)
            // ---------------------------------------------------------
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 12.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = "Queue",
                    fontSize = 17.sp,
                    fontWeight = FontWeight.Bold,
                    color = TextPrimary
                )

                val closeInteraction = remember { MutableInteractionSource() }
                val isCloseHovered by closeInteraction.collectIsHoveredAsState()

                Box(
                    modifier = Modifier
                        .size(32.dp)
                        .clip(CircleShape)
                        .background(if (isCloseHovered) Color(0x22FFFFFF) else Color.Transparent)
                        .hoverable(closeInteraction)
                        .clickable(onClick = onClose),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Rounded.Close,
                        contentDescription = "Close Queue",
                        tint = if (isCloseHovered) Color.White else Color(0xFFB3B3B3),
                        modifier = Modifier.size(20.dp)
                    )
                }
            }

            Text(
                text = "About recommendations and the impact of promotion",
                fontSize = 11.sp,
                color = Color(0xFF888888),
                modifier = Modifier.padding(bottom = 16.dp)
            )

            // ---------------------------------------------------------
            // 2. SCROLLABLE QUEUE LIST (Now Playing + Next Up)
            // ---------------------------------------------------------
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(bottom = 20.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                // Section: Now Playing
                if (currentTrack != null) {
                    item {
                        Text(
                            text = "Now playing",
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold,
                            color = TextPrimary,
                            modifier = Modifier.padding(vertical = 6.dp)
                        )

                        QueueTrackRow(
                            track = currentTrack,
                            isCurrent = true,
                            isPlaying = isPlaying,
                            onClick = {},
                            onMenuClick = { selectedMenuTrack = currentTrack }
                        )

                        Spacer(modifier = Modifier.height(16.dp))
                    }
                }

                // Section: Next Up
                item {
                    Text(
                        text = "Next up",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        color = TextPrimary,
                        modifier = Modifier.padding(vertical = 6.dp)
                    )
                }

                if (queueTracks.isEmpty()) {
                    item {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 32.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = "No songs currently in queue",
                                fontSize = 13.sp,
                                color = TextSecondary
                            )
                        }
                    }
                } else {
                    itemsIndexed(queueTracks) { _, track ->
                        QueueTrackRow(
                            track = track,
                            isCurrent = false,
                            isPlaying = false,
                            onClick = { onTrackClick(track) },
                            onMenuClick = { selectedMenuTrack = track }
                        )
                    }
                }
            }
        }

        // ---------------------------------------------------------
        // 3. SPOTIFY CONTEXT MENU POPUP (Screenshots 2 & 3)
        // ---------------------------------------------------------
        selectedMenuTrack?.let { track ->
            SpotifyQueueContextMenu(
                track = track,
                isLiked = isSongLiked(track.id),
                onToggleLike = { onToggleLikeSong(track) },
                onAddToQueue = { onAddToQueue(track) },
                onRemoveFromQueue = { onRemoveFromQueue(track) },
                onGoToArtist = { onGoToArtist(track.artist) },
                onGoToAlbum = { onGoToAlbum(track.album ?: track.title) },
                onCreatePlaylist = onCreatePlaylist,
                onDismiss = { selectedMenuTrack = null }
            )
        }
    }
}

@Composable
private fun QueueTrackRow(
    track: DisplayTrack,
    isCurrent: Boolean,
    isPlaying: Boolean,
    onClick: () -> Unit,
    onMenuClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val interactionSource = remember { MutableInteractionSource() }
    val isHovered by interactionSource.collectIsHoveredAsState()

    Row(
        modifier = modifier
            .fillMaxWidth()
            .height(56.dp)
            .clip(RoundedCornerShape(6.dp))
            .background(if (isHovered) Color(0x1FFFFFFF) else Color.Transparent)
            .hoverable(interactionSource)
            .clickable(onClick = onClick)
            .padding(horizontal = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        // Thumbnail + Optional Hover Play Icon
        Box(
            modifier = Modifier
                .size(44.dp)
                .clip(RoundedCornerShape(4.dp))
                .background(Color(0xFF282828)),
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

            if (isHovered && !isCurrent) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(Color(0x88000000)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Rounded.PlayArrow,
                        contentDescription = "Play",
                        tint = Color.White,
                        modifier = Modifier.size(22.dp)
                    )
                }
            }
        }

        // Title + Artist
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = track.title,
                fontSize = 13.sp,
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

        // Options Button (...)
        if (isHovered || isCurrent) {
            val menuInteraction = remember { MutableInteractionSource() }
            val isMenuHovered by menuInteraction.collectIsHoveredAsState()

            Box(
                modifier = Modifier
                    .size(32.dp)
                    .clip(CircleShape)
                    .background(if (isMenuHovered) Color(0x33FFFFFF) else Color.Transparent)
                    .hoverable(menuInteraction)
                    .clickable { onMenuClick() },
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Rounded.MoreHoriz,
                    contentDescription = "More Options",
                    tint = if (isMenuHovered) Color.White else Color(0xFFB3B3B3),
                    modifier = Modifier.size(20.dp)
                )
            }
        }
    }
}

/**
 * Spotify-styled Context Menu & Submenu Popup (Screenshots 2 & 3)
 */
@Composable
private fun SpotifyQueueContextMenu(
    track: DisplayTrack,
    isLiked: Boolean,
    onToggleLike: () -> Unit,
    onAddToQueue: () -> Unit,
    onRemoveFromQueue: () -> Unit,
    onGoToArtist: () -> Unit,
    onGoToAlbum: () -> Unit,
    onCreatePlaylist: () -> Unit,
    onDismiss: () -> Unit
) {
    var showPlaylistSubmenu by remember { mutableStateOf(false) }
    var playlistSearchQuery by remember { mutableStateOf("") }

    Popup(
        alignment = Alignment.CenterEnd,
        onDismissRequest = onDismiss,
        properties = PopupProperties(focusable = true)
    ) {
        Row(
            verticalAlignment = Alignment.Top,
            horizontalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            // -------------------------------------------------------------
            // SUB-MENU: Add to Playlist (Screenshot 3)
            // -------------------------------------------------------------
            if (showPlaylistSubmenu) {
                Box(
                    modifier = Modifier
                        .width(220.dp)
                        .shadow(16.dp, RoundedCornerShape(6.dp))
                        .clip(RoundedCornerShape(6.dp))
                        .background(Color(0xFF282828))
                        .padding(vertical = 6.dp)
                ) {
                    Column {
                        // Search "Find a playlist"
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 8.dp, vertical = 6.dp)
                                .clip(RoundedCornerShape(4.dp))
                                .background(Color(0xFF3E3E3E))
                                .padding(horizontal = 8.dp, vertical = 6.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Rounded.Search,
                                contentDescription = null,
                                tint = Color(0xFFB3B3B3),
                                modifier = Modifier.size(16.dp)
                            )
                            BasicTextField(
                                value = playlistSearchQuery,
                                onValueChange = { playlistSearchQuery = it },
                                singleLine = true,
                                textStyle = TextStyle(color = Color.White, fontSize = 12.sp),
                                cursorBrush = SolidColor(Color.White),
                                decorationBox = { innerTextField ->
                                    if (playlistSearchQuery.isEmpty()) {
                                        Text(
                                            text = "Find a playlist",
                                            fontSize = 12.sp,
                                            color = Color(0xFF888888)
                                        )
                                    }
                                    innerTextField()
                                }
                            )
                        }

                        // "+ New playlist"
                        ContextMenuItem(
                            icon = Icons.Rounded.Add,
                            label = "New playlist",
                            onClick = {
                                onDismiss()
                                onCreatePlaylist()
                            }
                        )
                    }
                }
            }

            // -------------------------------------------------------------
            // MAIN CONTEXT MENU (Screenshot 2)
            // -------------------------------------------------------------
            Box(
                modifier = Modifier
                    .width(240.dp)
                    .shadow(16.dp, RoundedCornerShape(6.dp))
                    .clip(RoundedCornerShape(6.dp))
                    .background(Color(0xFF282828))
                    .padding(vertical = 6.dp)
            ) {
                Column {
                    // 1. Add to playlist >
                    ContextMenuItem(
                        icon = Icons.Rounded.Add,
                        label = "Add to playlist",
                        hasSubmenu = true,
                        onClick = { showPlaylistSubmenu = !showPlaylistSubmenu }
                    )

                    // 2. Save to your Liked Songs
                    ContextMenuItem(
                        icon = Icons.Rounded.Favorite,
                        iconTint = if (isLiked) Color(0xFF1ED760) else Color(0xFFC4B5FD),
                        label = if (isLiked) "Remove from Liked Songs" else "Save to your Liked Songs",
                        onClick = {
                            onToggleLike()
                            onDismiss()
                        }
                    )

                    // 3. Add to queue
                    ContextMenuItem(
                        icon = Icons.AutoMirrored.Rounded.QueueMusic,
                        label = "Add to queue",
                        onClick = {
                            onAddToQueue()
                            onDismiss()
                        }
                    )

                    // 4. Remove from queue
                    ContextMenuItem(
                        icon = Icons.Rounded.DeleteOutline,
                        label = "Remove from queue",
                        onClick = {
                            onRemoveFromQueue()
                            onDismiss()
                        }
                    )

                    // 5. Exclude from your taste profile
                    ContextMenuItem(
                        icon = Icons.Rounded.Block,
                        label = "Exclude from your taste profile",
                        onClick = onDismiss
                    )

                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(1.dp)
                            .background(Color(0x22FFFFFF))
                            .padding(vertical = 4.dp)
                    )

                    // 6. Go to song radio
                    ContextMenuItem(
                        icon = Icons.Rounded.Radio,
                        label = "Go to song radio",
                        onClick = onDismiss
                    )

                    // 7. Go to artist
                    ContextMenuItem(
                        icon = Icons.Rounded.Person,
                        label = "Go to artist",
                        hasSubmenu = true,
                        onClick = {
                            onGoToArtist()
                            onDismiss()
                        }
                    )

                    // 8. Go to album
                    ContextMenuItem(
                        icon = Icons.Rounded.Album,
                        label = "Go to album",
                        onClick = {
                            onGoToAlbum()
                            onDismiss()
                        }
                    )

                    // 9. View credits
                    ContextMenuItem(
                        icon = Icons.Rounded.Info,
                        label = "View credits",
                        onClick = onDismiss
                    )

                    // 10. Share
                    ContextMenuItem(
                        icon = Icons.Rounded.Share,
                        label = "Share",
                        hasSubmenu = true,
                        onClick = onDismiss
                    )

                    // 11. Open in Desktop app
                    ContextMenuItem(
                        icon = Icons.Rounded.Laptop,
                        label = "Open in Desktop app",
                        onClick = onDismiss
                    )
                }
            }
        }
    }
}

@Composable
private fun ContextMenuItem(
    icon: ImageVector,
    label: String,
    iconTint: Color = Color(0xFFB3B3B3),
    hasSubmenu: Boolean = false,
    onClick: () -> Unit
) {
    val interactionSource = remember { MutableInteractionSource() }
    val isHovered by interactionSource.collectIsHoveredAsState()

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(38.dp)
            .background(if (isHovered) Color(0x22FFFFFF) else Color.Transparent)
            .hoverable(interactionSource)
            .clickable(onClick = onClick)
            .padding(horizontal = 12.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = if (isHovered) Color.White else iconTint,
                modifier = Modifier.size(16.dp)
            )
            Text(
                text = label,
                fontSize = 13.sp,
                color = if (isHovered) Color.White else TextPrimary,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }

        if (hasSubmenu) {
            Icon(
                imageVector = Icons.Rounded.ChevronRight,
                contentDescription = null,
                tint = if (isHovered) Color.White else Color(0xFFB3B3B3),
                modifier = Modifier.size(16.dp)
            )
        }
    }
}
