package com.music.echo.sharedui.component

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
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material.icons.automirrored.rounded.ArrowForward
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material.icons.rounded.Folder
import androidx.compose.material.icons.rounded.Group
import androidx.compose.material.icons.rounded.Home
import androidx.compose.material.icons.rounded.Notifications
import androidx.compose.material.icons.rounded.Person
import androidx.compose.material.icons.rounded.Search
import androidx.compose.material.icons.rounded.CheckCircle
import androidx.compose.material.icons.rounded.ContentCopy
import androidx.compose.material.icons.rounded.Delete
import androidx.compose.material.icons.rounded.Download
import androidx.compose.material.icons.rounded.Settings
import androidx.compose.material.icons.rounded.UnfoldMore
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
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
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Popup
import androidx.compose.ui.window.PopupProperties
import coil3.compose.AsyncImage
import com.music.echo.sharedui.screens.DisplayArtist
import com.music.echo.sharedui.screens.SearchSuggestionsData
import com.music.echo.sharedui.theme.NothingRed
import com.music.echo.sharedui.theme.PureBlack
import com.music.echo.sharedui.theme.TextPrimary
import com.music.echo.sharedui.theme.TextSecondary

@Composable
fun DesktopTopBar(
    searchQuery: String,
    onSearchQueryChange: (String) -> Unit,
    onSearchSubmit: (String) -> Unit = {},
    searchSuggestions: SearchSuggestionsData = SearchSuggestionsData(),
    onSuggestionClick: (String) -> Unit = {},
    onArtistSuggestionClick: (DisplayArtist) -> Unit = {},
    isHomeActive: Boolean = true,
    onHomeClick: () -> Unit,
    onBackClick: () -> Unit = {},
    onForwardClick: () -> Unit = {},
    onBrowseClick: () -> Unit = {},
    onNotificationsClick: () -> Unit = {},
    onFriendsClick: () -> Unit = {},
    onProfileClick: () -> Unit = {},
    storageStats: String = "0 songs • 0 MB",
    onOpenDownloadedClick: () -> Unit = {},
    onClearCacheClick: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    var isSearchFocused by remember { mutableStateOf(false) }
    var showNotificationsPopup by remember { mutableStateOf(false) }
    var showFriendsPopup by remember { mutableStateOf(false) }
    var showProfilePopup by remember { mutableStateOf(false) }
    val focusManager = LocalFocusManager.current
    val showSuggestionsPopup = isSearchFocused && (!searchSuggestions.isEmpty || searchQuery.isNotBlank())

    Row(
        modifier = modifier
            .fillMaxWidth()
            .height(64.dp)
            .background(PureBlack)
            .padding(horizontal = 16.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        // Left: Navigation Arrows (< and >)
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(34.dp)
                    .clip(CircleShape)
                    .background(Color(0xFF161616))
                    .clickable(onClick = onBackClick),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.AutoMirrored.Rounded.ArrowBack,
                    contentDescription = "Back",
                    tint = TextPrimary,
                    modifier = Modifier.size(18.dp)
                )
            }

            Box(
                modifier = Modifier
                    .size(34.dp)
                    .clip(CircleShape)
                    .background(Color(0xFF161616))
                    .clickable(onClick = onForwardClick),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.AutoMirrored.Rounded.ArrowForward,
                    contentDescription = "Forward",
                    tint = TextSecondary,
                    modifier = Modifier.size(18.dp)
                )
            }
        }

        // Center: Home Pill + Search Bar with Browse Icon & Suggestions Popup
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            // Home circular button
            Box(
                modifier = Modifier
                    .size(44.dp)
                    .clip(CircleShape)
                    .background(if (isHomeActive) Color(0xFF242424) else Color(0xFF161616))
                    .border(
                        1.dp,
                        if (isHomeActive) Color(0x33FFFFFF) else Color.Transparent,
                        CircleShape
                    )
                    .clickable {
                        isSearchFocused = false
                        focusManager.clearFocus()
                        onHomeClick()
                    },
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Rounded.Home,
                    contentDescription = "Home",
                    tint = if (isHomeActive) Color.White else TextSecondary,
                    modifier = Modifier.size(24.dp)
                )
            }

            // Search Bar Container ("What do you want to play?")
            Box(
                modifier = Modifier.width(440.dp)
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(44.dp)
                        .clip(CircleShape)
                        .background(Color(0xFF242424))
                        .border(
                            1.dp,
                            if (isSearchFocused) Color(0x66FFFFFF) else Color(0x28FFFFFF),
                            CircleShape
                        )
                        .padding(horizontal = 14.dp),
                    contentAlignment = Alignment.CenterStart
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Rounded.Search,
                            contentDescription = "Search",
                            tint = if (isSearchFocused) Color.White else TextSecondary,
                            modifier = Modifier.size(20.dp)
                        )

                        Spacer(modifier = Modifier.width(10.dp))

                        Box(modifier = Modifier.weight(1f)) {
                            if (searchQuery.isEmpty()) {
                                Text(
                                    text = "What do you want to play?",
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = TextSecondary
                                )
                            }

                            BasicTextField(
                                value = searchQuery,
                                onValueChange = { newQuery ->
                                    onSearchQueryChange(newQuery)
                                },
                                singleLine = true,
                                textStyle = TextStyle(
                                    color = TextPrimary,
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.Normal
                                ),
                                cursorBrush = SolidColor(NothingRed),
                                keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search),
                                keyboardActions = KeyboardActions(
                                    onSearch = {
                                        if (searchQuery.isNotBlank()) {
                                            isSearchFocused = false
                                            focusManager.clearFocus()
                                            onSearchSubmit(searchQuery)
                                        }
                                    }
                                ),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .onFocusChanged { focusState ->
                                        isSearchFocused = focusState.isFocused
                                    }
                            )
                        }

                        if (searchQuery.isNotEmpty()) {
                            Box(
                                modifier = Modifier
                                    .size(22.dp)
                                    .clip(CircleShape)
                                    .background(Color(0xFF333333))
                                    .clickable {
                                        onSearchQueryChange("")
                                    },
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Rounded.Close,
                                    contentDescription = "Clear",
                                    tint = TextSecondary,
                                    modifier = Modifier.size(14.dp)
                                )
                            }
                            Spacer(modifier = Modifier.width(8.dp))
                        }

                        // Divider before Browse Drawer icon
                        Box(
                            modifier = Modifier
                                .width(1.dp)
                                .height(18.dp)
                                .background(Color(0x33FFFFFF))
                        )

                        Spacer(modifier = Modifier.width(8.dp))

                        // Browse / Explore icon
                        Box(
                            modifier = Modifier
                                .size(28.dp)
                                .clip(CircleShape)
                                .clickable {
                                    isSearchFocused = false
                                    focusManager.clearFocus()
                                    onBrowseClick()
                                },
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Rounded.Folder,
                                contentDescription = "Browse",
                                tint = TextSecondary,
                                modifier = Modifier.size(18.dp)
                            )
                        }
                    }
                }

                // Spotify-like Dropdown Suggestions Popup
                if (showSuggestionsPopup) {
                    val offsetYPx = with(LocalDensity.current) { 50.dp.roundToPx() }
                    Popup(
                        alignment = Alignment.TopStart,
                        offset = IntOffset(0, offsetYPx),
                        onDismissRequest = {
                            isSearchFocused = false
                            focusManager.clearFocus()
                        },
                        properties = PopupProperties(focusable = false)
                    ) {
                        Surface(
                            modifier = Modifier
                                .width(440.dp)
                                .shadow(24.dp, RoundedCornerShape(12.dp), spotColor = Color.Black)
                                .clip(RoundedCornerShape(12.dp))
                                .background(Color(0xFF1E1E20))
                                .border(1.dp, Color(0x33FFFFFF), RoundedCornerShape(12.dp)),
                            color = Color(0xFF1E1E20),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 8.dp)
                            ) {
                                // Sub-header: Navigation shortcut hints
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(horizontal = 14.dp, vertical = 6.dp),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                                    ) {
                                        Icon(
                                            imageVector = Icons.Rounded.UnfoldMore,
                                            contentDescription = null,
                                            tint = TextSecondary,
                                            modifier = Modifier.size(14.dp)
                                        )
                                        Text(
                                            text = "Navigate",
                                            fontSize = 11.sp,
                                            color = TextSecondary
                                        )
                                    }

                                    Box(
                                        modifier = Modifier
                                            .clip(RoundedCornerShape(4.dp))
                                            .background(Color(0xFF2C2C2E))
                                            .padding(horizontal = 6.dp, vertical = 2.dp)
                                    ) {
                                        Text(
                                            text = "Enter Search",
                                            fontSize = 10.sp,
                                            color = TextSecondary,
                                            fontWeight = FontWeight.Medium
                                        )
                                    }
                                }

                                // Search query suggestions
                                if (searchSuggestions.queries.isNotEmpty()) {
                                    searchSuggestions.queries.take(5).forEach { query ->
                                        SuggestionRow(
                                            text = query,
                                            onClick = {
                                                isSearchFocused = false
                                                focusManager.clearFocus()
                                                onSuggestionClick(query)
                                            }
                                        )
                                    }
                                } else if (searchQuery.isNotBlank()) {
                                    SuggestionRow(
                                        text = searchQuery,
                                        onClick = {
                                            isSearchFocused = false
                                            focusManager.clearFocus()
                                            onSearchSubmit(searchQuery)
                                        }
                                    )
                                }

                                // Recommended Artists suggestions
                                if (searchSuggestions.recommendedArtists.isNotEmpty()) {
                                    Spacer(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(vertical = 4.dp)
                                            .height(1.dp)
                                            .background(Color(0x18FFFFFF))
                                    )

                                    searchSuggestions.recommendedArtists.take(2).forEach { artist ->
                                        ArtistSuggestionRow(
                                            artist = artist,
                                            onClick = {
                                                isSearchFocused = false
                                                focusManager.clearFocus()
                                                onArtistSuggestionClick(artist)
                                            }
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }

        // Right: Notifications, Friends, and Profile Avatar
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            // Notifications Bell & Popup
            Box {
                Box(
                    modifier = Modifier
                        .size(36.dp)
                        .clip(CircleShape)
                        .background(if (showNotificationsPopup) Color(0xFF2C2C2E) else Color(0xFF161616))
                        .clickable {
                            showNotificationsPopup = !showNotificationsPopup
                            showFriendsPopup = false
                            showProfilePopup = false
                        },
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Rounded.Notifications,
                        contentDescription = "Notifications",
                        tint = if (showNotificationsPopup) Color.White else TextSecondary,
                        modifier = Modifier.size(18.dp)
                    )
                }

                if (showNotificationsPopup) {
                    Popup(
                        alignment = Alignment.TopEnd,
                        offset = IntOffset(0, with(LocalDensity.current) { 54.dp.roundToPx() }),
                        onDismissRequest = { showNotificationsPopup = false },
                        properties = PopupProperties(focusable = false)
                    ) {
                        Surface(
                            modifier = Modifier
                                .width(340.dp)
                                .shadow(24.dp, RoundedCornerShape(12.dp))
                                .clip(RoundedCornerShape(12.dp))
                                .background(Color(0xFF1E1E20))
                                .border(1.dp, Color(0x33FFFFFF), RoundedCornerShape(12.dp)),
                            color = Color(0xFF1E1E20),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Column(modifier = Modifier.padding(16.dp)) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        text = "Notifications & What's New",
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 15.sp,
                                        color = TextPrimary
                                    )
                                    Box(
                                        modifier = Modifier
                                            .size(22.dp)
                                            .clip(CircleShape)
                                            .background(Color(0xFF2C2C2E))
                                            .clickable { showNotificationsPopup = false },
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Icon(Icons.Rounded.Close, null, tint = TextSecondary, modifier = Modifier.size(14.dp))
                                    }
                                }
                                Spacer(modifier = Modifier.height(12.dp))

                                // Item 1: Storage & Offline
                                NotificationCard(
                                    title = "Offline Storage Active",
                                    description = "Songs played online are automatically saved to storage (~/.echo_music/audio_cache) for offline listening.",
                                    icon = Icons.Rounded.Download
                                )
                                Spacer(modifier = Modifier.height(8.dp))

                                // Item 2: Continuous Radio
                                NotificationCard(
                                    title = "Continuous Radio Queue",
                                    description = "Echo automatically queues related tracks after every song for continuous playback.",
                                    icon = Icons.Rounded.CheckCircle
                                )
                                Spacer(modifier = Modifier.height(8.dp))

                                // Item 3: Recommendation Engine
                                NotificationCard(
                                    title = "Spotify Recommendation Model",
                                    description = "Your Home feed combines taste affinity, liked songs, and skip penalties to curate personalized picks.",
                                    icon = Icons.Rounded.Notifications
                                )
                            }
                        }
                    }
                }
            }

            // Friends / Community (Listen Together Unison)
            Box {
                Box(
                    modifier = Modifier
                        .size(36.dp)
                        .clip(CircleShape)
                        .background(if (showFriendsPopup) Color(0xFF2C2C2E) else Color(0xFF161616))
                        .clickable {
                            showFriendsPopup = !showFriendsPopup
                            showNotificationsPopup = false
                            showProfilePopup = false
                        },
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Rounded.Group,
                        contentDescription = "Friends",
                        tint = if (showFriendsPopup) Color.White else TextSecondary,
                        modifier = Modifier.size(18.dp)
                    )
                }

                if (showFriendsPopup) {
                    Popup(
                        alignment = Alignment.TopEnd,
                        offset = IntOffset(0, with(LocalDensity.current) { 54.dp.roundToPx() }),
                        onDismissRequest = { showFriendsPopup = false },
                        properties = PopupProperties(focusable = false)
                    ) {
                        Surface(
                            modifier = Modifier
                                .width(340.dp)
                                .shadow(24.dp, RoundedCornerShape(12.dp))
                                .clip(RoundedCornerShape(12.dp))
                                .background(Color(0xFF1E1E20))
                                .border(1.dp, Color(0x33FFFFFF), RoundedCornerShape(12.dp)),
                            color = Color(0xFF1E1E20),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Column(modifier = Modifier.padding(16.dp)) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                                    ) {
                                        Icon(Icons.Rounded.Group, null, tint = NothingRed, modifier = Modifier.size(18.dp))
                                        Text(
                                            text = "Listen Together (Unison)",
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 15.sp,
                                            color = TextPrimary
                                        )
                                    }
                                    Box(
                                        modifier = Modifier
                                            .size(22.dp)
                                            .clip(CircleShape)
                                            .background(Color(0xFF2C2C2E))
                                            .clickable { showFriendsPopup = false },
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Icon(Icons.Rounded.Close, null, tint = TextSecondary, modifier = Modifier.size(14.dp))
                                    }
                                }

                                Spacer(modifier = Modifier.height(6.dp))
                                Text(
                                    text = "Listen to music simultaneously with friends in real-time.",
                                    fontSize = 12.sp,
                                    color = TextSecondary
                                )

                                Spacer(modifier = Modifier.height(14.dp))

                                // Host Room Card
                                Column(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clip(RoundedCornerShape(8.dp))
                                        .background(Color(0xFF28282B))
                                        .padding(12.dp)
                                ) {
                                    Text(
                                        text = "YOUR SESSION ROOM CODE",
                                        fontSize = 10.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = TextSecondary,
                                        letterSpacing = 1.sp
                                    )
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Text(
                                            text = "ECHO-8421",
                                            fontSize = 18.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = Color.White
                                        )
                                        Box(
                                            modifier = Modifier
                                                .clip(RoundedCornerShape(6.dp))
                                                .background(NothingRed)
                                                .clickable { }
                                                .padding(horizontal = 10.dp, vertical = 4.dp)
                                        ) {
                                            Text(
                                                text = "Host Party",
                                                fontSize = 11.sp,
                                                fontWeight = FontWeight.SemiBold,
                                                color = Color.White
                                            )
                                        }
                                    }
                                }

                                Spacer(modifier = Modifier.height(12.dp))

                                Text(
                                    text = "No friends listening nearby right now. Share your room code to start a listening party!",
                                    fontSize = 12.sp,
                                    color = TextSecondary,
                                    lineHeight = 16.sp
                                )
                            }
                        }
                    }
                }
            }

            // User Profile Avatar & Popup
            Box {
                Box(
                    modifier = Modifier
                        .size(36.dp)
                        .clip(CircleShape)
                        .border(2.dp, Color(0xFFF5A623), CircleShape)
                        .background(if (showProfilePopup) Color(0xFF383838) else Color(0xFF2C2C2E))
                        .clickable {
                            showProfilePopup = !showProfilePopup
                            showNotificationsPopup = false
                            showFriendsPopup = false
                        },
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Rounded.Person,
                        contentDescription = "Profile",
                        tint = Color.White,
                        modifier = Modifier.size(20.dp)
                    )
                }

                if (showProfilePopup) {
                    Popup(
                        alignment = Alignment.TopEnd,
                        offset = IntOffset(0, with(LocalDensity.current) { 54.dp.roundToPx() }),
                        onDismissRequest = { showProfilePopup = false },
                        properties = PopupProperties(focusable = false)
                    ) {
                        Surface(
                            modifier = Modifier
                                .width(280.dp)
                                .shadow(24.dp, RoundedCornerShape(12.dp))
                                .clip(RoundedCornerShape(12.dp))
                                .background(Color(0xFF1E1E20))
                                .border(1.dp, Color(0x33FFFFFF), RoundedCornerShape(12.dp)),
                            color = Color(0xFF1E1E20),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Column(modifier = Modifier.padding(14.dp)) {
                                // Profile info header
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .size(40.dp)
                                            .clip(CircleShape)
                                            .border(2.dp, Color(0xFFF5A623), CircleShape)
                                            .background(Color(0xFF2C2C2E)),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Icon(Icons.Rounded.Person, null, tint = Color.White, modifier = Modifier.size(22.dp))
                                    }
                                    Column {
                                        Text(
                                            text = "Echo Listener",
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 14.sp,
                                            color = TextPrimary
                                        )
                                        Text(
                                            text = "Storage & Offline Active",
                                            fontSize = 11.sp,
                                            color = Color(0xFF1DB954)
                                        )
                                    }
                                }

                                Spacer(modifier = Modifier.height(12.dp))

                                // Offline Storage Card
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clip(RoundedCornerShape(8.dp))
                                        .background(Color(0xFF28282B))
                                        .padding(10.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    Icon(Icons.Rounded.Download, null, tint = Color(0xFF1DB954), modifier = Modifier.size(18.dp))
                                    Column {
                                        Text(
                                            text = "OFFLINE AUDIO CACHE",
                                            fontSize = 10.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = TextSecondary
                                        )
                                        Text(
                                            text = storageStats,
                                            fontSize = 12.sp,
                                            fontWeight = FontWeight.Medium,
                                            color = TextPrimary
                                        )
                                    }
                                }

                                Spacer(modifier = Modifier.height(10.dp))

                                // Action 1: Downloaded Music
                                ProfileMenuItem(
                                    text = "Downloaded Music (Offline)",
                                    icon = Icons.Rounded.Download,
                                    onClick = {
                                        showProfilePopup = false
                                        onOpenDownloadedClick()
                                    }
                                )

                                // Action 2: Settings
                                ProfileMenuItem(
                                    text = "Settings & Preferences",
                                    icon = Icons.Rounded.Settings,
                                    onClick = {
                                        showProfilePopup = false
                                        onNotificationsClick()
                                    }
                                )

                                // Action 3: Clear Storage Cache
                                ProfileMenuItem(
                                    text = "Clear Storage Cache",
                                    icon = Icons.Rounded.Delete,
                                    textColor = Color(0xFFE53935),
                                    onClick = {
                                        showProfilePopup = false
                                        onClearCacheClick()
                                    }
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun NotificationCard(
    title: String,
    description: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(8.dp))
            .background(Color(0xFF28282B))
            .padding(10.dp),
        horizontalArrangement = Arrangement.spacedBy(10.dp),
        verticalAlignment = Alignment.Top
    ) {
        Box(
            modifier = Modifier
                .size(28.dp)
                .clip(CircleShape)
                .background(Color(0xFF333336)),
            contentAlignment = Alignment.Center
        ) {
            Icon(icon, null, tint = NothingRed, modifier = Modifier.size(16.dp))
        }
        Column(modifier = Modifier.weight(1f)) {
            Text(title, fontWeight = FontWeight.SemiBold, fontSize = 13.sp, color = TextPrimary)
            Spacer(modifier = Modifier.height(2.dp))
            Text(description, fontSize = 11.sp, color = TextSecondary, lineHeight = 15.sp)
        }
    }
}

@Composable
private fun ProfileMenuItem(
    text: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    textColor: Color = TextPrimary,
    onClick: () -> Unit
) {
    val interactionSource = remember { MutableInteractionSource() }
    val isHovered by interactionSource.collectIsHoveredAsState()

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(6.dp))
            .background(if (isHovered) Color(0xFF2A2A2E) else Color.Transparent)
            .clickable(onClick = onClick)
            .padding(horizontal = 10.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        Icon(icon, null, tint = textColor.copy(alpha = 0.8f), modifier = Modifier.size(16.dp))
        Text(text, fontSize = 13.sp, fontWeight = FontWeight.Medium, color = textColor)
    }
}

