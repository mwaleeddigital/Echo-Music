package com.music.echo.sharedui.screens

import androidx.compose.foundation.Image
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
import androidx.compose.material.icons.rounded.ArrowBack
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material.icons.rounded.Edit
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
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
import androidx.compose.material.icons.rounded.Add
import androidx.compose.material.icons.rounded.Sync
import androidx.compose.material.icons.rounded.AccountBox
import androidx.compose.material.icons.rounded.Check
import androidx.compose.material.icons.rounded.Person

@Composable
fun AccountScreen(
    onBackClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .background(PureBlack)
            .padding(horizontal = 16.dp),
        contentPadding = PaddingValues(top = 24.dp, bottom = 140.dp)
    ) {
        // Top Bar
        item {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 24.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Rounded.ArrowBack,
                    contentDescription = "Back",
                    tint = Color.White,
                    modifier = Modifier
                        .size(28.dp)
                        .clickable(onClick = onBackClick)
                )
                Spacer(modifier = Modifier.width(16.dp))
                Text(
                    text = "Account",
                    fontSize = 24.sp,
                    color = Color.White,
                    fontWeight = FontWeight.Normal
                )
            }
        }

        // Settings Section
        item {
            SectionHeader(text = "Settings")
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(16.dp))
                    .background(Color(0xFF121110))
                    .padding(horizontal = 20.dp, vertical = 20.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    // Profile Image placeholder
                    Box(
                        modifier = Modifier
                            .size(50.dp)
                            .clip(CircleShape)
                            .background(Color(0xFFF05B4F)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Rounded.Person,
                            contentDescription = "Avatar",
                            tint = Color.White,
                            modifier = Modifier.size(32.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(16.dp))
                    Text(
                        text = "Waleed's\nMarketing\nUniverse",
                        fontSize = 17.sp,
                        color = Color(0xFFF2E9D8),
                        fontWeight = FontWeight.SemiBold,
                        lineHeight = 22.sp
                    )
                }
                
                // Log out button
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(20.dp))
                        .border(1.dp, Color(0xFF403C34), RoundedCornerShape(20.dp))
                        .clickable { }
                        .padding(horizontal = 16.dp, vertical = 8.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "Log out",
                        fontSize = 14.sp,
                        color = Color(0xFFF2E9D8),
                        fontWeight = FontWeight.Medium
                    )
                }
            }
            Spacer(modifier = Modifier.height(24.dp))
        }

        // Login with token Section
        item {
            SectionHeader(text = "Login with token")
            AccountRowItem(
                iconText = "⬡", // Cube-like placeholder
                text = "Tap to show token"
            )
            Spacer(modifier = Modifier.height(24.dp))
        }

        // Player & Content Section
        item {
            SectionHeader(text = "Player & Content")
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(16.dp))
                    .background(Color(0xFF121110))
            ) {
                AccountRowSwitch(
                    icon = { Icon(Icons.Rounded.Add, null, tint = Color(0xFFD4C49A), modifier = Modifier.size(20.dp)) },
                    text = "More content",
                    checked = true
                )
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(1.dp)
                        .background(Color(0x0AFFFFFF))
                )
                AccountRowSwitch(
                    icon = { Icon(Icons.Rounded.Sync, null, tint = Color(0xFFD4C49A), modifier = Modifier.size(20.dp)) },
                    text = "Auto sync with account",
                    checked = true
                )
            }
            Spacer(modifier = Modifier.height(24.dp))
        }

        // Integrations Section
        item {
            SectionHeader(text = "Integrations")
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(16.dp))
                    .background(Color(0xFF121110))
            ) {
                AccountRowItem(
                    iconText = "Discord",
                    text = "Discord Integration"
                )
                Box(modifier = Modifier.fillMaxWidth().height(1.dp).background(Color(0x0AFFFFFF)))
                AccountRowItem(
                    iconText = "Last.fm",
                    text = "Last.fm Integration"
                )
                Box(modifier = Modifier.fillMaxWidth().height(1.dp).background(Color(0x0AFFFFFF)))
                AccountRowSwitch(
                    iconText = "LB",
                    text = "ListenBrainz scrobbling",
                    checked = false
                )
                Box(modifier = Modifier.fillMaxWidth().height(1.dp).background(Color(0x0AFFFFFF)))
                AccountRowItem(
                    icon = { Icon(Icons.Rounded.Edit, null, tint = Color(0xFFD4C49A), modifier = Modifier.size(20.dp)) },
                    text = "Set ListenBrainz Token"
                )
            }
        }
    }
}

@Composable
private fun SectionHeader(text: String) {
    Text(
        text = text,
        fontSize = 15.sp,
        color = Color(0xFFD4C49A),
        fontWeight = FontWeight.SemiBold,
        modifier = Modifier.padding(bottom = 12.dp, start = 4.dp)
    )
}

@Composable
private fun AccountRowItem(
    icon: (@Composable () -> Unit)? = null,
    iconText: String? = null,
    text: String,
    onClick: () -> Unit = {}
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(horizontal = 20.dp, vertical = 20.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(42.dp)
                .clip(RoundedCornerShape(14.dp))
                .background(Color(0xFF232018)),
            contentAlignment = Alignment.Center
        ) {
            if (icon != null) {
                icon()
            } else if (iconText != null) {
                if (iconText == "Discord") {
                     Icon(Icons.Rounded.AccountBox, null, tint = Color(0xFFD4C49A), modifier = Modifier.size(20.dp))
                } else if (iconText == "Last.fm") {
                     Text("as", color = Color(0xFFD4C49A), fontWeight = FontWeight.Bold, fontSize = 16.sp)
                } else {
                     Text(iconText, color = Color(0xFFD4C49A), fontWeight = FontWeight.Bold, fontSize = 16.sp)
                }
            }
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

@Composable
private fun AccountRowSwitch(
    icon: (@Composable () -> Unit)? = null,
    iconText: String? = null,
    text: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit = {}
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Box(
                modifier = Modifier
                    .size(42.dp)
                    .clip(RoundedCornerShape(14.dp))
                    .background(Color(0xFF232018)),
                contentAlignment = Alignment.Center
            ) {
                if (icon != null) {
                    icon()
                } else if (iconText != null) {
                    Text(iconText, color = Color(0xFFD4C49A), fontWeight = FontWeight.Bold, fontSize = 16.sp)
                }
            }
            Spacer(modifier = Modifier.width(20.dp))
            Text(
                text = text,
                fontSize = 17.sp,
                color = Color(0xFFF2E9D8),
                fontWeight = FontWeight.Medium
            )
        }
        
        Switch(
            checked = checked,
            onCheckedChange = onCheckedChange,
            thumbContent = if (checked) {
                {
                    Icon(
                        imageVector = Icons.Rounded.Check,
                        contentDescription = null,
                        modifier = Modifier.size(16.dp),
                        tint = Color(0xFFD4C49A)
                    )
                }
            } else if (!checked && iconText == "LB") {
                {
                    Icon(
                        imageVector = Icons.Rounded.Close,
                        contentDescription = null,
                        modifier = Modifier.size(16.dp),
                        tint = Color(0xFF8E8877)
                    )
                }
            } else null,
            colors = SwitchDefaults.colors(
                checkedThumbColor = Color(0xFF28241A),
                checkedTrackColor = Color(0xFFD4C49A),
                uncheckedThumbColor = Color(0xFF8E8877),
                uncheckedTrackColor = Color(0xFF333029),
                checkedIconColor = Color(0xFFD4C49A),
                uncheckedIconColor = Color(0xFF8E8877)
            )
        )
    }
}
