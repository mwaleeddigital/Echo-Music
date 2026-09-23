package com.music.echo.sharedui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.PlayArrow
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.music.echo.sharedui.component.AlbumCard
import com.music.echo.sharedui.component.ArtistCard
import com.music.echo.sharedui.component.MediaCard
import com.music.echo.sharedui.component.QuickAccessCard
import com.music.echo.sharedui.component.SongItem
import com.music.echo.sharedui.theme.DarkSurfaceElevated
import com.music.echo.sharedui.theme.NothingRed
import com.music.echo.sharedui.theme.PureBlack
import com.music.echo.sharedui.theme.TextPrimary
import com.music.echo.sharedui.theme.TextSecondary

data class DisplayTrack(
    val id: String,
    val title: String,
    val artist: String,
    val duration: String = "",
    val thumbnailUrl: String? = null,
    val album: String? = null,
    val isVideo: Boolean = false
)

data class DisplayAlbum(
    val id: String,
    val title: String,
    val artist: String,
    val year: String? = null,
    val thumbnailUrl: String? = null
)

data class DisplayPlaylist(
    val id: String,
    val title: String,
    val author: String = "",
    val songCountText: String? = null,
    val thumbnailUrl: String? = null
)

data class DisplayArtist(
    val id: String,
    val name: String,
    val thumbnailUrl: String? = null
)

data class SearchResultsData(
    val songs: List<DisplayTrack> = emptyList(),
    val playlists: List<DisplayPlaylist> = emptyList(),
    val albums: List<DisplayAlbum> = emptyList(),
    val artists: List<DisplayArtist> = emptyList()
) {
    val isEmpty: Boolean get() = songs.isEmpty() && playlists.isEmpty() && albums.isEmpty() && artists.isEmpty()
    val totalCount: Int get() = songs.size + playlists.size + albums.size + artists.size
}

data class SearchSuggestionsData(
    val queries: List<String> = emptyList(),
    val recommendedArtists: List<DisplayArtist> = emptyList()
) {
    val isEmpty: Boolean get() = queries.isEmpty() && recommendedArtists.isEmpty()
}

data class HomeChip(
    val title: String,
    val params: String? = null
)

data class HomeSectionData(
    val title: String,
    val label: String? = null,
    val thumbnail: String? = null,
    val songs: List<DisplayTrack> = emptyList(),
    val albums: List<DisplayAlbum> = emptyList(),
    val playlists: List<DisplayPlaylist> = emptyList(),
    val artists: List<DisplayArtist> = emptyList()
) {
    val isEmpty: Boolean get() = songs.isEmpty() && albums.isEmpty() && playlists.isEmpty() && artists.isEmpty()
}

data class DisplayQuickPick(
    val id: String,
    val title: String,
    val subtitle: String = "",
    val thumbnailUrl: String? = null,
    val track: DisplayTrack? = null,
    val playlist: DisplayPlaylist? = null,
    val album: DisplayAlbum? = null
)

data class HomePageData(
    val chips: List<HomeChip> = emptyList(),
    val quickPicks: List<DisplayQuickPick> = emptyList(),
    val sections: List<HomeSectionData> = emptyList(),
    val newReleases: List<DisplayAlbum> = emptyList()
) {
    val isEmpty: Boolean get() = quickPicks.isEmpty() && sections.isEmpty() && newReleases.isEmpty()
}

private val heroGradients = listOf(
    listOf(Color(0xFF8A2387), Color(0xFFE94057), Color(0xFFF27121)),
    listOf(Color(0xFF0F2027), Color(0xFF203A43), Color(0xFF2C5364)),
    listOf(Color(0xFF134E5E), Color(0xFF71B280)),
    listOf(Color(0xFFF7971E), Color(0xFFFFD200)),
    listOf(Color(0xFFED213A), Color(0xFF93291E)),
    listOf(Color(0xFF2193B0), Color(0xFF6DD5ED)),
    listOf(Color(0xFF0052D4), Color(0xFF4364F7), Color(0xFF6FB1FC)),
    listOf(Color(0xFF3E5151), Color(0xFFDECBA4))
)

