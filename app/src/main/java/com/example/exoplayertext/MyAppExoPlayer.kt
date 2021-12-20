package com.example.exoplayertext

import android.content.Context
import android.net.Uri
import com.google.android.exoplayer2.ExoPlayer
import com.google.android.exoplayer2.trackselection.DefaultTrackSelector
import com.google.android.exoplayer2.source.ProgressiveMediaSource
import com.google.android.exoplayer2.source.ConcatenatingMediaSource
import com.google.android.exoplayer2.trackselection.DefaultTrackSelector.ParametersBuilder
import com.google.android.exoplayer2.trackselection.ExoTrackSelection
import com.google.android.exoplayer2.trackselection.AdaptiveTrackSelection
import com.google.android.exoplayer2.RenderersFactory
import com.google.android.exoplayer2.DefaultRenderersFactory
import com.google.android.exoplayer2.Player
import com.google.android.exoplayer2.util.EventLogger
import com.google.android.exoplayer2.source.MediaSource
import com.google.android.exoplayer2.upstream.AssetDataSource
import com.google.android.exoplayer2.upstream.DataSource
import com.google.android.exoplayer2.upstream.DataSpec
import com.google.android.exoplayer2.upstream.DefaultDataSource

/*
 * @author Tkachov Vasyl
 * @since 17.12.2021
 */
class MyAppExoPlayer(private val context: Context, private val uri: Uri) {

    private var dataSourceFactory: DataSource.Factory? = null
    var player: ExoPlayer? = null
    private var trackSelector: DefaultTrackSelector? = null
    private var trackSelectorParameters: DefaultTrackSelector.Parameters? = null
    private var progressiveMediaSourceFactory: ProgressiveMediaSource.Factory? = null
    private var concatenatingMediaSource: ConcatenatingMediaSource? = null
    private val currentMediaPlayerIndex = 0

    init {
//        val assetDataSource = AssetDataSource(context)
//        val dataSpec = DataSpec(uri)
//        try {
//            assetDataSource.open(dataSpec)
//        } catch (e: AssetDataSource.AssetDataSourceException) {
//            e.printStackTrace()
//        }

        if (dataSourceFactory == null) {
            dataSourceFactory = DefaultDataSource.Factory(context)
        }
        initPlayer()
    }

    private fun initPlayer() {
        if (player == null) {
            trackSelectorParameters = ParametersBuilder(context).build()
            val trackSelectionFactory: ExoTrackSelection.Factory = AdaptiveTrackSelection.Factory()
            trackSelector = DefaultTrackSelector(context, trackSelectionFactory)
            trackSelector?.parameters = trackSelectorParameters!!
            val renderersFactory: RenderersFactory =
                DefaultRenderersFactory(context.applicationContext)
            player = ExoPlayer.Builder(context)
                .setRenderersFactory(renderersFactory)
                .setTrackSelector(trackSelector!!)
                .build()
            player?.playWhenReady = true
            player?.addAnalyticsListener(EventLogger(trackSelector))
            progressiveMediaSourceFactory = ProgressiveMediaSource.Factory(dataSourceFactory!!)
        }
        concatenatingMediaSource = ConcatenatingMediaSource()
    }

    fun startPlaying() {
        if (player == null) initPlayer()
        val mediaSource: MediaSource = progressiveMediaSourceFactory!!.createMediaSource(uri)
        player?.apply {
            setMediaSource(mediaSource, true)
            repeatMode = Player.REPEAT_MODE_ONE
            prepare()
        }
    }

    fun addToQ(uri: Uri?) {
        if (player == null) return
        val mediaSource: MediaSource = progressiveMediaSourceFactory!!.createMediaSource(uri!!)
        concatenatingMediaSource?.addMediaSource(mediaSource)
    }

    fun stopPlaying() {
        player?.stop()
        concatenatingMediaSource?.clear()
    }

    val currentPosition: Long
        get() = if (player != null) player!!.currentPosition else 0

    fun releasePlayer() {
        if (player != null) {
            trackSelectorParameters = trackSelector?.parameters
            player?.release()
            player = null
            trackSelector = null
        }
    }
}