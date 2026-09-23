package com.music.echo.sharedui.component

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.material.icons.rounded.AspectRatio
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material.icons.rounded.DragIndicator
import androidx.compose.material.icons.rounded.Favorite
import androidx.compose.material.icons.rounded.FavoriteBorder
import androidx.compose.material.icons.rounded.MusicNote
import androidx.compose.material.icons.rounded.Pause
import androidx.compose.material.icons.rounded.PlayArrow
import androidx.compose.material.icons.rounded.SkipNext
import androidx.compose.material.icons.rounded.SkipPrevious
import androidx.compose.material.icons.rounded.ViewAgenda
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil3.compose.AsyncImage
import com.music.echo.sharedui.screens.DisplayTrack
import com.music.echo.sharedui.theme.DarkSurfaceVariant
import com.music.echo.sharedui.theme.NothingRed
import com.music.echo.sharedui.theme.PureBlack
import com.music.echo.sharedui.theme.TextPrimary
import com.music.echo.sharedui.theme.TextSecondary

enum class MiniPlayerStyle {
    SLIM_PILL,     // Middle style in the reference image
    SQUARE_CARD,   // Left style with large artwork
    OVERLAY_MEDIA  // Right style with playback controls overlay
}

@Composable
fun DesktopMiniPlayer(
    track: DisplayTrack,
    isPlaying: Boolean,
    currentPositionMs: Long = 0L,
    durationMs: Long = 0L,
    onPlayPauseClick: () -> Unit,
    onPreviousClick: () -> Unit,
    onNextClick: () -> Unit,
    onSeekTo: (Long) -> Unit = {},
    onClose: () -> Unit,
    onExpandFull: () -> Unit,
    modifier: Modifier = Modifier
) {
    var playerStyle by remember { mutableStateOf(MiniPlayerStyle.SLIM_PILL) }
    var isLiked by remember { mutableStateOf(false) }

    val progressFraction = if (durationMs > 0) (currentPositionMs.toFloat() / durationMs.toFloat()).coerceIn(0f, 1f) else 0f

    when (playerStyle) {
        // -------------------------------------------------------------
        // 1. SLIM PILL MINIPLAYER (Middle Style in Reference Image)
        // -------------------------------------------------------------
        MiniPlayerStyle.SLIM_PILL -> {
            Box(
                modifier = modifier
                    .shadow(elevation = 24.dp, shape = RoundedCornerShape(26.dp), spotColor = Color.Black.copy(alpha = 0.8f))
                    .clip(RoundedCornerShape(26.dp))
                    .background(Color(0xF018181A))
                    .border(1.dp, Color(0x38FFFFFF), RoundedCornerShape(26.dp))
                    .padding(horizontal = 14.dp, vertical = 8.dp)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    // Red Close Dot (🔴)
                    Box(
                        modifier = Modifier
                            .size(14.dp)
                            .clip(CircleShape)
                            .background(Color(0xFFFF5F56))
                            .clickable(onClick = onClose),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Rounded.Close,
                            contentDescription = "Close",
                            tint = Color.Black.copy(alpha = 0.7f),
                            modifier = Modifier.size(10.dp)
                        )
                    }

                    // Drag Grip Indicator
                    Icon(
                        imageVector = Icons.Rounded.DragIndicator,
                        contentDescription = "Drag Grip",
                        tint = Color(0x66FFFFFF),
                        modifier = Modifier.size(16.dp)
                    )

                    // Album Artwork Thumbnail
                    Box(
                        modifier = Modifier
                            .size(38.dp)
                            .clip(RoundedCornerShape(6.dp))
                            .background(DarkSurfaceVariant)
                            .clickable(onClick = onExpandFull),
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
                                modifier = Modifier.size(20.dp)
                            )
                        }
                    }

                    // Title and Artist
                    Column(
                        modifier = Modifier
                            .width(160.dp)
                            .clickable(onClick = onExpandFull),
                        verticalArrangement = Arrangement.Center
                    ) {
                        Text(
                            text = track.title.ifBlank { "Moonracer" },
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.Bold,
                            color = TextPrimary,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                        Text(
                            text = track.artist.ifBlank { "Tommi Waring" },
                            style = MaterialTheme.typography.bodySmall,
                            color = TextSecondary,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                            fontSize = 11.sp
                        )
                    }

                    // Style Switcher (to square card)
                    Box(
                        modifier = Modifier
                            .size(26.dp)
                            .clip(CircleShape)
                            .clickable { playerStyle = MiniPlayerStyle.SQUARE_CARD },
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Rounded.AspectRatio,
                            contentDescription = "Square Card Mode",
                            tint = TextSecondary,
                            modifier = Modifier.size(16.dp)
                        )
                    }

                    // Large White Play/Pause Circle Button
                    Box(
                        modifier = Modifier
                            .size(34.dp)
                            .shadow(elevation = 6.dp, shape = CircleShape)
                            .clip(CircleShape)
                            .background(Color.White)
                            .clickable(onClick = onPlayPauseClick),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = if (isPlaying) Icons.Rounded.Pause else Icons.Rounded.PlayArrow,
                            contentDescription = if (isPlaying) "Pause" else "Play",
                            tint = Color.Black,
                            modifier = Modifier.size(20.dp)
                        )
                    }

                    // Next Button
                    Box(
                        modifier = Modifier
                            .size(30.dp)
                            .clip(CircleShape)
                            .clickable(onClick = onNextClick),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Rounded.SkipNext,
                            contentDescription = "Next",
                            tint = TextPrimary,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }
            }
        }

        // -------------------------------------------------------------
        // 2. SQUARE CARD / OVERLAY MINIPLAYER (Left & Right Styles)
        // -------------------------------------------------------------
        MiniPlayerStyle.SQUARE_CARD, MiniPlayerStyle.OVERLAY_MEDIA -> {
            Box(
                modifier = modifier
                    .width(260.dp)
                    .shadow(elevation = 28.dp, shape = RoundedCornerShape(16.dp), spotColor = Color.Black.copy(alpha = 0.85f))
                    .clip(RoundedCornerShape(16.dp))
                    .background(Color(0xFF18181A))
                    .border(1.dp, Color(0x38FFFFFF), RoundedCornerShape(16.dp))
                    .padding(12.dp)
            ) {
                Column(modifier = Modifier.fillMaxWidth()) {
                    // Top Window Handle Row (Red dot, drag grip, style toggle)
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = 10.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Box(
                            modifier = Modifier
                                .size(14.dp)
                                .clip(CircleShape)
                                .background(Color(0xFFFF5F56))
                                .clickable(onClick = onClose),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Rounded.Close,
                                contentDescription = "Close",
                                tint = Color.Black.copy(alpha = 0.7f),
                                modifier = Modifier.size(10.dp)
                            )
                        }

                        Icon(
                            imageVector = Icons.Rounded.DragIndicator,
                            contentDescription = "Drag Grip",
                            tint = Color(0x66FFFFFF),
                            modifier = Modifier.size(16.dp)
                        )

                        Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                            // Toggle Pill vs Square vs Overlay
                            Box(
                                modifier = Modifier
                                    .size(24.dp)
                                    .clip(CircleShape)
                                    .clickable {
                                        playerStyle = if (playerStyle == MiniPlayerStyle.SQUARE_CARD) MiniPlayerStyle.OVERLAY_MEDIA else MiniPlayerStyle.SLIM_PILL
                                    },
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = if (playerStyle == MiniPlayerStyle.SQUARE_CARD) Icons.Rounded.ViewAgenda else Icons.Rounded.AspectRatio,
                                    contentDescription = "Toggle Mode",
                                    tint = TextSecondary,
                                    modifier = Modifier.size(16.dp)
                                )
                            }
                        }
                    }

                    // Center: Large Square Artwork
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .aspectRatio(1f)
                            .clip(RoundedCornerShape(10.dp))
                            .background(DarkSurfaceVariant)
                            .clickable(onClick = onExpandFull),
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
                                modifier = Modifier.size(48.dp)
                            )
                        }

                        // If OVERLAY_MEDIA mode: Show transport controls over artwork
                        if (playerStyle == MiniPlayerStyle.OVERLAY_MEDIA) {
                            Box(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .background(Color.Black.copy(alpha = 0.45f)),
                                contentAlignment = Alignment.Center
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(14.dp)
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .size(36.dp)
                                            .clip(CircleShape)
                                            .background(Color.Black.copy(alpha = 0.4f))
                                            .clickable(onClick = onPreviousClick),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Icon(
                                            imageVector = Icons.Rounded.SkipPrevious,
                                            contentDescription = "Previous",
                                            tint = Color.White,
                                            modifier = Modifier.size(20.dp)
                                        )
                                    }

                                    Box(
                                        modifier = Modifier
                                            .size(46.dp)
                                            .clip(CircleShape)
                                            .background(Color.White)
                                            .clickable(onClick = onPlayPauseClick),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Icon(
                                            imageVector = if (isPlaying) Icons.Rounded.Pause else Icons.Rounded.PlayArrow,
                                            contentDescription = if (isPlaying) "Pause" else "Play",
                                            tint = Color.Black,
                                            modifier = Modifier.size(24.dp)
                                        )
                                    }

                                    Box(
                                        modifier = Modifier
                                            .size(36.dp)
                                            .clip(CircleShape)
                                            .background(Color.Black.copy(alpha = 0.4f))
                                            .clickable(onClick = onNextClick),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Icon(
                                            imageVector = Icons.Rounded.SkipNext,
                                            contentDescription = "Next",
                                            tint = Color.White,
                                            modifier = Modifier.size(20.dp)
                                        )
                                    }
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    // Bottom Track Title & Subtitle
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = track.title,
                                style = MaterialTheme.typography.bodyMedium,
                                fontWeight = FontWeight.Bold,
                                color = TextPrimary,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                text = track.artist,
                                style = MaterialTheme.typography.bodySmall,
                                color = TextSecondary,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis,
                                fontSize = 11.sp
                            )
                        }

                        // Add to Library (Favorite Icon)
                        Box(
                            modifier = Modifier
                                .size(30.dp)
                                .clip(CircleShape)
                                .clickable { isLiked = !isLiked },
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = if (isLiked) Icons.Rounded.Favorite else Icons.Rounded.FavoriteBorder,
                                contentDescription = if (isLiked) "Favorited" else "Favorite",
                                tint = if (isLiked) NothingRed else TextSecondary,
                                modifier = Modifier.size(18.dp)
                            )
                        }
                    }

                    // Progress Bar Line
                    Spacer(modifier = Modifier.height(8.dp))
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(3.dp)
                            .clip(RoundedCornerShape(2.dp))
                            .background(Color.White.copy(alpha = 0.15f))
                    ) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth(progressFraction)
                                .height(3.dp)
                                .clip(RoundedCornerShape(2.dp))
                                .background(Color.White)
                        )
                    }
                }
            }
        }
    }
}