@Composable
fun HomeScreen(
    homePageData: HomePageData,
    selectedChip: HomeChip? = null,
    onSelectChip: (HomeChip?) -> Unit = {},
    isLoading: Boolean = false,
    currentTrackId: String? = null,
    onTrackClick: (DisplayTrack) -> Unit,
    onPlaylistClick: (DisplayPlaylist) -> Unit = {},
    onAlbumClick: (DisplayAlbum) -> Unit = {},
    onArtistClick: (DisplayArtist) -> Unit = {},
    onShowAllClick: (String) -> Unit = {},
    modifier: Modifier = Modifier
) {
    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .clip(RoundedCornerShape(topStart = 12.dp))
            .background(PureBlack)
            .padding(horizontal = 24.dp),
        contentPadding = PaddingValues(top = 16.dp, bottom = 140.dp)
    ) {
        // -----------------------------------------------------------------
        // 1. MOOD & ACTIVITY CHIPS (All, Relax, Workout, Focus, Energize, Commute...)
        // -----------------------------------------------------------------
        item {
            LazyRow(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 18.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // "All" chip
                item {
                    val isAllSelected = (selectedChip == null)
                    Box(
                        modifier = Modifier
                            .clip(CircleShape)
                            .background(if (isAllSelected) Color.White else DarkSurfaceElevated)
                            .border(
                                1.dp,
                                if (isAllSelected) Color.White else Color(0x24FFFFFF),
                                CircleShape
                            )
                            .clickable { onSelectChip(null) }
                            .padding(horizontal = 16.dp, vertical = 7.dp)
                    ) {
                        Text(
                            text = "All",
                            fontSize = 13.sp,
                            fontWeight = if (isAllSelected) FontWeight.Bold else FontWeight.Medium,
                            color = if (isAllSelected) PureBlack else TextPrimary
                        )
                    }
                }

                // Dynamic YouTube Music Home Chips
                items(homePageData.chips) { chip ->
                    val isSelected = (selectedChip == chip)
                    Box(
                        modifier = Modifier
                            .clip(CircleShape)
                            .background(if (isSelected) NothingRed else DarkSurfaceElevated)
                            .border(
                                1.dp,
                                if (isSelected) NothingRed else Color(0x24FFFFFF),
                                CircleShape
                            )
                            .clickable {
                                if (isSelected) onSelectChip(null) else onSelectChip(chip)
                            }
                            .padding(horizontal = 16.dp, vertical = 7.dp)
                    ) {
                        Text(
                            text = chip.title,
                            fontSize = 13.sp,
                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                            color = if (isSelected) Color.White else TextPrimary
                        )
                    }
                }
            }
        }

        // Loading Spinner
        if (isLoading && homePageData.isEmpty) {
            item {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 48.dp),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator(
                        color = NothingRed,
                        modifier = Modifier.size(36.dp)
                    )
                }
            }
        }

        // -----------------------------------------------------------------
        // 2. 2x4 QUICK ACCESS HERO GRID (Top 8 Algorithmic Songs / Items)
        // -----------------------------------------------------------------
        if (homePageData.quickPicks.isNotEmpty()) {
            val quickAccessTracks = homePageData.quickPicks.take(8)
            item {
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    fun handlePickClick(pick: DisplayQuickPick) {
                        if (pick.track != null) {
                            onTrackClick(pick.track)
                        } else if (pick.playlist != null) {
                            onPlaylistClick(pick.playlist)
                        } else if (pick.album != null) {
                            onAlbumClick(pick.album)
                        } else {
                            onTrackClick(
                                DisplayTrack(
                                    id = pick.id,
                                    title = pick.title,
                                    artist = pick.subtitle,
                                    thumbnailUrl = pick.thumbnailUrl
                                )
                            )
                        }
                    }

                    // Row 1 (4 cards)
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        quickAccessTracks.take(4).forEachIndexed { index, pick ->
                            Box(modifier = Modifier.weight(1f)) {
                                QuickAccessCard(
                                    title = pick.title,
                                    thumbnailUrl = pick.thumbnailUrl,
                                    gradientColors = heroGradients.getOrNull(index),
                                    isPlaying = (currentTrackId == pick.id),
                                    onClick = { handlePickClick(pick) },
                                    onPlayClick = { handlePickClick(pick) }
                                )
                            }
                        }
                        // Pad row if less than 4
                        repeat(4 - quickAccessTracks.take(4).size) {
                            Spacer(modifier = Modifier.weight(1f))
                        }
                    }

                    // Row 2 (4 cards)
                    if (quickAccessTracks.size > 4) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            quickAccessTracks.drop(4).take(4).forEachIndexed { index, pick ->
                                Box(modifier = Modifier.weight(1f)) {
                                    QuickAccessCard(
                                        title = pick.title,
                                        thumbnailUrl = pick.thumbnailUrl,
                                        gradientColors = heroGradients.getOrNull(index + 4),
                                        isPlaying = (currentTrackId == pick.id),
                                        onClick = { handlePickClick(pick) },
                                        onPlayClick = { handlePickClick(pick) }
                                    )
                                }
                            }
                            repeat(4 - quickAccessTracks.drop(4).take(4).size) {
                                Spacer(modifier = Modifier.weight(1f))
                            }
                        }
                    }
                }
            }

            item { Spacer(modifier = Modifier.height(28.dp)) }
        }

        // -----------------------------------------------------------------
        // 3. DYNAMIC YOUTUBE MUSIC HOME SECTIONS (Same as mobile algorithm)
        // -----------------------------------------------------------------
        homePageData.sections.forEach { section ->
            item {
                Column(modifier = Modifier.fillMaxWidth().padding(bottom = 28.dp)) {
                    // Section Title & Strapline Header
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = 12.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            if (!section.label.isNullOrBlank()) {
                                Text(
                                    text = section.label.uppercase(),
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    letterSpacing = 1.2.sp,
                                    color = NothingRed
                                )
                                Spacer(modifier = Modifier.height(2.dp))
                            }
                            Text(
                                text = section.title,
                                fontSize = 22.sp,
                                fontWeight = FontWeight.Bold,
                                color = TextPrimary
                            )
                        }

                        Text(
                            text = "Show all",
                            fontSize = 13.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = TextSecondary,
                            modifier = Modifier.clickable { onShowAllClick(section.title) }
                        )
                    }

                    // Songs Carousel
                    if (section.songs.isNotEmpty()) {
                        LazyRow(
                            horizontalArrangement = Arrangement.spacedBy(14.dp),
                            contentPadding = PaddingValues(horizontal = 2.dp)
                        ) {
                            items(section.songs) { track ->
                                MediaCard(
                                    title = track.title,
                                    subtitle = track.artist,
                                    thumbnailUrl = track.thumbnailUrl,
                                    isPlaying = (currentTrackId == track.id),
                                    onClick = { onTrackClick(track) },
                                    onPlayClick = { onTrackClick(track) }
                                )
                            }
                        }
                    }

                    // Playlists Carousel
                    if (section.playlists.isNotEmpty()) {
                        LazyRow(
                            horizontalArrangement = Arrangement.spacedBy(14.dp),
                            contentPadding = PaddingValues(horizontal = 2.dp)
                        ) {
                            items(section.playlists) { playlist ->
                                MediaCard(
                                    title = playlist.title,
                                    subtitle = playlist.author.ifBlank { "Playlist" },
                                    thumbnailUrl = playlist.thumbnailUrl,
                                    isPlaying = false,
                                    onClick = { onPlaylistClick(playlist) },
                                    onPlayClick = { onPlaylistClick(playlist) }
                                )
                            }
                        }
                    }

                    // Albums Carousel
                    if (section.albums.isNotEmpty()) {
                        LazyRow(
                            horizontalArrangement = Arrangement.spacedBy(14.dp),
                            contentPadding = PaddingValues(horizontal = 2.dp)
                        ) {
                            items(section.albums) { album ->
                                AlbumCard(
                                    title = album.title,
                                    subtitle = album.artist,
                                    thumbnailUrl = album.thumbnailUrl,
                                    onClick = { onAlbumClick(album) }
                                )
                            }
                        }
                    }

                    // Artists Carousel
                    if (section.artists.isNotEmpty()) {
                        LazyRow(
                            horizontalArrangement = Arrangement.spacedBy(16.dp),
                            contentPadding = PaddingValues(horizontal = 2.dp)
                        ) {
                            items(section.artists) { artist ->
                                ArtistCard(
                                    name = artist.name,
                                    thumbnailUrl = artist.thumbnailUrl,
                                    onClick = { onArtistClick(artist) }
                                )
                            }
                        }
                    }
                }
            }
        }

        // -----------------------------------------------------------------
        // 4. NEW RELEASES / EXPLORE (From YouTube.explore)
        // -----------------------------------------------------------------
        if (homePageData.newReleases.isNotEmpty()) {
            item {
                Column(modifier = Modifier.fillMaxWidth().padding(bottom = 28.dp)) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = 12.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "New releases & trending",
                            fontSize = 22.sp,
                            fontWeight = FontWeight.Bold,
                            color = TextPrimary
                        )
                        Text(
                            text = "Show all",
                            fontSize = 13.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = TextSecondary,
                            modifier = Modifier.clickable { onShowAllClick("New releases") }
                        )
                    }

                    LazyRow(
                        horizontalArrangement = Arrangement.spacedBy(14.dp),
                        contentPadding = PaddingValues(horizontal = 2.dp)
                    ) {
                        items(homePageData.newReleases) { album ->
                            AlbumCard(
                                title = album.title,
                                subtitle = album.artist,
                                thumbnailUrl = album.thumbnailUrl,
                                onClick = { onAlbumClick(album) }
                            )
                        }
                    }
                }
            }
        }

        // -----------------------------------------------------------------
        // 5. SPOTLIGHT / DISCOVER HERO BANNER (Spotify Style)
        // -----------------------------------------------------------------
        if (homePageData.quickPicks.isNotEmpty()) {
            val spotlightSong = homePageData.quickPicks.firstOrNull()
            if (spotlightSong != null) {
                fun playSpotlight() {
                    if (spotlightSong.track != null) {
                        onTrackClick(spotlightSong.track)
                    } else if (spotlightSong.playlist != null) {
                        onPlaylistClick(spotlightSong.playlist)
                    } else if (spotlightSong.album != null) {
                        onAlbumClick(spotlightSong.album)
                    } else {
                        onTrackClick(
                            DisplayTrack(
                                id = spotlightSong.id,
                                title = spotlightSong.title,
                                artist = spotlightSong.subtitle,
                                thumbnailUrl = spotlightSong.thumbnailUrl
                            )
                        )
                    }
                }

                item {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 18.dp)
                            .clip(RoundedCornerShape(12.dp))
                            .background(
                                Brush.horizontalGradient(
                                    listOf(Color(0xFF0F3E36), Color(0xFF1B6B5C), Color(0xFF0D2520))
                                )
                            )
                            .clickable { playSpotlight() }
                            .padding(24.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = "SPOTLIGHT SELECTION",
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    letterSpacing = 1.2.sp,
                                    color = Color(0xFF4EEAB4)
                                )
                                Spacer(modifier = Modifier.height(6.dp))
                                Text(
                                    text = spotlightSong.title,
                                    fontSize = 24.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color.White,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = spotlightSong.subtitle.ifBlank { "Featured Selection" },
                                    fontSize = 14.sp,
                                    color = Color.White.copy(alpha = 0.8f),
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                            }

                            Spacer(modifier = Modifier.width(16.dp))

                            Box(
                                modifier = Modifier
                                    .size(48.dp)
                                    .shadow(8.dp, CircleShape)
                                    .clip(CircleShape)
                                    .background(Color(0xFF1ED760))
                                    .clickable { playSpotlight() },
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Rounded.PlayArrow,
                                    contentDescription = "Play Spotlight",
                                    tint = PureBlack,
                                    modifier = Modifier.size(28.dp)
                                )
                            }
                        }
                    }
                }
            }
        }

        // -----------------------------------------------------------------
        // 6. QUICK PICKS SONG LIST (Live Playing Tracklist)
        // -----------------------------------------------------------------
        val quickSongTracks = homePageData.quickPicks.mapNotNull { it.track }.ifEmpty {
            homePageData.sections.flatMap { it.songs }.distinctBy { it.id }
        }
        if (quickSongTracks.isNotEmpty()) {
            item {
                Text(
                    text = "Quick Picks",
                    fontSize = 22.sp,
                    fontWeight = FontWeight.Bold,
                    color = TextPrimary,
                    modifier = Modifier.padding(bottom = 12.dp)
                )
            }

            items(quickSongTracks.take(6)) { track ->
                SongItem(
                    title = track.title,
                    artist = track.artist,
                    duration = track.duration,
                    thumbnailUrl = track.thumbnailUrl,
                    isPlaying = (track.id == currentTrackId),
                    onClick = { onTrackClick(track) },
                    modifier = Modifier.padding(vertical = 2.dp)
                )
            }
        }
    }
}
