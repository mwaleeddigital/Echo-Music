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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.CloudDownload
import androidx.compose.material.icons.rounded.GraphicEq
import androidx.compose.material.icons.rounded.Group
import androidx.compose.material.icons.rounded.Info
import androidx.compose.material.icons.rounded.Language
import androidx.compose.material.icons.rounded.Palette
import androidx.compose.material.icons.rounded.Person
import androidx.compose.material.icons.rounded.PlayArrow
import androidx.compose.material.icons.rounded.Search
import androidx.compose.material.icons.rounded.Security
import androidx.compose.material.icons.rounded.Storage
import androidx.compose.material.icons.rounded.Sync
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.music.echo.sharedui.theme.PureBlack

@Composable
fun SettingsScreen(
    onAccountClick: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .background(PureBlack)
            .padding(horizontal = 16.dp),
        contentPadding = PaddingValues(top = 24.dp, bottom = 140.dp)
    ) {
        // Large Settings Title
        item {
            Text(
                text = "Settings",
                style = MaterialTheme.typography.displaySmall,
                color = Color(0xFFF2E9D8), // Slightly warm off-white, matching the screenshot
                fontWeight = FontWeight.SemiBold,
                modifier = Modifier.padding(bottom = 20.dp, start = 4.dp)
            )
        }

        // Search Bar
        item {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp)
                    .clip(CircleShape)
                    .border(1.dp, Color(0xFF403C34), CircleShape)
                    .background(Color.Transparent)
                    .padding(horizontal = 16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Rounded.Search,
                    contentDescription = "Search",
                    tint = Color(0xFFC4B89D),
                    modifier = Modifier.size(20.dp)
                )
                Spacer(modifier = Modifier.width(12.dp))
                Text(
                    text = "Search",
                    color = Color(0xFF8E8877),
                    fontSize = 16.sp
                )
            }
            Spacer(modifier = Modifier.height(24.dp))
        }

        // Setting Items
        item {
            SettingsRowItem(
                icon = { Icon(Icons.Rounded.Person, null, tint = Color(0xFFD4C49A), modifier = Modifier.size(22.dp)) },
                text = "Account",
                onClick = onAccountClick
            )
            SettingsRowItem(icon = { Text("Ai", fontWeight = FontWeight.Bold, color = Color(0xFFD4C49A), fontSize = 15.sp) }, text = "AI Hub")
            SettingsRowItem(icon = { Icon(Icons.Rounded.GraphicEq, null, tint = Color(0xFFD4C49A), modifier = Modifier.size(22.dp)) }, text = "Contribute to Lossless Music")
            SettingsRowItem(icon = { Icon(Icons.Rounded.Palette, null, tint = Color(0xFFD4C49A), modifier = Modifier.size(22.dp)) }, text = "Appearance")
            SettingsRowItem(icon = { Icon(Icons.Rounded.PlayArrow, null, tint = Color(0xFFD4C49A), modifier = Modifier.size(22.dp)) }, text = "Player and audio")
            SettingsRowItem(icon = { Icon(Icons.Rounded.Group, null, tint = Color(0xFFD4C49A), modifier = Modifier.size(22.dp)) }, text = "Listen Together")
            SettingsRowItem(icon = { Icon(Icons.Rounded.Language, null, tint = Color(0xFFD4C49A), modifier = Modifier.size(22.dp)) }, text = "Content")
            SettingsRowItem(icon = { Icon(Icons.Rounded.Security, null, tint = Color(0xFFD4C49A), modifier = Modifier.size(22.dp)) }, text = "Privacy")
            SettingsRowItem(icon = { Icon(Icons.Rounded.Storage, null, tint = Color(0xFFD4C49A), modifier = Modifier.size(22.dp)) }, text = "Storage")
            SettingsRowItem(icon = { Icon(Icons.Rounded.CloudDownload, null, tint = Color(0xFFD4C49A), modifier = Modifier.size(22.dp)) }, text = "Backup and restore")
            SettingsRowItem(icon = { Icon(Icons.Rounded.Sync, null, tint = Color(0xFFD4C49A), modifier = Modifier.size(22.dp)) }, text = "System update")
            SettingsRowItem(icon = { Icon(Icons.Rounded.Info, null, tint = Color(0xFFD4C49A), modifier = Modifier.size(22.dp)) }, text = "About")
        }
    }
}

@Composable
private fun SettingsRowItem(
    icon: @Composable () -> Unit,
    text: String,
    onClick: () -> Unit = {}
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp)
            .clip(RoundedCornerShape(16.dp))
            .background(Color(0xFF121110)) // Very dark brown/black
            .clickable(onClick = onClick)
            .padding(horizontal = 20.dp, vertical = 20.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(42.dp)
                .clip(RoundedCornerShape(14.dp))
                .background(Color(0xFF232018)), // Dark olive/brownish tint
            contentAlignment = Alignment.Center
        ) {
            icon()
        }
        Spacer(modifier = Modifier.width(20.dp))
        Text(
            text = text,
            fontSize = 17.sp,
            color = Color(0xFFF2E9D8),
            fontWeight = FontWeight.Medium
        )
    }
}
