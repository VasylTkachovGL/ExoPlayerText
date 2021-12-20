package com.example.exoplayertext

import android.net.Uri
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.material.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.exoplayertext.ui.theme.ExoPlayerTextTheme
import androidx.compose.foundation.layout.*
import androidx.compose.material.Checkbox
import androidx.compose.material.Text
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import com.google.android.exoplayer2.ui.PlayerView


class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            ExoPlayerTextTheme {
                Surface(modifier = Modifier.fillMaxSize()) {
                    Column {
                        CheckablePlayerRow("Drums", "asset:///android_asset/StemDrums.mp3")
                        CheckablePlayerRow("Bass", "asset:///android_asset/StemBass.mp3")
                        CheckablePlayerRow("Rhythm", "asset:///android_asset/StemGRhythm.mp3")
                        CheckablePlayerRow("Guitar", "asset:///android_asset/StemGSolo.mp3")
                    }
                }
            }
        }
    }
}

@Composable
fun CheckablePlayerRow(label: String, path: String) {
    Row(
        modifier = Modifier
            .wrapContentSize()
            .padding(8.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        val drumPlayer =
            exoPlayerView(Uri.parse(path))
        Text(text = label)
        Spacer(Modifier.size(16.dp))
        PlayerCheckBox(drumPlayer)
    }
}

@Composable
fun exoPlayerView(uri: Uri) : MyAppExoPlayer {
        PlayerView(LocalContext.current).apply {
            setErrorMessageProvider(PlayerErrorMessageProvider())

            val myAppExoPlayer = MyAppExoPlayer(LocalContext.current, uri)
            this.player = myAppExoPlayer.player
            LaunchedEffect(myAppExoPlayer) {
                myAppExoPlayer.startPlaying()
            }
            return myAppExoPlayer
        }
}

@Composable
fun PlayerCheckBox(player: MyAppExoPlayer) {
    val checkedState = remember { mutableStateOf(true) }
    Checkbox(
        checked = checkedState.value,
        onCheckedChange = {
            checkedState.value = it
            if (it) {
                player.startPlaying()
            } else {
                player.stopPlaying()
            }
        }
    )
}

@Preview(showBackground = true)
@Composable
fun DefaultPreview() {
    ExoPlayerTextTheme {
        exoPlayerView(Uri.parse("asset:///android_asset/guitar.wav"))
    }
}