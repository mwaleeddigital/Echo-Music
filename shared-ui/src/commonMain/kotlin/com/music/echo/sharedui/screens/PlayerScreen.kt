package com.music.echo.sharedui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Favorite
import androidx.compose.material.icons.rounded.ArrowDownward
import androidx.compose.material.icons.rounded.CheckCircle
import androidx.compose.material.icons.rounded.FavoriteBorder
import androidx.compose.material.icons.rounded.KeyboardArrowDown
import androidx.compose.material.icons.rounded.Mic
import androidx.compose.material.icons.rounded.MusicNote
import androidx.compose.material.icons.rounded.Pause
import androidx.compose.material.icons.rounded.PlayArrow
import androidx.compose.material.icons.rounded.Repeat
import androidx.compose.material.icons.rounded.Shuffle
import androidx.compose.material.icons.rounded.SkipNext
import androidx.compose.material.icons.rounded.SkipPrevious
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
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
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil3.compose.AsyncImage
import com.music.echo.sharedui.theme.DarkSurfaceElevated
import com.music.echo.sharedui.theme.DarkSurfaceVariant
import com.music.echo.sharedui.theme.NothingRed
import com.music.echo.sharedui.theme.PureBlack
import com.music.echo.sharedui.theme.TextPrimary
import com.music.echo.sharedui.theme.TextSecondary

