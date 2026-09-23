package com.music.echo.sharedui.component

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.hoverable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsHoveredAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.QueueMusic
import androidx.compose.material.icons.automirrored.rounded.VolumeDown
import androidx.compose.material.icons.automirrored.rounded.VolumeMute
import androidx.compose.material.icons.automirrored.rounded.VolumeUp
import androidx.compose.material.icons.rounded.AddCircleOutline
import androidx.compose.material.icons.rounded.Check
import androidx.compose.material.icons.rounded.Devices
import androidx.compose.material.icons.rounded.Favorite
import androidx.compose.material.icons.rounded.Fullscreen
import androidx.compose.material.icons.rounded.Laptop
import androidx.compose.material.icons.rounded.Mic
import androidx.compose.material.icons.rounded.Pause
import androidx.compose.material.icons.rounded.PictureInPictureAlt
import androidx.compose.material.icons.rounded.PlayArrow
import androidx.compose.material.icons.rounded.Repeat
import androidx.compose.material.icons.rounded.Shuffle
import androidx.compose.material.icons.rounded.SkipNext
import androidx.compose.material.icons.rounded.SkipPrevious
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Popup
import androidx.compose.ui.window.PopupProperties
import coil3.compose.AsyncImage
import com.music.echo.sharedui.screens.DisplayTrack
import com.music.echo.sharedui.theme.DarkSurfaceElevated
import com.music.echo.sharedui.theme.DarkSurfaceVariant
import com.music.echo.sharedui.theme.NothingRed
import com.music.echo.sharedui.theme.PureBlack
import com.music.echo.sharedui.theme.TextPrimary
import com.music.echo.sharedui.theme.TextSecondary

