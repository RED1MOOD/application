package com.example.player

import android.media.AudioFormat
import android.media.AudioManager
import android.media.AudioTrack
import kotlinx.coroutines.*

object AmbientSoundGenerator {
    private var audioTrack: AudioTrack? = null
    private var isPlaying = false
    private var synthJob: Job? = null
    private val scope = CoroutineScope(Dispatchers.Default + SupervisorJob())

    /**
     * Starts synthesizing a beautiful, rich 432Hz ambient spiritual mosque hum.
     * The speed parameter dynamically alters the pitch, verifying playback speed modifiers!
     */
    fun start(speed: Float = 1.0f) {
        if (isPlaying) stop()
        isPlaying = true

        synthJob = scope.launch {
            var track: AudioTrack? = null
            try {
                val sampleRate = 22050
                val minBufferSize = AudioTrack.getMinBufferSize(
                    sampleRate,
                    AudioFormat.CHANNEL_OUT_MONO,
                    AudioFormat.ENCODING_PCM_16BIT
                )

                track = AudioTrack(
                    AudioManager.STREAM_MUSIC,
                    sampleRate,
                    AudioFormat.CHANNEL_OUT_MONO,
                    AudioFormat.ENCODING_PCM_16BIT,
                    java.lang.Math.max(minBufferSize, 4096),
                    AudioTrack.MODE_STREAM
                )

                audioTrack = track
                track.play()

                val bufferSize = 1024
                val buffer = ShortArray(bufferSize)
                var phase = 0.0

                // Synthesize compound harmonic waves (432Hz with an ambient octave chord)
                while (isPlaying && isActive) {
                    val currentSpeed = speed
                    val baseFreq1 = 330.0 * currentSpeed
                    val baseFreq2 = 432.0 * currentSpeed

                    for (i in 0 until bufferSize) {
                        val wave1 = java.lang.Math.sin(phase * 2.0 * java.lang.Math.PI * baseFreq1 / sampleRate)
                        val wave2 = java.lang.Math.sin(phase * 2.0 * java.lang.Math.PI * baseFreq2 / sampleRate)
                        // Warm spiritual resonance chord, padded to safe non-clipping digital limit
                        val sample = ((wave1 * 0.35 + wave2 * 0.25) * 32767.0).toInt().toShort()
                        buffer[i] = sample
                        phase += 1.0
                    }
                    if (isPlaying && isActive) {
                        track.write(buffer, 0, bufferSize)
                    }
                }
            } catch (e: Exception) {
                e.printStackTrace()
            } finally {
                try {
                    track?.stop()
                    track?.release()
                } catch (e: Exception) {
                    e.printStackTrace()
                }
                if (audioTrack == track) {
                    audioTrack = null
                }
            }
        }
    }

    fun stop() {
        isPlaying = false
        synthJob?.cancel()
        synthJob = null
    }
}