@Composable
fun PlayerScreen(
    track: DisplayTrack,
    isPlaying: Boolean,
    currentPositionMs: Long = 0L,
    durationMs: Long = 0L,
    onPlayPauseClick: () -> Unit,
    onPreviousClick: () -> Unit,
    onNextClick: () -> Unit,
    isLiked: Boolean = false,
    isDownloaded: Boolean = false,
    onToggleLike: () -> Unit = {},
    onDownload: () -> Unit = {},
    onSeekTo: (Long) -> Unit = {},
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier
) {
    var isShuffle by remember { mutableStateOf(false) }
    var isRepeat by remember { mutableStateOf(false) }
    var showLyrics by remember { mutableStateOf(false) }

    var isDraggingSlider by remember { mutableStateOf(false) }
    var dragSliderFraction by remember { mutableFloatStateOf(0f) }

    val actualFraction = if (durationMs > 0) (currentPositionMs.toFloat() / durationMs.toFloat()).coerceIn(0f, 1f) else 0f
    val displayFraction = if (isDraggingSlider) dragSliderFraction else actualFraction

    fun formatTime(ms: Long): String {
        val totalSec = ms / 1000
        val m = totalSec / 60
        val s = totalSec % 60
        return "$m:${s.toString().padStart(2, '0')}"
    }

    val currentTimeText = formatTime(if (isDraggingSlider && durationMs > 0) (dragSliderFraction * durationMs).toLong() else currentPositionMs)
    val durationText = if (durationMs > 0) formatTime(durationMs) else track.duration.ifBlank { "--:--" }

    // Ambient background gradient
    val bgBrush = Brush.verticalGradient(
        colors = listOf(
            Color(0xFF281114),
            Color(0xFF14080A),
            PureBlack,
            PureBlack
        )
    )

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(bgBrush)
            .padding(horizontal = 28.dp, vertical = 24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.SpaceBetween
    ) {
        // Top Navigation Bar
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(42.dp)
                    .clip(CircleShape)
                    .background(DarkSurfaceElevated)
                    .clickable(onClick = onDismiss),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Rounded.KeyboardArrowDown,
                    contentDescription = "Dismiss Player",
                    tint = TextPrimary,
                    modifier = Modifier.size(28.dp)
                )
            }

            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(
                    text = "PLAYING FROM ECHO",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 1.5.sp,
                    color = NothingRed
                )
            }

            Box(
                modifier = Modifier
                    .size(42.dp)
                    .clip(CircleShape)
                    .background(if (showLyrics) NothingRed.copy(alpha = 0.2f) else DarkSurfaceElevated)
                    .clickable { showLyrics = !showLyrics },
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Rounded.Mic,
                    contentDescription = "Lyrics",
                    tint = if (showLyrics) NothingRed else TextPrimary,
                    modifier = Modifier.size(20.dp)
                )
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        // Center Area: Album Artwork OR Lyrics
        if (showLyrics) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(340.dp)
                    .clip(RoundedCornerShape(24.dp))
                    .background(Color(0xEE1E1E22))
                    .padding(24.dp),
                contentAlignment = Alignment.Center
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    Text(
                        text = "Lyrics syncing enabled",
                        style = MaterialTheme.typography.bodyLarge,
                        color = TextSecondary,
                        textAlign = TextAlign.Center
                    )
                    Text(
                        text = track.title,
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        color = NothingRed,
                        textAlign = TextAlign.Center
                    )
                    Text(
                        text = "Echo Music • Multiplatform Player",
                        style = MaterialTheme.typography.bodyMedium,
                        color = TextSecondary,
                        textAlign = TextAlign.Center
                    )
                }
            }
        } else {
            // Large Album Artwork
            Box(
                modifier = Modifier
                    .fillMaxWidth(0.9f)
                    .aspectRatio(1f)
                    .shadow(elevation = 28.dp, shape = RoundedCornerShape(24.dp), spotColor = NothingRed.copy(alpha = 0.35f))
                    .clip(RoundedCornerShape(24.dp))
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
                    Icon(
                        imageVector = Icons.Rounded.MusicNote,
                        contentDescription = null,
                        tint = NothingRed,
                        modifier = Modifier.size(64.dp)
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Title & Artist with Favorite Heart
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = track.title,
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold,
                    color = TextPrimary,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Spacer(modifier = Modifier.height(3.dp))
                Text(
                    text = track.artist,
                    style = MaterialTheme.typography.titleMedium,
                    color = TextSecondary,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }

            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                // Download Button
                Box(
                    modifier = Modifier
                        .size(44.dp)
                        .clip(CircleShape)
                        .background(if (isDownloaded) Color(0xFF1ED760).copy(alpha = 0.2f) else DarkSurfaceElevated)
                        .clickable { onDownload() },
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = if (isDownloaded) Icons.Rounded.CheckCircle else Icons.Rounded.ArrowDownward,
                        contentDescription = if (isDownloaded) "Downloaded" else "Download",
                        tint = if (isDownloaded) Color(0xFF1ED760) else TextSecondary,
                        modifier = Modifier.size(24.dp)
                    )
                }

                // Favorite Heart
                Box(
                    modifier = Modifier
                        .size(44.dp)
                        .clip(CircleShape)
                        .background(if (isLiked) NothingRed.copy(alpha = 0.2f) else DarkSurfaceElevated)
                        .clickable { onToggleLike() },
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = if (isLiked) Icons.Rounded.Favorite else Icons.Rounded.FavoriteBorder,
                        contentDescription = if (isLiked) "Remove from Favorites" else "Add to Favorites",
                        tint = if (isLiked) NothingRed else TextSecondary,
                        modifier = Modifier.size(24.dp)
                    )
                }
            }
        }

        // Seeker Bar & Timestamps
        Column(modifier = Modifier.fillMaxWidth()) {
            Slider(
                value = displayFraction,
                onValueChange = {
                    isDraggingSlider = true
                    dragSliderFraction = it
                },
                onValueChangeFinished = {
                    if (durationMs > 0) {
                        onSeekTo((dragSliderFraction * durationMs).toLong())
                    }
                    isDraggingSlider = false
                },
                colors = SliderDefaults.colors(
                    thumbColor = NothingRed,
                    activeTrackColor = NothingRed,
                    inactiveTrackColor = Color.White.copy(alpha = 0.15f)
                ),
                modifier = Modifier.fillMaxWidth()
            )
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(text = currentTimeText, style = MaterialTheme.typography.labelMedium, color = TextSecondary)
                Text(text = durationText, style = MaterialTheme.typography.labelMedium, color = TextSecondary)
            }
        }

        // Playback Controls
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 10.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Shuffle
            Box(
                modifier = Modifier
                    .size(44.dp)
                    .clip(CircleShape)
                    .clickable { isShuffle = !isShuffle },
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Rounded.Shuffle,
                    contentDescription = "Shuffle",
                    tint = if (isShuffle) NothingRed else TextSecondary,
                    modifier = Modifier.size(22.dp)
                )
            }

            // Previous
            Box(
                modifier = Modifier
                    .size(52.dp)
                    .clip(CircleShape)
                    .background(DarkSurfaceElevated)
                    .clickable(onClick = onPreviousClick),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Rounded.SkipPrevious,
                    contentDescription = "Previous Track",
                    tint = TextPrimary,
                    modifier = Modifier.size(28.dp)
                )
            }

            // Play / Pause (Big Central Action)
            Box(
                modifier = Modifier
                    .size(68.dp)
                    .shadow(elevation = 16.dp, shape = CircleShape, spotColor = NothingRed.copy(alpha = 0.5f))
                    .clip(CircleShape)
                    .background(NothingRed)
                    .clickable(onClick = onPlayPauseClick),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = if (isPlaying) Icons.Rounded.Pause else Icons.Rounded.PlayArrow,
                    contentDescription = if (isPlaying) "Pause" else "Play",
                    tint = Color.White,
                    modifier = Modifier.size(36.dp)
                )
            }

            // Next
            Box(
                modifier = Modifier
                    .size(52.dp)
                    .clip(CircleShape)
                    .background(DarkSurfaceElevated)
                    .clickable(onClick = onNextClick),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Rounded.SkipNext,
                    contentDescription = "Next Track",
                    tint = TextPrimary,
                    modifier = Modifier.size(28.dp)
                )
            }

            // Repeat
            Box(
                modifier = Modifier
                    .size(44.dp)
                    .clip(CircleShape)
                    .clickable { isRepeat = !isRepeat },
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Rounded.Repeat,
                    contentDescription = "Repeat",
                    tint = if (isRepeat) NothingRed else TextSecondary,
                    modifier = Modifier.size(22.dp)
                )
            }
        }
    }
}