@Composable
fun DesktopBottomPlayer(
    track: DisplayTrack?,
    isPlaying: Boolean,
    currentPositionMs: Long = 0L,
    durationMs: Long = 0L,
    currentVolume: Float = 0.85f,
    onVolumeChange: (Float) -> Unit = {},
    isLiked: Boolean = false,
    onToggleLike: () -> Unit = {},
    onPlayPauseClick: () -> Unit,
    onPreviousClick: () -> Unit,
    onNextClick: () -> Unit,
    onSeekTo: (Long) -> Unit = {},
    onLyricsToggle: () -> Unit = {},
    isLyricsActive: Boolean = false,
    onQueueToggle: () -> Unit = {},
    isQueueActive: Boolean = false,
    onMiniPlayerToggle: () -> Unit = {},
    isMiniPlayerActive: Boolean = false,
    onFullscreenToggle: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    var isShuffle by remember { mutableStateOf(false) }
    var isRepeat by remember { mutableStateOf(false) }
    var showDevicePicker by remember { mutableStateOf(false) }
    var previousVolume by remember { mutableFloatStateOf(0.85f) }

    fun formatTime(ms: Long): String {
        val totalSec = (ms / 1000).coerceAtLeast(0)
        val m = totalSec / 60
        val s = totalSec % 60
        return "$m:${s.toString().padStart(2, '0')}"
    }

    val actualFraction = if (durationMs > 0) (currentPositionMs.toFloat() / durationMs.toFloat()).coerceIn(0f, 1f) else 0f
    val durationText = if (durationMs > 0) formatTime(durationMs) else track?.duration?.ifBlank { "0:00" } ?: "0:00"

    Row(
        modifier = modifier
            .fillMaxWidth()
            .height(88.dp)
            .background(PureBlack)
            .padding(horizontal = 18.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        // ----------------------------------------------------
        // 1. LEFT: Track Thumbnail, Title, Artist, and Favorite
        // ----------------------------------------------------
        Row(
            modifier = Modifier.width(300.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            if (track != null) {
                // Cover Art
                Box(
                    modifier = Modifier
                        .size(56.dp)
                        .clip(RoundedCornerShape(6.dp))
                        .background(DarkSurfaceVariant),
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
                        Text(
                            text = if (track.title.isNotEmpty()) track.title.take(1).uppercase() else "♪",
                            color = NothingRed,
                            fontWeight = FontWeight.Bold,
                            fontSize = 20.sp
                        )
                    }
                }

                Spacer(modifier = Modifier.width(14.dp))

                // Title and Artist
                Column(
                    modifier = Modifier.weight(1f, fill = false),
                    verticalArrangement = Arrangement.Center
                ) {
                    Text(
                        text = track.title.ifBlank { "No Song Selected" },
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Bold,
                        color = TextPrimary,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = track.artist.ifBlank { "Echo Music" },
                        style = MaterialTheme.typography.bodySmall,
                        color = TextSecondary,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }

                Spacer(modifier = Modifier.width(12.dp))

                // Favorite Icon
                Box(
                    modifier = Modifier
                        .size(34.dp)
                        .clip(CircleShape)
                        .clickable { onToggleLike() },
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = if (isLiked) Icons.Rounded.Favorite else Icons.Rounded.AddCircleOutline,
                        contentDescription = if (isLiked) "Remove from Liked Songs" else "Save to Liked Songs",
                        tint = if (isLiked) Color(0xFF1ED760) else TextSecondary,
                        modifier = Modifier.size(20.dp)
                    )
                }
            } else {
                Text(
                    text = "Select a song to start playing",
                    style = MaterialTheme.typography.bodyMedium,
                    color = TextSecondary
                )
            }
        }

        // ----------------------------------------------------
        // 2. CENTER: Transport Controls + Progress Seek Slider
        // ----------------------------------------------------
        Column(
            modifier = Modifier.width(540.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            // Controls row
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(20.dp)
            ) {
                // Shuffle
                Box(
                    modifier = Modifier
                        .size(32.dp)
                        .clip(CircleShape)
                        .clickable { isShuffle = !isShuffle },
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Rounded.Shuffle,
                        contentDescription = "Shuffle",
                        tint = if (isShuffle) Color(0xFF1ED760) else TextSecondary,
                        modifier = Modifier.size(18.dp)
                    )
                }

                // Previous
                Box(
                    modifier = Modifier
                        .size(32.dp)
                        .clip(CircleShape)
                        .clickable(onClick = onPreviousClick),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Rounded.SkipPrevious,
                        contentDescription = "Previous",
                        tint = Color.White,
                        modifier = Modifier.size(24.dp)
                    )
                }

                // Big White Circular Play/Pause Button
                Box(
                    modifier = Modifier
                        .size(36.dp)
                        .shadow(elevation = 6.dp, shape = CircleShape)
                        .clip(CircleShape)
                        .background(Color.White)
                        .clickable(onClick = onPlayPauseClick),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = if (isPlaying) Icons.Rounded.Pause else Icons.Rounded.PlayArrow,
                        contentDescription = if (isPlaying) "Pause" else "Play",
                        tint = PureBlack,
                        modifier = Modifier.size(22.dp)
                    )
                }

                // Next
                Box(
                    modifier = Modifier
                        .size(32.dp)
                        .clip(CircleShape)
                        .clickable(onClick = onNextClick),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Rounded.SkipNext,
                        contentDescription = "Next",
                        tint = Color.White,
                        modifier = Modifier.size(24.dp)
                    )
                }

                // Repeat
                Box(
                    modifier = Modifier
                        .size(32.dp)
                        .clip(CircleShape)
                        .clickable { isRepeat = !isRepeat },
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Rounded.Repeat,
                        contentDescription = "Repeat",
                        tint = if (isRepeat) Color(0xFF1ED760) else TextSecondary,
                        modifier = Modifier.size(18.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(4.dp))

            // Progress Bar Row (Timestamp - Custom Slim Slider - Timestamp)
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Text(
                    text = formatTime(currentPositionMs),
                    style = MaterialTheme.typography.labelMedium,
                    color = TextSecondary,
                    fontSize = 11.sp,
                    textAlign = androidx.compose.ui.text.style.TextAlign.End,
                    modifier = Modifier.width(42.dp)
                )

                SpotifySlimSlider(
                    fraction = actualFraction,
                    onSeek = { newFraction ->
                        if (durationMs > 0) {
                            onSeekTo((newFraction * durationMs).toLong())
                        }
                    },
                    activeColor = Color.White,
                    hoverColor = Color(0xFF1ED760),
                    modifier = Modifier.weight(1f).height(16.dp)
                )

                Text(
                    text = durationText,
                    style = MaterialTheme.typography.labelMedium,
                    color = TextSecondary,
                    fontSize = 11.sp,
                    textAlign = androidx.compose.ui.text.style.TextAlign.Start,
                    modifier = Modifier.width(42.dp)
                )
            }
        }

        // ----------------------------------------------------
        // 3. RIGHT: Queue, Device, Volume & Fullscreen
        // ----------------------------------------------------
        Row(
            modifier = Modifier.width(280.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(10.dp, Alignment.End)
        ) {
            // Queue Icon
            Box(
                modifier = Modifier
                    .size(32.dp)
                    .clip(CircleShape)
                    .clickable(onClick = onQueueToggle),
                contentAlignment = Alignment.Center
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Rounded.QueueMusic,
                        contentDescription = "Queue",
                        tint = if (isQueueActive) Color(0xFF1ED760) else TextSecondary,
                        modifier = Modifier.size(18.dp)
                    )
                    if (isQueueActive) {
                        Spacer(modifier = Modifier.height(2.dp))
                        Box(
                            modifier = Modifier
                                .size(4.dp)
                                .clip(CircleShape)
                                .background(Color(0xFF1ED760))
                        )
                    }
                }
            }

            // Connect to Device (Interactive Button & Popup)
            Box(
                modifier = Modifier
                    .size(32.dp)
                    .clip(CircleShape)
                    .background(if (showDevicePicker) Color(0xFF1ED760).copy(alpha = 0.2f) else Color.Transparent)
                    .clickable { showDevicePicker = !showDevicePicker },
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Rounded.Devices,
                    contentDescription = "Connect to a device",
                    tint = if (showDevicePicker) Color(0xFF1ED760) else TextSecondary,
                    modifier = Modifier.size(18.dp)
                )

                // Device Picker Popup
                if (showDevicePicker) {
                    Popup(
                        alignment = Alignment.TopCenter,
                        onDismissRequest = { showDevicePicker = false },
                        properties = PopupProperties(focusable = true)
                    ) {
                        Box(
                            modifier = Modifier
                                .padding(bottom = 8.dp)
                                .width(260.dp)
                                .shadow(16.dp, RoundedCornerShape(12.dp))
                                .clip(RoundedCornerShape(12.dp))
                                .background(DarkSurfaceElevated)
                                .padding(16.dp)
                        ) {
                            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Rounded.Laptop,
                                        contentDescription = null,
                                        tint = Color(0xFF1ED760),
                                        modifier = Modifier.size(24.dp)
                                    )
                                    Column {
                                        Text(
                                            text = "Current Device",
                                            fontSize = 11.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = Color(0xFF1ED760)
                                        )
                                        Text(
                                            text = "This Computer (Echo Desktop)",
                                            fontSize = 13.sp,
                                            fontWeight = FontWeight.SemiBold,
                                            color = TextPrimary
                                        )
                                    }
                                }

                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(1.dp)
                                        .background(Color(0x22FFFFFF))
                                )

                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Text(
                                        text = "High Quality Audio (320kbps)",
                                        fontSize = 12.sp,
                                        color = TextSecondary
                                    )
                                    Icon(
                                        imageVector = Icons.Rounded.Check,
                                        contentDescription = "Active",
                                        tint = Color(0xFF1ED760),
                                        modifier = Modifier.size(16.dp)
                                    )
                                }
                            }
                        }
                    }
                }
            }

            // Volume Icon + Custom Slim Slider
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                val volumeIcon = when {
                    currentVolume > 0.5f -> Icons.AutoMirrored.Rounded.VolumeUp
                    currentVolume > 0.01f -> Icons.AutoMirrored.Rounded.VolumeDown
                    else -> Icons.AutoMirrored.Rounded.VolumeMute
                }

                Icon(
                    imageVector = volumeIcon,
                    contentDescription = "Volume",
                    tint = if (currentVolume > 0f) TextSecondary else NothingRed,
                    modifier = Modifier
                        .size(18.dp)
                        .clickable {
                            if (currentVolume > 0f) {
                                previousVolume = currentVolume
                                onVolumeChange(0f)
                            } else {
                                onVolumeChange(if (previousVolume > 0f) previousVolume else 0.85f)
                            }
                        }
                )

                SpotifySlimSlider(
                    fraction = currentVolume.coerceIn(0f, 1f),
                    onSeek = { newVol ->
                        onVolumeChange(newVol.coerceIn(0f, 1f))
                    },
                    activeColor = Color.White,
                    hoverColor = Color(0xFF1ED760),
                    modifier = Modifier.width(90.dp).height(16.dp)
                )
            }

            // Fullscreen / Expand
            Box(
                modifier = Modifier
                    .size(32.dp)
                    .clip(CircleShape)
                    .clickable(onClick = onFullscreenToggle),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Rounded.Fullscreen,
                    contentDescription = "Fullscreen",
                    tint = TextSecondary,
                    modifier = Modifier.size(20.dp)
                )
            }
        }
    }
}

