package com.example.exoplayertext

import android.content.Context
import android.net.Uri
import com.google.android.exoplayer2.*
import com.google.android.exoplayer2.source.ConcatenatingMediaSource
import com.google.android.exoplayer2.source.ProgressiveMediaSource
import com.google.android.exoplayer2.trackselection.AdaptiveTrackSelection
import com.google.android.exoplayer2.trackselection.DefaultTrackSelector
import com.google.android.exoplayer2.trackselection.DefaultTrackSelector.ParametersBuilder
import com.google.android.exoplayer2.trackselection.ExoTrackSelection
import com.google.android.exoplayer2.upstream.DataSource
import com.google.android.exoplayer2.upstream.DefaultDataSource
import com.google.android.exoplayer2.util.EventLogger

/*
 * @author Tkachov Vasyl
 * @since 17.12.2021
 */
class MyAppExoPlayer(private val context: Context, private val uri: Uri) {

    private var player: ExoPlayer? = null

    private var trackSelector: DefaultTrackSelector? = null
    private var trackSelectorParameters: DefaultTrackSelector.Parameters? = null

    private var dataSourceFactory: DataSource.Factory? = null
    private var progressiveMediaSourceFactory: ProgressiveMediaSource.Factory? = null
    private var concatenatingMediaSource: ConcatenatingMediaSource? = null

    val currentPosition: Long
        get() = player?.currentPosition ?: 0

    init {
        if (dataSourceFactory == null) {
            dataSourceFactory = DefaultDataSource.Factory(context)
        }
        initPlayer()
    }

    private fun initPlayer() {
        trackSelectorParameters = ParametersBuilder(context).build()
        val trackSelectionFactory: ExoTrackSelection.Factory = AdaptiveTrackSelection.Factory()
        trackSelector = DefaultTrackSelector(context, trackSelectionFactory)
        trackSelector?.apply {
            parameters = trackSelectorParameters!!
            val renderersFactory: RenderersFactory =
                DefaultRenderersFactory(context.applicationContext)
            player = ExoPlayer.Builder(context)
                .setRenderersFactory(renderersFactory)
                .setTrackSelector(this)
                .build()
        }
        player?.playWhenReady = true
        player?.addAnalyticsListener(EventLogger(trackSelector))
        progressiveMediaSourceFactory = dataSourceFactory?.let { ProgressiveMediaSource.Factory(it) }
        concatenatingMediaSource = ConcatenatingMediaSource()
    }

    fun startPlaying(repeat: @Player.RepeatMode Int) {
        if (player == null) {
            initPlayer()
        }
        progressiveMediaSourceFactory?.let {
            val mediaSource = it.createMediaSource(MediaItem.fromUri(uri))
            player?.apply {
                setMediaSource(mediaSource, true)
                repeatMode = repeat
                prepare()
            }
        }
    }

    fun addToQueue(uri: Uri) {
        if (player == null) {
            return
        }
        progressiveMediaSourceFactory?.let {
            val mediaSource = it.createMediaSource(MediaItem.fromUri(uri))
            concatenatingMediaSource?.addMediaSource(mediaSource)
        }
    }

    fun mute() {
        player?.volume = 0f
    }

    fun volumeUp() {
        player?.volume = 1f
    }

    fun stopPlaying() {
        player?.stop()
        concatenatingMediaSource?.clear()
    }

    fun releasePlayer() {
        trackSelectorParameters = trackSelector?.parameters
        player?.release()
        player = null
        trackSelector = null
    }
}