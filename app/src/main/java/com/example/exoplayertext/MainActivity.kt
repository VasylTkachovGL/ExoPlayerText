package com.example.exoplayertext

import android.net.Uri
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.vectorResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.exoplayertext.ui.theme.ExoPlayerTextTheme
import com.google.android.exoplayer2.Player
import com.google.android.exoplayer2.ui.PlayerView


class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            ExoPlayerTextTheme {
                Surface(
                    modifier = Modifier.fillMaxSize()
                ) {
                    Column(modifier = Modifier.background(color = Color.Black)) {
                        TrackPlayer("Drums", "asset:///android_asset/StemDrums.mp3")
                        TrackPlayer("Bass", "asset:///android_asset/StemBass.mp3")
                        TrackPlayer("Rhythm", "asset:///android_asset/StemGRhythm.mp3")
                        TrackPlayer("Guitar", "asset:///android_asset/StemGSolo.mp3")
                    }
                }
            }
        }
    }
}

@Composable
fun TrackPlayer(label: String, path: String) {
    Row(
        modifier = Modifier
            .wrapContentWidth()
            .background(color = MaterialTheme.colors.background)
            .padding(16.dp, 8.dp, 8.dp, 16.dp),
        horizontalArrangement = Arrangement.SpaceEvenly,
        verticalAlignment = Alignment.CenterVertically
    ) {
        val myAppExoPlayer = exoPlayerView(Uri.parse(path))
        Text(
            text = label,
            color = Color.White,
            style = MaterialTheme.typography.h5,
            modifier = Modifier
                .weight(1f)
        )
        PlayStopButton(myAppExoPlayer)
    }
    Spacer(
        modifier = Modifier.size(2.dp)
    )
}

@Composable
fun exoPlayerView(uri: Uri): MyAppExoPlayer {
    PlayerView(LocalContext.current).apply {
        setErrorMessageProvider(PlayerErrorMessageProvider())

        val myAppExoPlayer = MyAppExoPlayer(LocalContext.current, uri)
        this.player = myAppExoPlayer.player
        LaunchedEffect(myAppExoPlayer) {
            myAppExoPlayer.startPlaying(Player.REPEAT_MODE_ONE)
        }
        return myAppExoPlayer
    }
}

@Composable
fun PlayStopButton(player: MyAppExoPlayer) {
    val checkedState = remember { mutableStateOf(true) }
    IconToggleButton(
        checked = checkedState.value,
        onCheckedChange = {
            checkedState.value = it
            if (it) {
                player.volumeUp()
            } else {
                player.mute()
            }
        }
    ) {
        Icon(
            imageVector = ImageVector.vectorResource(
                id = if (checkedState.value)
                    R.drawable.ic_pause else R.drawable.ic_play
            ),
            tint = MaterialTheme.colors.secondary,
            contentDescription = if (checkedState.value) "Stop" else "Play"
        )
    }
}

@Preview(showBackground = true)
@Composable
fun DefaultPreview() {
    ExoPlayerTextTheme {
        Surface(modifier = Modifier.fillMaxSize()) {
        }
    }
}