/**
 * Spotify-styled Slim Slider:
 * - Ultra-thin 4dp track
 * - Smooth active fill
 * - Circular 10dp thumb appears on hover or drag, perfectly aligned with progress
 */
@Composable
private fun SpotifySlimSlider(
    fraction: Float,
    onSeek: (Float) -> Unit,
    activeColor: Color = Color.White,
    hoverColor: Color = Color(0xFF1ED760),
    inactiveColor: Color = Color(0x33FFFFFF),
    modifier: Modifier = Modifier
) {
    val interactionSource = remember { MutableInteractionSource() }
    val isHovered by interactionSource.collectIsHoveredAsState()
    var isDragging by remember { mutableStateOf(false) }
    var widthPx by remember { mutableFloatStateOf(1f) }
    val density = androidx.compose.ui.platform.LocalDensity.current

    val clampedFraction = fraction.coerceIn(0f, 1f)
    val trackColor = if (isHovered || isDragging) hoverColor else activeColor

    Box(
        modifier = modifier
            .hoverable(interactionSource)
            .onSizeChanged { widthPx = it.width.toFloat().coerceAtLeast(1f) }
            .pointerInput(Unit) {
                detectTapGestures { offset ->
                    val newFrac = (offset.x / widthPx).coerceIn(0f, 1f)
                    onSeek(newFrac)
                }
            }
            .pointerInput(Unit) {
                detectDragGestures(
                    onDragStart = { offset ->
                        isDragging = true
                        val newFrac = (offset.x / widthPx).coerceIn(0f, 1f)
                        onSeek(newFrac)
                    },
                    onDragEnd = { isDragging = false },
                    onDragCancel = { isDragging = false },
                    onDrag = { change, _ ->
                        change.consume()
                        val newFrac = (change.position.x / widthPx).coerceIn(0f, 1f)
                        onSeek(newFrac)
                    }
                )
            },
        contentAlignment = Alignment.CenterStart
    ) {
        // Inactive background bar
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(4.dp)
                .clip(RoundedCornerShape(2.dp))
                .background(inactiveColor)
        )

        // Active filled progress bar
        Box(
            modifier = Modifier
                .fillMaxWidth(clampedFraction)
                .height(4.dp)
                .clip(RoundedCornerShape(2.dp))
                .background(trackColor)
        )

        // Thumb indicator (Visible when hovered or dragged)
        if (isHovered || isDragging) {
            val thumbRadiusDp = 5.dp
            val thumbRadiusPx = with(density) { thumbRadiusDp.toPx() }
            val centerPx = widthPx * clampedFraction
            val offsetPx = (centerPx - thumbRadiusPx).coerceIn(0f, (widthPx - thumbRadiusPx * 2f).coerceAtLeast(0f))
            val offsetDp = with(density) { offsetPx.toDp() }

            Box(
                modifier = Modifier
                    .padding(start = offsetDp)
                    .size(10.dp)
                    .shadow(4.dp, CircleShape)
                    .clip(CircleShape)
                    .background(Color.White)
            )
        }
    }
}

