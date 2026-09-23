package com.music.echo.desktop

import androidx.compose.runtime.remember
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Window
import androidx.compose.ui.window.WindowState
import androidx.compose.ui.window.application
import com.music.echo.sharedui.EchoApp

fun main() = application {
    val audioPlayer = remember { echo.music.iad1tya.playback.DesktopAudioPlayer() }
    val localRepository = remember { DesktopLocalMusicRepository() }
    val appIcon = painterResource("icon.png")
    Window(
        onCloseRequest = {
            audioPlayer.release()
            exitApplication()
        },
        title = "Echo Music",
        icon = appIcon,
        state = WindowState(width = 1200.dp, height = 800.dp)
    ) {
        EchoApp(audioPlayer = audioPlayer, repository = localRepository)
    }
}

