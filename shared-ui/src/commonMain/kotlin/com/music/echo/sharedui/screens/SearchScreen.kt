package com.music.echo.sharedui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Add
import androidx.compose.material.icons.rounded.Check
import androidx.compose.material.icons.rounded.MusicNote
import androidx.compose.material.icons.rounded.Pause
import androidx.compose.material.icons.rounded.PlayArrow
import androidx.compose.material3.CircularProgressIndicator
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
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil3.compose.AsyncImage
import com.music.echo.sharedui.component.AlbumCard
import com.music.echo.sharedui.component.ArtistCard
import com.music.echo.sharedui.theme.DarkSurfaceElevated
import com.music.echo.sharedui.theme.NothingRed
import com.music.echo.sharedui.theme.PureBlack
import com.music.echo.sharedui.theme.TextPrimary
import com.music.echo.sharedui.theme.TextSecondary

enum class SearchFilter(val label: String) {
    ALL("All"),
    SONGS("Songs"),
    PLAYLISTS("Playlists"),
    ALBUMS("Albums"),
    ARTISTS("Artists"),
    GENRES("Genres & Moods"),
    PROFILES("Profiles"),
    PODCASTS("Podcasts & Shows"),
    AUDIOBOOKS("Audiobooks")
}

private data class SearchCategoryItem(
    val title: String,
    val query: String,
    val colors: List<Color>
)

private val popularCategories = listOf(
    SearchCategoryItem("Pop", "Pop", listOf(Color(0xFFE91E63), Color(0xFFFF5722))),
    SearchCategoryItem("Hip-Hop & Rap", "Hip Hop", listOf(Color(0xFFFF9800), Color(0xFFF44336))),
    SearchCategoryItem("Punjabi", "Punjabi", listOf(Color(0xFFFF5722), Color(0xFF795548))),
    SearchCategoryItem("Bollywood", "Bollywood", listOf(Color(0xFF9C27B0), Color(0xFFE91E63))),
    SearchCategoryItem("Rock", "Rock", listOf(Color(0xFFE50914), Color(0xFF5A0000))),
    SearchCategoryItem("Chill & Lofi", "Lofi Chill", listOf(Color(0xFF3F51B5), Color(0xFF00BCD4))),
    SearchCategoryItem("Workout", "Workout", listOf(Color(0xFF4CAF50), Color(0xFF009688))),
    SearchCategoryItem("Electronic / Dance", "EDM Dance", listOf(Color(0xFF00BCD4), Color(0xFF3F51B5))),
    SearchCategoryItem("Indie & Alt", "Indie", listOf(Color(0xFF673AB7), Color(0xFF9C27B0))),
    SearchCategoryItem("Romance", "Romantic Songs", listOf(Color(0xFFE91E63), Color(0xFF880E4F))),
    SearchCategoryItem("Gaming", "Gaming Music", listOf(Color(0xFF00E676), Color(0xFF1B5E20))),
    SearchCategoryItem("Classical", "Classical Music", listOf(Color(0xFF795548), Color(0xFF3E2723)))
)

