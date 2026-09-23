package com.music.echo.sharedui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.QueueMusic
import androidx.compose.material.icons.rounded.ChevronRight
import androidx.compose.material.icons.rounded.Download
import androidx.compose.material.icons.rounded.Favorite
import androidx.compose.material.icons.rounded.History
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.music.echo.sharedui.component.SongItem
import com.music.echo.sharedui.theme.DarkSurfaceElevated
import com.music.echo.sharedui.theme.NothingRed
import com.music.echo.sharedui.theme.PureBlack
import com.music.echo.sharedui.theme.TextPrimary
import com.music.echo.sharedui.theme.TextSecondary

data class LibraryCategory(
    val title: String,
    val countDescription: String,
    val icon: ImageVector
)

@Composable
fun LibraryScreen(
    currentTrackId: String? = null,
    downloadedSongs: List<DisplayTrack> = emptyList(),
    likedSongs: List<DisplayTrack> = emptyList(),
    historySongs: List<DisplayTrack> = emptyList(),
    storageStats: String = "0 songs • 0 MB",
    onTrackClick: (DisplayTrack) -> Unit,
    onLikedSongsClick: () -> Unit = {},
    onDownloadedSongsClick: () -> Unit = {},
    onHistoryClick: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    val categories = listOf(
        LibraryCategory("Liked Songs", "${likedSongs.size} songs • Auto playlist", Icons.Rounded.Favorite),
        LibraryCategory("Downloaded Music", "${downloadedSongs.size} songs • $storageStats", Icons.Rounded.Download),
        LibraryCategory("Listening History", "${historySongs.size} songs • Recently played", Icons.Rounded.History),
        LibraryCategory("Playlists", "Saved & Created", Icons.AutoMirrored.Rounded.QueueMusic)
    )

    val displayTracks = if (downloadedSongs.isNotEmpty()) {
        downloadedSongs.take(20)
    } else if (historySongs.isNotEmpty()) {
        historySongs.take(20)
    } else {
        likedSongs.take(20)
    }

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .background(PureBlack),
        contentPadding = PaddingValues(bottom = 140.dp)
    ) {
        // Title
        item {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp, vertical = 18.dp)
            ) {
                Text(
                    text = "LIBRARY",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 2.sp,
                    color = NothingRed
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "Your Collection",
                    style = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.Bold,
                    color = TextPrimary
                )
            }
        }

        // Category Cards Grid/List
        items(categories) { cat ->
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp, vertical = 6.dp)
                    .clip(RoundedCornerShape(16.dp))
                    .background(Color(0xFF161618))
                    .clickable {
                        when (cat.title) {
                            "Liked Songs" -> onLikedSongsClick()
                            "Downloaded Music" -> onDownloadedSongsClick()
                            "Listening History" -> onHistoryClick()
                        }
                    }
                    .padding(16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(46.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(NothingRed.copy(alpha = 0.15f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = cat.icon,
                        contentDescription = cat.title,
                        tint = NothingRed,
                        modifier = Modifier.size(22.dp)
                    )
                }

                Spacer(modifier = Modifier.width(16.dp))

                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = cat.title,
                        style = MaterialTheme.typography.bodyLarge,
                        fontWeight = FontWeight.Bold,
                        color = TextPrimary
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = cat.countDescription,
                        style = MaterialTheme.typography.bodyMedium,
                        color = TextSecondary
                    )
                }

                Icon(
                    imageVector = Icons.Rounded.ChevronRight,
                    contentDescription = null,
                    tint = TextSecondary,
                    modifier = Modifier.size(22.dp)
                )
            }
        }

        item { Spacer(modifier = Modifier.height(24.dp)) }

        // Recently Liked / Downloaded
        item {
            Text(
                text = if (downloadedSongs.isNotEmpty()) "DOWNLOADED & OFFLINE SONGS" else "RECENT ACTIVITY",
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold,
                letterSpacing = 1.5.sp,
                color = TextSecondary,
                modifier = Modifier.padding(horizontal = 20.dp, vertical = 8.dp)
            )
        }

        if (displayTracks.isEmpty()) {
            item {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(32.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "Play songs online to automatically download and cache them for offline listening.",
                        color = TextSecondary,
                        fontSize = 13.sp
                    )
                }
            }
        } else {
            items(displayTracks) { track ->
                SongItem(
                    title = track.title,
                    artist = track.artist,
                    duration = track.duration,
                    thumbnailUrl = track.thumbnailUrl,
                    isPlaying = (currentTrackId == track.id),
                    onClick = { onTrackClick(track) },
                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 2.dp)
                )
            }
        }
    }
}
