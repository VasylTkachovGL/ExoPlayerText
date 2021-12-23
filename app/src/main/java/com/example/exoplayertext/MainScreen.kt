package com.example.exoplayertext

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.vectorResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel


/*
* @author Tkachov Vasyl
* @since 22.12.2021
*/
@Composable
fun MainScreen(viewModel: BaseViewModel = viewModel()) {

    Surface(modifier = Modifier.fillMaxSize()) {
        Column(modifier = Modifier.background(color = Color.Black)) {
            PlayerRow("Drums", "asset:///android_asset/StemDrums.mp3", viewModel)
            PlayerRow("Bass", "asset:///android_asset/StemBass.mp3", viewModel)
            PlayerRow("Rhythm", "asset:///android_asset/StemGRhythm.mp3", viewModel)
            PlayerRow("Guitar", "asset:///android_asset/StemGSolo.mp3", viewModel)
        }
    }
}

@Composable
fun PlayerRow(label: String, filePath: String, viewModel: BaseViewModel) {
    viewModel.addPlayer(label, filePath)
    LaunchedEffect(label) {
        viewModel.startPlaying(label)
    }

    Row(
        modifier = Modifier
            .wrapContentWidth()
            .background(color = MaterialTheme.colors.background)
            .padding(16.dp, 8.dp, 8.dp, 8.dp),
        horizontalArrangement = Arrangement.SpaceEvenly,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = label,
            color = Color.White,
            style = MaterialTheme.typography.h5,
            modifier = Modifier
                .weight(1f)
        )
        IconButton(
            onClick = {
                if (viewModel.mutedState(label) == false) {
                    viewModel.onPlayerVolumeUp(label)
                } else {
                    viewModel.onPlayerMute(label)
                }
            }
        ) {
            Icon(
                imageVector = ImageVector.vectorResource(
                    id = if (viewModel.mutedState(label) == false) R.drawable.ic_pause else R.drawable.ic_play
                ),
                tint = MaterialTheme.colors.secondary,
                contentDescription = if (viewModel.mutedState(label) == false) "Stop" else "Play"
            )
        }
        Spacer(modifier = Modifier.size(8.dp))
        Button(
            onClick = {
                viewModel.onSoloClick(label)
            }) {
            Text(text = "Solo")
        }
    }
    Spacer(
        modifier = Modifier.size(1.dp)
    )
}