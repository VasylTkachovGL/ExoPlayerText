package com.example.exoplayertext

import android.util.Pair
import com.google.android.exoplayer2.util.ErrorMessageProvider
import com.google.android.exoplayer2.PlaybackException
import com.google.android.exoplayer2.mediacodec.MediaCodecRenderer.DecoderInitializationException
import com.google.android.exoplayer2.mediacodec.MediaCodecUtil.DecoderQueryException

/*
 * @author Tkachov Vasyl
 * @since 20.12.2021
 */
class PlayerErrorMessageProvider : ErrorMessageProvider<PlaybackException> {
    override fun getErrorMessage(e: PlaybackException): Pair<Int, String> {
        var errorString = "Playback failed"
        val cause = e.cause
        if (cause is DecoderInitializationException) {
            // Special case for decoder initialization failures.
            errorString = if (cause.codecInfo == null) {
                when {
                    cause.cause is DecoderQueryException -> {
                        "Unable to query device decoders"
                    }
                    cause.secureDecoderRequired -> {
                        String.format(
                            "This device does not provide a secure decoder for %s",
                            cause.mimeType
                        )
                    }
                    else -> {
                        String.format(
                            "This device does not provide a decoder for %s",
                            cause.mimeType
                        )
                    }
                }
            } else {
                String.format(
                    "Unable to instantiate decoder %s",
                    cause.codecInfo?.name
                )
            }
        }
        return Pair.create(0, errorString)
    }
}