@Composable
fun SearchScreen(
    searchQuery: String,
    selectedFilter: SearchFilter = SearchFilter.ALL,
    onFilterChange: (SearchFilter) -> Unit = {},
    searchResults: SearchResultsData,
    isLoading: Boolean = false,
    currentTrackId: String? = null,
    onCategoryClick: (String) -> Unit = {},
    onTrackClick: (DisplayTrack) -> Unit,
    onPlaylistClick: (DisplayPlaylist) -> Unit = {},
    onAlbumClick: (DisplayAlbum) -> Unit = {},
    onArtistClick: (DisplayArtist) -> Unit = {},
    modifier: Modifier = Modifier
) {
    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .background(PureBlack),
        contentPadding = PaddingValues(bottom = 140.dp)
    ) {
        // -----------------------------------------------------------------
        // 1. FILTER PILLS (All, Songs, Playlists, Albums, Artists, Genres, Podcasts...)
        // -----------------------------------------------------------------
        item {
            LazyRow(
                contentPadding = PaddingValues(horizontal = 24.dp, vertical = 16.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(SearchFilter.entries) { filter ->
                    val isSelected = (filter == selectedFilter)
                    Box(
                        modifier = Modifier
                            .clip(CircleShape)
                            .background(if (isSelected) Color.White else Color(0x22FFFFFF))
                            .clickable {
                                onFilterChange(filter)
                            }
                            .padding(horizontal = 16.dp, vertical = 8.dp)
                    ) {
                        Text(
                            text = filter.label,
                            fontSize = 13.sp,
                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                            color = if (isSelected) PureBlack else TextPrimary
                        )
                    }
                }
            }
        }

        // Loading State
        if (isLoading) {
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

        if (searchQuery.isBlank()) {
            // Initial Explore / Popular Categories Grid
            item {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 24.dp, vertical = 8.dp)
                ) {
                    Text(
                        text = "POPULAR CATEGORIES",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 1.5.sp,
                        color = TextSecondary
                    )
                    Spacer(modifier = Modifier.height(16.dp))

                    val rows = popularCategories.chunked(4)
                    rows.forEach { rowCategories ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(bottom = 14.dp),
                            horizontalArrangement = Arrangement.spacedBy(14.dp)
                        ) {
                            rowCategories.forEach { category ->
                                CategoryCard(
                                    category = category,
                                    onClick = { onCategoryClick(category.query) },
                                    modifier = Modifier.weight(1f)
                                )
                            }
                            repeat(4 - rowCategories.size) {
                                Spacer(modifier = Modifier.weight(1f))
                            }
                        }
                    }
                }
            }
        } else if (!isLoading && searchResults.isEmpty) {
            item {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 64.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            text = "No results found for \"$searchQuery\"",
                            style = MaterialTheme.typography.titleMedium,
                            color = TextPrimary,
                            fontWeight = FontWeight.SemiBold
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = "Please check the spelling or try searching for another song, artist, or album.",
                            style = MaterialTheme.typography.bodyMedium,
                            color = TextSecondary
                        )
                    }
                }
            }
        } else if (!isLoading) {
            when (selectedFilter) {
                SearchFilter.ALL -> {
                    val topTrack = searchResults.songs.firstOrNull()

                    // 1. TOP RESULT CARD (Spotify-style highlighted header card)
                    if (topTrack != null) {
                        item {
                            TopResultCard(
                                track = topTrack,
                                isPlaying = (currentTrackId == topTrack.id),
                                onPlayClick = { onTrackClick(topTrack) },
                                modifier = Modifier.padding(horizontal = 24.dp, vertical = 8.dp)
                            )
                            Spacer(modifier = Modifier.height(14.dp))
                        }
                    }

                    // 2. SEARCH RESULTS LIST (Spotify style items)
                    val remainingSongs = if (topTrack != null) searchResults.songs.drop(1) else searchResults.songs
                    items(remainingSongs) { song ->
                        SearchResultListRow(
                            title = song.title,
                            subtitle = "Song • ${song.artist}",
                            tag = "Song",
                            thumbnailUrl = song.thumbnailUrl,
                            isPlaying = (currentTrackId == song.id),
                            onClick = { onTrackClick(song) },
                            modifier = Modifier.padding(horizontal = 24.dp, vertical = 2.dp)
                        )
                    }

                    // Playlists in results list
                    items(searchResults.playlists) { playlist ->
                        SearchResultListRow(
                            title = playlist.title,
                            subtitle = "Playlist • ${playlist.author.ifBlank { "Echo Music" }}",
                            tag = "Playlist",
                            thumbnailUrl = playlist.thumbnailUrl,
                            isPlaying = false,
                            onClick = { onPlaylistClick(playlist) },
                            modifier = Modifier.padding(horizontal = 24.dp, vertical = 2.dp)
                        )
                    }

                    // Albums in results list
                    items(searchResults.albums) { album ->
                        SearchResultListRow(
                            title = album.title,
                            subtitle = if (album.year != null) "Album • ${album.artist} • ${album.year}" else "Album • ${album.artist}",
                            tag = "Album",
                            thumbnailUrl = album.thumbnailUrl,
                            isPlaying = false,
                            onClick = { onAlbumClick(album) },
                            modifier = Modifier.padding(horizontal = 24.dp, vertical = 2.dp)
                        )
                    }

                    // Artists in results list
                    items(searchResults.artists) { artist ->
                        SearchResultListRow(
                            title = artist.name,
                            subtitle = "Artist",
                            tag = "Artist",
                            thumbnailUrl = artist.thumbnailUrl,
                            isCircleImage = true,
                            isPlaying = false,
                            onClick = { onArtistClick(artist) },
                            modifier = Modifier.padding(horizontal = 24.dp, vertical = 2.dp)
                        )
                    }
                }

                SearchFilter.SONGS -> {
                    items(searchResults.songs) { song ->
                        SearchResultListRow(
                            title = song.title,
                            subtitle = "Song • ${song.artist}",
                            tag = "Song",
                            thumbnailUrl = song.thumbnailUrl,
                            isPlaying = (currentTrackId == song.id),
                            onClick = { onTrackClick(song) },
                            modifier = Modifier.padding(horizontal = 24.dp, vertical = 2.dp)
                        )
                    }
                }

                SearchFilter.PLAYLISTS -> {
                    items(searchResults.playlists) { playlist ->
                        SearchResultListRow(
                            title = playlist.title,
                            subtitle = "Playlist • ${playlist.author.ifBlank { "Echo Music" }}",
                            tag = "Playlist",
                            thumbnailUrl = playlist.thumbnailUrl,
                            isPlaying = false,
                            onClick = { onPlaylistClick(playlist) },
                            modifier = Modifier.padding(horizontal = 24.dp, vertical = 2.dp)
                        )
                    }
                }

                SearchFilter.ALBUMS -> {
                    items(searchResults.albums) { album ->
                        SearchResultListRow(
                            title = album.title,
                            subtitle = "Album • ${album.artist}",
                            tag = "Album",
                            thumbnailUrl = album.thumbnailUrl,
                            isPlaying = false,
                            onClick = { onAlbumClick(album) },
                            modifier = Modifier.padding(horizontal = 24.dp, vertical = 2.dp)
                        )
                    }
                }

                SearchFilter.ARTISTS -> {
                    items(searchResults.artists) { artist ->
                        SearchResultListRow(
                            title = artist.name,
                            subtitle = "Artist",
                            tag = "Artist",
                            thumbnailUrl = artist.thumbnailUrl,
                            isCircleImage = true,
                            isPlaying = false,
                            onClick = { onArtistClick(artist) },
                            modifier = Modifier.padding(horizontal = 24.dp, vertical = 2.dp)
                        )
                    }
                }

                else -> {
                    items(searchResults.songs) { song ->
                        SearchResultListRow(
                            title = song.title,
                            subtitle = "Song • ${song.artist}",
                            tag = "Song",
                            thumbnailUrl = song.thumbnailUrl,
                            isPlaying = (currentTrackId == song.id),
                            onClick = { onTrackClick(song) },
                            modifier = Modifier.padding(horizontal = 24.dp, vertical = 2.dp)
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun TopResultCard(
    track: DisplayTrack,
    isPlaying: Boolean,
    onPlayClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val interactionSource = remember { MutableInteractionSource() }
    val isHovered by interactionSource.collectIsHoveredAsState()
    var isAdded by remember { mutableStateOf(false) }

    Box(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(if (isHovered) Color(0xFF262628) else Color(0xFF1E1E20))
            .border(1.dp, Color(0x1AFFFFFF), RoundedCornerShape(12.dp))
            .hoverable(interactionSource)
            .clickable(onClick = onPlayClick)
            .padding(18.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Big Square Artwork
            Box(
                modifier = Modifier
                    .size(88.dp)
                    .shadow(12.dp, RoundedCornerShape(8.dp), spotColor = Color.Black.copy(alpha = 0.5f))
                    .clip(RoundedCornerShape(8.dp))
                    .background(Color(0xFF2A2A2E)),
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
                        modifier = Modifier.size(36.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.width(20.dp))

            // Title and Subtitle
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = track.title,
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Bold,
                    color = if (isPlaying) NothingRed else TextPrimary,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Spacer(modifier = Modifier.height(6.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = "Song • ${track.artist}",
                        fontSize = 14.sp,
                        color = TextSecondary,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }

            Spacer(modifier = Modifier.width(16.dp))

            // Add (+) Button
            Box(
                modifier = Modifier
                    .size(36.dp)
                    .clip(CircleShape)
                    .background(Color(0x18FFFFFF))
                    .clickable { isAdded = !isAdded },
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = if (isAdded) Icons.Rounded.Check else Icons.Rounded.Add,
                    contentDescription = "Add to playlist",
                    tint = if (isAdded) Color(0xFF1ED760) else TextSecondary,
                    modifier = Modifier.size(20.dp)
                )
            }

            Spacer(modifier = Modifier.width(14.dp))

            // Big Circular Play Button (Spotify Green / NothingRed)
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .shadow(10.dp, CircleShape, spotColor = Color.Black.copy(alpha = 0.6f))
                    .clip(CircleShape)
                    .background(Color(0xFF1ED760))
                    .clickable(onClick = onPlayClick),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = if (isPlaying) Icons.Rounded.Pause else Icons.Rounded.PlayArrow,
                    contentDescription = if (isPlaying) "Pause" else "Play",
                    tint = PureBlack,
                    modifier = Modifier.size(28.dp)
                )
            }
        }
    }
}

@Composable
private fun SearchResultListRow(
    title: String,
    subtitle: String,
    tag: String,
    thumbnailUrl: String?,
    isCircleImage: Boolean = false,
    isPlaying: Boolean = false,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val interactionSource = remember { MutableInteractionSource() }
    val isHovered by interactionSource.collectIsHoveredAsState()
    var isAdded by remember { mutableStateOf(false) }

    Row(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(8.dp))
            .background(if (isHovered) Color(0x1CFFFFFF) else Color.Transparent)
            .hoverable(interactionSource)
            .clickable(onClick = onClick)
            .padding(horizontal = 12.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Thumbnail
        Box(
            modifier = Modifier
                .size(48.dp)
                .clip(if (isCircleImage) CircleShape else RoundedCornerShape(6.dp))
                .background(Color(0xFF242426)),
            contentAlignment = Alignment.Center
        ) {
            if (!thumbnailUrl.isNullOrBlank()) {
                AsyncImage(
                    model = thumbnailUrl,
                    contentDescription = title,
                    contentScale = ContentScale.Crop,
                    modifier = Modifier.fillMaxSize()
                )
            } else {
                Icon(
                    imageVector = Icons.Rounded.MusicNote,
                    contentDescription = null,
                    tint = TextSecondary,
                    modifier = Modifier.size(22.dp)
                )
            }

            // Hover play overlay
            if (isHovered || isPlaying) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(Color.Black.copy(alpha = 0.5f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = if (isPlaying) Icons.Rounded.Pause else Icons.Rounded.PlayArrow,
                        contentDescription = null,
                        tint = Color.White,
                        modifier = Modifier.size(22.dp)
                    )
                }
            }
        }

        Spacer(modifier = Modifier.width(14.dp))

        // Title and Subtitle
        Column(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.Center
        ) {
            Text(
                text = title,
                fontSize = 15.sp,
                fontWeight = FontWeight.SemiBold,
                color = if (isPlaying) NothingRed else TextPrimary,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = subtitle,
                fontSize = 13.sp,
                color = TextSecondary,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }

        Spacer(modifier = Modifier.width(12.dp))

        // Right Tag / Badge ("Song", "Playlist", "Album", "Single", etc.)
        Text(
            text = tag,
            fontSize = 12.sp,
            color = TextSecondary,
            modifier = Modifier.padding(end = 12.dp)
        )

        // "+" Action Button
        Box(
            modifier = Modifier
                .size(32.dp)
                .clip(CircleShape)
                .clickable { isAdded = !isAdded },
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = if (isAdded) Icons.Rounded.Check else Icons.Rounded.Add,
                contentDescription = "Add",
                tint = if (isAdded) Color(0xFF1ED760) else TextSecondary,
                modifier = Modifier.size(18.dp)
            )
        }
    }
}

@Composable
private fun CategoryCard(
    category: SearchCategoryItem,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val interactionSource = remember { MutableInteractionSource() }
    val isHovered by interactionSource.collectIsHoveredAsState()

    Box(
        modifier = modifier
            .height(110.dp)
            .clip(RoundedCornerShape(10.dp))
            .background(Brush.linearGradient(category.colors))
            .border(
                1.dp,
                if (isHovered) Color(0x55FFFFFF) else Color.Transparent,
                RoundedCornerShape(10.dp)
            )
            .hoverable(interactionSource)
            .clickable(onClick = onClick)
            .padding(16.dp),
        contentAlignment = Alignment.TopStart
    ) {
        Text(
            text = category.title,
            color = Color.White,
            fontWeight = FontWeight.Bold,
            fontSize = 17.sp,
            modifier = Modifier.fillMaxWidth()
        )
    }
}
