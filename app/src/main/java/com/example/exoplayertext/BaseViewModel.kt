package com.example.exoplayertext

import android.app.Application
import android.net.Uri
import androidx.compose.runtime.MutableState
import androidx.lifecycle.AndroidViewModel
import com.google.android.exoplayer2.Player


/*
* @author Tkachov Vasyl
* @since 22.12.2021
*/
class BaseViewModel(application: Application) : AndroidViewModel(application) {

    private val playerStates = mutableMapOf<String, MyAppExoPlayer>()

    fun addPlayer(key: String, filePath: String) {
        val context = getApplication<Application>().applicationContext
        val uri = Uri.parse(filePath)
        val player = MyAppExoPlayer(context, uri)
        playerStates[key] = player
    }

    fun onPlayerMute(key: String) {
        playerStates[key]?.mute()
    }

    fun onPlayerVolumeUp(key: String) {
        playerStates[key]?.volumeUp()
    }

    fun startPlaying(key: String) {
        playerStates[key]?.startPlaying(Player.REPEAT_MODE_ONE)
    }

    /**
     * Mutes all players except from one
     */
    fun onSoloClick(key: String) {
        for (state in playerStates) {
            if (state.key != key) {
                state.value.mute()
            } else {
                state.value.volumeUp()
            }
        }
    }

    fun mutedState(key: String): Boolean? {
        return playerStates[key]?.mutedState?.value
    }
}