@Composable
private fun SuggestionRow(
    text: String,
    onClick: () -> Unit
) {
    val interactionSource = remember { MutableInteractionSource() }
    val isHovered by interactionSource.collectIsHoveredAsState()

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(if (isHovered) Color(0xFF2E2E32) else Color.Transparent)
            .hoverable(interactionSource)
            .clickable(onClick = onClick)
            .padding(horizontal = 14.dp, vertical = 9.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Icon(
            imageVector = Icons.Rounded.Search,
            contentDescription = null,
            tint = if (isHovered) Color.White else TextSecondary,
            modifier = Modifier.size(18.dp)
        )
        Text(
            text = text,
            fontSize = 14.sp,
            fontWeight = FontWeight.Medium,
            color = if (isHovered) Color.White else TextPrimary,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
    }
}

@Composable
private fun ArtistSuggestionRow(
    artist: DisplayArtist,
    onClick: () -> Unit
) {
    val interactionSource = remember { MutableInteractionSource() }
    val isHovered by interactionSource.collectIsHoveredAsState()

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(if (isHovered) Color(0xFF2E2E32) else Color.Transparent)
            .hoverable(interactionSource)
            .clickable(onClick = onClick)
            .padding(horizontal = 14.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(36.dp)
                .clip(CircleShape)
                .background(Color(0xFF2A2A2E)),
            contentAlignment = Alignment.Center
        ) {
            if (!artist.thumbnailUrl.isNullOrBlank()) {
                AsyncImage(
                    model = artist.thumbnailUrl,
                    contentDescription = artist.name,
                    contentScale = ContentScale.Crop,
                    modifier = Modifier.fillMaxSize()
                )
            } else {
                Icon(
                    imageVector = Icons.Rounded.Person,
                    contentDescription = null,
                    tint = TextSecondary,
                    modifier = Modifier.size(20.dp)
                )
            }
        }

        Spacer(modifier = Modifier.width(12.dp))

        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = artist.name,
                fontSize = 14.sp,
                fontWeight = FontWeight.SemiBold,
                color = if (isHovered) Color.White else TextPrimary,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            Text(
                text = "Artist",
                fontSize = 12.sp,
                color = TextSecondary
            )
        }

        Box(
            modifier = Modifier
                .clip(CircleShape)
                .border(1.dp, Color(0x44FFFFFF), CircleShape)
                .background(Color(0xFF282828))
                .clickable(onClick = onClick)
                .padding(horizontal = 14.dp, vertical = 6.dp),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = "Follow",
                fontSize = 12.sp,
                fontWeight = FontWeight.Medium,
                color = Color.White
            )
        }
    }
}
