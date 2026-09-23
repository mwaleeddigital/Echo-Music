package com.music.echo.sharedui.component

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Add
import androidx.compose.material.icons.rounded.Download
import androidx.compose.material.icons.rounded.Favorite
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil3.compose.AsyncImage
import com.music.echo.sharedui.theme.DarkSurface
import com.music.echo.sharedui.theme.DarkSurfaceElevated
import com.music.echo.sharedui.theme.NothingRed
import com.music.echo.sharedui.theme.PureBlack
import com.music.echo.sharedui.theme.TextPrimary
import com.music.echo.sharedui.theme.TextSecondary

data class SidebarPlaylist(
    val id: String,
    val title: String,
    val thumbnailUrl: String? = null,
    val gradient: List<Color>? = null
)

@Composable
fun DesktopSidebar(
    onLibraryClick: () -> Unit = {},
    onLikedSongsClick: () -> Unit = {},
    onDownloadedSongsClick: () -> Unit = {},
    onPlaylistClick: (SidebarPlaylist) -> Unit = {},
    onCreatePlaylist: () -> Unit = {},
    onBrowsePodcasts: () -> Unit = {},
    onSpotifyImportSubmit: (String) -> Unit = {},
    userPlaylists: List<SidebarPlaylist> = emptyList(),
    selectedPlaylistId: String? = null,
    modifier: Modifier = Modifier
) {
    var isExpanded by remember { mutableStateOf(false) }
    val animatedWidth by animateDpAsState(if (isExpanded) 310.dp else 72.dp)
    var showSpotifyImportDialog by remember { mutableStateOf(false) }

    if (showSpotifyImportDialog) {
        androidx.compose.ui.window.Dialog(onDismissRequest = { showSpotifyImportDialog = false }) {
            var urlInput by remember { mutableStateOf("") }
            Column(
                modifier = Modifier
                    .width(400.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(DarkSurfaceElevated)
                    .padding(24.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Text("Import from Spotify", fontSize = 20.sp, fontWeight = FontWeight.Bold, color = Color.White)
                Text("Paste a public Spotify playlist URL below.", color = TextSecondary, fontSize = 14.sp)
                
                androidx.compose.material3.OutlinedTextField(
                    value = urlInput,
                    onValueChange = { urlInput = it },
                    placeholder = { Text("https://open.spotify.com/playlist/...") },
                    modifier = Modifier.fillMaxWidth(),
                    colors = androidx.compose.material3.OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = NothingRed,
                        unfocusedBorderColor = Color.DarkGray,
                        focusedTextColor = Color.White,
                        unfocusedTextColor = Color.White,
                        cursorColor = NothingRed
                    ),
                    singleLine = true
                )
                
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
                    androidx.compose.material3.TextButton(onClick = { showSpotifyImportDialog = false }) {
                        Text("Cancel", color = Color.White)
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    androidx.compose.material3.Button(
                        onClick = {
                            if (urlInput.isNotBlank()) {
                                onSpotifyImportSubmit(urlInput)
                                showSpotifyImportDialog = false
                            }
                        },
                        colors = androidx.compose.material3.ButtonDefaults.buttonColors(containerColor = NothingRed)
                    ) {
                        Text("Import", color = Color.White, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }

    Column(
        modifier = modifier
            .width(animatedWidth)
            .fillMaxHeight()
            .clip(RoundedCornerShape(8.dp))
            .background(DarkSurface)
            .padding(if (isExpanded) 16.dp else 10.dp),
        horizontalAlignment = if (isExpanded) Alignment.Start else Alignment.CenterHorizontally
    ) {
        // -------------------------------------------------------------
        // HEADER / TOP ACTION ROW
        // -------------------------------------------------------------
        if (!isExpanded) {
            // COLLAPSED STATE (Image 1 & Image 2)
            val libInteractionSource = remember { MutableInteractionSource() }
            val isLibHovered by libInteractionSource.collectIsHoveredAsState()

            val plusInteractionSource = remember { MutableInteractionSource() }
            val isPlusHovered by plusInteractionSource.collectIsHoveredAsState()

            // 1. Library Icon Button (with hover expand icon in Image 2)
            Box(
                modifier = Modifier
                    .size(46.dp)
                    .clip(RoundedCornerShape(8.dp))
                    .background(if (isLibHovered) Color(0x22FFFFFF) else Color.Transparent)
                    .hoverable(libInteractionSource)
                    .clickable { isExpanded = true },
                contentAlignment = Alignment.Center
            ) {
                SpotifyLibraryIcon(
                    isHovered = isLibHovered,
                    isExpanded = false,
                    tint = if (isLibHovered) Color.White else Color(0xFFB3B3B3)
                )
            }

            Spacer(modifier = Modifier.height(10.dp))

            // 2. Circular '+' button
            Box(
                modifier = Modifier
                    .size(38.dp)
                    .clip(CircleShape)
                    .background(if (isPlusHovered) Color(0x33FFFFFF) else Color(0x18FFFFFF))
                    .hoverable(plusInteractionSource)
                    .clickable {
                        isExpanded = true
                        onCreatePlaylist()
                    },
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Rounded.Add,
                    contentDescription = "Create playlist or folder",
                    tint = if (isPlusHovered) Color.White else Color(0xFFB3B3B3),
                    modifier = Modifier.size(20.dp)
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Liked Songs Icon (Purple Gradient with Heart)
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .clip(RoundedCornerShape(6.dp))
                    .background(
                        Brush.linearGradient(
                            listOf(Color(0xFF450AF5), Color(0xFF8E8EE5), Color(0xFFC4B5FD))
                        )
                    )
                    .clickable {
                        onLikedSongsClick()
                    },
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Rounded.Favorite,
                    contentDescription = "Liked Songs",
                    tint = Color.White,
                    modifier = Modifier.size(20.dp)
                )
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Downloaded / Offline Songs Icon (Green Gradient with Download Arrow)
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .clip(RoundedCornerShape(6.dp))
                    .background(
                        Brush.linearGradient(
                            listOf(Color(0xFF0F9D58), Color(0xFF1DB954), Color(0xFF34D399))
                        )
                    )
                    .clickable {
                        onDownloadedSongsClick()
                    },
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Rounded.Download,
                    contentDescription = "Downloaded Music",
                    tint = Color.White,
                    modifier = Modifier.size(20.dp)
                )
            }

            if (userPlaylists.isNotEmpty()) {
                Spacer(modifier = Modifier.height(12.dp))

                // Custom User Playlists Rail
                Column(
                    modifier = Modifier
                        .fillMaxHeight()
                        .verticalScroll(rememberScrollState()),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    userPlaylists.forEach { pl ->
                        val isSelected = pl.id == selectedPlaylistId
                        Box(
                            modifier = Modifier
                                .size(48.dp)
                                .clip(RoundedCornerShape(6.dp))
                                .border(
                                    2.dp,
                                    if (isSelected) NothingRed else Color.Transparent,
                                    RoundedCornerShape(6.dp)
                                )
                                .background(
                                    if (pl.gradient != null) {
                                        Brush.linearGradient(pl.gradient)
                                    } else {
                                        Brush.linearGradient(listOf(Color(0xFF282828), Color(0xFF181818)))
                                    }
                                )
                                .clickable { onPlaylistClick(pl) },
                            contentAlignment = Alignment.Center
                        ) {
                            if (!pl.thumbnailUrl.isNullOrBlank()) {
                                AsyncImage(
                                    model = pl.thumbnailUrl,
                                    contentDescription = pl.title,
                                    contentScale = ContentScale.Crop,
                                    modifier = Modifier.fillMaxSize()
                                )
                            } else {
                                Text(
                                    text = pl.title.take(1).uppercase(),
                                    color = Color.White,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 16.sp
                                )
                            }
                        }
                    }
                }
            }
        } else {
            // -------------------------------------------------------------
            // EXPANDED STATE (Image 3)
            // -------------------------------------------------------------
            val headerLibInteraction = remember { MutableInteractionSource() }
            val isHeaderLibHovered by headerLibInteraction.collectIsHoveredAsState()

            val createBtnInteraction = remember { MutableInteractionSource() }
            val isCreateBtnHovered by createBtnInteraction.collectIsHoveredAsState()

            // Header: "Your Library" + "+ Create"
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 16.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    modifier = Modifier
                        .clip(RoundedCornerShape(6.dp))
                        .hoverable(headerLibInteraction)
                        .clickable { isExpanded = false }
                        .padding(horizontal = 4.dp, vertical = 4.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    SpotifyLibraryIcon(
                        isHovered = false,
                        isExpanded = true,
                        tint = if (isHeaderLibHovered) Color.White else Color(0xFFB3B3B3)
                    )
                    Text(
                        text = "Your Library",
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold,
                        color = if (isHeaderLibHovered) Color.White else Color(0xFFB3B3B3)
                    )
                }

                // '+ Create' Button
                Row(
                    modifier = Modifier
                        .clip(CircleShape)
                        .background(if (isCreateBtnHovered) Color(0x33FFFFFF) else Color(0x18FFFFFF))
                        .hoverable(createBtnInteraction)
                        .clickable { onCreatePlaylist() }
                        .padding(horizontal = 12.dp, vertical = 6.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Icon(
                        imageVector = Icons.Rounded.Add,
                        contentDescription = "Create",
                        tint = Color.White,
                        modifier = Modifier.size(16.dp)
                    )
                    Text(
                        text = "Create",
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                }
            }

            // Scrollable Content Cards
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // Card 1: "Create your first playlist"
                LibraryPromoCard(
                    title = "Create your first playlist",
                    subtitle = "It's easy, we'll help you",
                    buttonText = "Create playlist",
                    onButtonClick = { onCreatePlaylist() }
                )

                // Card 2: "Let's find some podcasts to follow"
                LibraryPromoCard(
                    title = "Let's find some podcasts to follow",
                    subtitle = "We'll keep you updated on new episodes",
                    buttonText = "Browse podcasts",
                    onButtonClick = { onBrowsePodcasts() }
                )

                // Card 3: "Import from Spotify"
                LibraryPromoCard(
                    title = "Import from Spotify",
                    subtitle = "Bring your Spotify playlists to Echo",
                    buttonText = "Import playlist",
                    onButtonClick = { showSpotifyImportDialog = true }
                )

                Spacer(modifier = Modifier.height(8.dp))

                // Liked Songs Row (Expanded)
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(6.dp))
                        .clickable {
                            onLikedSongsClick()
                        }
                        .padding(8.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(48.dp)
                            .clip(RoundedCornerShape(4.dp))
                            .background(
                                Brush.linearGradient(
                                    listOf(Color(0xFF450AF5), Color(0xFF8E8EE5), Color(0xFFC4B5FD))
                                )
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Rounded.Favorite,
                            contentDescription = "Liked Songs",
                            tint = Color.White,
                            modifier = Modifier.size(22.dp)
                        )
                    }

                    Column {
                        Text(
                            text = "Liked Songs",
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold,
                            color = TextPrimary
                        )
                        Text(
                            text = "Playlist • Auto",
                            fontSize = 12.sp,
                            color = TextSecondary
                        )
                    }
                }

                // Downloaded Music Row (Expanded)
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(6.dp))
                        .clickable {
                            onDownloadedSongsClick()
                        }
                        .padding(8.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(48.dp)
                            .clip(RoundedCornerShape(4.dp))
                            .background(
                                Brush.linearGradient(
                                    listOf(Color(0xFF0F9D58), Color(0xFF1DB954), Color(0xFF34D399))
                                )
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Rounded.Download,
                            contentDescription = "Downloaded Music",
                            tint = Color.White,
                            modifier = Modifier.size(22.dp)
                        )
                    }

                    Column {
                        Text(
                            text = "Downloaded Music",
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold,
                            color = TextPrimary
                        )
                        Text(
                            text = "Storage • Offline",
                            fontSize = 12.sp,
                            color = Color(0xFF1DB954)
                        )
                    }
                }

                // Custom User Playlists Expanded List
                userPlaylists.forEach { pl ->
                    val isSelected = pl.id == selectedPlaylistId
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(6.dp))
                            .background(if (isSelected) Color(0x1AFFFFFF) else Color.Transparent)
                            .clickable { onPlaylistClick(pl) }
                            .padding(8.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(48.dp)
                                .clip(RoundedCornerShape(4.dp))
                                .background(
                                    if (pl.gradient != null) {
                                        Brush.linearGradient(pl.gradient)
                                    } else {
                                        Brush.linearGradient(listOf(Color(0xFF282828), Color(0xFF181818)))
                                    }
                                ),
                            contentAlignment = Alignment.Center
                        ) {
                            if (!pl.thumbnailUrl.isNullOrBlank()) {
                                AsyncImage(
                                    model = pl.thumbnailUrl,
                                    contentDescription = pl.title,
                                    contentScale = ContentScale.Crop,
                                    modifier = Modifier.fillMaxSize()
                                )
                            } else {
                                Text(
                                    text = pl.title.take(1).uppercase(),
                                    color = Color.White,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 16.sp
                                )
                            }
                        }

                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = pl.title,
                                fontSize = 14.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = if (isSelected) NothingRed else TextPrimary,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                            Text(
                                text = "Playlist • Echo Music",
                                fontSize = 12.sp,
                                color = TextSecondary
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun LibraryPromoCard(
    title: String,
    subtitle: String,
    buttonText: String,
    onButtonClick: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(8.dp))
            .background(Color(0xFF1F1F1F))
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Text(
            text = title,
            fontSize = 14.sp,
            fontWeight = FontWeight.Bold,
            color = Color.White
        )
        Text(
            text = subtitle,
            fontSize = 12.sp,
            color = Color(0xFFB3B3B3),
            lineHeight = 16.sp
        )
        Spacer(modifier = Modifier.height(4.dp))
        Box(
            modifier = Modifier
                .clip(CircleShape)
                .background(Color.White)
                .clickable(onClick = onButtonClick)
                .padding(horizontal = 16.dp, vertical = 7.dp)
        ) {
            Text(
                text = buttonText,
                fontSize = 13.sp,
                fontWeight = FontWeight.Bold,
                color = PureBlack
            )
        }
    }
}

/**
 * Spotify Library Icon:
 * - Default: 3-book vertical lines `||\`
 * - Hovered in collapsed state: `[|>]` Expand Icon
 */
@Composable
fun SpotifyLibraryIcon(
    isHovered: Boolean,
    isExpanded: Boolean,
    tint: Color = Color(0xFFB3B3B3),
    modifier: Modifier = Modifier
) {
    Canvas(modifier = modifier.size(24.dp)) {
        val strokeWidth = 2.dp.toPx()
        val width = size.width
        val height = size.height

        if (!isExpanded && isHovered) {
            // Hovered state (Image 2): [|>] Expand sidebar icon
            val rectWidth = width * 0.82f
            val rectHeight = height * 0.82f
            val left = (width - rectWidth) / 2
            val top = (height - rectHeight) / 2

            // Outer rounded rectangle
            drawRoundRect(
                color = tint,
                topLeft = Offset(left, top),
                size = Size(rectWidth, rectHeight),
                cornerRadius = CornerRadius(3.dp.toPx(), 3.dp.toPx()),
                style = Stroke(width = strokeWidth)
            )

            // Left vertical separator
            val separatorX = left + rectWidth * 0.35f
            drawLine(
                color = tint,
                start = Offset(separatorX, top),
                end = Offset(separatorX, top + rectHeight),
                strokeWidth = strokeWidth
            )

            // Right chevron '>'
            val chevronCenterX = left + rectWidth * 0.65f
            val chevronCenterY = top + rectHeight / 2
            val chevronSize = 3.dp.toPx()
            val path = Path().apply {
                moveTo(chevronCenterX - chevronSize * 0.8f, chevronCenterY - chevronSize)
                lineTo(chevronCenterX + chevronSize * 0.8f, chevronCenterY)
                lineTo(chevronCenterX - chevronSize * 0.8f, chevronCenterY + chevronSize)
            }
            drawPath(
                path = path,
                color = tint,
                style = Stroke(
                    width = strokeWidth,
                    cap = StrokeCap.Round,
                    join = StrokeJoin.Round
                )
            )
        } else {
            // Default state (Image 1 & Image 3): ||\ 3-book shelf icon
            val barHeight = height * 0.68f
            val startY = (height - barHeight) / 2
            val endY = startY + barHeight

            // Bar 1 (vertical)
            drawLine(
                color = tint,
                start = Offset(width * 0.25f, startY),
                end = Offset(width * 0.25f, endY),
                strokeWidth = strokeWidth,
                cap = StrokeCap.Round
            )
            // Bar 2 (vertical)
            drawLine(
                color = tint,
                start = Offset(width * 0.48f, startY),
                end = Offset(width * 0.48f, endY),
                strokeWidth = strokeWidth,
                cap = StrokeCap.Round
            )
            // Bar 3 (slanted / tilted book)
            drawLine(
                color = tint,
                start = Offset(width * 0.68f, startY + barHeight * 0.08f),
                end = Offset(width * 0.86f, endY),
                strokeWidth = strokeWidth,
                cap = StrokeCap.Round
            )
        }
    }
}
